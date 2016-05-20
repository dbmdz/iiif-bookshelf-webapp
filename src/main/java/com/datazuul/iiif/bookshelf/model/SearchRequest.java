package com.datazuul.iiif.bookshelf.model;

public class SearchRequest {

  private String term;

  public SearchRequest() {}

  public SearchRequest(String term) {
    this.term = term;
  }

  public String getTerm() {
    return term;
  }

  public void setTerm(String term) {
    this.term = term;
  }
}
