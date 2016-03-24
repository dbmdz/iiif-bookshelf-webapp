package com.datazuul.iiif.bookshelf.business.service.impl;

import com.datazuul.iiif.bookshelf.model.IiifManifestSummary;
import com.datazuul.iiif.presentation.api.model.Manifest;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.datazuul.iiif.bookshelf.backend.repository.IiifManifestSummaryRepository;
import com.datazuul.iiif.bookshelf.business.service.IiifManifestSummaryService;
import com.datazuul.iiif.presentation.backend.repository.PresentationRepository;
import com.datazuul.iiif.presentation.model.NotFoundException;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ralf
 */
@Service
public class IiifManifestSummaryServiceImpl implements IiifManifestSummaryService {

  private static final Logger LOGGER = LoggerFactory.getLogger(IiifManifestSummaryServiceImpl.class);

  @Autowired
  private IiifManifestSummaryRepository iiifManifestSummaryRepository;

  @Autowired
  private PresentationRepository presentationRepository;

  @Override
  public List<IiifManifestSummary> getAll() {
    return iiifManifestSummaryRepository.findAll();
  }

  @Override
  public IiifManifestSummary add(IiifManifestSummary manifest) {
    if (iiifManifestSummaryRepository.exists(manifest.getManifestUri())) {
      throw new IllegalArgumentException("object already exists");
    }
    return iiifManifestSummaryRepository.save(manifest);
  }

  @Override
  public void enrichAndSave(IiifManifestSummary manifestSummary) {
    try {
      Manifest manifest = presentationRepository.getManifest(manifestSummary.getManifestUri());

      // enrichment
      String label = manifest.getLabel();
      manifestSummary.setLabel(label);

      String description = manifest.getDescription();
      manifestSummary.setDescription(description);

      String attribution = manifest.getAttribution();
      manifestSummary.setAttribution(attribution);

      URI thumbnailServiceUri = null;
      try {
        thumbnailServiceUri = manifest.getThumbnail().getService().getId();
      } catch (Exception ex) {

      }
      if (thumbnailServiceUri == null) {
        try {
          // first image
          thumbnailServiceUri = manifest.getSequences().get(0).getCanvases().get(0).getImages().get(0).getResource().getService().getId();
        } catch (Exception ex) {

        }
      }
      manifestSummary.setPreviewImageIiifImageServiceUri(thumbnailServiceUri);

      iiifManifestSummaryRepository.save(manifestSummary);
    } catch (NotFoundException ex) {
      LOGGER.warn("No manifest found for " + manifestSummary.getManifestUri().toString(), ex);
    }
  }

}
