package de.digitalcollections.iiif.bookshelf.business.impl.service;

import de.digitalcollections.iiif.bookshelf.model.IiifManifestSummary;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import org.junit.Assert;
import org.junit.Test;

public class GraciousManifestParserTest {

  GraciousManifestParser parser = new GraciousManifestParser();

  @Test
  public void testManifestYale() throws IOException, URISyntaxException {
    InputStream is = this.getClass().getClassLoader().getResourceAsStream("manifests/manifests.ydc2.yale.edu-manifest-BlakeCopyL.json");
    IiifManifestSummary iiifManifestSummary = new IiifManifestSummary();
    parser.fillFromInputStream(is, iiifManifestSummary);
    Assert.assertNotNull(iiifManifestSummary.getThumbnail());
  }
}
