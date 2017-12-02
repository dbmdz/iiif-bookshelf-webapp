package de.digitalcollections.iiif.bookshelf.business.impl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.iiif.bookshelf.model.IiifManifestSummary;
import de.digitalcollections.iiif.bookshelf.model.Thumbnail;
import de.digitalcollections.iiif.bookshelf.model.exceptions.NotFoundException;
import de.digitalcollections.iiif.model.ImageContent;
import de.digitalcollections.iiif.model.PropertyValue;
import de.digitalcollections.iiif.model.image.ImageApiProfile;
import de.digitalcollections.iiif.model.image.ImageService;
import de.digitalcollections.iiif.model.openannotation.Choice;
import de.digitalcollections.iiif.model.sharedcanvas.Manifest;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StrictManifestParser extends AbstractManifestParser {

  @Autowired
  private ObjectMapper objectMapper;

  @Override
  public void fillSummary(IiifManifestSummary manifestSummary) throws IOException, URISyntaxException {
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
   * Language may be associated with strings that are intended to be displayed to the user with the following pattern of
   * &#64;value plus the RFC 5646 code in &#64;language, instead of a plain string. This pattern may be used in label,
   * description, attribution and the label and value fields of the metadata construction.
   */
  private void fillFromManifest(Manifest manifest, IiifManifestSummary manifestSummary) throws NotFoundException {
    // set from "@id" value to avoid using slightly different http-urls (e.g. with or without request params) pointing to same manifest
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

  private Thumbnail getThumbnail(Manifest manifest) {
    ImageContent thumb;
    if (manifest.getThumbnails() != null && manifest.getThumbnails().size() > 0) {
      thumb = manifest.getThumbnail();
    } else {
      thumb = manifest.getDefaultSequence().getCanvases().stream()
              .map(c -> c.getThumbnails())
              .filter(ts -> ts != null && ts.size() > 0)
              .map(ts -> ts.get(0))
              .findFirst().orElse(null);

    }
    if (thumb == null) {
      ImageService service = manifest.getDefaultSequence().getCanvases().get(0).getImages().stream()
              .map(a -> {
                if (a.getResource() instanceof ImageContent) {
                  return (ImageContent) a.getResource();
                } else {
                  return (ImageContent) ((Choice) a.getResource()).getDefault();
                }
              })
              .flatMap(r -> r.getServices().stream())
              .filter(ImageService.class::isInstance)
              .map(ImageService.class::cast)
              .findFirst().orElse(null);
      boolean isV1 = service.getProfiles().stream()
              .map(p -> p.getIdentifier().toString())
              .anyMatch(ImageApiProfile.V1_PROFILES::contains);

      String serviceUrl = service.getIdentifier().toString();
      if (serviceUrl.endsWith("/")) {
        serviceUrl = serviceUrl.substring(0, serviceUrl.length() - 1);
      }
      String thumbnailUrl = String.format("%s/full/280,/0/", serviceUrl);
      if (isV1) {
        thumbnailUrl += "native.jpg";
      } else {
        thumbnailUrl += "default.jpg";
      }
      thumb = new ImageContent(thumbnailUrl);
    }

    if (thumb != null && thumb.getServices() != null && thumb.getServices().size() > 0) {
      de.digitalcollections.iiif.model.Service service = thumb.getServices().get(0);
      return new Thumbnail(service.getContext().toString(), service.getIdentifier().toString());
    } else if (thumb != null) {
      return new Thumbnail(thumb.getIdentifier().toString());
    } else {
      return null;
    }
  }

}
