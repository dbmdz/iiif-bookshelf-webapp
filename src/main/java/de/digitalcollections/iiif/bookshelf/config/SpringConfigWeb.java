package de.digitalcollections.iiif.bookshelf.config;

import de.digitalcollections.commons.springmvc.interceptors.CurrentUrlAsModelAttributeHandlerInterceptor;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import nz.net.ultraq.thymeleaf.LayoutDialect;
import org.ocpsoft.prettytime.PrettyTime;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.yaml.snakeyaml.Yaml;

@Configuration
@EnableAspectJAutoProxy
@EnableSpringDataWebSupport
public class SpringConfigWeb extends WebMvcConfigurerAdapter {

  static final String ENCODING = "UTF-8";

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    return new PropertySourcesPlaceholderConfigurer();
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/favicon.ico").addResourceLocations("classpath:/static/images/favicon.png");
    registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/").setCachePeriod(Integer.MAX_VALUE);
  }

  @Bean
  public LayoutDialect layoutDialect() {
    return new LayoutDialect();
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
    localeChangeInterceptor.setParamName("language");
    registry.addInterceptor(localeChangeInterceptor);

    CurrentUrlAsModelAttributeHandlerInterceptor currentUrlAsModelAttributeHandlerInterceptor = new CurrentUrlAsModelAttributeHandlerInterceptor();
    currentUrlAsModelAttributeHandlerInterceptor.deleteParams("language");
    registry.addInterceptor(currentUrlAsModelAttributeHandlerInterceptor);

    // InterceptorRegistration createAdminUserInterceptorRegistration =
    // registry.addInterceptor(createAdminUserInterceptor());
    // createAdminUserInterceptorRegistration.addPathPatterns("/login");
  }

  @Bean(name = "localeResolver")
  public LocaleResolver sessionLocaleResolver() {
    SessionLocaleResolver localeResolver = new SessionLocaleResolver();
    localeResolver.setDefaultLocale(Locale.GERMAN);
    return localeResolver;
  }

  // @Bean
  // public CreateAdminUserInterceptor createAdminUserInterceptor() {
  // return new CreateAdminUserInterceptor();
  // }
  @Bean
  public PrettyTime prettyTime() {
    return new MyPrettyTime();
  }

  private class MyPrettyTime extends PrettyTime {

    public String format(Date then, Locale locale) {
      PrettyTime prettyTime = new PrettyTime(locale);
      return prettyTime.format(then);
    }
  }

  @Override
  public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
    configurer.enable();
  }

  @Bean(name = "iiifVersions")
  public Map<String, String> iiifVersions() {
    Map<String, String> versions = null;
    Map<String, String> customVersions;
    Yaml yaml = new Yaml();
    try (InputStream in = this.getClass().getResourceAsStream("/iiif-versions.yml")) {
      versions = (Map<String, String>) yaml.load(in);
    } catch (IOException exception) {
      throw new IllegalStateException(exception);
    }
    try (InputStream in = this.getClass().getResourceAsStream("/iiif-versions-custom.yml")) {
      customVersions = (Map<String, String>) yaml.load(in);
      if (customVersions != null) {
        customVersions.forEach(versions::putIfAbsent);
      }
    } catch (IOException exception) {
      throw new IllegalStateException(exception);
    }
    return versions;
  }
}
