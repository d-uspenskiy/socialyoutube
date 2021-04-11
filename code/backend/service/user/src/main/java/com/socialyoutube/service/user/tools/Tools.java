package com.socialyoutube.service.user.tools;

public class Tools {
  public interface FunctionWithException<T, R, E extends Exception> {
    R apply(T t) throws E;
  }

  private Tools() {}
}
