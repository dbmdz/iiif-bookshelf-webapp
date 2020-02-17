package de.digitalcollections.iiif.bookshelf.business.impl.service;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.iiif.bookshelf.model.IiifManifestSummary;
import de.digitalcollections.iiif.bookshelf.model.Thumbnail;
import de.digitalcollections.iiif.model.jackson.IiifObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = {IiifObjectMapper.class, GraciousManifestParser.class})
@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = {"custom.summary.thumbnail.width=280"})
public class GraciousManifestParserTest {

  @Autowired private GraciousManifestParser parser;

  @Test
  public void testGetThumbnail() throws IOException, URISyntaxException {
    InputStream is =
        this.getClass()
            .getClassLoader()
            .getResourceAsStream("manifests/manifests.ydc2.yale.edu-manifest-BlakeCopyL.json");
    IiifManifestSummary iiifManifestSummary = new IiifManifestSummary();
    parser.fillFromInputStream(is, iiifManifestSummary);
    Thumbnail result = iiifManifestSummary.getThumbnail();
    assertThat(result).isNotNull();

    String expResult = "classpath:manifests/blakecopyl/full/712,/0/native.jpg";
    String url = iiifManifestSummary.getThumbnail().getUrl();
    assertThat(url).isEqualTo(expResult);

    // v1 info.json
    is =
        this.getClass()
            .getClassLoader()
            .getResourceAsStream("manifests/manifests.ydc2.yale.edu-manifest-Admont23.json");
    iiifManifestSummary = new IiifManifestSummary();
    parser.fillFromInputStream(is, iiifManifestSummary);
    result = iiifManifestSummary.getThumbnail();
    assertThat(result).isNotNull();

    expResult = "classpath:manifests/admont23/full/753,/0/native.jpg";
    url = result.getUrl();
    assertThat(url).isEqualTo(expResult);

    // v2 info.json
    is =
        this.getClass()
            .getClassLoader()
            .getResourceAsStream(
                "manifests/api.digitale-sammlungen.de_iiif_presentation_v2_bsb10916320_00001_u001-manifest.json");
    iiifManifestSummary = new IiifManifestSummary();
    parser.fillFromInputStream(is, iiifManifestSummary);
    result = iiifManifestSummary.getThumbnail();
    assertThat(result).isNotNull();

    expResult = "classpath:manifests/bsb10916320_00001/full/1028,/0/default.jpg";
    url = result.getUrl();
    assertThat(url).isEqualTo(expResult);

    // info.json inline in manifest:
    is =
        this.getClass()
            .getClassLoader()
            .getResourceAsStream("manifests/wellcomelibrary.org_iiif_b19792827-manifest.json");
    iiifManifestSummary = new IiifManifestSummary();
    parser.fillFromInputStream(is, iiifManifestSummary);
    result = iiifManifestSummary.getThumbnail();
    assertThat(result).isNotNull();

    expResult =
        "https://dlcs.io/thumbs/wellcome/1/1a1fcf18-8965-4f72-9324-45c3f6b4b469/full/654,/0/default.jpg";
    url = result.getUrl();
    assertThat(url).isEqualTo(expResult);

    // @value metadata without @language in manifest:
    is =
        this.getClass()
            .getClassLoader()
            .getResourceAsStream("manifests/gallica.bnf.fr-manifest-btv1b7100627v.json");
    iiifManifestSummary = new IiifManifestSummary();
    parser.fillFromInputStream(is, iiifManifestSummary);
    result = iiifManifestSummary.getThumbnail();
    assertThat(result).isNotNull();

    expResult = "http://gallica.bnf.fr/ark:/12148/btv1b7100627v.thumbnail";
    url = result.getUrl();
    assertThat(url).isEqualTo(expResult);
  }
}
