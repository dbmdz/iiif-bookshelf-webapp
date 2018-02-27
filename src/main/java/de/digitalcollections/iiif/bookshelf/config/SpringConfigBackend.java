package de.digitalcollections.iiif.bookshelf.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import de.digitalcollections.iiif.model.jackson.IiifObjectMapper;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
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
@ComponentScan(basePackages = {
  "de.digitalcollections.iiif.bookshelf.backend.api.repository",
  "de.digitalcollections.iiif.bookshelf.backend.impl.repository"
})
@EnableAutoConfiguration(exclude = {
  SolrAutoConfiguration.class
})
@EnableMongoRepositories(basePackages = {"de.digitalcollections.iiif.bookshelf.backend.api.repository"})
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
  private String solrCollection;

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
  public SolrClient bookshelfCollection() {
    return getSolrServerForCollection(solrCollection);
  }

  private SolrClient getSolrServerForCollection(String collection) {
    SolrClient client = null;

    try {
      client = new HttpSolrClient.Builder(solrServerAddress + "/" + collection).build();
      LOGGER.info("State of solr access to " + solrServerAddress + "/" + collection + ": " + client.ping().getStatus());
    } catch (Exception e) {
      LOGGER.error("Cannot connect to " + solrServerAddress + ": " + e, e);
    }

    return client;
  }

}
