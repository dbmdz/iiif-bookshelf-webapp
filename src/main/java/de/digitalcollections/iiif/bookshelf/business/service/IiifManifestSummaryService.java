package de.digitalcollections.iiif.bookshelf.business.service;

import de.digitalcollections.iiif.bookshelf.model.IiifManifestSummary;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 *
 * @author ralf
 */
public interface IiifManifestSummaryService {

  public IiifManifestSummary add(IiifManifestSummary manifest);

  public long countAll();

  public void enrichAndSave(IiifManifestSummary manifestSummary);

  public Page<IiifManifestSummary> findAll(String searchText, Pageable pageable);

  public IiifManifestSummary get(UUID uuid);

  public List<IiifManifestSummary> getAll();

  public Page<IiifManifestSummary> getAll(Pageable pageable);
}
