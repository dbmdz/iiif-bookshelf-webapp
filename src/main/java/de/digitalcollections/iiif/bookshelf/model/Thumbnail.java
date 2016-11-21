package de.digitalcollections.iiif.bookshelf.model;

public class Thumbnail {

  private String iiifImageServiceUri;
  private IiifImageApiVersion iiifImageApiVersion;
  private String url;

  public Thumbnail() {

  }

  public Thumbnail(IiifImageApiVersion iiifImageApiVersion, String iiifImageServiceUri) {
    this.iiifImageApiVersion = iiifImageApiVersion;
    this.iiifImageServiceUri = iiifImageServiceUri;
  }

  public Thumbnail(String context, String iiifImageServiceUri) {
    this(IiifImageApiVersion.getVersion(context), iiifImageServiceUri);
  }

  public Thumbnail(String url) {
    this.url = url;
  }

  public IiifImageApiVersion getIiifImageApiVersion() {
    return iiifImageApiVersion;
  }

  public void setIiifImageApiVersion(IiifImageApiVersion iiifImageApiVersion) {
    this.iiifImageApiVersion = iiifImageApiVersion;
  }

  public void setIiifImageServiceUri(String iiifImageServiceUri) {
    if (iiifImageServiceUri != null) {
      iiifImageServiceUri = iiifImageServiceUri.trim();
      if (iiifImageServiceUri.endsWith("/")) {
        iiifImageServiceUri = iiifImageServiceUri.substring(0, iiifImageServiceUri.lastIndexOf("/"));
      }
    }
    this.iiifImageServiceUri = iiifImageServiceUri;
  }

  public String getUrl() {
    if (iiifImageServiceUri != null && iiifImageApiVersion != null) {
      if (IiifImageApiVersion.V1_1 == iiifImageApiVersion) {
        setUrl(iiifImageServiceUri + "/full/,90/0/native.jpg");
      } else if (IiifImageApiVersion.V2 == iiifImageApiVersion) {
        setUrl(iiifImageServiceUri + "/full/,90/0/default.jpg");
      }
    }
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }
}
