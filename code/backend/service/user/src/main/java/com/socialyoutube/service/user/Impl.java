package com.socialyoutube.service.user;

import com.socialyoutube.service.api.Misc.UserInfo;
import com.duspensky.jutils.rmqrmi.Gateway;
import com.duspensky.jutils.rmqrmi.Exceptions.BadInterface;
import com.socialyoutube.service.api.User;

public class Impl implements User {

  private Gateway gateway;

  public Impl(Gateway gw) throws BadInterface {
    gateway = gw;
    gateway.registerImplementation(User.class, this);
  }

  @Override
  public UserInfo getUserInfo(String token) {
    return new UserInfo();
  }
}
