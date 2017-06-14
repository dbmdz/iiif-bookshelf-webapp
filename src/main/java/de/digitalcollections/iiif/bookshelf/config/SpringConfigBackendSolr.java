package de.digitalcollections.iiif.bookshelf.config;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = {
    "classpath:de/digitalcollections/iiif/bookshelf/config/SpringConfigBackend-${spring.profiles.active:PROD}.properties"
})
public class SpringConfigBackendSolr {

  @Value("${solr.protocol}")
  private String solrProtocol;

  @Value("${solr.host}")
  private String solrHost;

  @Value("${solr.port}")
  private String solrPort;

  @Value("${solr.path}")
  private String solrPath;

  @Bean
  public SolrClient solr() {
    String solrUrl = solrProtocol.concat("://" + solrHost).concat(":" + solrPort).concat(solrPath);
    return new HttpSolrClient(solrUrl);
  }

}
