package com.socialytube.backend.gateway.service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.socialytube.backend.common.Tools;
import com.socialytube.backend.common.Tools.JsonStringWriter;
import com.socialytube.backend.gateway.Utils;
import com.socialytube.backend.gateway.api.Data.Channel;
import com.socialytube.backend.gateway.api.Data.Comment;
import com.socialytube.backend.gateway.api.Data.History;
import com.socialytube.backend.gateway.api.Data.Subscription;
import com.socialytube.backend.gateway.api.Data.Thumbnail;
import com.socialytube.backend.gateway.api.Data.Video;

import org.apache.commons.collections4.iterators.ReverseListIterator;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.stereotype.Service;

@Service
public class DataUpdater implements IDataUpdater {
  private static final Logger LOG = LoggerFactory.getLogger(DataUpdater.class);

  @Autowired
  private JdbcOperations jdbcOperations_;

  private class VideoInserter {
    private JsonStringWriter writer_ = new JsonStringWriter();
    private Map<String, Integer> channelIds_ = new HashMap<>();
    private Map<String, Pair<Integer, Integer>> videoIds_ = new HashMap<>();

    public Pair<Integer, Integer> insert(Video video, Function<String, Channel> channelProvider) {
      Pair<Integer, Integer> vInfo = videoIds_.get(video.id);
      if (vInfo == null) {
        Integer cId = channelIds_.get(video.channel_id);
        if (cId == null) {
          Channel c = channelProvider.apply(video.channel_id);
          cId = insert(c);
          channelIds_.put(c.id, cId);
        }
        writer_.clear().obj().str("nm", video.title).num("dur", video.duration).array("thmbs");
        for (final Thumbnail t : video.thumbnails) {
          Utils.thumbnail(writer_.obj(), t).close();
        }
        Integer vId = Tools.fetch(jdbcOperations_, Integer.class, "SELECT force_get_video(?, ?, ?::JSON)", cId, video.id,
            writer_.result());
        vInfo = new ImmutablePair<Integer,Integer>(cId, vId);
        videoIds_.put(video.id, vInfo);
      }
      return vInfo;
    }

    private int insert(Channel channel) {
      prepareChannel(writer_, channel.title);
      if (channel.thumbnail != null) {
        Utils.thumbnail(writer_.obj(), channel.thumbnail);
      }
      return insertChannel(channel.id, writer_.result());
    }
  }

  @Override
  public void history(int tubeId, List<Video> videos) {
    LOG.info("history ch {} v {}", tubeId, videos.size());
/*    final List<Pair<Video, String>> hashedVideos = new ArrayList<>();
    String currentHash = null;
    final ReverseListIterator<Video> rV = new ReverseListIterator<>(videos);
    while (rV.hasNext()) {
      final Video v = rV.next();
      if (v != null) {
        if (currentHash == null) {
          LOG.info("Last history item {}", v.id);
          currentHash = Tools.fetchWithDefault(jdbcOperations_, "",
              "SELECT position_hash FROM tube_video AS tv INNER JOIN video AS v ON tv.video_id=v.id WHERE tv.tube_id=? AND v.external_id=?",
              tubeId, v.id);
          LOG.info("Current hash '{}'", currentHash);
        } else {
          currentHash = newHash(currentHash, v.id);
        }
        hashedVideos.add(new ImmutablePair<Video, String>(v, currentHash));
      }
    }
    int newItems = 0;
    final ReverseListIterator<Pair<Video, String>> rH = new ReverseListIterator<>(hashedVideos);
    while (rH.hasNext()) {
      final String pos_hash = rH.next().getRight();
      if (Tools.fetchOptional(jdbcOperations_, Integer.class,
          "SELECT id FROM tube_video WHERE tube_id=? AND position_hash=?", tubeId, pos_hash) != null) {
        break;
      }
      ++newItems;
    }
    if (newItems == 0) {
      // This should be filtered by request hash, clear duplicates
      LOG.warn("No new history items");
      return;
    }
    final int actualStart = hashedVideos.size() - newItems;
    LOG.info("Actual start {} of {}", actualStart, hashedVideos.size());
    final VideoInserter inserter = new VideoInserter();
    for (int i = actualStart; i < hashedVideos.size(); ++i) {
      final Pair<Video, String> pair = hashedVideos.get(i);
      final Video v = pair.getLeft();
      LOG.info("Update tube video {} {}", v.id, v.title);
      final int vid = inserter.insert(v);
      final int tvid = Tools.fetch(jdbcOperations_, Integer.class,
          "INSERT INTO tube_video(tube_id, video_id, position_hash) VALUES(?, ?, ?) ON CONFLICT (tube_id, video_id) DO UPDATE SET position_hash = EXCLUDED.position_hash, created_at = EXCLUDED.created_at, position = EXCLUDED.position RETURNING id",
          tubeId, vid, pair.getRight());
      LOG.info("Tube video id {}", tvid);
    }*/
  }

  @Override
  public void history(int tubeId, History.DataContainer data) {
    VideoInserter inserter = new VideoInserter();
    Map<String, Channel> channels = new HashMap<>();
    Map<String, Video> videos = new HashMap<>();
    for (Channel c : data.channel) {
      channels.put(c.id, c);
    }
    for (Video v : data.video) {
      videos.put(v.id, v);
    }
    Function<String, Channel> channelProvider = (String channelId)-> channels.get(channelId);
    int position = 0;
    for (final ReverseListIterator<String> rW = new ReverseListIterator<>(data.watch); rW.hasNext(); ++position) {
      final Pair<Integer, Integer> vInfo = inserter.insert(videos.get(rW.next()), channelProvider);
      final int wid = Tools.fetch(jdbcOperations_, Integer.class,
          "SELECT insert_watch(?, ?, ?, ?, ?)",
          tubeId, vInfo.getLeft(), vInfo.getRight(), position, newHash("", UUID.randomUUID().toString()));
      LOG.info("watch list video id {}", wid);
    }
    position = 0;
    for (ReverseListIterator<String> rL = new ReverseListIterator<>(data.like); rL.hasNext(); ++position) {
      final Pair<Integer, Integer> vInfo = inserter.insert(videos.get(rL.next()), channelProvider);
      final int lid = Tools.fetch(jdbcOperations_, Integer.class,
          "SELECT insert_like(?, ?, ?, ?, ?)",
          tubeId, vInfo.getLeft(), vInfo.getRight(), position, newHash("", UUID.randomUUID().toString()));
      LOG.info("like list video id {}", lid);
    }
  }

  @Override
  public void likes(int tubeId, List<Video> videos) {
    LOG.info("likes ch {} v {}", tubeId, videos.size());
/*    final VideoInserter inserter = new VideoInserter();
    final ReverseListIterator<Video> rV = new ReverseListIterator<>(videos);
    while (rV.hasNext()) {
      final int vId = inserter.insert(rV.next());
      Tools.fetch(jdbcOperations_, Integer.class, "SELECT set_like_marker(?,?,TRUE)", tubeId, vId);
    }*/
  }

  @Override
  public void comments(int tubeId, List<Comment> comments) {
    LOG.info("comments ch {} v {}", tubeId, comments.size());
    JsonStringWriter writer = new JsonStringWriter();
    int position = 0;
    for (ReverseListIterator<Comment> rC = new ReverseListIterator<>(comments); rC.hasNext(); ++position) {
      Comment c = rC.next();
      final int vId = Tools.fetch(jdbcOperations_, Integer.class, "SELECT force_get_video_stub(?)", c.video_id);
      writer.clear().obj().str("txt", c.text).str("eid", c.id);
      Tools.fetch(jdbcOperations_, Integer.class,
        "INSERT INTO comment_list(tube_id, video_id, external_id, extra, position, position_hash) VALUES(?, ?, ?, ?::JSON, ?, ?) RETURNING id", tubeId, vId, c.id, writer.result(), position, newHash("", UUID.randomUUID().toString()));
    }
  }

  @Override
  public void subscriptions(int tubeId, List<Subscription> subscriptions) {
    JsonStringWriter w = new JsonStringWriter();
    for (Subscription s : subscriptions) {
      prepareChannel(w, s.title);
      for (Thumbnail t : s.thumbnails) {
        Utils.thumbnail(w.obj(), t).close();
      }
      int cid = insertChannel(s.channel_id, w.result());
      jdbcOperations_.update("INSERT INTO subscription(tube_id, channel_id) VALUES(?, ?)", tubeId, cid);
    }
  }

  private static String newHash(final String prevHash, final String externalVideoId) {
    return Hashing.murmur3_128().newHasher()
      .putString(prevHash, StandardCharsets.UTF_8)
      .putString(externalVideoId, StandardCharsets.UTF_8)
      .hash()
      .toString();
  }

  private int insertChannel(String channel_id, String extra) {
    return Tools.fetch(jdbcOperations_, Integer.class, "SELECT force_get_channel(?, ?::JSON)", channel_id, extra);
  }

  private static JsonStringWriter prepareChannel(JsonStringWriter writer, String title) {
    return writer.clear().obj().str("nm", title).array("thmbs");
  }
}