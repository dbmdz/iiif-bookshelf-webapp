package de.digitalcollections.iiif.bookshelf.business.impl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.iiif.bookshelf.backend.api.repository.IiifManifestSummaryRepository;
import de.digitalcollections.iiif.bookshelf.backend.api.repository.IiifManifestSummarySearchRepository;
import de.digitalcollections.iiif.bookshelf.business.api.service.IiifManifestSummaryService;
import de.digitalcollections.iiif.bookshelf.model.IiifManifestSummary;
import de.digitalcollections.iiif.bookshelf.model.Thumbnail;
import de.digitalcollections.iiif.bookshelf.model.exceptions.NotFoundException;
import de.digitalcollections.iiif.bookshelf.model.exceptions.SearchSyntaxException;
import de.digitalcollections.iiif.model.ImageContent;
import de.digitalcollections.iiif.model.PropertyValue;
import de.digitalcollections.iiif.model.image.ImageApiProfile;
import de.digitalcollections.iiif.model.image.ImageService;
import de.digitalcollections.iiif.model.openannotation.Choice;
import de.digitalcollections.iiif.model.sharedcanvas.Manifest;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class IiifManifestSummaryServiceImpl implements IiifManifestSummaryService {

  public static final Locale DEFAULT_LOCALE = Locale.GERMAN;

  private static final Logger LOGGER = LoggerFactory.getLogger(IiifManifestSummaryServiceImpl.class);

  @Autowired
  private IiifManifestSummaryRepository iiifManifestSummaryRepository;

  @Autowired
  private IiifManifestSummarySearchRepository iiifManifestSummarySearchRepository;

  @Autowired
  private ObjectMapper objectMapper;

  @Override
  public IiifManifestSummary add(IiifManifestSummary manifest) {
    final IiifManifestSummary existingManifest = iiifManifestSummaryRepository
            .findByManifestUri(manifest.getManifestUri());
    if (existingManifest != null) {
      throw new IllegalArgumentException("object already exists");
    }
    return iiifManifestSummaryRepository.save(manifest);
  }

  @Override
  public long countAll() {
    return iiifManifestSummaryRepository.count();
  }

  @Override
  public void enrichAndSave(IiifManifestSummary manifestSummary) throws NotFoundException, IOException {
    // if exists already: update existing manifest
    final IiifManifestSummary existingManifest = iiifManifestSummaryRepository
            .findByManifestUri(manifestSummary.getManifestUri());
    if (existingManifest != null) {
      manifestSummary.setUuid(existingManifest.getUuid());
      manifestSummary.setViewId(existingManifest.getViewId());
    }

    Manifest manifest = objectMapper.readValue(new URL(manifestSummary.getManifestUri()), Manifest.class);
    fillFromManifest(manifest, manifestSummary);
    iiifManifestSummaryRepository.save(manifestSummary);
    iiifManifestSummarySearchRepository.save(manifestSummary);
    LOGGER.info("successfully imported and indexed {}", manifestSummary.getManifestUri());
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
  private void fillFromManifest(Manifest manifest, IiifManifestSummary manifestSummary) throws NotFoundException {
    // set from "@id" value to avoid using slightly different http-urls (e.g. with or without request params) pointing to same manifest
    manifestSummary.setManifestUri(manifest.getIdentifier().toString());
    manifestSummary.setLabels(getLocalizedStrings(manifest.getLabel()));
    manifestSummary.setDescriptions(getLocalizedStrings(manifest.getDescription()));
    manifestSummary.setAttributions(getLocalizedStrings(manifest.getAttribution()));

    Thumbnail thumbnail = getThumbnail(manifest);
    manifestSummary.setThumbnail(thumbnail);

    URI logoUri = manifest.getLogoUri();
    if (logoUri != null) {
      manifestSummary.setLogoUrl(logoUri.toString());
    }

    if (manifestSummary.getViewId() == null) {
      String viewId;
      // if null: manifest not yet exists in database (see calling method "enrichAndSave")
      // set convenience (short) viewId:
      String uri = manifestSummary.getManifestUri();
      viewId = getIdentifierFromManifestUri(uri);
      if (viewId != null) {
        // check if another object with same viewId exists
        List<IiifManifestSummary> others = iiifManifestSummaryRepository.findByViewId(viewId);
        if (others != null && !others.isEmpty()) {
          viewId += "-" + others.size();
        }
      } else {
        viewId = manifestSummary.getUuid().toString();
      }
//      String prefix = uri.substring(0, uri.indexOf("/manifest"));
//      String viewId = prefix.substring(prefix.lastIndexOf("/") + 1);
      manifestSummary.setViewId(viewId);
    }
  }

  /**
   * try to get identifier if uri follows recommended URI pattern
   * see http://iiif.io/api/presentation/2.1/#manifest
   * @param uri manifest uri
   * @return identifier or null
   */
  public String getIdentifierFromManifestUri(String uri) {
    Pattern manifestUriPattern = Pattern.compile(".*/(?<identifier>.*)/manifest");
    Matcher matcher = manifestUriPattern.matcher(uri);
    if (matcher.matches()) {
      return matcher.group("identifier");
    }
    return null;
  }

  @Override
  public Page<IiifManifestSummary> findAll(String searchText, Pageable pageable) throws SearchSyntaxException {
    return iiifManifestSummarySearchRepository.findBy(searchText, pageable);
  }

  @Override
  public IiifManifestSummary get(String id) {
    UUID uuid;
    try {
      uuid = UUID.fromString(id);
    } catch (IllegalArgumentException e) {
      uuid = null;
    }
    List<IiifManifestSummary> objects = iiifManifestSummaryRepository.findByViewIdOrUuid(id, uuid);
    if (objects == null || objects.isEmpty()) {
      return null;
    }
    if (objects.size() > 1) {
      // TODO: or throw a (to be introduced) NotUniqueException?
      return null;
    }
    return objects.get(0);
  }

  @Override
  public IiifManifestSummary get(UUID uuid) {
    return iiifManifestSummaryRepository.findOne(uuid);
  }

  @Override
  public List<IiifManifestSummary> getAll() {
    return iiifManifestSummaryRepository.findAllByOrderByLastModifiedDesc();
  }

  @Override
  public Page<IiifManifestSummary> getAll(Pageable pageable) {
    return iiifManifestSummaryRepository.findAllByOrderByLastModifiedDesc(pageable);
  }

  @Override
  public String getLabel(IiifManifestSummary manifestSummary, Locale locale) {
    String result = null;
    if (manifestSummary == null) {
      return result;
    }
    result = manifestSummary.getLabel(locale);
    if (result == null) {
      result = manifestSummary.getLabel(DEFAULT_LOCALE);
    }
    if (result == null) {
      result = (String) (manifestSummary.getLabels().values().toArray())[0];
    }
    return result;
  }

  public HashMap<Locale, String> getLocalizedStrings(PropertyValue val) {
    HashMap<Locale, String> strings = new HashMap<>();
    if (val != null) {
      val.getLocalizations().forEach(l -> strings.put(l, val.getFirstValue(l)));
    }
    return strings;
  }

  private Thumbnail getThumbnail(Manifest manifest) {
    ImageContent thumb;
    if (manifest.getThumbnails() != null && manifest.getThumbnails().size() > 0) {
      thumb = manifest.getThumbnail();
    } else {
      thumb = manifest.getDefaultSequence().getCanvases().stream()
              .map(c -> c.getThumbnails())
              .filter(ts -> ts != null && ts.size() > 0)
              .map(ts -> ts.get(0))
              .findFirst().orElse(null);

    }
    if (thumb == null) {
      ImageService service = manifest.getDefaultSequence().getCanvases().get(0).getImages().stream()
              .map(a -> {
                if (a.getResource() instanceof ImageContent) {
                  return (ImageContent) a.getResource();
                } else {
                  return (ImageContent) ((Choice) a.getResource()).getDefault();
                }
              })
              .flatMap(r -> r.getServices().stream())
              .filter(ImageService.class::isInstance)
              .map(ImageService.class::cast)
              .findFirst().orElse(null);
      boolean isV1 = service.getProfiles().stream()
              .map(p -> p.getIdentifier().toString())
              .anyMatch(ImageApiProfile.V1_PROFILES::contains);

      String serviceUrl = service.getIdentifier().toString();
      if (serviceUrl.endsWith("/")) {
        serviceUrl = serviceUrl.substring(0, serviceUrl.length() - 1);
      }
      String thumbnailUrl = String.format("%s/full/280,/0/", serviceUrl);
      if (isV1) {
        thumbnailUrl += "native.jpg";
      } else {
        thumbnailUrl += "default.jpg";
      }
      thumb = new ImageContent(thumbnailUrl);
    }

    if (thumb != null && thumb.getServices() != null && thumb.getServices().size() > 0) {
      de.digitalcollections.iiif.model.Service service = thumb.getServices().get(0);
      return new Thumbnail(service.getContext().toString(), service.getIdentifier().toString());
    } else if (thumb != null) {
      return new Thumbnail(thumb.getIdentifier().toString());
    } else {
      return null;
    }
  }

  @Override
  public void reindexSearch() {
    for (IiifManifestSummary summary : iiifManifestSummaryRepository.findAll()) {
      // TODO: Could probably benefit from batched ingest
      iiifManifestSummarySearchRepository.save(summary);
    }

  }
}
