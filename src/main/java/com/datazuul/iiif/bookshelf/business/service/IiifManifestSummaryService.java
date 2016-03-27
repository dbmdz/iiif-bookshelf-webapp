package com.datazuul.iiif.bookshelf.business.service;

import com.datazuul.iiif.bookshelf.model.IiifManifestSummary;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author ralf
 */
public interface IiifManifestSummaryService {

  public IiifManifestSummary add(IiifManifestSummary manifest);

  public long countAll();

  public void enrichAndSave(IiifManifestSummary manifestSummary);

  public IiifManifestSummary get(UUID uuid);

  public List<IiifManifestSummary> getAll();
}
