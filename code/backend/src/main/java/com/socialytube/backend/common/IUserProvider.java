package com.socialytube.backend.common;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;

public interface IUserProvider {
  public interface Info {
    String channelId();
    String title();
    String extraJson();
  }

  User getUser(GoogleIdToken.Payload tokenPayload);
  int resolveTubeId(User user, Info info);
}