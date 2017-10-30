package de.digitalcollections.iiif.bookshelf.business.impl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.iiif.bookshelf.business.api.service.IiifCollectionService;
import de.digitalcollections.iiif.bookshelf.business.api.service.IiifManifestSummaryService;
import de.digitalcollections.iiif.bookshelf.model.IiifManifestSummary;
import de.digitalcollections.iiif.model.sharedcanvas.Collection;
import de.digitalcollections.iiif.model.sharedcanvas.Manifest;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IiifCollectionServiceImpl implements IiifCollectionService {

  private static final Logger LOGGER = LoggerFactory.getLogger(IiifCollectionServiceImpl.class);

  @Autowired
  private IiifManifestSummaryService iiifManifestSummaryService;

  @Autowired
  private ObjectMapper objectMapper;

  @Override
  public void importAllObjects(IiifManifestSummary manifestSummary) throws MalformedURLException, IOException {
    final String manifestUri = manifestSummary.getManifestUri();
    Collection collection = objectMapper.readValue(new URL(manifestUri), Collection.class);
    saveManifestsFromCollection(collection);
  }

  private void saveManifestsFromCollection(Collection collection) {
    // try to get list of manifests
    final List<Manifest> manifests = collection.getManifests();
    if (manifests != null) {
      for (Manifest manifest : manifests) {
        final String manifestUri = manifest.getIdentifier().toString();

        IiifManifestSummary summary = new IiifManifestSummary();
        summary.setManifestUri(manifestUri);

        try {
          iiifManifestSummaryService.enrichAndSave(summary);
        } catch (Exception e) {
          LOGGER.warn("Could not read manifest from {}", manifestUri, e);
        }
      }
    }

    // try to get subcollections
    final List<Collection> subCollections = collection.getCollections();
    if (subCollections != null) {
      for (Collection subCollection : subCollections) {
        final String subCollectionIdentifier = subCollection.getIdentifier().toString();
        try {
          subCollection = objectMapper.readValue(new URL(subCollectionIdentifier), Collection.class);
          saveManifestsFromCollection(subCollection);
        } catch (IOException e) {
          LOGGER.warn("Could not read collection from {}", subCollectionIdentifier, e);
        }
      }
    }
  }
}
