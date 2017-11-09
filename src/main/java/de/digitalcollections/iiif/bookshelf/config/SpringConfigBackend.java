package de.digitalcollections.iiif.bookshelf.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import de.digitalcollections.iiif.model.jackson.IiifObjectMapper;
import org.mongeez.MongeezRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
@EnableMongoRepositories(basePackages = {"de.digitalcollections.iiif.bookshelf.backend.api.repository"})
@EnableMongoAuditing
public class SpringConfigBackend {

  private static final Logger LOGGER = LoggerFactory.getLogger(SpringConfigBackend.class);

  @Value("${custom.mongeez.classpathToMongeezXml}")
  private String mongeezClasspathToMongeezXml;

  @Value("${custom.mongeez.dbName}")
  private String mongeezDbName;

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
}
