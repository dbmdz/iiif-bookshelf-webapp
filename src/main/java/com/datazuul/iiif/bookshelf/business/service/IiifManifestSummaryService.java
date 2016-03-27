package com.datazuul.iiif.bookshelf.business.service;

import com.datazuul.iiif.bookshelf.model.IiifManifestSummary;
import java.util.List;

/**
 *
 * @author ralf
 */
public interface IiifManifestSummaryService {

  public IiifManifestSummary add(IiifManifestSummary manifest);

  public long countAll();

  public void enrichAndSave(IiifManifestSummary manifestSummary);

  public List<IiifManifestSummary> getAll();
}
