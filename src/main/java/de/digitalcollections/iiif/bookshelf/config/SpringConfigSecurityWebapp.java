package de.digitalcollections.iiif.bookshelf.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Order(2)
public class SpringConfigSecurityWebapp extends WebSecurityConfigurerAdapter {

  @Value("${custom.app.security.enabled}")
  private boolean isAuthenticationEnabled;

  @Value("${custom.app.security.username}")
  private String username;

  @Value("${custom.app.security.password}")
  private String password;

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    if (isAuthenticationEnabled) {
      auth.inMemoryAuthentication()
          .passwordEncoder(passwordEncoderDummy())
          .withUser(User.withUsername(username).password(password).roles("USER"));
    }
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests()
        .requestMatchers(PathRequest.toStaticResources().atCommonLocations())
        .permitAll();
    http.headers().frameOptions().disable(); // to make universalviewer work
    /* Refused to display 'http://localhost:8080/webjars/universalviewer/2.0.2/dist/uv-2.0.2/app.html?isHomeDomain=true&isOnlyInstance=true&manifestUri=https%3A%2F%2Fapi.digitale-sammlungen.de%2Fiiif%2Fpresentation%2Fv2%2Fbsb00010484_00505_u001%2Fmanifest&embedScriptUri=http://localhost:8080/webjars/universalviewer/2.0.2/dist/uv-2.0.2/lib/embed.js&embedDomain=localhost&domain=localhost&isLightbox=false&locale=en-GB&xdm_e=http%3A%2F%2Flocalhost%3A8080%2Fuv%2F1FC1F766&xdm_c=default127&xdm_p=4' in a frame because it set 'X-Frame-Options' to 'deny'. */
    if (!isAuthenticationEnabled) {
      http.authorizeRequests().antMatchers("/add*", "/api/**").permitAll().and().csrf().disable();
      return;
    }

    http.authorizeRequests()
        .antMatchers("/add*")
        .authenticated()
        .and()
        .formLogin()
        .loginPage("/login"); // enable form based log in
    http.authorizeRequests()
        .antMatchers("/api/**")
        .authenticated()
        .and()
        .httpBasic()
        .and()
        .csrf()
        .disable(); // enable basic auth for api
  }

  private PasswordEncoder passwordEncoderDummy() {
    return new PasswordEncoder() {
      @Override
      public String encode(CharSequence rawPassword) {
        return rawPassword.toString();
      }

      @Override
      public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return rawPassword.toString().equals(encodedPassword);
      }
    };
  }
}
