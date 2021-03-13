package com.socialytube.backend.site;

import com.socialytube.backend.common.Tools;
import com.socialytube.backend.site.api.Api;
import com.socialytube.backend.site.api.Data.Account;
import com.socialytube.backend.site.api.Data.Events;
import com.socialytube.backend.site.api.Data.Tube;
import com.socialytube.backend.site.api.Data.ChannelInfo;
import com.socialytube.backend.site.api.Data.VideoInfo;

import com.socialytube.backend.site.service.IDataAccessor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("SiteController")
@RequestMapping
public class Controller implements Api.Base, Api.TubeSpecific, Api.ExternalInfo {

  @Autowired
  IDataAccessor accessor_;

  @Override
  public Iterable<Tube> tubesList() throws Exception {
    return accessor_.tubes();
  }

  @Override
  public Tube tube(String tube_id) throws Exception {
    return accessor_.tube(tube_id);
  }

  @Override
  public Events tubeEvents(String tube_id) {
    return accessor_.tubeEvents(tube_id);
  }

  @Override
  public Events tubeLikes(String tube_id) {
    return accessor_.likes(tube_id);
  }

  @Override
  public Events tubeComments(String tube_id) {
    return accessor_.comments(tube_id);
  }

  @Override
  public Events commonEvents() {
    return accessor_.commonEvents();
  }

  @Override
  public Events selfEvents() {
    return accessor_.accountEvents(Tools.getCurrentUser().getDbid());
  }

  @Override
  public Events tubeSubscriptions(String tube_id) {
    return accessor_.subscriptions(tube_id);
  }

  @Override
  public Account selfInfo() {
    return null;
  }

  @Override
  public void auth() {
  }

  @Override
  public VideoInfo extInfoVideo(String extVideoId) {
    return accessor_.videoInfo(extVideoId);
  }

  @Override
  public ChannelInfo extInfoChannel(String extChannelId) {
    return accessor_.channelInfo(extChannelId);
  }
}