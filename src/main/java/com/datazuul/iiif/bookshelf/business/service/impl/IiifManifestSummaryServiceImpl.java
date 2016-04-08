package com.datazuul.iiif.bookshelf.business.service.impl;

import com.datazuul.iiif.bookshelf.model.IiifManifestSummary;
import com.datazuul.iiif.presentation.api.model.Manifest;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.datazuul.iiif.bookshelf.backend.repository.IiifManifestSummaryRepository;
import com.datazuul.iiif.bookshelf.business.service.IiifManifestSummaryService;
import com.datazuul.iiif.bookshelf.model.Thumbnail;
import com.datazuul.iiif.presentation.api.model.Version;
import com.datazuul.iiif.presentation.backend.repository.PresentationRepository;
import com.datazuul.iiif.presentation.model.NotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 *
 * @author ralf
 */
@Service
public class IiifManifestSummaryServiceImpl implements IiifManifestSummaryService {

  private static final Logger LOGGER = LoggerFactory.getLogger(IiifManifestSummaryServiceImpl.class);

  public static final Locale DEFAULT_LOCALE = Locale.GERMAN;

  @Autowired
  private IiifManifestSummaryRepository iiifManifestSummaryRepository;

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
  public Page<IiifManifestSummary> findAll(String searchText, Pageable pageable) {
    return iiifManifestSummaryRepository.findBy(searchText, pageable);
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
    final IiifManifestSummary existingManifest = iiifManifestSummaryRepository.findByManifestUri(manifest.getManifestUri());
    if (existingManifest != null) {
      throw new IllegalArgumentException("object already exists");
    }
    return iiifManifestSummaryRepository.save(manifest);
  }

  @Override
  public void enrichAndSave(IiifManifestSummary manifestSummary) {
    try {
      // if exists already: update existing manifest
      final IiifManifestSummary existingManifest = iiifManifestSummaryRepository.findByManifestUri(manifestSummary.getManifestUri());
      if (existingManifest != null) {
        manifestSummary = existingManifest;
      }
//      fillFromManifest(manifestSummary);

      JSONObject jsonObject = presentationRepository.getManifestAsJsonObject(manifestSummary.getManifestUri());
//      @type: "sc:Manifest"

      fillFromJsonObject(jsonObject, manifestSummary);

      iiifManifestSummaryRepository.save(manifestSummary);
    } catch (Exception ex) {
      LOGGER.warn("Can not fill from manifest " + manifestSummary.getManifestUri(), ex);
    }
  }

  @Deprecated
  public void fillFromManifest(IiifManifestSummary manifestSummary) throws NotFoundException {
    Manifest manifest = presentationRepository.getManifest(manifestSummary.getManifestUri());

    // enrichment
    String label = manifest.getLabel();
    manifestSummary.addLabel(DEFAULT_LOCALE, label);

    String description = manifest.getDescription();
    manifestSummary.addDescription(DEFAULT_LOCALE, description);

    String attribution = manifest.getAttribution();
    manifestSummary.addAttribution(DEFAULT_LOCALE, attribution);

    URI thumbnailServiceUri = null;
    String context = null;
    try {
      thumbnailServiceUri = manifest.getThumbnail().getService().getId();
    } catch (Exception ex) {

    }
    if (thumbnailServiceUri == null) {
      try {
        final com.datazuul.iiif.presentation.api.model.other.Service service = manifest.getSequences().get(0).getCanvases().get(0).getImages().get(0).getResource().getService();
        // first image
        thumbnailServiceUri = service.getId();
        context = service.getContext();
      } catch (Exception ex) {

      }
    }
    manifestSummary.setThumbnail(new Thumbnail(thumbnailServiceUri.toString(), context));
  }

  /**
   * Language may be associated with strings that are intended to be displayed
   * to the user with the following pattern of @value plus the RFC 5646 code in
   *
   * @language, instead of a plain string. This pattern may be used in label,
   * description, attribution and the label and value fields of the metadata
   * construction.
   *
   * @param manifestSummary
   * @throws NotFoundException
   * @throws ParseException
   */
  private void fillFromJsonObject(JSONObject jsonObject, IiifManifestSummary manifestSummary) throws URISyntaxException, NotFoundException, ParseException {

    // TODO if this is allowed:
    /*
    IiifManifestSummaryServiceImpl (qtp1871259950-20) > Can not fill from manifest http://wellcomelibrary.org/iiif/b24744803/manifest
    java.lang.ClassCastException: org.json.simple.JSONArray cannot be cast to java.lang.String
	at com.datazuul.iiif.bookshelf.business.service.impl.IiifManifestSummaryServiceImpl.fillFromJsonObject(IiifManifestSummaryServiceImpl.java:142)
     */
    Version version = Version.getVersion((String) jsonObject.get("@context"));
    manifestSummary.setVersion(version);

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
    try {
      // manifest.getThumbnail().getService().getId();
      JSONObject thumbnailObj = (JSONObject) manifestObj.get("thumbnail");
      JSONObject serviceObj = (JSONObject) thumbnailObj.get("service");
      String context = (String) serviceObj.get("@context");
      String id = (String) serviceObj.get("@id");
      return new Thumbnail(context, id);
    } catch (Exception ex) {
    }

    try {
      // manifest.getSequences().get(0).getCanvases().get(0).getImages().get(0).getResource().getService().getId();
      JSONArray sequencesArray = (JSONArray) manifestObj.get("sequences");
      JSONObject firstSequence = (JSONObject) sequencesArray.get(0);

      JSONArray canvasesArray = (JSONArray) firstSequence.get("canvases");
      JSONObject firstCanvas = (JSONObject) canvasesArray.get(0);

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
