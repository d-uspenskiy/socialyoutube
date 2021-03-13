package com.socialytube.backend.gateway.api;

import com.socialytube.backend.gateway.api.Data.Comments;
import com.socialytube.backend.gateway.api.Data.History;
import com.socialytube.backend.gateway.api.Data.HistoryOld;
import com.socialytube.backend.gateway.api.Data.LikeState;
import com.socialytube.backend.gateway.api.Data.Likes;
import com.socialytube.backend.gateway.api.Data.Subscriptions;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface Api {
  @PostMapping("/gateway/yt/history")
  void history(@RequestBody History history);

  @PostMapping("/gateway/yt/history_old")
  void old_history(@RequestBody HistoryOld history);

  @PostMapping("/gateway/yt/comments")
  void comments(@RequestBody Comments comments);

  @PostMapping("/gateway/yt/likes")
  void likes(@RequestBody Likes likes);

  @PostMapping("/gateway/yt/subscriptions")
  void subscriptions(@RequestBody Subscriptions subscriptions);

  @PostMapping("/gateway/yt/like_state")
  void likeState(@RequestBody LikeState likeState);

  @PostMapping("/gateway/yt/error")
  void error(@RequestBody String json);
}
