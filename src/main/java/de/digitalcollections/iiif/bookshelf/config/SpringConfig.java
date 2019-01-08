package de.digitalcollections.iiif.bookshelf.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan(basePackages = {
  "de.digitalcollections.commons.springboot.actuator",
  "de.digitalcollections.commons.springboot.contributor",
  "de.digitalcollections.commons.springboot.monitoring"
})
@Import({
  SpringConfigBackend.class, SpringConfigBusiness.class,
  SpringConfigSecurity.class,
  SpringConfigWeb.class
})
public class SpringConfig {

}
