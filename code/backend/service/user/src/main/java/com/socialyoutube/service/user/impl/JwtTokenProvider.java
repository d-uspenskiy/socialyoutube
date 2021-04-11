package com.socialyoutube.service.user.impl;

import java.util.Base64;
import java.util.Date;
import java.util.stream.Collectors;

import com.socialyoutube.service.user.domain.User;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {
  private static final String AUTHORITIES_KEY = "roles";

  @Value("${jwt.expiration}")
  private Long expirationSeconds;

  @Value("${jwt.secret}")
  private String secret;

  public String createToken(Authentication authentication) {
    var username = authentication.getName();
    var claims = Jwts.claims().setSubject(username);
    claims.put(
        AUTHORITIES_KEY, 
        authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(",")));
    var now = new Date();
    return Jwts.builder()
        .setClaims(claims)
        .setIssuedAt(now)
        .setExpiration(new Date(now.getTime() + expirationSeconds * 1000))
        .signWith(Keys.hmacShaKeyFor(secret.getBytes()))
        .compact();
  }

  public Authentication getAuthentication(String token) {
    var claims = getClaims(token);
    var authorities = AuthorityUtils.commaSeparatedStringToAuthorityList(claims.get(AUTHORITIES_KEY).toString());
    var principal = new User(claims.getSubject());
    return new UsernamePasswordAuthenticationToken(principal, token, authorities);
  }

  private Claims getClaims(String token) {
    var key = Base64.getEncoder().encodeToString(secret.getBytes());
    return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
  }
}