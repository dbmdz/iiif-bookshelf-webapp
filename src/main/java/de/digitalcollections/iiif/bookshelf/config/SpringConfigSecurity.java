package de.digitalcollections.iiif.bookshelf.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * Contains Spring Security related configuration.
 */
@Configuration
@EnableWebSecurity
//@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SpringConfigSecurity extends WebSecurityConfigurerAdapter {

  @Value("${custom.app.security.enabled}")
  private boolean authentication;

  @Value("${custom.app.security.username}")
  private String username;

  @Value("${custom.app.security.password}")
  private String password;

  @Autowired
  public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
    if (authentication) {
      auth.inMemoryAuthentication().withUser(username).password(password).roles("USER");
    }
  }

  @Configuration
  @Order(1)
  public static class ApiWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

    @Value("${custom.app.security.enabled}")
    private boolean authentication;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
      if (!authentication) {
        return;
      }
      http
              .antMatcher("/api/add").authorizeRequests()
              .antMatchers("/api/add").authenticated()
              .and()
              .httpBasic()
              .and()
              .csrf().disable();
    }
  }

  @Configuration
  @Order(2)
  public static class FormLoginWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

    @Value("${custom.app.security.enabled}")
    private boolean authentication;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
      if (!authentication) {
        return;
      }
      http.antMatcher("/add").authorizeRequests().antMatchers("/add").authenticated()
              .and()
              .formLogin().loginPage("/login").permitAll().and().httpBasic();
    }
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests().anyRequest().permitAll()
            .and().formLogin().loginPage("/login").and().httpBasic()
            .and().headers().frameOptions().disable(); // to make universalviewer work
    /* Refused to display 'http://localhost:8080/webjars/universalviewer/2.0.2/dist/uv-2.0.2/app.html?isHomeDomain=true&isOnlyInstance=true&manifestUri=https%3A%2F%2Fapi.digitale-sammlungen.de%2Fiiif%2Fpresentation%2Fv2%2Fbsb00010484_00505_u001%2Fmanifest&embedScriptUri=http://localhost:8080/webjars/universalviewer/2.0.2/dist/uv-2.0.2/lib/embed.js&embedDomain=localhost&domain=localhost&isLightbox=false&locale=en-GB&xdm_e=http%3A%2F%2Flocalhost%3A8080%2Fuv%2F1FC1F766&xdm_c=default127&xdm_p=4' in a frame because it set 'X-Frame-Options' to 'deny'.
     */

    // and this line is needed so that http basic authentication with configured username and password (in application.yml) is working again
//    http.authorizeRequests().antMatchers("/monitoring").authenticated().and().httpBasic();
  }
}
