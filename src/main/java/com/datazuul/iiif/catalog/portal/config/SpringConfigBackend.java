package com.datazuul.iiif.catalog.portal.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 *
 * @author ralf
 */
@Configuration
@ComponentScan(basePackages = {
    "com.datazuul.iiif.catalog.portal.backend.repository.impl"
})
@PropertySource(value = {
    "classpath:com/datazuul/iiif/catalog/portal/config/SpringConfigBackend-${spring.profiles.active:PROD}.properties"
})
public class SpringConfigBackend {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringConfigBackend.class);

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
