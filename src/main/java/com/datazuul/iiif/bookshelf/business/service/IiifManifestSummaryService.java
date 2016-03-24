package com.datazuul.iiif.bookshelf.business.service;

import com.datazuul.iiif.bookshelf.model.IiifManifestSummary;
import java.util.List;

/**
 *
 * @author ralf
 */
public interface IiifManifestSummaryService {
    public List<IiifManifestSummary> getAll();
    
    public IiifManifestSummary add(IiifManifestSummary manifest);

    public void enrichAndSave(IiifManifestSummary manifestSummary);
}
