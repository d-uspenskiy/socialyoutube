package com.socialyoutube.service.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class Controller {
  private static final Logger LOG = LoggerFactory.getLogger(Controller.class);

  public Controller() {
    LOG.info("Controller");
  }

  @PostMapping("/login")
  public void login() {
    return;
  }

  @PostMapping("/logout")
  public void logout() {
    return;
  }
}
