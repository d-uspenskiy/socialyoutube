package com.socialyoutube.service.user.impl;

import com.socialyoutube.service.rapi.UserServiceRemoteApi;

import reactor.core.publisher.Mono;

public interface UserServiceLocalApi extends UserServiceRemoteApi {
  Mono<String> login(String googleToken);
  Mono<String> renewToken(String token);
  Mono<Void> logoutAll();
}
