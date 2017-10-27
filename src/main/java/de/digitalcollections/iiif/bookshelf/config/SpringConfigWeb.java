package de.digitalcollections.iiif.bookshelf.config;

import de.digitalcollections.commons.springmvc.controller.ErrorController;
import de.digitalcollections.commons.springmvc.interceptors.CurrentUrlAsModelAttributeHandlerInterceptor;
import java.util.Date;
import java.util.Locale;
import nz.net.ultraq.thymeleaf.LayoutDialect;
import org.ocpsoft.prettytime.PrettyTime;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

@Configuration
@ComponentScan(basePackages = {
  //  "de.digitalcollections.cudami.client.webapp.aop",
  //  "de.digitalcollections.cudami.client.webapp.controller",
  //  "de.digitalcollections.cudami.client.webapp.propertyeditor",
  "de.digitalcollections.commons.springmvc.controller"}, excludeFilters = {
  @ComponentScan.Filter(value = ErrorController.class, type = FilterType.ASSIGNABLE_TYPE)}
)
@EnableAspectJAutoProxy
//@EnableWebMvc
@EnableSpringDataWebSupport
//@PropertySource(value = {
//  "classpath:de/digitalcollections/iiif/bookshelf/config/SpringConfigWeb-${spring.profiles.active:PROD}.properties"
//})
//@Import(SpringConfigCommonsMvc.class)
public class SpringConfigWeb extends WebMvcConfigurerAdapter {

  static final String ENCODING = "UTF-8";

//  @Value("${cacheTemplates}")
//  private boolean cacheTemplates;
//  @Autowired
//  @Qualifier("CommonsClasspathThymeleafResolver")
//  private ClassLoaderTemplateResolver commonsClasspathThymeleafResolver;
  @Bean
  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    return new PropertySourcesPlaceholderConfigurer();
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/favicon.ico").addResourceLocations("classpath:/static/images/favicon.png");
    registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/").setCachePeriod(Integer.MAX_VALUE);

//    registry.addResourceHandler("/css/**").addResourceLocations("/css/");
//    registry.addResourceHandler("/favicon.ico").addResourceLocations("/images/favicon.ico");
//    registry.addResourceHandler("/fonts/**").addResourceLocations("/fonts/");
//    registry.addResourceHandler("/html/**").addResourceLocations("/html/");
//    registry.addResourceHandler("/img/**").addResourceLocations("/img/");
//    registry.addResourceHandler("/images/**").addResourceLocations("/images/").setCachePeriod(Integer.MAX_VALUE);
//    registry.addResourceHandler("/js/**").addResourceLocations("/js/").setCachePeriod(Integer.MAX_VALUE);
//    registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/").setCachePeriod(Integer.MAX_VALUE);
  }

//  @Bean
//  public TemplateResolver servletContextTemplateResolver(ApplicationContext context) {
//    ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver();
//    templateResolver.setPrefix("/WEB-INF/templates/");
//    templateResolver.setSuffix(".html");
//    templateResolver.setCharacterEncoding("UTF-8");
//    templateResolver.setTemplateMode("HTML5");
//    templateResolver.setCacheable(cacheTemplates);
//    return templateResolver;
//  }
//
//  @Bean
//  public SpringTemplateEngine templateEngine(ServletContextTemplateResolver servletContextTemplateResolver) {
//    SpringTemplateEngine templateEngine = new SpringTemplateEngine();
//    commonsClasspathThymeleafResolver.setOrder(1);
//    servletContextTemplateResolver.setOrder(2);
//    templateEngine.addTemplateResolver(commonsClasspathThymeleafResolver);
//    templateEngine.addTemplateResolver(servletContextTemplateResolver);
//    // Activate Thymeleaf LayoutDialect[1] (for 'layout'-namespace)
//    // [1] https://github.com/ultraq/thymeleaf-layout-dialect
//    templateEngine.addDialect(new LayoutDialect());
//    // templateEngine.addDialect(new SpringSecurityDialect());
//    // templateEngine.addDialect(new DataAttributeDialect());
//    return templateEngine;
//  }
//  @Bean
//  public DataAttributeDialect dataAttributeDialect() {
//    return new DataAttributeDialect();
//  }
  @Bean
  public LayoutDialect layoutDialect() {
    return new LayoutDialect();
  }

//  @Bean
//  public SpringDataDialect springDataDialect() {
//    return new SpringDataDialect();
//  }
//
//  @Bean
//  public SpringSecurityDialect springSecurityDialect() {
//    return new SpringSecurityDialect();
//  }
//  @Bean
//  public ViewResolver viewResolver(SpringTemplateEngine templateEngine) {
//    ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
//    viewResolver.setTemplateEngine(templateEngine);
//    viewResolver.setOrder(1);
//    viewResolver.setCharacterEncoding("UTF-8");
//    return viewResolver;
//  }
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
}
