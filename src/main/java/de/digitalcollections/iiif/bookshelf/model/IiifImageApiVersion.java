package de.digitalcollections.iiif.bookshelf.model;

/**
 * TODO move to Image API library (model api)
 */
public enum IiifImageApiVersion {
  V1_1, V2;

  public static IiifImageApiVersion getVersion(String context) {
    if (context == null) {
      return null;
    }

    switch (context) {
      case "http://library.stanford.edu/iiif/image-api/1.1/context.json":
        return V1_1;
      case "http://iiif.io/api/image/1/context.json":
        return V1_1;
      case "http://iiif.io/api/image/2/context.json":
        return V2;
      default:
        return null;
    }
  }
}
