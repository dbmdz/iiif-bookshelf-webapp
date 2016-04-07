package com.datazuul.iiif.bookshelf.backend.repository;

import com.datazuul.iiif.bookshelf.model.IiifManifestSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 *
 * @author ralf
 */
public interface IiifManifestSummaryRepositoryCustom {

  public Page<IiifManifestSummary> findBy(String text, Pageable page);
}
