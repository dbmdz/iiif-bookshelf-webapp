package de.digitalcollections.iiif.bookshelf.backend.impl.repository;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class IiiifManifestSummarySearchRepositoryImplSolrjTest {

  private String text;
  private String result;
  IiifManifestSummarySearchRepositoryImplSolrj manifestSummarySearch;

  @Test
  public void textWithoutSpecialCharactersShallRemainUnchanged() {
    manifestSummarySearch = new IiifManifestSummarySearchRepositoryImplSolrj();
    text = "Das ist ein Text";
    result = manifestSummarySearch.escapeUnwantedSpecialChars(text);
    assertThat(result).isEqualTo(text);
  }

  @Test
  public void unwantedSpecialCharactersShallBeEscaped() {
    manifestSummarySearch = new IiifManifestSummarySearchRepositoryImplSolrj();
    text = "+-&&||!(){}[]^~:\\";
    result = manifestSummarySearch.escapeUnwantedSpecialChars(text);
    assertThat(result).isEqualTo("\\+\\-\\&\\&\\|\\|\\!\\(\\)\\{\\}\\[\\]\\^\\~\\:\\\\");
  }

  @Test
  public void asteriskAndQuotationMarkShallRemainUnchanged() {
    manifestSummarySearch = new IiifManifestSummarySearchRepositoryImplSolrj();
    text = "* \"?";
    result = manifestSummarySearch.escapeUnwantedSpecialChars(text);
    assertThat(result).isEqualTo("* \"?");
  }
}
