package com.socialytube.backend.site.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.socialytube.backend.common.Tools;
import com.socialytube.backend.site.api.Data.BaseEventData;
import com.socialytube.backend.site.api.Data.Channel;
import com.socialytube.backend.site.api.Data.ChannelInfo;
import com.socialytube.backend.site.api.Data.Events;
import com.socialytube.backend.site.api.Data.LikeEventData;
import com.socialytube.backend.site.api.Data.SubscriptionEventData;
import com.socialytube.backend.site.api.Data.CommentEventData;
import com.socialytube.backend.site.api.Data.Thumbnail;
import com.socialytube.backend.site.api.Data.Tube;
import com.socialytube.backend.site.api.Data.Video;
import com.socialytube.backend.site.api.Data.VideoEvents;
import com.socialytube.backend.site.api.Data.VideoEventComments;
import com.socialytube.backend.site.api.Data.VideoInfo;
import com.socialytube.backend.site.api.Data.EventType;
import com.socialytube.backend.site.api.Data.WatchEventData;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class DataAccessor implements IDataAccessor {
  private static final Logger LOG = LoggerFactory.getLogger(DataAccessor.class);

  @Autowired
  private JdbcOperations jdbcOperations_;

  private static class TubeMapper implements RowMapper<Tube> {
    @Override
    public Tube mapRow(ResultSet rs, int rowNum) throws SQLException {
      Tube tube = new Tube();
      tube.id = rs.getString(1);
      tube.title = rs.getString(2);
      tube.thumbnails = new ArrayList<Thumbnail>();
      return tube;
    }
  }

  private static final RowMapper<Tube> TUBE_MAPPER = new TubeMapper();

  /*
   * - @Override - public List<Channel> channels() { - return
   * jdbcOperations_.query("SELECT public_id,name, extra FROM tube ORDER BY name",
   * CHANNEL_MAPPER); - } - - @Override - public List<Base> channelEvents(String
   * channel_id) { - return jdbcOperations_.
   * query("SELECT v_external_id, v_extra FROM tube_video_helper WHERE c_public_id=?::uuid ORDER BY position DESC LIMIT 200"
   * , VIDEO_EVENT_MAPPER, channel_id); - }
   */

  @Override
  public List<Tube> tubes() {
    return jdbcOperations_.query("SELECT public_id, title, extra FROM tube ORDER BY title", TUBE_MAPPER);
  }

  @Override
  public Tube tube(String tpid) {
    return null;
  }

  @Override
  public Events tubeEvents(String tpid) {
    return events(
        "SELECT source, t_public_id, t_title, v_external_id, v_extra, c_external_id, c_extra, s_extra FROM tube_event_helper WHERE t_public_id =?::uuid ORDER BY created_at DESC LIMIT 100",
        tpid);
  }

  @Override
  public Events likes(String tpid) {
    return events(
        "SELECT source, t_public_id, t_title, v_external_id, v_extra, c_external_id, c_extra, s_extra FROM tube_event_helper WHERE t_public_id =?::uuid AND source = 'like' ORDER BY created_at DESC LIMIT 100",
        tpid);
  }

  @Override
  public Events subscriptions(String tpid) {
    return events(
        "SELECT source, t_public_id, t_title, v_external_id, v_extra, c_external_id, c_extra, s_extra FROM tube_event_helper WHERE t_public_id =?::uuid AND source = 'subscribe' ORDER BY created_at DESC LIMIT 100",
        tpid);
  }

  @Override
  public Events comments(String tpid) {
    return events(
        "SELECT source, t_public_id, t_title, v_external_id, v_extra, c_external_id, c_extra, s_extra FROM tube_event_helper WHERE t_public_id =?::uuid AND source = 'comment' ORDER BY created_at DESC LIMIT 100",
        tpid);
  }

  @Override
  public Events commonEvents() {
    return events(
        "SELECT source, t_public_id, t_title, v_external_id, v_extra, c_external_id, c_extra, s_extra FROM tube_event_helper ORDER BY created_at DESC LIMIT 100");
  }

  @Override
  public Events accountEvents(int accountId) {
    // return events("SELECT v_external_id, v_extra, c_external_id, c_extra,
    // t_public_id, t_title FROM tube_video_helper WHERE account_id=? ORDER BY
    // position DESC LIMIT 200", accountId);
    return null;
  }

  private Events events(String query, Object... args) {
    final Map<String, Channel> channels = new HashMap<>();
    final Map<String, Tube> tubes = new HashMap<>();
    final Map<String, Video> videos = new HashMap<>();
    Events result = new Events();
    result.datas = jdbcOperations_.query(query, args, (ResultSet rs, int rowNum) -> {
      String source = rs.getString(1);
      BaseEventData event = null;
      String channelId = rs.getString(6);
      if (!channels.containsKey(channelId)) {
        channels.put(channelId, buildChannel(channelId, rs.getString(7)));
      }
      if (source.equals(EventType.WATCH.value)) {
        event = new WatchEventData();
        String videoId = rs.getString(4);
        event.video_id = videoId;
        if (!videos.containsKey(videoId)) {
          videos.put(videoId, buildVideo(videoId, channelId, rs.getString(5)));
        }
      } else if (source.equals(EventType.LIKE.value)) {
        event = new LikeEventData();
        String videoId = rs.getString(4);
        event.video_id = videoId;
        if (!videos.containsKey(videoId)) {
          videos.put(videoId, buildVideo(videoId, channelId, rs.getString(5)));
        }
      } else if (source.equals(EventType.SUBSCRIBE.value)) {
        event = new SubscriptionEventData();
        event.channel_id = channelId;
      } else if (source.equals(EventType.COMMENT.value)) {
        JSONObject extra = new JSONObject(rs.getString(8));
        event = new CommentEventData(extra.getString("eid"), extra.getString("txt"));
        String videoId = rs.getString(4);
        event.video_id = videoId;
        if (!videos.containsKey(videoId)) {
          videos.put(videoId, buildVideo(videoId, channelId, rs.getString(5)));
        }
      }
      String tubeId = rs.getString(2);
      if (!tubes.containsKey(tubeId)) {
        Tube t = new Tube();
        t.id = tubeId;
        t.title = rs.getString(3);
        tubes.put(tubeId, t);
      }
      event.tube_id = tubeId;
      return event;
    });
    result.channels = channels.values();
    result.tubes = tubes.values();
    result.videos = videos.values();
    return result;
  }

  private static Thumbnail thumbnail(JSONObject t) {
    if (t != null) {
      Thumbnail result = new Thumbnail();
      result.width = t.getInt("w");
      result.height = t.getInt("h");
      result.url = t.getString("u");
      return result;
    }
    return null;
  }

  private static Video buildVideo(String id, String channelId, String extra) {
    Video v = new Video();
    v.external_id = id;
    v.channel_id = channelId;
    if (extra != null) {
      JSONObject obj = new JSONObject(extra);
      v.duration = obj.getInt("dur");
      v.title = obj.getString("nm");
      List<Thumbnail> thmbs = new ArrayList<>();
      for (Object t : obj.getJSONArray("thmbs")) {
        thmbs.add(thumbnail((JSONObject) t));
      }
      v.thumbnails = thmbs;
    }
    return v;
  }

  private static Channel buildChannel(String id, String extra) {
    Channel c = new Channel();
    c.external_id = id;
    if (extra != null) {
      JSONObject obj = new JSONObject(extra);
      c.title = obj.getString("nm");
      List<Thumbnail> thmbs = new ArrayList<>();
      JSONArray src = Tools.safeGetJSONArray(obj, "thmbs");
      if (src != null && src.length() > 0) {
        for (Object t : src) {
          thmbs.add(thumbnail((JSONObject)t));
        }
      }
      c.thumbnails = thmbs;
    }
    return c;
  }

  @Override
  public VideoInfo videoInfo(String videoId) {
    Map<String, Tube> tubes = new HashMap<>();
    List<String> watches = new ArrayList<>();
    List<String> likes = new ArrayList<>();
    List<String> dislikes = new ArrayList<>();
    List<VideoEventComments.Comment> comments = new ArrayList<>();
    jdbcOperations_.query("SELECT source, s_extra, t_public_id, t_title FROM video_event_helper WHERE v_external_id=?", 
      (ResultSet rs) -> {
        String tube_id = rs.getString(3);
        if (!tubes.containsKey(tube_id)) {
          Tube t = new Tube();
          t.id = tube_id;
          t.title = rs.getString(4);
          tubes.put(tube_id, t);
        }
        String s = rs.getString(1);
        if (s.equals(EventType.LIKE.value)) {
          likes.add(tube_id);
        } else if (s.equals(EventType.COMMENT.value)) {
          VideoEventComments.Comment c = new VideoEventComments.Comment();
          c.tube_id = tube_id;
          comments.add(c);
        } else if (s.equals(EventType.WATCH.value)) {
          watches.add(tube_id);
        }
      }, videoId);
    VideoInfo result = new VideoInfo();
    result.comments = new VideoEventComments();
    result.comments.total = comments.size();
    result.comments.last_comments = comments;
    result.likes = buildVideoEvents(likes);
    result.watches = buildVideoEvents(watches);
    result.dislikes = buildVideoEvents(dislikes);
    result.tubes = tubes.values();
    return result;
  }

  @Override
  public ChannelInfo channelInfo(String videoId) {
    return null;
  }

  private static VideoEvents buildVideoEvents(List<String> src) {
    VideoEvents result = new VideoEvents();
    result.last_tubes = src;
    result.total = src.size();
    return result;
  }
}