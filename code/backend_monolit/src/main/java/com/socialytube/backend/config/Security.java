package com.socialytube.backend.config;

import com.socialytube.backend.security.TokenAuthenticator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
public class Security extends WebSecurityConfigurerAdapter {
  @Autowired
  TokenAuthenticator tokenAuthenticator_;

  @Bean
  @Override
  public AuthenticationManager authenticationManagerBean() throws Exception {
    return super.authenticationManagerBean();
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    //@formatter:off
    http
      .httpBasic().disable()
      .csrf().disable()
      .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
      .and()
        .authorizeRequests()
        .antMatchers("/resources/**", "/public/**").permitAll()
        .anyRequest().authenticated()
      .and()
      .apply(new SecurityToken(tokenAuthenticator_))
      .and()
      .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    //@formatter:on
  }

}