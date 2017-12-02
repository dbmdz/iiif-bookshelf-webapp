package de.digitalcollections.iiif.bookshelf.business.api.service;

import de.digitalcollections.iiif.bookshelf.model.IiifManifestSummary;
import java.io.IOException;
import java.net.URISyntaxException;

public interface IiifCollectionService {

  public void importAllObjects(IiifManifestSummary manifest) throws URISyntaxException, IOException;
}
