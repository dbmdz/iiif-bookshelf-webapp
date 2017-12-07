package de.digitalcollections.iiif.bookshelf.business.impl.service;

import de.digitalcollections.iiif.bookshelf.model.IiifManifestSummary;
import de.digitalcollections.iiif.bookshelf.model.Thumbnail;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class GraciousManifestParser extends AbstractManifestParser {
  private static final Logger LOGGER = LoggerFactory.getLogger(GraciousManifestParser.class);
  
  @Value("${custom.summary.thumbnail.width}")
  private int thumbnailWidth;
  
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

    String logoUrl = (String) jsonObject.get("logo");
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
    // try to get thumbnail of manifest itself
    JSONObject thumbnailObj = (JSONObject) manifestObj.get("thumbnail");
    if (thumbnailObj != null) {
      JSONObject serviceObj = (JSONObject) thumbnailObj.get("service");
      if (serviceObj != null) {
        String context = (String) serviceObj.get("@context");
        String id = (String) serviceObj.get("@id");
        return new Thumbnail(context, id);
      } else {
        String url = (String) thumbnailObj.get("@id");
        return new Thumbnail(url);
      }
    }

    // try to get thumbnail of first canvas
    try {
      // manifest.getSequences().get(0).getCanvases().get(0).getImages().get(0).getResource().getService().getId();
      JSONArray sequencesArray = (JSONArray) manifestObj.get("sequences");
      JSONObject firstSequence = (JSONObject) sequencesArray.get(0);

      JSONArray canvasesArray = (JSONArray) firstSequence.get("canvases");
      JSONObject firstCanvas = (JSONObject) canvasesArray.get(0);

      Object obj = firstCanvas.get("thumbnail");
      if (obj instanceof JSONObject) {
        JSONObject firstCanvasThumbnail = (JSONObject) firstCanvas.get("thumbnail");
        if (firstCanvasThumbnail != null) {
          String url = (String) firstCanvasThumbnail.get("@id");
          return new Thumbnail(url);
        }
      } else if (obj instanceof String) {
        String url = (String) obj;
        return new Thumbnail(url);
      }

      JSONArray imagesArray = (JSONArray) firstCanvas.get("images");
      JSONObject firstImage = (JSONObject) imagesArray.get(0);

      JSONObject resourceObj = (JSONObject) firstImage.get("resource");
      JSONObject serviceObj = (JSONObject) resourceObj.get("service");
      String context = (String) serviceObj.get("@context");
      String id = (String) serviceObj.get("@id");
      return new Thumbnail(context, id);
    } catch (Exception ex) {
    }

    return null;
  }
}
