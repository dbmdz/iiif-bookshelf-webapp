package de.digitalcollections.iiif.bookshelf.config;

import de.digitalcollections.commons.springmvc.config.SpringConfigCommonsMvc;
import de.digitalcollections.commons.springmvc.interceptors.CurrentUrlAsModelAttributeHandlerInterceptor;
import java.util.Date;
import java.util.Locale;
import javax.servlet.ServletContext;
import nz.net.ultraq.thymeleaf.LayoutDialect;
import org.ocpsoft.prettytime.PrettyTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
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

@Configuration
@ComponentScan(basePackages = {
    "de.digitalcollections.iiif.bookshelf.frontend.controller",
    "de.digitalcollections.commons.springmvc.controller"
})
@EnableAspectJAutoProxy
@EnableWebMvc
@PropertySource(value = {
    "classpath:de/digitalcollections/iiif/bookshelf/config/SpringConfigWeb-${spring.profiles.active:PROD}.properties"
})
@Import(SpringConfigCommonsMvc.class)
public class SpringConfigWeb extends WebMvcConfigurerAdapter {

  @Value("${cacheTemplates}")
  private boolean cacheTemplates;

  @Autowired
  @Qualifier("CommonsClasspathThymeleafResolver")
  private ClassLoaderTemplateResolver commonsClasspathThymeleafResolver;

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    return new PropertySourcesPlaceholderConfigurer();
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/css/**").addResourceLocations("/css/");
    registry.addResourceHandler("/favicon.ico").addResourceLocations("/images/favicon.ico");
    registry.addResourceHandler("/fonts/**").addResourceLocations("/fonts/");
    registry.addResourceHandler("/html/**").addResourceLocations("/html/");
    registry.addResourceHandler("/img/**").addResourceLocations("/img/");
    registry.addResourceHandler("/images/**").addResourceLocations("/images/");
    registry.addResourceHandler("/js/**").addResourceLocations("/js/");
    registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
  }

  @Bean
  public ClassLoaderTemplateResolver classLoaderTemplateResolver() {
    ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
    templateResolver.setPrefix("/de/digitalcollections/commons/springmvc/thymeleaf/templates/");
    templateResolver.setSuffix(".html");
    templateResolver.setCharacterEncoding("UTF-8");
    templateResolver.setTemplateMode("HTML5");
    templateResolver.setCacheable(cacheTemplates);
    templateResolver.setOrder(1);
    return templateResolver;
  }

  @Bean
  public ServletContextTemplateResolver servletContextTemplateResolver(ServletContext context) {
    ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(context);
    templateResolver.setPrefix("/WEB-INF/templates/");
    templateResolver.setSuffix(".html");
    templateResolver.setCharacterEncoding("UTF-8");
    templateResolver.setTemplateMode("HTML5");
    templateResolver.setCacheable(cacheTemplates);
    templateResolver.setOrder(2);
    return templateResolver;
  }

  @Bean
  public SpringTemplateEngine templateEngine(ServletContextTemplateResolver servletContextTemplateResolver) {
    SpringTemplateEngine templateEngine = new SpringTemplateEngine();
    templateEngine.addTemplateResolver(classLoaderTemplateResolver());
    templateEngine.addTemplateResolver(servletContextTemplateResolver);
    templateEngine.addTemplateResolver(commonsClasspathThymeleafResolver);
    // Activate Thymeleaf LayoutDialect[1] (for 'layout'-namespace)
    // [1] https://github.com/ultraq/thymeleaf-layout-dialect
    templateEngine.addDialect(new LayoutDialect());
    // templateEngine.addDialect(new SpringSecurityDialect());
    // templateEngine.addDialect(new DataAttributeDialect());
    return templateEngine;
  }

  @Bean
  public ViewResolver viewResolver(SpringTemplateEngine templateEngine) {
    ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
    viewResolver.setTemplateEngine(templateEngine);
    viewResolver.setOrder(1);
    viewResolver.setCharacterEncoding("UTF-8");
    return viewResolver;
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
}
