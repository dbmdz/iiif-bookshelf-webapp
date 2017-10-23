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
import de.digitalcollections.iiif.model.sharedcanvas.Collection;
import de.digitalcollections.iiif.model.sharedcanvas.Manifest;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class IiifManifestSummaryServiceImpl implements IiifManifestSummaryService {

  private static final Logger LOGGER = LoggerFactory.getLogger(IiifManifestSummaryServiceImpl.class);

  public static final Locale DEFAULT_LOCALE = Locale.GERMAN;

  @Autowired
  private IiifManifestSummaryRepository iiifManifestSummaryRepository;

  @Autowired
  private IiifManifestSummarySearchRepository iiifManifestSummarySearchRepository;

  @Autowired
  private ObjectMapper objectMapper;

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

  private void saveManifestsFromCollection(Collection collection) {
    OkHttpClient httpClient = new OkHttpClient();
    // try to get list of manifests
    for (Manifest manifest : collection.getManifests()) {
      IiifManifestSummary summary = new IiifManifestSummary();
      summary.setManifestUri(manifest.getIdentifier().toString());
      try {
        enrichAndSave(summary);
      } catch (Exception e) {
        LOGGER.warn("Could not read manifest from {}", manifest.getIdentifier(), e);
      }
    }

    // try to get subcollections
    for (Collection subCollection : collection.getCollections()) {
      try {
        subCollection = objectMapper.readValue(subCollection.getIdentifier().toString(), Collection.class);
        saveManifestsFromCollection(subCollection);
      } catch (IOException e) {
        LOGGER.warn("Could not read collection from {}", subCollection.getIdentifier(), e);
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
  public void enrichAndSave(IiifManifestSummary manifestSummary) throws NotFoundException, IOException {
    // if exists already: update existing manifest
    final IiifManifestSummary existingManifest = iiifManifestSummaryRepository
            .findByManifestUri(manifestSummary.getManifestUri());
    if (existingManifest != null) {
      manifestSummary.setUuid(existingManifest.getUuid());
    }

    Manifest manifest = objectMapper.readValue(new URL(manifestSummary.getManifestUri()), Manifest.class);
    fillFromManifest(manifest, manifestSummary);
    iiifManifestSummaryRepository.save(manifestSummary);
    iiifManifestSummarySearchRepository.save(manifestSummary);
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
    manifestSummary.setLabels(getLocalizedStrings(manifest.getLabel()));
    manifestSummary.setDescriptions(getLocalizedStrings(manifest.getDescription()));
    manifestSummary.setAttributions(getLocalizedStrings(manifest.getAttribution()));

    Thumbnail thumbnail = getThumbnail(manifest);
    manifestSummary.setThumbnail(thumbnail);

    URI logoUri = manifest.getLogoUri();
    if (logoUri != null) {
      manifestSummary.setLogoUrl(logoUri.toString());
    }
  }

  public HashMap<Locale, String> getLocalizedStrings(PropertyValue val) {
    HashMap<Locale, String> strings = new HashMap<>();
    val.getLocalizations().forEach(l -> strings.put(l, val.getFirstValue(l)));
    return strings;
  }

  private Thumbnail getThumbnail(Manifest manifest) {
    ImageContent thumb;
    if (manifest.getThumbnail() != null) {
      thumb = manifest.getThumbnail();
    } else {
      thumb = manifest.getDefaultSequence().getCanvases().stream()
          .map(c -> c.getThumbnails())
          .filter(ts -> ts != null && ts.size() > 0)
          .map(ts -> ts.get(0))
          .findFirst().get();
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
}
