package de.digitalcollections.iiif.bookshelf.business.impl.service;

import de.digitalcollections.iiif.bookshelf.backend.api.repository.IiifManifestSummaryRepository;
import de.digitalcollections.iiif.bookshelf.backend.api.repository.IiifManifestSummarySearchRepository;
import de.digitalcollections.iiif.bookshelf.business.api.service.IiifManifestSummaryService;
import de.digitalcollections.iiif.bookshelf.model.IiifManifestSummary;
import de.digitalcollections.iiif.bookshelf.model.Thumbnail;
import de.digitalcollections.iiif.bookshelf.model.exceptions.SearchSyntaxException;
import de.digitalcollections.iiif.presentation.backend.api.exceptions.NotFoundException;
import de.digitalcollections.iiif.presentation.backend.api.repository.v2.PresentationRepository;
import de.digitalcollections.iiif.presentation.model.api.enums.Version;
import de.digitalcollections.iiif.presentation.model.api.v2.Manifest;
import de.digitalcollections.iiif.presentation.model.api.v2.PropertyValue;
import java.net.URI;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class IiifManifestSummaryServiceImpl implements IiifManifestSummaryService {

  private static final Logger LOGGER = LoggerFactory.getLogger(IiifManifestSummaryServiceImpl.class);

  public static final Locale DEFAULT_LOCALE = Locale.GERMAN;

  @Autowired
  private IiifManifestSummaryRepository iiifManifestSummaryRepository;

  @Autowired
  private IiifManifestSummarySearchRepository iiifManifestSummarySearchRepository;

  @Autowired
  private PresentationRepository presentationRepository;

  @Override
  public List<IiifManifestSummary> getAll() {
    return iiifManifestSummaryRepository.findAllByOrderByLastModifiedDesc();
  }

  @Override
  public Page<IiifManifestSummary> getAll(Pageable pageable) {
    return iiifManifestSummaryRepository.findAllByOrderByLastModifiedDesc(pageable);
  }

  @Override
  public Page<IiifManifestSummary> findAll(String searchText, Pageable pageable) throws SearchSyntaxException {
    return iiifManifestSummarySearchRepository.findBy(searchText, pageable);
  }

  @Override
  public long countAll() {
    return iiifManifestSummaryRepository.count();
  }

  public IiifManifestSummary get(UUID uuid) {
    return iiifManifestSummaryRepository.findOne(uuid);
  }

  @Override
  public IiifManifestSummary add(IiifManifestSummary manifest) {
    final IiifManifestSummary existingManifest = iiifManifestSummaryRepository
            .findByManifestUri(manifest.getManifestUri());
    if (existingManifest != null) {
      throw new IllegalArgumentException("object already exists");
    }
    return iiifManifestSummaryRepository.save(manifest);
  }

  private void saveManifestsFromCollection(JSONObject collection) {
    // try to get list of manifests
    Object manifestsNode = collection.get("manifests");
    if (manifestsNode != null && JSONArray.class.isAssignableFrom(manifestsNode.getClass())) {
      JSONArray manifests = (JSONArray) manifestsNode;
      for (Object manifest : manifests) {
        JSONObject manifestObj = (JSONObject) manifest;
        String uri = (String) manifestObj.get("@id");
        String manifestType = (String) manifestObj.get("@type");
        if ("sc:Manifest".equalsIgnoreCase(manifestType)) {
          IiifManifestSummary childManifestSummary = new IiifManifestSummary();
          childManifestSummary.setManifestUri(uri);
          try {
            enrichAndSave(childManifestSummary);
          } catch (Exception e) {
            LOGGER.warn("Could not read manifest from {}", uri, e);
          }
        }
      }
    }
    // try to get subcollections
    Object collectionsNode = collection.get("collections");
    if (collectionsNode != null && JSONArray.class.isAssignableFrom(collectionsNode.getClass())) {
      JSONArray collections = (JSONArray) collectionsNode;
      collections.sort(Comparator.comparing(JSONObject::hashCode));
      for (Object subcollection : collections) {
        JSONObject collectionObj = (JSONObject) collection;
        String uri = (String) collectionObj.get("@id");
        String collectionType = (String) collectionObj.get("@type");
        if ("sc:Manifest".equalsIgnoreCase(collectionType) || "sc:Collection".equalsIgnoreCase(collectionType)) {
          IiifManifestSummary childManifestSummary = new IiifManifestSummary();
          childManifestSummary.setManifestUri(uri);
          try {
            JSONObject subcollObject = presentationRepository.getResourceAsJsonObject(uri);
            saveManifestsFromCollection(subcollObject);
          } catch (Exception e) {
            LOGGER.warn("Could not read collection from {}", uri, e);
          }
        }
      }
    }

  }

  @Override
  public void reindexSearch() {
    for (IiifManifestSummary summary : iiifManifestSummaryRepository.findAll()) {
      // TODO: Could probably benefit from batched ingest
      iiifManifestSummarySearchRepository.save(summary);
    }

  }

  @Override
  public void enrichAndSave(IiifManifestSummary manifestSummary) throws ParseException, NotFoundException {
    // if exists already: update existing manifest
    final IiifManifestSummary existingManifest = iiifManifestSummaryRepository
            .findByManifestUri(manifestSummary.getManifestUri());
    if (existingManifest != null) {
      manifestSummary.setUuid(existingManifest.getUuid());
    }

    JSONObject jsonObject = presentationRepository.getResourceAsJsonObject(manifestSummary.getManifestUri());

    String type = (String) jsonObject.get("@type");
    if ("sc:Manifest".equalsIgnoreCase(type)) {
      fillFromJsonObject(jsonObject, manifestSummary);
      iiifManifestSummaryRepository.save(manifestSummary);
      iiifManifestSummarySearchRepository.save(manifestSummary);
    } else if ("sc:Collection".equalsIgnoreCase(type)) {
      // FIXME: This breaks the "enrich" contract, since we're adding potentially thousands of manifests that
      // the user of this method won't know about, the API should probably be reworked
      saveManifestsFromCollection(jsonObject);
    }
  }

  @Deprecated
  public void fillFromManifest(IiifManifestSummary manifestSummary) throws NotFoundException {
    Manifest manifest = presentationRepository.getManifest(manifestSummary.getManifestUri());

    // enrichment
    final PropertyValue label1 = manifest.getLabel();
    if (label1 != null) {
      String label = label1.getFirstValue();
      manifestSummary.addLabel(DEFAULT_LOCALE, label);
    }

    final PropertyValue description1 = manifest.getDescription();
    if (description1 != null) {
      String description = description1.getFirstValue();
      manifestSummary.addDescription(DEFAULT_LOCALE, description);
    }

    final PropertyValue attribution1 = manifest.getAttribution();
    if (attribution1 != null) {
      String attribution = attribution1.getFirstValue();
      manifestSummary.addAttribution(DEFAULT_LOCALE, attribution);
    }

    URI thumbnailServiceUri = null;
    String context;
    try {
      thumbnailServiceUri = manifest.getThumbnail().getService().getId();
    } catch (Exception ex) {

    }
    if (thumbnailServiceUri == null) {
      try {
        final de.digitalcollections.iiif.presentation.model.api.v2.Service service = manifest.getSequences().get(0).
                getCanvases().get(0).getImages().get(0).getResource().getService();
        // first image
        thumbnailServiceUri = service.getId();
        context = service.getContext();
        manifestSummary.setThumbnail(new Thumbnail(thumbnailServiceUri.toString(), context));
      } catch (Exception ex) {

      }
    }
  }

  /**
   * Language may be associated with strings that are intended to be displayed to the user with the following pattern of
   *
   * @value plus the RFC 5646 code in
   *
   *        @language, instead of a plain string. This pattern may be used in label, description, attribution and the
   *        label and value fields of the metadata construction.
   *
   * @param manifestSummary
   * @throws NotFoundException
   * @throws ParseException
   */
  private void fillFromJsonObject(JSONObject jsonObject, IiifManifestSummary manifestSummary) throws NotFoundException, ParseException {

    Version version = null;
    final Object contextNode = jsonObject.get("@context");
    if (JSONArray.class.isAssignableFrom(contextNode.getClass())) {
      JSONArray contexts = (JSONArray) contextNode;
      for (Object context : contexts) {
        version = Version.getVersion((String) context);
        if (version != null) {
          break;
        }
      }
    } else {
      version = Version.getVersion((String) contextNode);
    }
    if (version != null) {
      manifestSummary.setVersion(version);
    }

    Object label = jsonObject.get("label");
    HashMap<Locale, String> localizedLabels = getLocalizedStrings(label);
    manifestSummary.setLabels(localizedLabels);

    Object description = jsonObject.get("description");
    HashMap<Locale, String> localizedDescriptions = getLocalizedStrings(description);
    manifestSummary.setDescriptions(localizedDescriptions);

    Object attribution = jsonObject.get("attribution");
    HashMap<Locale, String> localizedAttributions = getLocalizedStrings(attribution);
    manifestSummary.setAttributions(localizedAttributions);

    Thumbnail thumbnail = getThumbnail(jsonObject);
    manifestSummary.setThumbnail(thumbnail);

    String logoUrl = (String) jsonObject.get("logo");
    if (!StringUtils.isEmpty(logoUrl)) {
      manifestSummary.setLogoUrl(logoUrl);
    }
  }

  public HashMap<Locale, String> getLocalizedStrings(Object jsonNode) {
    HashMap<Locale, String> result = new HashMap<>();
    if (jsonNode == null) {
      return result;
    }
    if (JSONArray.class.isAssignableFrom(jsonNode.getClass())) {
      JSONArray descriptions = (JSONArray) jsonNode;
      for (Object descr : descriptions) {
        JSONObject descrObj = (JSONObject) descr;
        String value = (String) descrObj.get("@value");
        String language = (String) descrObj.get("@language");
        result.put(new Locale(language), value);
      }
    } else {
      String value = (String) jsonNode;
      result.put(DEFAULT_LOCALE, value);
    }
    return result;
  }

  private Thumbnail getThumbnail(JSONObject manifestObj) {
    // try to get thumbnail of manifest itself
    JSONObject thumbnailObj = (JSONObject) manifestObj.get("thumbnail");
    if (thumbnailObj != null) {
      JSONObject serviceObj = (JSONObject) thumbnailObj.get("service");
      if (serviceObj != null) {
        String context = (String) serviceObj.get("@context");
        String id = (String) serviceObj.get("@id");
        return new Thumbnail(context, id);
      } else {
        String url = (String) thumbnailObj.get("@id");
        return new Thumbnail(url);
      }
    }

    // try to get thumbnail of first canvas
    try {
      // manifest.getSequences().get(0).getCanvases().get(0).getImages().get(0).getResource().getService().getId();
      JSONArray sequencesArray = (JSONArray) manifestObj.get("sequences");
      JSONObject firstSequence = (JSONObject) sequencesArray.get(0);

      JSONArray canvasesArray = (JSONArray) firstSequence.get("canvases");
      JSONObject firstCanvas = (JSONObject) canvasesArray.get(0);

      Object obj = firstCanvas.get("thumbnail");
      if (obj instanceof JSONObject) {
        JSONObject firstCanvasThumbnail = (JSONObject) firstCanvas.get("thumbnail");
        if (firstCanvasThumbnail != null) {
          String url = (String) firstCanvasThumbnail.get("@id");
          return new Thumbnail(url);
        }
      } else if (obj instanceof String) {
        String url = (String) obj;
        return new Thumbnail(url);
      }

      JSONArray imagesArray = (JSONArray) firstCanvas.get("images");
      JSONObject firstImage = (JSONObject) imagesArray.get(0);

      JSONObject resourceObj = (JSONObject) firstImage.get("resource");
      JSONObject serviceObj = (JSONObject) resourceObj.get("service");
      String context = (String) serviceObj.get("@context");
      String id = (String) serviceObj.get("@id");
      return new Thumbnail(context, id);
    } catch (Exception ex) {
    }

    return null;
  }

}
