package de.digitalcollections.iiif.bookshelf.business.impl.service;

import de.digitalcollections.iiif.bookshelf.model.IiifManifestSummary;
import de.digitalcollections.iiif.bookshelf.model.Thumbnail;
import de.digitalcollections.iiif.model.jackson.IiifObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
//@SpringBootTest(classes = {Application.class})
@ContextConfiguration(classes = {IiifObjectMapper.class, GraciousManifestParser.class})
@TestPropertySource(properties = {"custom.summary.thumbnail.width=280"})
public class GraciousManifestParserTest {

  @Autowired
  private GraciousManifestParser parser;

  @Test
  public void testGetThumbnail() throws IOException, URISyntaxException {
    InputStream is = this.getClass().getClassLoader().getResourceAsStream("manifests/manifests.ydc2.yale.edu-manifest-BlakeCopyL.json");
    IiifManifestSummary iiifManifestSummary = new IiifManifestSummary();
    parser.fillFromInputStream(is, iiifManifestSummary);
    Thumbnail result = iiifManifestSummary.getThumbnail();
    Assert.assertNotNull(result);

    String expResult = "classpath:manifests/blakecopyl/full/712,/0/native.jpg";
    String url = iiifManifestSummary.getThumbnail().getUrl();
    assertEquals(expResult, url);

    // v1 info.json
    is = this.getClass().getClassLoader().getResourceAsStream("manifests/manifests.ydc2.yale.edu-manifest-Admont23.json");
    iiifManifestSummary = new IiifManifestSummary();
    parser.fillFromInputStream(is, iiifManifestSummary);
    result = iiifManifestSummary.getThumbnail();
    Assert.assertNotNull(result);

    expResult = "classpath:manifests/admont23/full/753,/0/native.jpg";
    url = result.getUrl();
    assertEquals(expResult, url);

    // v2 info.json
    is = this.getClass().getClassLoader().getResourceAsStream("manifests/api.digitale-sammlungen.de_iiif_presentation_v2_bsb10916320_00001_u001-manifest.json");
    iiifManifestSummary = new IiifManifestSummary();
    parser.fillFromInputStream(is, iiifManifestSummary);
    result = iiifManifestSummary.getThumbnail();
    Assert.assertNotNull(result);

    expResult = "classpath:manifests/bsb10916320_00001/full/1028,/0/default.jpg";
    url = result.getUrl();
    assertEquals(expResult, url);

    // info.json inline in manifest:
    is = this.getClass().getClassLoader().getResourceAsStream("manifests/wellcomelibrary.org_iiif_b19792827-manifest.json");
    iiifManifestSummary = new IiifManifestSummary();
    parser.fillFromInputStream(is, iiifManifestSummary);
    result = iiifManifestSummary.getThumbnail();
    Assert.assertNotNull(result);

    expResult = "https://dlcs.io/thumbs/wellcome/1/1a1fcf18-8965-4f72-9324-45c3f6b4b469/full/654,/0/default.jpg";
    url = result.getUrl();
    assertEquals(expResult, url);

    // @value metadata without @language in manifest:
    is = this.getClass().getClassLoader().getResourceAsStream("manifests/gallica.bnf.fr-manifest-btv1b7100627v.json");
    iiifManifestSummary = new IiifManifestSummary();
    parser.fillFromInputStream(is, iiifManifestSummary);
    result = iiifManifestSummary.getThumbnail();
    Assert.assertNotNull(result);

    expResult = "http://gallica.bnf.fr/ark:/12148/btv1b7100627v.thumbnail";
    url = result.getUrl();
    assertEquals(expResult, url);
  }
}
