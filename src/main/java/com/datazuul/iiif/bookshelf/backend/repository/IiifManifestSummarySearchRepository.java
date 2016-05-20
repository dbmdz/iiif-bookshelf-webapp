package com.datazuul.iiif.bookshelf.backend.repository;

import com.datazuul.iiif.bookshelf.model.IiifManifestSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IiifManifestSummarySearchRepository<T> {

  public Iterable<T> findBy(String text, int start, int rows);

  public Page<IiifManifestSummary> findBy(String text, Pageable pageable);

  public Iterable<T> findBy(String text);

  public void save(IiifManifestSummary manifestSummary);
}
