package de.digitalcollections.iiif.bookshelf.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"de.digitalcollections.iiif.bookshelf.business.impl.service"})
public class SpringConfigBusiness {}
