package com.datazuul.iiif.bookshelf.business.service.impl;

import com.datazuul.iiif.bookshelf.backend.repository.IiifManifestRepository;
import com.datazuul.iiif.bookshelf.business.service.IiifManifestService;
import com.datazuul.iiif.bookshelf.model.IiifManifestSummary;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author ralf
 */
@Service
public class IiifManifestServiceImpl implements IiifManifestService {

    @Autowired
    private IiifManifestRepository iiifManifestRepository;

    @Override
    public List<IiifManifestSummary> getAll() {
        return iiifManifestRepository.findAll();
    }

    @Override
    public IiifManifestSummary add(IiifManifestSummary manifest) {
        if (iiifManifestRepository.exists(manifest.getManifestUri())) {
            throw new IllegalArgumentException("object already exists");
        }
        return iiifManifestRepository.save(manifest);
    }

}
