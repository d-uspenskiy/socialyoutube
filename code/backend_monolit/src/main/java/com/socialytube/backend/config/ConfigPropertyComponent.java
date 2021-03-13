package com.socialytube.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("file:${SYT_CONFIG_PROPERTIES_FILE}")
public class ConfigPropertyComponent {
  @Value("${db.url}")
  public String dbUrl;

  @Value("${db.username}")
  public String dbUsername;

  @Value("${db.password}")
  public String dbPassword;

  @Value("${google_client_id}")
  public String googleClientId;
}