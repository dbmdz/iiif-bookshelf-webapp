package de.digitalcollections.iiif.bookshelf.util;

import de.digitalcollections.iiif.model.ImageContent;
import de.digitalcollections.iiif.model.Service;
import de.digitalcollections.iiif.model.image.ImageService;
import de.digitalcollections.iiif.model.openannotation.Annotation;
import de.digitalcollections.iiif.model.sharedcanvas.Canvas;
import de.digitalcollections.iiif.model.sharedcanvas.Manifest;
import de.digitalcollections.iiif.model.sharedcanvas.Resource;
import de.digitalcollections.iiif.model.sharedcanvas.Sequence;
import java.net.URI;
import java.util.List;

public class PreviewImageUtil {

  private final Manifest manifest;

  public PreviewImageUtil(Manifest manifest) {
    this.manifest = manifest;
  }

  public static ImageService findImageService(List<Service> services) {
    if (services == null) {
      return null;
    }
    for (Service service : services) {
      if (service instanceof ImageService) {
        return (ImageService) service;
      }
    }
    return null;
  }

  public ImageContent findImageResource(URI imageServiceURI) {
    if (imageServiceURI == null) {
      return null;
    }
    if (manifest.getSequences() == null) {
      return null;
    }
    for (Sequence sequence : manifest.getSequences()) {
      if (sequence.getCanvases() == null) {
        continue;
      }
      for (Canvas canvas : sequence.getCanvases()) {
        if (canvas.getImages() == null) {
          continue;
        }
        for (Annotation image : canvas.getImages()) {
          if (image.getResource() == null) {
            continue;
          }
          Resource<?> resource = image.getResource();
          ImageService imageService = findImageService(resource.getServices());
          if (imageService != null && imageServiceURI.equals(imageService.getIdentifier())) {
            return (ImageContent) resource;
          }
        }
      }
    }
    return null;
  }

  public ImageContent findBestPreviewImage() {
    ImageContent preview = manifest.getThumbnail();
    if (preview == null) {
      return findFirstImage();
    }

    List<Service> services = manifest.getThumbnail().getServices();
    ImageService imageService = findImageService(services);
    if (imageService == null) {
      return findFirstImage();
    }

    URI imageServiceURI = imageService.getIdentifier();
    ImageContent betterPreview = findImageResource(imageServiceURI);
    if (betterPreview == null) {
      return preview;
    }

    return betterPreview;
  }

  public ImageContent findFirstImage() {
    if (manifest.getSequences() == null) {
      return null;
    }
    for (Sequence sequence : manifest.getSequences()) {
      if (sequence.getCanvases() == null) {
        continue;
      }
      for (Canvas canvas : sequence.getCanvases()) {
        if (canvas.getImages() == null) {
          continue;
        }
        for (Annotation image : canvas.getImages()) {
          if (image.getResource() == null) {
            continue;
          }
          return (ImageContent) image.getResource();
        }
      }
    }
    return null;
  }
}
