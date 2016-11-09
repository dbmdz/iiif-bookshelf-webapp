package de.digitalcollections.iiif.bookshelf.model;

public class SearchRequest {

  private String query;

  public SearchRequest() {
  }

  public SearchRequest(String query) {
    this.query = query;
  }

  public String getQuery() {
    return query;
  }

  public void setQuery(String query) {
    this.query = query;
  }
}
