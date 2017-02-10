package de.digitalcollections.iiif.bookshelf.model;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class ThumbnailTest {

  @Test
  public void thumbnailWithoutSpecificApiVersionShouldReturnOriginalUrl() {
    final String url = "http://example.com/some/path";
    Thumbnail thumbnail = new Thumbnail(url);
    assertThat(thumbnail.getUrl()).isEqualTo(url);
  }

}
