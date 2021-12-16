package com.socialyoutube.service.user.impl;

import java.util.function.Function;

import com.fasterxml.jackson.databind.JsonNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;

import reactor.core.publisher.Mono;

public class UserServiceRestAccessor {
  private static final Logger LOG = LoggerFactory.getLogger(UserServiceRestAccessor.class);

  private final UserServiceLocalApi userService;

  public UserServiceRestAccessor(UserServiceLocalApi api) {
    userService = api;
  }

  public Mono<ServerResponse> self(ServerRequest request) {
    return Mono.empty();
  }

  public Mono<ServerResponse> login(ServerRequest request) {
    return exchangeToken(request, userService::login);
  }

  public Mono<ServerResponse> logoutAll(ServerRequest request) {
    LOG.debug("logoutAll called");
    return jsonResponse(request.principal().flatMap(principal -> {
        LOG.debug("Principal {}", principal.getName());
        return userService.logoutAll();
      }), Void.class);
  }

  public Mono<ServerResponse> renewToken(ServerRequest request) {
    return exchangeToken(request, userService::renewToken);
  }

  public RouterFunctions.Builder setupRoute(RouterFunctions.Builder builder) {
    getRoute(builder, "/self", this::self);
    postRoute(builder, "/auth/login", this::login);
    postRoute(builder, "/auth/logout_all", this::logoutAll);
    postRoute(builder, "/auth/renew", this::renewToken);
    return builder;
  }

  private void postRoute(RouterFunctions.Builder builder, String url, HandlerFunction<ServerResponse> handler) {
    routeImpl(builder, RequestPredicates.POST(url), handler);
  }

  private void getRoute(RouterFunctions.Builder builder, String url, HandlerFunction<ServerResponse> handler) {
    routeImpl(builder, RequestPredicates.GET(url), handler);
  }

  private void routeImpl(RouterFunctions.Builder builder,
                         RequestPredicate url,
                         HandlerFunction<ServerResponse> handler) {
    builder.route(url.and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), handler);
  }

  private static <T> Mono<ServerResponse> jsonResponse(Mono<T> response, Class<T> cl) {
    return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(response, cl);
  }

  private static Mono<ServerResponse> exchangeToken(
      ServerRequest request, Function<String, Mono<String>> mapper) {
    return jsonResponse(jsonBody(request).flatMap(
        json -> mapper.apply(json.get("token").asText()).map(RestData.Token::new)).onErrorMap(e -> {
          return new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }), RestData.Token.class);
  }

  private static Mono<JsonNode> jsonBody(ServerRequest request) {
    return request.bodyToMono(JsonNode.class);
  }
}
