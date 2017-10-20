package de.digitalcollections.iiif.bookshelf.business.api.service;

import de.digitalcollections.iiif.bookshelf.model.IiifManifestSummary;
import de.digitalcollections.iiif.bookshelf.model.exceptions.NotFoundException;
import de.digitalcollections.iiif.bookshelf.model.exceptions.SearchSyntaxException;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
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

  public void enrichAndSave(IiifManifestSummary manifestSummary) throws NotFoundException, IOException;

  public Page<IiifManifestSummary> findAll(String searchText, Pageable pageable) throws SearchSyntaxException;

  public IiifManifestSummary get(UUID uuid);

  public List<IiifManifestSummary> getAll();

  public Page<IiifManifestSummary> getAll(Pageable pageable);

  public String getLabel(IiifManifestSummary manifestSummary, Locale locale);

  public void reindexSearch();
}
