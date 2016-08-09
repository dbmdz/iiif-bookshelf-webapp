/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.digitalcollections.iiif.bookshelf.config;

import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.env.Environment;

@Configuration
@Import({SpringConfigWeb.class, SpringConfigBusiness.class, SpringConfigBackend.class})
public class SpringConfig implements EnvironmentAware {

  private static final Logger LOGGER = LoggerFactory.getLogger(SpringConfig.class);

  /**
   * Create a resource bundle for your messages ("messages.properties").<br/>
   * This file goes in src/main/resources because you want it to appear at the root of the classpath
   * on deployment.
   *
   * @return message source
   */
  @Bean(name = "messageSource")
  public MessageSource configureMessageSource() {
    ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
    messageSource.setBasename("classpath:messages");
    messageSource.setCacheSeconds(5);
    messageSource.setDefaultEncoding("UTF-8");
    return messageSource;
  }

  @Override
  public void setEnvironment(Environment environment) {
    String[] activeProfiles = environment.getActiveProfiles();
    String toString = Arrays.toString(activeProfiles);
    LOGGER.info("##### Active Profiles: " + toString);
  }
}
