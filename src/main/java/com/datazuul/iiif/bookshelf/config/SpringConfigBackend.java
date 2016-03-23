package com.datazuul.iiif.bookshelf.config;

import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 *
 * @author ralf
 */
@Configuration
@ComponentScan(basePackages = {
    "com.datazuul.iiif.bookshelf.backend.repository.impl"
})
@PropertySource(value = {
    "classpath:com/datazuul/iiif/bookshelf/config/SpringConfigBackend-${spring.profiles.active:PROD}.properties"
})
@EnableMongoRepositories(basePackages = {"com.datazuul.iiif.bookshelf.backend.repository"})
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
        return "com.datazuul.iiif.bookshelf.model";
    }
    
    @Bean
    @Override
    public MongoTemplate mongoTemplate() throws Exception {
        return new MongoTemplate(mongo(), getDatabaseName());
    }
}
