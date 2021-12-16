package com.socialyoutube.service.user.impl;

import com.socialyoutube.service.rapi.Data.UserInfo;
import com.socialyoutube.service.user.impl.UserServiceExceptions.BadCredentials;
import com.socialyoutube.service.user.repo.UserRepo;

import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import reactor.core.publisher.Mono;

import java.util.Collections;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

public class UserService implements UserServiceLocalApi, AutoCloseable {
  private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

  private final JwtTokenProvider tokenProvider;
  private final GoogleIdTokenVerifier verifier;
  private final UserRepo userRepo;

  public UserService(JwtTokenProvider provider, String clientId, UserRepo repo) {
    tokenProvider = provider;
    verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
        .setAudience(Collections.singletonList(clientId))
        .build();
    userRepo = repo;
  }

  @Override
  public Mono<String> login(String googleToken) {
    LOG.debug("login with google token '{}'", googleToken);
    try {
      var idToken = verifier.verify(googleToken);
      var payload = idToken.getPayload();
      var email = payload.getEmail();
      var name = payload.get("name");
      LOG.info("login as {} {}", email, name);
      return userRepo.forceGet(email)
          .map(user -> tokenProvider.createToken(
              new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities())));
    } catch (Exception e) {
      return Mono.error(new BadCredentials("Bad google token", e));
    }
  }

  @Override
  public Mono<Void> logoutAll() {
    return Mono.empty();
  }

  @Override
  public Mono<String> renewToken(String token) {
    LOG.debug("renewToken '{}'", token);
    try {
      var email = tokenProvider.getAuthentication(token).getName();
      return userRepo.findByEmail(email)
          .switchIfEmpty(Mono.error(() -> {
              LOG.error("user not found '{}'", email);
              return new BadCredentials("user not found");
          }))
          .map(user -> tokenProvider.createToken(
              new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities())));
    } catch (Exception e) {
      return Mono.error(new BadCredentials("bad old token", e));
    }
  }

  @Override
  public UserInfo getUserInfo(String token) {
    throw new NotImplementedException();
  }

  @Override
  public void close(){
    LOG.info("close");
  }
}
