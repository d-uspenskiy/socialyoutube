package com.socialytube.backend.config;

import com.socialytube.backend.security.TokenAuthenticationEntryPoint;
import com.socialytube.backend.security.TokenAuthenticationFilter;
import com.socialytube.backend.security.TokenAuthenticator;

import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class SecurityToken extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {
  private TokenAuthenticator tokenAuthenticator_;

  public SecurityToken(TokenAuthenticator tokenAuthenticator) {
    tokenAuthenticator_ = tokenAuthenticator;
  }

  @Override
  public void configure(HttpSecurity http) throws Exception {
    http.exceptionHandling()
      .authenticationEntryPoint(new TokenAuthenticationEntryPoint())
      .and()
      .addFilterBefore(new TokenAuthenticationFilter(tokenAuthenticator_), 
                       UsernamePasswordAuthenticationFilter.class);
  }
}
