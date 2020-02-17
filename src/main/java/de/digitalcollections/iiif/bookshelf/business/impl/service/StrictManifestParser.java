package de.digitalcollections.iiif.bookshelf.business.impl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.iiif.bookshelf.model.IiifManifestSummary;
import de.digitalcollections.iiif.bookshelf.model.Thumbnail;
import de.digitalcollections.iiif.bookshelf.model.exceptions.NotFoundException;
import de.digitalcollections.iiif.model.ImageContent;
import de.digitalcollections.iiif.model.PropertyValue;
import de.digitalcollections.iiif.model.image.ImageApiProfile;
import de.digitalcollections.iiif.model.image.ImageService;
import de.digitalcollections.iiif.model.image.Size;
import de.digitalcollections.iiif.model.jackson.IiifObjectMapper;
import de.digitalcollections.iiif.model.openannotation.Annotation;
import de.digitalcollections.iiif.model.sharedcanvas.Manifest;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StrictManifestParser extends AbstractManifestParser {

  private static final Logger LOGGER = LoggerFactory.getLogger(StrictManifestParser.class);

  @Autowired private ObjectMapper objectMapper;

  @Override
  public void fillSummary(IiifManifestSummary manifestSummary)
      throws IOException, URISyntaxException {
    final InputStream is;
    try {
      is = getContentInputStream(manifestSummary.getManifestUri());
    } catch (UnsupportedOperationException ex) {
      throw new IOException(ex);
    }
    Manifest manifest = objectMapper.readValue(is, Manifest.class);
    fillFromManifest(manifest, manifestSummary);
  }

  /**
   * Language may be associated with strings that are intended to be displayed to the user with the
   * following pattern of &#64;value plus the RFC 5646 code in &#64;language, instead of a plain
   * string. This pattern may be used in label, description, attribution and the label and value
   * fields of the metadata construction.
   */
  private void fillFromManifest(Manifest manifest, IiifManifestSummary manifestSummary)
      throws NotFoundException {
    // set from "@id" value to avoid using slightly different http-urls (e.g. with or without
    // request params) pointing to same manifest
    manifestSummary.setManifestUri(manifest.getIdentifier().toString());
    manifestSummary.setLabels(getLocalizedStrings(manifest.getLabel()));
    manifestSummary.setDescriptions(getLocalizedStrings(manifest.getDescription()));
    manifestSummary.setAttributions(getLocalizedStrings(manifest.getAttribution()));

    Thumbnail thumbnail = getThumbnail(manifest);
    manifestSummary.setThumbnail(thumbnail);

    URI logoUri = manifest.getLogoUri();
    if (logoUri != null) {
      manifestSummary.setLogoUrl(logoUri.toString());
    }
  }

  public HashMap<Locale, String> getLocalizedStrings(PropertyValue val) {
    HashMap<Locale, String> strings = new HashMap<>();
    if (val != null) {
      val.getLocalizations().forEach(l -> strings.put(l, val.getFirstValue(l)));
    }
    return strings;
  }

  public void setObjectMapper(IiifObjectMapper mapper) {
    this.objectMapper = mapper;
  }

  /**
   * Thumbnail: "A small image that depicts or pictorially represents the resource that the property
   * is attached to, such as the title page, a significant image or rendering of a canvas with
   * multiple content resources associated with it. It is recommended that a IIIF Image API service
   * be available for this image for manipulations such as resizing. If a resource has multiple
   * thumbnails, then each of them should be different."
   *
   * <p>see http://iiif.io/api/presentation/2.1/#thumbnail
   *
   * @param manifest iiif manifest
   * @return thumbnail representing this manifest's object
   */
  public Thumbnail getThumbnail(Manifest manifest) {
    /*
    A manifest should have exactly one thumbnail image, and may have more than one.
    A sequence may have one or more thumbnails and should have at least one thumbnail if there are multiple sequences in a single manifest.
    A canvas may have one or more thumbnails and should have at least one thumbnail if there are multiple images or resources that make up the representation.
    A content resource may have one or more thumbnails and should have at least one thumbnail if it is an option in a choice of resources.
    Other resource types may have one or more thumbnails.
     */
    ImageContent imageContent = null;

    // A manifest should have exactly one thumbnail image, and may have more than one.
    if (manifest.getThumbnails() != null) {
      imageContent = manifest.getThumbnail();
    }

    if (imageContent == null && manifest.getDefaultSequence() != null) {
      // A sequence may have one or more thumbnails and should have at least one thumbnail if there
      // are multiple sequences in a single manifest.
      imageContent = manifest.getDefaultSequence().getThumbnail();
    }

    if (imageContent == null
        && manifest.getDefaultSequence() != null
        && manifest.getDefaultSequence().getCanvases() != null) {
      // A canvas may have one or more thumbnails and should have at least one thumbnail if there
      // are multiple images or resources that make up the representation.
      imageContent =
          manifest.getDefaultSequence().getCanvases().stream()
              .map(c -> c.getThumbnails())
              .filter(ts -> ts != null && ts.size() > 0)
              .map(ts -> ts.get(0))
              .findFirst()
              .orElse(null);
    }

    if (imageContent == null) {
      // No thumbnail found, yet. Take the first image of first canvas as "thumbnail".
      imageContent =
          manifest.getDefaultSequence().getCanvases().get(0).getImages().stream()
              .map(Annotation::getResource)
              .filter(ImageContent.class::isInstance)
              .map(ImageContent.class::cast)
              .findFirst()
              .orElse(null);
    }

    if (imageContent != null) {
      // thumbnail candidate found
      ImageService imageService = null;
      if (imageContent.getServices() != null) {
        imageService =
            imageContent.getServices().stream()
                .filter(ImageService.class::isInstance)
                .map(ImageService.class::cast)
                .findFirst()
                .orElse(null);
      }
      if (imageService != null) {
        boolean isV1 =
            imageService.getProfiles().stream()
                .map(p -> p.getIdentifier().toString())
                .anyMatch(ImageApiProfile.V1_PROFILES::contains);

        String serviceUrl = imageService.getIdentifier().toString();
        if (serviceUrl.endsWith("/")) {
          serviceUrl = serviceUrl.substring(0, serviceUrl.length() - 1);
        }

        List<Size> sizes = imageService.getSizes();
        return createThumbnail(sizes, serviceUrl, isV1);
      } else {
        return new Thumbnail(imageContent.getIdentifier().toString());
      }
    }
    return null;
  }
}
