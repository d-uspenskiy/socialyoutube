package com.socialytube.backend.site.api;

import com.fasterxml.jackson.annotation.JsonInclude;

public class Data {
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Account {};

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Thumbnail {
    public int width;
    public int height;
    public String url;
  };

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Tube {
    public String id;
    public String title;
    public Iterable<Thumbnail> thumbnails;
  };

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Channel {
    public String external_id;
    public String title;
    public Iterable<Thumbnail> thumbnails;
  };

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Video {
    public String external_id;
    public String title;
    public int duration;
    public String channel_id;
    public String tube_id;
    public Iterable<Thumbnail> thumbnails;
  };

  public enum EventType {
    WATCH("watch"),
    LIKE("like"),
    COMMENT("comment"),
    SUBSCRIBE("subscribe");

    public final String value;

    private EventType(String v) {
      value = v;
    }
  };

  public static class BaseEventData {
    public String type;
    public String tube_id;
    public String channel_id;
    public String video_id;
  };

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Events {
    public Iterable<Tube> tubes;
    public Iterable<Channel> channels;
    public Iterable<Video> videos;
    public Iterable<BaseEventData> datas;
  };

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class WatchEventData extends BaseEventData {
    public WatchEventData() {
      type = EventType.WATCH.value;
    }
  };

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class LikeEventData extends BaseEventData {
    public LikeEventData() {
      type = EventType.LIKE.value;
    }
  };

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class SubscriptionEventData extends BaseEventData {
    public SubscriptionEventData() {
      type = EventType.SUBSCRIBE.value;
    }
  };

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class CommentEventData extends BaseEventData {
    public static class Extra {
      public String text;
      public String comment_id;
      public Extra(String eid, String t) {
        text = t;
        comment_id = eid;
      }
    }

    public CommentEventData(String eid, String txt) {
      type = EventType.COMMENT.value;
      extra = new Extra(eid, txt);
    }
    public Extra extra;
  };

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class VideoEvents {
    public int total;
    public Iterable<String> last_tubes;
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class VideoEventComments {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Comment {
      public String tube_id;
      public String text;
      public String id;
    }
    public int total;
    public Iterable<Comment> last_comments;
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class VideoInfo {
    public Iterable<Tube> tubes;

    public VideoEvents watches;
    public VideoEvents likes;
    public VideoEvents dislikes;

    public VideoEventComments comments;
  };

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class ChannelInfo {
    public Iterable<Tube> tubes;

    public VideoEvents subscriptions;
  };
}