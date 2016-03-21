package com.datazuul.iiif.catalog.portal.business.service.impl;

import com.datazuul.iiif.catalog.portal.backend.repository.IiifManifestRepository;
import com.datazuul.iiif.catalog.portal.business.service.IiifManifestService;
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
    public List<String> getAllManifests() {
        return iiifManifestRepository.findAll();
    }

}
