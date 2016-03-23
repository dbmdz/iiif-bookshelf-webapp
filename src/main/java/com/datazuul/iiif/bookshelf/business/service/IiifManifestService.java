package com.datazuul.iiif.bookshelf.business.service;

import com.datazuul.iiif.bookshelf.model.IiifManifestSummary;
import java.util.List;

/**
 *
 * @author ralf
 */
public interface IiifManifestService {
    public List<IiifManifestSummary> getAll();
    
    public IiifManifestSummary add(IiifManifestSummary manifest);
}
