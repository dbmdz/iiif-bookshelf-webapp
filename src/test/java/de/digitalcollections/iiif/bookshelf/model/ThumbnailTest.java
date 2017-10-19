package de.digitalcollections.iiif.bookshelf.model;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ThumbnailTest {

  @Test
  public void thumbnailWithoutSpecificApiVersionShouldReturnOriginalUrl() {
    final String url = "http://example.com/some/path";
    Thumbnail thumbnail = new Thumbnail(url);
    assertThat(thumbnail.getUrl()).isEqualTo(url);
  }

}
