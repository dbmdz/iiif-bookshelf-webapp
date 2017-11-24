package de.digitalcollections.iiif.bookshelf.business.impl.service;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class IiifManifestSummaryServiceImplTest {

  @Test
  public void test_getIdentifierFromManifestUri() {
    IiifManifestSummaryServiceImpl instance = new IiifManifestSummaryServiceImpl();
    String identifier = instance.getIdentifierFromManifestUri("http://manifests.ydc2.yale.edu/manifest/BLHarleyMS7334");
    assertThat(identifier).isNull();

    identifier = instance.getIdentifierFromManifestUri("https://api.digitale-sammlungen.de/iiif/presentation/v2/bsb00046285/manifest");
    assertThat(identifier).isEqualTo("bsb00046285");

  }

}
