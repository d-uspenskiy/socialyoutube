package com.socialytube.backend.gateway;

import java.util.function.Consumer;

import com.socialytube.backend.common.IUserProvider;
import com.socialytube.backend.common.Tools;
import com.socialytube.backend.common.IUserProvider.Info;
import com.socialytube.backend.common.Tools.JsonStringWriter;
import com.socialytube.backend.gateway.api.Api;
import com.socialytube.backend.gateway.api.Data.BaseData;
import com.socialytube.backend.gateway.api.Data.Comments;
import com.socialytube.backend.gateway.api.Data.History;
import com.socialytube.backend.gateway.api.Data.HistoryOld;
import com.socialytube.backend.gateway.api.Data.LikeState;
import com.socialytube.backend.gateway.api.Data.Likes;
import com.socialytube.backend.gateway.api.Data.SelfInfo;
import com.socialytube.backend.gateway.api.Data.Subscriptions;
import com.socialytube.backend.gateway.api.Data.Thumbnail;
import com.socialytube.backend.gateway.service.IDataUpdater;
import com.socialytube.backend.gateway.service.IUpdateFilter;
import com.socialytube.backend.gateway.service.IUpdateFilter.Marker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("GatewayController")
@RequestMapping
public class Controller implements Api {
  private static final Logger LOG = LoggerFactory.getLogger(Controller.class);

  @Autowired private IUserProvider userProvider_;

  @Autowired private IDataUpdater updater_;

  @Autowired private IUpdateFilter filter_;

  @Override
  public void history(History history) {
    History.DataContainer data = history.data;
    LOG.info("HISTORY hash={}, ch={}, v={}, w={}, l={}, fwc={}", history.hash, data.channel.size(), data.video.size(), data.watch.size(), data.like.size(), data.fresh_watch_count);
    processFiltered(history, Marker.HISTORY, tubeId -> updater_.history(tubeId, data));
  }

   @Override
  public void old_history(HistoryOld history) {
    processFiltered(history, Marker.HISTORY, tubeId -> updater_.history(tubeId, history.data));
  }

  @Override
  public void comments(Comments comments) {
    processFiltered(comments, Marker.COMMENTS, tubeId -> updater_.comments(tubeId, comments.data));
  }

  @Override
  public void likes(Likes likes) {
    processFiltered(likes, Marker.LIKES, tubeId -> updater_.likes(tubeId, likes.data));
  }

  @Override
  public void subscriptions(Subscriptions subscriptions) {
    processFiltered(subscriptions, Marker.SUBSCRIPTIONS, tubeId -> updater_.subscriptions(tubeId, subscriptions.data));
  }

  @Override
  public void likeState(LikeState likeState) {
    // TODO Auto-generated method stub

  }

  @Override
  public void error(String json) {
    // TODO Auto-generated method stub

  }

  private int getTubeId(SelfInfo self) {
    return userProvider_.resolveTubeId(Tools.getCurrentUser(), new Info() {
      @Override
      public String channelId() {
        return self.channel_id;
      }

      @Override
      public String title() {
        return self.title;
      }

      @Override
      public String extraJson() {
        JsonStringWriter w = Tools.jsonObjectWriter().array("thmbs");
        for (Thumbnail t : self.thumbnails) {
          Utils.thumbnail(w.obj(), t).close();
        }
        return w.result();
      }
    });
  }

  void processFiltered(BaseData data, IUpdateFilter.Marker marker, Consumer<Integer> consumer) {
    LOG.info("{} {} {} {}", marker.value, data.hash, data.version, data.self.channel_id);
    final int tubeId = getTubeId(data.self);

    if (!filter_.filtered(tubeId, data.hash, data.version, marker)) {
      consumer.accept(tubeId);
    }
  }
}