package com.socialyoutube.service.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class App {
  private static final Logger LOG = LoggerFactory.getLogger(App.class);
  public static void main(String[] args) {
    ApplicationContext context = new ClassPathXmlApplicationContext("app.xml");
  }
}