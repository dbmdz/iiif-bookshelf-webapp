package de.digitalcollections.iiif.bookshelf.business.impl.service;

import com.google.common.io.Resources;
import de.digitalcollections.iiif.bookshelf.model.Thumbnail;
import de.digitalcollections.iiif.model.jackson.IiifObjectMapper;
import de.digitalcollections.iiif.model.sharedcanvas.Manifest;
import java.io.IOException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
//@SpringBootTest(classes = {Application.class})
@ContextConfiguration(classes = {IiifObjectMapper.class, StrictManifestParser.class})
@TestPropertySource(properties = {"custom.summary.thumbnail.width=280"})
public class StrictManifestParserTest {

  @Autowired
  private StrictManifestParser parser;

  @Autowired
  private IiifObjectMapper mapper;

  private <T> T readFromResources(String filename, Class<T> clz) throws IOException {
    return mapper.readValue(
            Resources.getResource(filename), clz);
  }

  @Test
  public void testGetThumbnail() throws IOException {
    // v1 info.json
    Manifest manifest = readFromResources("manifests/manifests.ydc2.yale.edu-manifest-Admont23.json", Manifest.class);
    String expResult = "classpath:manifests/admont23/full/753,/0/native.jpg";
    Thumbnail result = parser.getThumbnail(manifest);
    String url = result.getUrl();
    assertEquals(expResult, url);

    // v2 info.json
    manifest = readFromResources("manifests/api.digitale-sammlungen.de_iiif_presentation_v2_bsb10916320_00001_u001-manifest.json", Manifest.class);
    expResult = "classpath:manifests/bsb10916320_00001/full/1028,/0/default.jpg";
    result = parser.getThumbnail(manifest);
    url = result.getUrl();
    assertEquals(expResult, url);

    // info.json inline in manifest:
    manifest = readFromResources("manifests/wellcomelibrary.org_iiif_b19792827-manifest.json", Manifest.class);
    expResult = "https://dlcs.io/thumbs/wellcome/1/1a1fcf18-8965-4f72-9324-45c3f6b4b469/full/654,/0/default.jpg";
    result = parser.getThumbnail(manifest);
    url = result.getUrl();
    assertEquals(expResult, url);

    // @value metadata without @language in manifest:
    manifest = readFromResources("manifests/gallica.bnf.fr-manifest-btv1b7100627v.json", Manifest.class);
    expResult = "http://gallica.bnf.fr/ark:/12148/btv1b7100627v.thumbnail";
    result = parser.getThumbnail(manifest);
    url = result.getUrl();
    assertEquals(expResult, url);

  }

}
