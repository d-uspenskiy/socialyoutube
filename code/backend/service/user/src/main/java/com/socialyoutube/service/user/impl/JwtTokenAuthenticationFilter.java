package com.socialyoutube.service.user.impl;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

public class JwtTokenAuthenticationFilter implements WebFilter {
  private static final String HEADER_PREFIX = "Bearer ";

  private final JwtTokenProvider tokenProvider;

  public JwtTokenAuthenticationFilter(JwtTokenProvider provider) {
    tokenProvider = provider;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    var filter = chain.filter(exchange);
    var token = resolveToken(exchange.getRequest());
    if (StringUtils.hasText(token)) {
      var authentication = tokenProvider.getAuthentication(token);
      return filter.contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
    }
    return filter;
  }

  private String resolveToken(ServerHttpRequest request) {
    var bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    return StringUtils.hasText(bearerToken) && bearerToken.startsWith(HEADER_PREFIX)
        ? bearerToken.substring(7) : null;
  }
}
