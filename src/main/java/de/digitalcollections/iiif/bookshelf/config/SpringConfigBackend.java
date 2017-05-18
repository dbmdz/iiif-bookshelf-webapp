package de.digitalcollections.iiif.bookshelf.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import de.digitalcollections.iiif.presentation.config.SpringConfigBackendPresentation;
import de.digitalcollections.iiif.presentation.model.impl.jackson.v2.IiifPresentationApiObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.mongeez.MongeezRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@Configuration
@ComponentScan(basePackages = {
  "de.digitalcollections.iiif.bookshelf.backend.api.repository",
  "de.digitalcollections.iiif.bookshelf.backend.impl.repository"
})
@PropertySource(value = {
  "classpath:de/digitalcollections/iiif/bookshelf/config/SpringConfigBackend-${spring.profiles.active:PROD}.properties"
})
@EnableMongoRepositories(basePackages = {"de.digitalcollections.iiif.bookshelf.backend.api.repository"})
@EnableMongoAuditing
@EnableSpringDataWebSupport
@Import(SpringConfigBackendPresentation.class)
public class SpringConfigBackend extends AbstractMongoConfiguration {

  private static final Logger LOGGER = LoggerFactory.getLogger(SpringConfigBackend.class);

  @Value("${mongo.host}")
  private String mongoHost;

  @Value("${mongo.port}")
  private int mongoPort;

  @Value("${mongeez.classpathToMongeezXml}")
  private String mongeezClasspathToMongeezXml;

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    return new PropertySourcesPlaceholderConfigurer();
  }

  @Override
  protected String getDatabaseName() {
    return "iiif-bookshelf";
  }

  /*
   * Factory bean that creates the com.mongodb.MongoClient instance
   */
  @Override
  @Bean
  public MongoClient mongo() throws Exception {
    MongoClient client;
    if (mongoHost.contains(",")) {
      List<ServerAddress> addresses = new ArrayList<>();
      for (String host : mongoHost.split(",")) {
        addresses.add(new ServerAddress(host, mongoPort));
      }
      client = new MongoClient(addresses);
      client.setReadPreference(ReadPreference.secondaryPreferred());
    } else {
      client = new MongoClient(mongoHost, mongoPort);
    }
    client.setWriteConcern(WriteConcern.ACKNOWLEDGED);
    return client;
  }

  @Bean(name = "mongeez")
  public MongeezRunner mongeez() throws Exception {
    MongeezRunner mongeezRunner = new MongeezRunner();
    mongeezRunner.setMongo(mongo());
    mongeezRunner.setExecuteEnabled(true);
    mongeezRunner.setDbName(getDatabaseName());
    mongeezRunner.setFile(new ClassPathResource(mongeezClasspathToMongeezXml));
    return mongeezRunner;
  }

  @Override
  protected String getMappingBasePackage() {
    return "de.digitalcollections.iiif.bookshelf.model";
  }

  @Bean
  @Override
  public MongoTemplate mongoTemplate() throws Exception {
    return new MongoTemplate(mongo(), getDatabaseName());
  }

  @Bean
  public ObjectMapper objectMapper() {
    ObjectMapper objectMapper = new IiifPresentationApiObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
    // define which fields schould be ignored with Filter-classes:
    // objectMapper.addMixIn(User.class, UserJsonFilter.class);
    // objectMapper.addMixIn(GrantedAuthority.class, GrantedAuthorityJsonFilter.class);
    return objectMapper;
  }
}
