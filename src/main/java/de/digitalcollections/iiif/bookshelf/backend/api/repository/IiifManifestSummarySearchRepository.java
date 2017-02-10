package de.digitalcollections.iiif.bookshelf.backend.api.repository;

import de.digitalcollections.iiif.bookshelf.model.exceptions.SearchSyntaxException;
import de.digitalcollections.iiif.bookshelf.model.IiifManifestSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IiifManifestSummarySearchRepository<T> {

  public Iterable<T> findBy(String text, int start, int rows);

  public Page<IiifManifestSummary> findBy(String text, Pageable pageable) throws SearchSyntaxException;

  public Iterable<T> findBy(String text);

  public void save(IiifManifestSummary manifestSummary);
}
