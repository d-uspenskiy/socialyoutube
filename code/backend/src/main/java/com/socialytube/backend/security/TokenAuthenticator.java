package com.socialytube.backend.security;

import java.util.Collections;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.socialytube.backend.common.IUserProvider;

import org.apache.commons.collections4.map.LRUMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class TokenAuthenticator {
  private static final Logger LOG = LoggerFactory.getLogger(TokenAuthenticator.class);

  private final static String BEARER_PREFIX = "Bearer ";

  @Autowired
  private IUserProvider userProvider_;

  private Map<String, Authentication> cachedUsers_ = Collections.synchronizedMap(new LRUMap<String, Authentication>(100000));

  @Value("${google.client.id}")
  private String client_id_;

  private GoogleIdTokenVerifier verifier_ = null;

  @PostConstruct
  private void init() {
    verifier_ = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
        .setAudience(Collections.singletonList(client_id_))
        .setAcceptableTimeSkewSeconds(24 * 60 * 60)
        .build();
  }


  Authentication authenticate(HttpServletRequest req) {
    String token = req.getHeader("Authorization");
    if (token != null && token.startsWith(BEARER_PREFIX)) {
      token = token.substring(BEARER_PREFIX.length(), token.length());
      Authentication auth = cachedUsers_.get(token);
      return auth == null ? newAuthentication(token) : auth;
    }
    return null;
  }

  private Authentication newAuthentication(String token) {
    LOG.debug("newAuthentication: {}", token);
    GoogleIdToken idToken = null;
    try {
      idToken = verifier_.verify(token);
    } catch (Exception e) {
      LOG.error("Exc", e);
    }
    if (idToken == null) {
      LOG.info("wrong token: {}", token);
      return null;
    }

    UserDetails userDetails = userProvider_.getUser(idToken.getPayload());
    Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    cachedUsers_.put(token, auth);
    return auth;
  }
}