package de.digitalcollections.iiif.bookshelf.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import de.digitalcollections.iiif.model.jackson.IiifObjectMapper;
import java.io.IOException;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.SolrPing;
import org.apache.solr.client.solrj.response.SolrPingResponse;
import org.mongeez.MongeezRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.solr.SolrAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@ComponentScan(
    basePackages = {
      "de.digitalcollections.iiif.bookshelf.backend.api.repository",
      "de.digitalcollections.iiif.bookshelf.backend.impl.repository"
    })
@EnableAutoConfiguration(exclude = {SolrAutoConfiguration.class})
@EnableMongoRepositories(
    basePackages = {"de.digitalcollections.iiif.bookshelf.backend.api.repository"})
@EnableMongoAuditing
public class SpringConfigBackend {

  private static final Logger LOGGER = LoggerFactory.getLogger(SpringConfigBackend.class);

  @Value("${custom.mongeez.classpathToMongeezXml}")
  private String mongeezClasspathToMongeezXml;

  @Value("${custom.mongeez.dbName}")
  private String mongeezDbName;

  @Value("${spring.data.solr.host}")
  private String solrServerAddress;

  @Value("${custom.solr.collection}")
  private String collection;

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    return new PropertySourcesPlaceholderConfigurer();
  }

  @Bean(name = "mongeez")
  public MongeezRunner mongeez(MongoClient mongo) throws Exception {
    MongeezRunner mongeezRunner = new MongeezRunner();
    mongeezRunner.setMongo(mongo);
    mongeezRunner.setExecuteEnabled(true);
    mongeezRunner.setDbName(mongeezDbName);
    mongeezRunner.setFile(new ClassPathResource(mongeezClasspathToMongeezXml));
    return mongeezRunner;
  }

  @Bean
  @Primary
  public ObjectMapper objectMapper() {
    return new IiifObjectMapper();
  }

  @Bean
  public SolrClient solrClient() {
    SolrClient client = new HttpSolrClient.Builder(solrServerAddress).build();

    // check if solr collection is correctly configured and responding
    try {
      SolrPing ping = new SolrPing();
      SolrPingResponse response = ping.process(client, collection);
      LOGGER.info(
          "State of solr ping request to "
              + solrServerAddress
              + "/"
              + collection
              + ": "
              + response.getStatus());
    } catch (IOException | SolrServerException e) {
      LOGGER.error("Cannot connect to " + solrServerAddress + ": " + e, e);
    }

    return client;
  }
}
