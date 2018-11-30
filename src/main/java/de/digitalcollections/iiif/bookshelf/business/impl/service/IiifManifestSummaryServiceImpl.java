package de.digitalcollections.iiif.bookshelf.business.impl.service;

import de.digitalcollections.iiif.bookshelf.backend.api.repository.IiifManifestSummaryRepository;
import de.digitalcollections.iiif.bookshelf.backend.api.repository.IiifManifestSummarySearchRepository;
import de.digitalcollections.iiif.bookshelf.business.api.service.IiifManifestSummaryService;
import de.digitalcollections.iiif.bookshelf.model.IiifManifestSummary;
import de.digitalcollections.iiif.bookshelf.model.exceptions.NotFoundException;
import de.digitalcollections.iiif.bookshelf.model.exceptions.SearchSyntaxException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class IiifManifestSummaryServiceImpl implements IiifManifestSummaryService {

  public static final Locale DEFAULT_LOCALE = Locale.GERMAN;

  private static final Logger LOGGER = LoggerFactory.getLogger(IiifManifestSummaryServiceImpl.class);

  @Autowired
  private GraciousManifestParser graciousManifestParser;

  @Value("${custom.iiif.graciousParsing}")
  private boolean graciousParsing;

  @Autowired
  private IiifManifestSummaryRepository iiifManifestSummaryRepository;

  @Autowired
  private IiifManifestSummarySearchRepository iiifManifestSummarySearchRepository;

  @Autowired
  private StrictManifestParser strictManifestParser;

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
  public void enrichAndSave(IiifManifestSummary manifestSummary) throws URISyntaxException, NotFoundException, IOException {
    try {
      strictManifestParser.fillSummary(manifestSummary);
    } catch (Exception ex) {
      if (graciousParsing) {
        // Manifest might not be standard conform. Nevertheless we just want some values from it to form a short summary.
        // As long viewer can handle the manifest, we are fine to show it.
        LOGGER.warn("Manifest at uri {} might be not standard conform, trying gracious parsing.", manifestSummary.getManifestUri(), ex);
        try {
          graciousManifestParser.fillSummary(manifestSummary);
        } catch (Exception e) {
          throw e;
        }
      } else {
        throw ex;
      }
    }

    // add system specific unique view id for shorter viewer urls.
    String viewId = getViewId(manifestSummary);
    manifestSummary.setViewId(viewId);
    // manifestUri now set to field @id of manifest.
    // no longer might be the same value as from user input (could have been redirected...). now it is save to use it as unique key for lookup:
    IiifManifestSummary existingManifest = prepareUpdateIfAlreadyExists(manifestSummary.getManifestUri(), manifestSummary);
    try {
      iiifManifestSummaryRepository.save(manifestSummary);
    } catch (Exception ex) {
      LOGGER.error("Error saving manifest with uri '{}', existing manifest uri '{}'", manifestSummary.getManifestUri(), existingManifest != null ? existingManifest.getManifestUri() : "not existing");
      throw ex;
    }
    iiifManifestSummarySearchRepository.save(manifestSummary);
    LOGGER.info("Successfully imported and indexed {}", manifestSummary.getManifestUri());
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

  protected String getViewId(IiifManifestSummary manifestSummary) {
    // if sha-1 leads to not unique collisions, use this:
    // return = manifestSummary.getUuid().toString();
    // create a short reasonable unique view id
    String viewId = manifestSummary.getManifestUri(); // if it does not work, just use uuid (which is longer)
    viewId = DigestUtils.sha1Hex(viewId);
    return viewId.substring(0, 8);
  }

  private IiifManifestSummary prepareUpdateIfAlreadyExists(final String manifestIdentifier, IiifManifestSummary manifestSummary) {
    // if exists already: set/overwrite unique fields to values of existing summary to enforce update instead of insert
    final IiifManifestSummary existingManifest = iiifManifestSummaryRepository.findByManifestUri(manifestIdentifier);
    if (existingManifest != null) {
      manifestSummary.setUuid(existingManifest.getUuid());
      manifestSummary.setViewId(existingManifest.getViewId());
    }
    return existingManifest;
  }

  @Override
  public void reindexSearch() {
    for (IiifManifestSummary summary : iiifManifestSummaryRepository.findAll()) {
      // TODO: Could probably benefit from batched ingest
      iiifManifestSummarySearchRepository.save(summary);
    }

  }
}
