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

import java.util.Locale;
import nz.net.ultraq.thymeleaf.LayoutDialect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolver;

@Configuration
@ComponentScan(basePackages = {
  "com.datazuul.iiif.bookshelf.frontend.controller"
})
@EnableAspectJAutoProxy
@EnableWebMvc
@PropertySource(value = {
  "classpath:com/datazuul/iiif/bookshelf/config/SpringConfigWeb-${spring.profiles.active:PROD}.properties"
})
public class SpringConfigWeb extends WebMvcConfigurerAdapter {

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    return new PropertySourcesPlaceholderConfigurer();
  }

  @Value("${cacheTemplates}")
  private boolean cacheTemplates;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/css/**").addResourceLocations("/css/");
    registry.addResourceHandler("/favicon.ico").addResourceLocations("/images/favicon.ico");
    registry.addResourceHandler("/fonts/**").addResourceLocations("/fonts/");
    registry.addResourceHandler("/html/**").addResourceLocations("/html/");
    registry.addResourceHandler("/img/**").addResourceLocations("/img/");
    registry.addResourceHandler("/images/**").addResourceLocations("/images/");
    registry.addResourceHandler("/js/**").addResourceLocations("/js/");
    registry.addResourceHandler("/mirador/**").addResourceLocations("/mirador/");
    registry.addResourceHandler("/vendor/**").addResourceLocations("/vendor/");
  }

  @Bean
  public ClassLoaderTemplateResolver classLoaderTemplateResolver() {
    ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
    templateResolver.setPrefix("/org/mdz/common/frontend/webapp/thymeleaf/templates/");
    templateResolver.setSuffix(".html");
    templateResolver.setCharacterEncoding("UTF-8");
    templateResolver.setTemplateMode("HTML5");
    templateResolver.setCacheable(cacheTemplates);
    templateResolver.setOrder(1);
    return templateResolver;
  }

  @Bean
  public TemplateResolver servletContextTemplateResolver() {
    ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver();
    templateResolver.setPrefix("/WEB-INF/templates/");
    templateResolver.setSuffix(".html");
    templateResolver.setCharacterEncoding("UTF-8");
    templateResolver.setTemplateMode("HTML5");
    templateResolver.setCacheable(cacheTemplates);
    templateResolver.setOrder(2);
    return templateResolver;
  }

  @Bean
  public SpringTemplateEngine templateEngine() {
    SpringTemplateEngine templateEngine = new SpringTemplateEngine();
    templateEngine.addTemplateResolver(classLoaderTemplateResolver());
    templateEngine.addTemplateResolver(servletContextTemplateResolver());
    // Activate Thymeleaf LayoutDialect[1] (for 'layout'-namespace)
    // [1] https://github.com/ultraq/thymeleaf-layout-dialect
    templateEngine.addDialect(new LayoutDialect());
//        templateEngine.addDialect(new SpringSecurityDialect());
//    templateEngine.addDialect(new DataAttributeDialect());
    return templateEngine;
  }

  @Bean
  public ViewResolver viewResolver() {
    ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
    viewResolver.setTemplateEngine(templateEngine());
    viewResolver.setOrder(1);
    viewResolver.setCharacterEncoding("UTF-8");
    return viewResolver;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
    localeChangeInterceptor.setParamName("language");
    registry.addInterceptor(localeChangeInterceptor);

//        InterceptorRegistration createAdminUserInterceptorRegistration = registry.addInterceptor(createAdminUserInterceptor());
//        createAdminUserInterceptorRegistration.addPathPatterns("/login");
  }

  @Bean(name = "localeResolver")
  public LocaleResolver sessionLocaleResolver() {
    SessionLocaleResolver localeResolver = new SessionLocaleResolver();
    localeResolver.setDefaultLocale(Locale.GERMAN);
    return localeResolver;
  }

//    @Bean
//    public CreateAdminUserInterceptor createAdminUserInterceptor() {
//        return new CreateAdminUserInterceptor();
//    }
}
