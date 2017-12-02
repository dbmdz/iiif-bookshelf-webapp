package de.digitalcollections.iiif.bookshelf.business.impl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.iiif.bookshelf.model.IiifManifestSummary;
import de.digitalcollections.iiif.model.sharedcanvas.Collection;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StrictCollectionParser extends AbstractManifestParser {

  @Autowired
  private ObjectMapper objectMapper;

  @Override
  public void fillSummary(IiifManifestSummary manifestSummary) throws IOException, URISyntaxException {
    throw new UnsupportedOperationException();
  }

  public Collection parse(String uri) throws URISyntaxException, IOException {
    InputStream content = getContentInputStream(uri);
    Collection collection = objectMapper.readValue(content, Collection.class);
    return collection;
  }
}
