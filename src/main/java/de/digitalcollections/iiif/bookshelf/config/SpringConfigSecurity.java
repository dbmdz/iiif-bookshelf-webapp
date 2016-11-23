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

  @Value("${authentication}")
  private boolean authentication;

  @Value("${username}")
  private String username;

  @Value("${password}")
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
    @Value("${authentication}")
    private boolean authentication;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
      if (!authentication) return;
      http
          .antMatcher("/api/**").authorizeRequests()
            .anyRequest().authenticated()
            .and()
            .httpBasic()
            .and()
            .csrf().disable();
    }
  }

  @Configuration
  @Order(2)
  public static class FormLoginWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {
    @Value("${authentication}")
    private boolean authentication;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
      if (!authentication) return;
      http
          .antMatcher("/add").authorizeRequests()
          .anyRequest().authenticated()
          .and()
          .formLogin();
    }
  }
}
