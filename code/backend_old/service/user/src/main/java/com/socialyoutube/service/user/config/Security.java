package com.socialyoutube.service.user.config;

import com.socialyoutube.service.user.impl.JwtTokenAuthenticationFilter;
import com.socialyoutube.service.user.impl.JwtTokenProvider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

import reactor.core.publisher.Mono;

@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class Security {

  private final JwtTokenProvider tokenProvider;

  @Autowired
  public Security(JwtTokenProvider provider) {
    tokenProvider = provider;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return NoOpPasswordEncoder.getInstance();
  }

  @Bean
  public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity httpSecurity) {
    return httpSecurity
        .exceptionHandling()
        .authenticationEntryPoint(
            (swe, e) ->
                Mono.fromRunnable(
                    () -> swe.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED)))
        .accessDeniedHandler(
            (swe, e) ->
                Mono.fromRunnable(
                    () -> swe.getResponse().setStatusCode(HttpStatus.FORBIDDEN)))
        .and()
        .csrf().disable()
        .formLogin().disable()
        .httpBasic().disable()
        .logout().disable()
        .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
        .authorizeExchange()
        .pathMatchers("/auth/login", "/auth/renew").permitAll()
        .anyExchange().authenticated()
        .and()
        .addFilterAt(new JwtTokenAuthenticationFilter(tokenProvider), SecurityWebFiltersOrder.AUTHENTICATION)
        .build();
  }
}
