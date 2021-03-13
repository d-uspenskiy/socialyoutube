package com.socialytube.backend.site.service;

import java.util.List;

import com.socialytube.backend.site.api.Data.Events;
import com.socialytube.backend.site.api.Data.Tube;
import com.socialytube.backend.site.api.Data.ChannelInfo;
import com.socialytube.backend.site.api.Data.VideoInfo;

public interface IDataAccessor {
  List<Tube> tubes();
  Tube tube(String publicId);
  Events tubeEvents(String publicId);
  Events likes(String publicId);
  Events subscriptions(String publicId);
  Events comments(String publicId);
  Events commonEvents();
  Events accountEvents(int accountId);
  VideoInfo videoInfo(String videoId);
  ChannelInfo channelInfo(String videoId);
}