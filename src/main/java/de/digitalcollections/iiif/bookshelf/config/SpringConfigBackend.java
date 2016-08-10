package de.digitalcollections.iiif.bookshelf.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import de.digitalcollections.iiif.presentation.config.SpringConfigBackendPresentation;
import de.digitalcollections.iiif.presentation.model.impl.jackson.v2_0_0.IiifPresentationApiObjectMapper;
import org.mongeez.MongeezRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
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
    "de.digitalcollections.iiif.bookshelf.backend.repository.impl"
})
@PropertySource(value = {
    "classpath:de/digitalcollections/iiif/bookshelf/config/SpringConfigBackend-${spring.profiles.active:PROD}.properties"
})
@EnableMongoRepositories(basePackages = {"de.digitalcollections.iiif.bookshelf.backend.repository"})
@EnableMongoAuditing
@EnableSpringDataWebSupport
@Import(SpringConfigBackendPresentation.class)
public class SpringConfigBackend extends AbstractMongoConfiguration {

  private static final Logger LOGGER = LoggerFactory.getLogger(SpringConfigBackend.class);

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    return new PropertySourcesPlaceholderConfigurer();
  }

  @Override
  protected String getDatabaseName() {
    return "iiif-bookshelf";
  }

  /*
   * Factory bean that creates the com.mongodb.Mongo instance
   */
  @Override
  @Bean
  public MongoClient mongo() throws Exception {
    MongoClient client = new MongoClient("localhost");
    client.setWriteConcern(WriteConcern.SAFE);
    return client;
  }

  @Override
  protected String getMappingBasePackage() {
    return "de.digitalcollections.iiif.bookshelf.model";
  }

  @Bean
  @Override
  @DependsOn(value = "mongeezRunner")
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

  /*
   * see https://github.com/mongeez/mongeez/wiki/How-to-use-mongeez
   * 
   * done migration: [2016-04-05 16:46:55,826 INFO ] [...] ChangeSetExecutor (main ) > ChangeSet already executed:
   * ChangeSet-1_1
   * 
   * Mongeez uses a separate MongoDB collection to record previously run scripts: db.mongeez.find().pretty()
   */
  @Bean
  public MongeezRunner mongeezRunner() throws Exception {
    MongeezRunner mongeezRunner = new MongeezRunner();
    mongeezRunner.setMongo(mongo());
    mongeezRunner.setExecuteEnabled(true);
    mongeezRunner.setDbName(getDatabaseName());
    mongeezRunner.setFile(new ClassPathResource("/de/digitalcollections/iiif/bookshelf/mongeez/mongeez.xml"));
    return mongeezRunner;
  }
}
