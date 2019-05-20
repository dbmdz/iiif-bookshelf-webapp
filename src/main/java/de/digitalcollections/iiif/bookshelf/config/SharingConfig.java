package de.digitalcollections.iiif.bookshelf.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SharingConfig {

  private String previewImageUrl;

  private Integer previewImageWidth;

  private Integer previewImageHeight;

  private String twitterSiteHandle;

  public SharingConfig(
      @Value("${custom.sharing.previewImage.url:#{null}}") String previewImageUrl,
      @Value("${custom.sharing.previewImage.width:#{null}}") String previewImageWidth,
      @Value("${custom.sharing.previewImage.height:#{null}}") String previewImageHeight,
      @Value("${custom.sharing.twitter.siteHandle:#{null}}") String twitterSiteHandle
  ) {
    this.previewImageUrl = previewImageUrl;
    if (previewImageHeight != null) {
      this.previewImageHeight = Integer.parseInt(previewImageHeight);
    }
    if (previewImageWidth != null) {
      this.previewImageWidth = Integer.parseInt(previewImageWidth);
    }

    this.twitterSiteHandle = twitterSiteHandle;
  }

  public String getPreviewImageUrl() {
    return previewImageUrl;
  }

  public Integer getPreviewImageWidth() {
    return previewImageWidth;
  }

  public Integer getPreviewImageHeight() {
    return previewImageHeight;
  }

  public String getTwitterSiteHandle() {
    return twitterSiteHandle;
  }

}
