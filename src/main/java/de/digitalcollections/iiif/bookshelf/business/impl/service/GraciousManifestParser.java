package de.digitalcollections.iiif.bookshelf.business.impl.service;

import de.digitalcollections.iiif.bookshelf.model.IiifManifestSummary;
import de.digitalcollections.iiif.bookshelf.model.Thumbnail;
import de.digitalcollections.iiif.model.image.ImageApiProfile;
import de.digitalcollections.iiif.model.image.Size;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class GraciousManifestParser extends AbstractManifestParser {

  private static final Logger LOGGER = LoggerFactory.getLogger(GraciousManifestParser.class);

  public static final Locale DEFAULT_LOCALE = Locale.GERMAN;

  @Override
  public void fillSummary(IiifManifestSummary manifestSummary) throws IOException, URISyntaxException {
    final InputStream is;
    try {
      is = getContentInputStream(manifestSummary.getManifestUri());
    } catch (UnsupportedOperationException ex) {
      throw new IOException(ex);
    }
    fillFromInputStream(is, manifestSummary);
  }

  protected void fillFromInputStream(final InputStream is, IiifManifestSummary manifestSummary) throws IOException {
    try {
      JSONParser jsonParser = new JSONParser();
      JSONObject jsonObject = (JSONObject) jsonParser.parse(new InputStreamReader(is, StandardCharsets.UTF_8));

      fillFromJsonObject(jsonObject, manifestSummary);
    } catch (ParseException ex) {
      LOGGER.warn("Could not parse json at {}.", manifestSummary.getManifestUri());
      throw new IOException("Invalid JSON.");
    }
  }

  /**
   * Language may be associated with strings that are intended to be displayed to the user with the following pattern of
   *
   * @value plus the RFC 5646 code in
   *
   *        @language, instead of a plain string. This pattern may be used in label, description, attribution and the
   *        label and value fields of the metadata construction.
   *
   * @param manifestSummary
   * @throws NotFoundException
   * @throws ParseException
   */
  private void fillFromJsonObject(JSONObject jsonObject, IiifManifestSummary manifestSummary) throws ParseException {

    Object label = jsonObject.get("label");
    HashMap<Locale, String> localizedLabels = getLocalizedStrings(label);
    manifestSummary.setLabels(localizedLabels);

    Object description = jsonObject.get("description");
    HashMap<Locale, String> localizedDescriptions = getLocalizedStrings(description);
    manifestSummary.setDescriptions(localizedDescriptions);

    Object attribution = jsonObject.get("attribution");
    HashMap<Locale, String> localizedAttributions = getLocalizedStrings(attribution);
    manifestSummary.setAttributions(localizedAttributions);

    Thumbnail thumbnail = getThumbnail(jsonObject);
    manifestSummary.setThumbnail(thumbnail);

    String logoUrl = null;
    Object logo = jsonObject.get("logo");
    if (logo instanceof JSONObject) {
      logoUrl = (String) ((JSONObject) logo).get("@id");
    }
    if (logo instanceof String) {
      logoUrl = (String) logo;
    }
    if (!StringUtils.isEmpty(logoUrl)) {
      manifestSummary.setLogoUrl(logoUrl);
    }
  }

  public HashMap<Locale, String> getLocalizedStrings(Object jsonNode) {
    HashMap<Locale, String> result = new HashMap<>();
    if (jsonNode == null) {
      return result;
    }
    if (JSONArray.class.isAssignableFrom(jsonNode.getClass())) {
      JSONArray descriptions = (JSONArray) jsonNode;
      for (Object descr : descriptions) {
        JSONObject descrObj = (JSONObject) descr;
        String value = (String) descrObj.get("@value");
        String language = (String) descrObj.get("@language");
        result.put(new Locale(language), value);
      }
    } else {
      String value = (String) jsonNode;
      result.put(DEFAULT_LOCALE, value);
    }
    return result;
  }

  private Thumbnail getThumbnail(JSONObject manifestObj) {
    JSONObject thumbnailObj;

    // A manifest should have exactly one thumbnail image, and may have more than one.
    thumbnailObj = (JSONObject) manifestObj.get("thumbnail");

    JSONObject firstSequence = null;
    if (thumbnailObj == null) {
      // A sequence may have one or more thumbnails and should have at least one thumbnail if there are multiple sequences in a single manifest.
      JSONArray sequencesArray = (JSONArray) manifestObj.get("sequences");
      if (sequencesArray != null) {
        firstSequence = (JSONObject) sequencesArray.get(0);
        if (firstSequence != null) {
          thumbnailObj = (JSONObject) firstSequence.get("thumbnail");
        }
      }
    }

    JSONArray canvases = null;
    if (thumbnailObj == null && firstSequence != null) {
      // A canvas may have one or more thumbnails and should have at least one thumbnail if there are multiple images or resources that make up the representation.
      canvases = (JSONArray) firstSequence.get("canvases");
      if (canvases != null) {
        Object obj = canvases.stream()
                .map(c -> ((JSONObject) c).get("thumbnail"))
                .filter(t -> t != null)
                .findFirst().orElse(null);
        if (obj != null) {
          if (obj instanceof JSONObject) {
            thumbnailObj = (JSONObject) obj;
          }
          // TODO String = url
        }
      }
    }

    if (thumbnailObj == null && canvases != null) {
      // No thumbnail found, yet. Take the first image of first canvas as "thumbnail".
      JSONObject firstCanvas = ((JSONObject) canvases.get(0));
      if (firstCanvas != null) {
        JSONArray images = (JSONArray) firstCanvas.get("images");
        if (images != null) {
          Object obj = images.stream()
                  .map(i -> ((JSONObject) i).get("resource"))
                  .findFirst().orElse(null);
          if (obj != null) {
            if (obj instanceof JSONObject) {
              thumbnailObj = (JSONObject) obj;
            }
            // TODO String = url
          }
        }
      }
    }

    if (thumbnailObj != null) {
      // thumbnail candidate found
      JSONObject serviceObj = (JSONObject) thumbnailObj.get("service");
      if (serviceObj != null) {
        boolean isV1 = false;

        String profile = null;
        Object profileObj = serviceObj.get("profile");
        if (profileObj == null) {
          profileObj = (String) serviceObj.get("dcterms:conformsTo");
        }
        if (profileObj instanceof JSONArray) {
          profile = (String) ((JSONArray) profileObj).get(0);
        }
        if (profileObj instanceof String) {
          profile = (String) profileObj;
        }
        if (profile != null) {
          isV1 = ImageApiProfile.V1_PROFILES.contains(profile);
        }

        String serviceUrl = (String) serviceObj.get("@id");
        if (serviceUrl.endsWith("/")) {
          serviceUrl = serviceUrl.substring(0, serviceUrl.length() - 1);
        }

        List<Size> sizes = new ArrayList<>();
        JSONArray sizesArray = (JSONArray) serviceObj.get("sizes");
        if (sizesArray != null) {
          for (Object size : sizesArray) {
            JSONObject sizeObj = (JSONObject) size;
            int width = ((Long) sizeObj.get("width")).intValue();
            int height = ((Long) sizeObj.get("height")).intValue();
            sizes.add(new Size(width, height));
          }
        }
        return createThumbnail(sizes, serviceUrl, isV1);
      } else {
        return new Thumbnail(thumbnailObj.get("@id").toString());
      }
    }
    return null;
  }
}
