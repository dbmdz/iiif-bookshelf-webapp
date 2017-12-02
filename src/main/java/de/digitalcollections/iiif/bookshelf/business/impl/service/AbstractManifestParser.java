package de.digitalcollections.iiif.bookshelf.business.impl.service;

import de.digitalcollections.iiif.bookshelf.model.IiifManifestSummary;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

public abstract class AbstractManifestParser {
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
