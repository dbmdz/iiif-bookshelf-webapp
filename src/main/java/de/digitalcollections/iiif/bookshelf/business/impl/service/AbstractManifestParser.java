package de.digitalcollections.iiif.bookshelf.business.impl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.iiif.bookshelf.model.IiifManifestSummary;
import de.digitalcollections.iiif.bookshelf.model.Thumbnail;
import de.digitalcollections.iiif.model.Service;
import de.digitalcollections.iiif.model.image.ImageService;
import de.digitalcollections.iiif.model.image.Size;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

public abstract class AbstractManifestParser {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractManifestParser.class);

  @Autowired
  private ApplicationContext appContext;

  @Autowired
  private ObjectMapper objectMapper;

  @Value("${custom.summary.thumbnail.width}")
  private int thumbnailWidth;

  protected Thumbnail createThumbnail(List<Size> sizes, String serviceUrl, boolean isV1) {
    if (sizes == null || sizes.isEmpty()) {
      try {
        Resource springResource = appContext.getResource(serviceUrl + "/info.json");
        // get info.json for available sizes
        ImageService imageServiceExternal = (ImageService) objectMapper.readValue(springResource.getInputStream(), Service.class);
        sizes = imageServiceExternal.getSizes();
      } catch (IOException ex) {
        LOGGER.debug("Can not read info.json", ex);
      }
    }
    int bestWidth = thumbnailWidth;
    if (sizes != null) {
      bestWidth = sizes.stream()
              .filter(s -> s.getWidth() >= thumbnailWidth)
              .sorted(Comparator.comparing(s -> Math.abs(thumbnailWidth - s.getWidth())))
              .map(Size::getWidth).findFirst().orElse(thumbnailWidth);
    }
    // TODO add check, if minimal width is met (make minWidth configurable), otherwise get second best width...
    String thumbnailUrl = String.format("%s/full/%d,/0/", serviceUrl, bestWidth);
    if (isV1) {
      thumbnailUrl += "native.jpg";
    } else {
      thumbnailUrl += "default.jpg";
    }
    if (thumbnailUrl.startsWith("http")) {
      // try to get thumbnail url 200 response for head request
      HttpClient httpClient = HttpClientBuilder.create().build();
      HttpHead httpHead = new HttpHead(thumbnailUrl);
      try {
        HttpResponse response = httpClient.execute(httpHead);
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
          return null; // no valid thumbnail available
        }
      } catch (IOException ex) {
        return null; // no valid thumbnail available
      }
    }
    LOGGER.debug("Thumbnail url = '{}'", thumbnailUrl);
    final Thumbnail thumbnail = new Thumbnail(thumbnailUrl);
    if (serviceUrl != null) {
      thumbnail.setIiifImageServiceUri(serviceUrl);
    }
    return thumbnail;
  }

  protected InputStream getContentInputStream(String uri) throws URISyntaxException, UnsupportedOperationException, IOException {
    return getContentInputStream(new URI(uri));
  }

  protected InputStream getContentInputStream(URI uri) throws UnsupportedOperationException, IOException {
    HttpClient httpClient = HttpClientBuilder.create().build();
    HttpGet httpGet = new HttpGet(uri);
    HttpResponse response = httpClient.execute(httpGet);
    final InputStream content = response.getEntity().getContent();
    return content;
  }

  public abstract void fillSummary(IiifManifestSummary manifestSummary) throws IOException, URISyntaxException;
}
