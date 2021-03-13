package com.socialytube.backend.config;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;

public class ConfigPropertySource extends PropertySource<Object> {

  private static final String CONFIG_PROPERTY_SOURCE_NAME = "config";

  private static final String PREFIX = CONFIG_PROPERTY_SOURCE_NAME + ".";

  private ConfigPropertyComponent component_;

  public ConfigPropertySource(ConfigPropertyComponent component) {
    super(CONFIG_PROPERTY_SOURCE_NAME);
    this.component_ = component;
  }

  @Override
  public Object getProperty(String name) {
    if (!name.startsWith(PREFIX)) {
      return null;
    }
    String tail = name.substring(PREFIX.length());
    if (tail.equals("db_url")) {
      return component_.dbUrl;
    }
    if (tail.equals("db_username")) {
      return component_.dbUsername;
    }

    if (tail.equals("db_password")) {
      return component_.dbPassword;
    }

    if (tail.equals("google_client_id")) {
      return component_.googleClientId;
    }
    return null;
  }

  public static void addToEnvironment(ConfigurableEnvironment environment, ConfigPropertyComponent component) {
    MutablePropertySources sources = environment.getPropertySources();
    PropertySource<?> existing = sources.get(CONFIG_PROPERTY_SOURCE_NAME);
    if (existing != null) {
      return;
    }
    ConfigPropertySource randomSource = new ConfigPropertySource(component);
    if (sources.get(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME) != null) {
      sources.addAfter(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, randomSource);
    } else {
      sources.addLast(randomSource);
    }
  }    
}
