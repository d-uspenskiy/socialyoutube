package com.socialytube.backend.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class User implements UserDetails {
  public static class CachedData {
    public Pair<String, Integer> latestTube;
  }

  private static final long serialVersionUID = -2213526758882574925L;

  private String username_;

  private int dbId_;

  private CachedData data_;

  public User(String username, int dbId) {
    username_ = username;
    dbId_ = dbId;
    data_ = new CachedData();
  }

  public int getDbid() {
    return dbId_;
  }

  public CachedData getCachedData() {
    return data_;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return (new ArrayList<String>()).stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
  }

  @Override
  public String getPassword() {
    return null;
  }

  @Override
  public String getUsername() {
    return username_;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
