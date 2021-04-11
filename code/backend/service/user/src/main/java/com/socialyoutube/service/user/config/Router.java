package com.socialyoutube.service.user.config;

import com.socialyoutube.service.user.impl.UserServiceRestAccessor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class Router {
  @Bean
  public RouterFunction<ServerResponse> route(UserServiceRestAccessor accessor) {
    var builder = RouterFunctions.route();
    accessor.setupRoute(builder);
    return builder.build();
  }
}
