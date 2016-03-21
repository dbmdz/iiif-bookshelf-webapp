package com.datazuul.iiif.catalog.portal.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author ralf
 */
@Configuration
@ComponentScan(basePackages = {
  "com.datazuul.iiif.catalog.portal.business.service.impl"
})
public class SpringConfigBusiness {
    
}
