package de.digitalcollections.iiif.bookshelf.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author ralf
 */
@Configuration
@ComponentScan(basePackages = {
  "com.datazuul.iiif.bookshelf.business.service.impl"
})
public class SpringConfigBusiness {
    
}
