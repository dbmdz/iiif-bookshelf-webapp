package de.digitalcollections.iiif.bookshelf.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
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

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    if (authentication) {
      http.authorizeRequests()
              //            .antMatchers("/css/**", "/fonts", "/images", "/js/**", "/vendor/**").permitAll()
              //            .anyRequest().authenticated()
              .antMatchers("/add").authenticated()
              .and()
              .formLogin()
              .and()
              .httpBasic();
    }
  }
}
