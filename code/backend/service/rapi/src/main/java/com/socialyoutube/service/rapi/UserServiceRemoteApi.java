package com.socialyoutube.service.rapi;

public interface UserServiceRemoteApi {
  Data.UserInfo getUserInfo(String token);
}
