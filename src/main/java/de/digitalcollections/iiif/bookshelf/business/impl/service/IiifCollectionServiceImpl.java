package de.digitalcollections.iiif.bookshelf.business.impl.service;

import de.digitalcollections.iiif.bookshelf.business.api.service.IiifCollectionService;
import de.digitalcollections.iiif.bookshelf.business.api.service.IiifManifestSummaryService;
import de.digitalcollections.iiif.bookshelf.model.IiifManifestSummary;
import de.digitalcollections.iiif.model.sharedcanvas.Collection;
import de.digitalcollections.iiif.model.sharedcanvas.Manifest;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IiifCollectionServiceImpl implements IiifCollectionService {

  private static final Logger LOGGER = LoggerFactory.getLogger(IiifCollectionServiceImpl.class);

  @Autowired private IiifManifestSummaryService iiifManifestSummaryService;

  @Autowired private StrictCollectionParser strictCollectionParser;

  @Override
  public void importAllObjects(IiifManifestSummary manifestSummary)
      throws URISyntaxException, IOException {
    Collection collection = strictCollectionParser.parse(manifestSummary.getManifestUri());
    saveManifestsFromCollection(collection);
  }

  private void saveManifestsFromCollection(Collection collection) throws IOException {
    final List<Manifest> manifests = collection.getManifests();
    final List<Collection> subCollections = collection.getCollections();

    int manifestsCount = (manifests != null) ? manifests.size() : -1;
    int subCollectionsCount = (subCollections != null) ? subCollections.size() : -1;
    LOGGER.info(
        "Processing collection '{}' with {} manifests and {} sub collections.",
        collection.getIdentifier().toString(),
        manifestsCount,
        subCollectionsCount);

    // try to get list of manifests
    if (manifests != null) {
      for (Manifest manifest : manifests) {
        final String manifestUri = manifest.getIdentifier().toString();

        IiifManifestSummary summary = new IiifManifestSummary();
        summary.setManifestUri(manifestUri);

        try {
          iiifManifestSummaryService.enrichAndSave(summary);
        } catch (Exception e) {
          LOGGER.warn(
              "Could not read manifest from {} because of error {}", manifestUri, e.getMessage());
        }
      }
    }

    // try to get subcollections
    if (subCollections != null) {
      for (Collection subCollection : subCollections) {
        final String subCollectionIdentifier = subCollection.getIdentifier().toString();
        try {
          subCollection = strictCollectionParser.parse(subCollectionIdentifier);
          saveManifestsFromCollection(subCollection);
        } catch (Exception e) {
          LOGGER.warn(
              "Could not read collection from {} because of error {}",
              subCollectionIdentifier,
              e.getMessage());
        }
      }
    }
  }
}
