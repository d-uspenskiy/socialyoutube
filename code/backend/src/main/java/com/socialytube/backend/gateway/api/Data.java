package com.socialytube.backend.gateway.api;

import java.util.List;

public class Data {
  public static class Thumbnail {
    public String url;
    public int    width;
    public int    height;
  };

  public static class Channel {
    public String    id;
    public Thumbnail thumbnail;
    public String    title;
  };

  public static class Video {
    public List<Thumbnail> thumbnails;
    public String          id;
    public int             duration;
    public String          title;
    public String          channel_id;
  };
  
  public static class Comment {
    public String id;
    public String video_id;
    public String text;
  };

  public static class Subscription {
    public String          channel_id;
    public List<Thumbnail> thumbnails;
    public String          title;
  };

  public static class SelfInfo {
    public String          channel_id;
    public String          title;
    public List<Thumbnail> thumbnails;
  }

  public static class BaseData {
    public SelfInfo self;
    public long     hash;
    public long     version;
  };

  public static class HistoryOld extends BaseData {
    public List<Video> data;
  };

  public static class History extends BaseData {
    public static class DataContainer {
      public List<Channel> channel;
      public List<Video>   video;
      public List<String>  watch;
      public List<String>  like;
      public int           fresh_watch_count;
    }
    public DataContainer data;
  };

  public static class LikeState extends BaseData {};

  public static class Subscriptions extends BaseData {
    public List<Subscription> data;
  };

  public static class Comments extends BaseData {
    public List<Comment> data;
  };

  public static class Likes extends BaseData {
    public List<Video> data;
  };
}