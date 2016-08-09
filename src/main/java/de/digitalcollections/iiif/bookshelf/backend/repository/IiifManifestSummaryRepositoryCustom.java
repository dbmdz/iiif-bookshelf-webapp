package de.digitalcollections.iiif.bookshelf.backend.repository;

import de.digitalcollections.iiif.bookshelf.model.IiifManifestSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 *
 * @author ralf
 */
public interface IiifManifestSummaryRepositoryCustom {

  public Page<IiifManifestSummary> findBy(String text, Pageable page);
}
