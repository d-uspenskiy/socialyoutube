package com.socialyoutube.service.user;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.duspensky.jutils.common.Misc;
import com.socialyoutube.service.common.GatewayProvider;
import com.socialyoutube.service.rapi.UserServiceRemoteApi;
import com.socialyoutube.service.user.impl.JwtTokenProvider;
import com.socialyoutube.service.user.impl.UserService;
import com.socialyoutube.service.user.impl.UserServiceRestAccessor;
import com.socialyoutube.service.user.repo.UserRepo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.client.reactive.ReactorResourceFactory;
import org.springframework.web.server.session.WebSessionManager;

import reactor.core.publisher.Mono;
import reactor.netty.resources.LoopResources;

@SpringBootApplication
@PropertySource("file:${SYT_CONFIG_PROPERTIES_FILE}")
public class App implements InitializingBean, DisposableBean {
  private static final Logger LOG = LoggerFactory.getLogger(App.class);

  @Autowired
  private JwtTokenProvider tokenProvider;

  @Autowired
  private UserRepo userRepo;

  @Value("${google.client.id}")
  private String clientId;

  private GatewayProvider gatewayProvider;
  private ExecutorService mainExecutor;
  private UserService userService;

  @Bean
  public UserServiceRestAccessor userServiceRestAccessor() {
    return new UserServiceRestAccessor(userService);
  }

  @Bean
  public ReactorResourceFactory resourceFactory() {
    var reactorResourceFactory = new ReactorResourceFactory();
    reactorResourceFactory.setLoopResourcesSupplier(() -> LoopResources.create("reactor", 1, true));
    reactorResourceFactory.setUseGlobalResources(false);
    return reactorResourceFactory;
  }

  @Bean
  public WebSessionManager webSessionManager() {
    // Emulate SessionCreationPolicy.STATELESS
    return exchange -> Mono.empty();
  }

  public static void main(String[] args) {
    displayAllBeans(SpringApplication.run(App.class, args));
  }

  private static void displayAllBeans(ApplicationContext ctx) {
    for(String beanName : ctx.getBeanDefinitionNames()) {
      LOG.debug("Bean {}", beanName);
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    LOG.info("afterPropertiesSet");
    gatewayProvider = new GatewayProvider();
    mainExecutor = Executors.newFixedThreadPool(5);
    userService = new UserService(tokenProvider, clientId, userRepo);
    gatewayProvider.getGateway().registerImplementation(UserServiceRemoteApi.class, userService, mainExecutor);
  }

  @Override
  public void destroy() throws Exception {
    LOG.info("afterPropertiesSet");
    gatewayProvider.close();
    Misc.shutdown(mainExecutor);
    userService.close();
  }
}