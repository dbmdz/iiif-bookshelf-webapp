package com.datazuul.iiif.bookshelf.model;

/**
 *
 * @author ralf
 */
public class Thumbnail {

  private String iiifImageServiceUri;
  private IiifImageApiVersion iiifImageApiVersion;

  public Thumbnail() {

  }

  public Thumbnail(IiifImageApiVersion iiifImageApiVersion, String iiifImageServiceUri) {
    this.iiifImageApiVersion = iiifImageApiVersion;
    setIiifImageServiceUri(iiifImageServiceUri);
  }

  public Thumbnail(String context, String iiifImageServiceUri) {
    this.iiifImageApiVersion = IiifImageApiVersion.getVersion(context);
    setIiifImageServiceUri(iiifImageServiceUri);
  }

  public IiifImageApiVersion getIiifImageApiVersion() {
    return iiifImageApiVersion;
  }

  public void setIiifImageApiVersion(IiifImageApiVersion iiifImageApiVersion) {
    this.iiifImageApiVersion = iiifImageApiVersion;
  }

  public String getIiifImageServiceUri() {
    return iiifImageServiceUri;
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
}
