package com.socialytube.backend.gateway.service;

public interface IUpdateFilter {
  public enum Marker {
    HISTORY("history"),
    LIKES("likes"),
    COMMENTS("comments"),
    SUBSCRIPTIONS("subscriptions");

    public final String value;

    private Marker(String v) {
      value = v;
    }
  }

  boolean filtered(int tubeId, long hash, long version, Marker marker);
}