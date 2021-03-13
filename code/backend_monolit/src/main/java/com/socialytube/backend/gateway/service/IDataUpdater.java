package com.socialytube.backend.gateway.service;

import java.util.List;

import com.socialytube.backend.gateway.api.Data.Comment;
import com.socialytube.backend.gateway.api.Data.History;
import com.socialytube.backend.gateway.api.Data.Subscription;
import com.socialytube.backend.gateway.api.Data.Video;

public interface IDataUpdater {
  void history(int tubeId, List<Video> videos);
  void history(int tubeId, History.DataContainer data);
  void likes(int tubeId, List<Video> videos);
  void comments(int tubeId, List<Comment> comments);
  void subscriptions(int tubeId, List<Subscription> subscriptions);
}