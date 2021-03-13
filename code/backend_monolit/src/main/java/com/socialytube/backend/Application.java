package com.socialytube.backend;

import javax.annotation.PostConstruct;

import com.socialytube.backend.config.ConfigPropertyComponent;
import com.socialytube.backend.config.ConfigPropertySource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.ConfigurableEnvironment;

@SpringBootApplication
public class Application {
  @Autowired
  private ConfigurableEnvironment env_;

  @Autowired
  private ConfigPropertyComponent component_;

  @PostConstruct
  public void init() {
    ConfigPropertySource.addToEnvironment(env_, component_);
  }

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}