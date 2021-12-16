package com.socialyoutube.service.user.repo;

import com.socialyoutube.service.user.domain.User;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Mono;

public interface UserRepo extends ReactiveCrudRepository<User, Long> {
  Mono<User> findByEmail(String email);

  @Query("SELECT id,email FROM force_get_account(:email) as a(id INT, email VARCHAR(128), public_id UUID, extra JSONB, created_at TIMESTAMP, last_auth_at TIMESTAMP, last_unauth_at TIMESTAMP)")
  Mono<User> forceGet(String email);
}
