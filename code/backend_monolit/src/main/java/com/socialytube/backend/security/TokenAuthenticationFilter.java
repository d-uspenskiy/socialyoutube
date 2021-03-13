package com.socialytube.backend.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

public class TokenAuthenticationFilter extends GenericFilterBean {
  private TokenAuthenticator tokenAuthenticator_;

  public TokenAuthenticationFilter(TokenAuthenticator tokenAuthenticator) {
    tokenAuthenticator_ = tokenAuthenticator;
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain) throws IOException, ServletException {
    Authentication auth = tokenAuthenticator_.authenticate((HttpServletRequest) req);
    if (auth != null) {
      SecurityContextHolder.getContext().setAuthentication(auth);
    }
    filterChain.doFilter(req, res);
  }
}