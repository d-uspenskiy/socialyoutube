package com.socialyoutube.service.user.impl;

public class UserServiceExceptions {
  public static class BaseException extends Exception {
    private static final long serialVersionUID = -6792035022029491297L;

    public BaseException(String message) { super(message); }
  
    public BaseException(String message, Throwable cause) { super(message, cause); }
  }

  public static class BadCredentials extends BaseException {
    private static final long serialVersionUID = 6640581826396677033L;

    public BadCredentials(String message) { super(message); }

    public BadCredentials(String message, Throwable cause) { super(message, cause); }
  }

  private  UserServiceExceptions() {}
}
