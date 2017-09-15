package de.digitalcollections.iiif.bookshelf.business.api.service;

import de.digitalcollections.iiif.bookshelf.model.IiifManifestSummary;
import de.digitalcollections.iiif.bookshelf.model.exceptions.SearchSyntaxException;
import de.digitalcollections.iiif.presentation.model.api.exceptions.NotFoundException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.json.simple.parser.ParseException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 *
 * @author ralf
 */
public interface IiifManifestSummaryService {

  public IiifManifestSummary add(IiifManifestSummary manifest);

  public long countAll();

  public void enrichAndSave(IiifManifestSummary manifestSummary) throws ParseException, NotFoundException;

  public Page<IiifManifestSummary> findAll(String searchText, Pageable pageable) throws SearchSyntaxException;

  public IiifManifestSummary get(UUID uuid);

  public List<IiifManifestSummary> getAll();

  public Page<IiifManifestSummary> getAll(Pageable pageable);

  public String getLabel(IiifManifestSummary manifestSummary, Locale locale);

  public void reindexSearch();
}
