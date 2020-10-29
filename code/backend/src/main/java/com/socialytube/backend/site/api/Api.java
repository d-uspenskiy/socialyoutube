package com.socialytube.backend.site.api;

import com.socialytube.backend.site.api.Data.Account;
import com.socialytube.backend.site.api.Data.Events;
import com.socialytube.backend.site.api.Data.Tube;
import com.socialytube.backend.site.api.Data.ChannelInfo;
import com.socialytube.backend.site.api.Data.VideoInfo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

public class Api {
  public interface Base {
    @GetMapping("/public/events")
    Events commonEvents();

    @GetMapping("/events")
    Events selfEvents();

    @GetMapping("/self")
    Account selfInfo();

    @GetMapping("/auth")
    void auth();
  }

  public interface TubeSpecific {
    @GetMapping("/public/tubes")
    Iterable<Tube> tubesList() throws Exception;

    @GetMapping({"/public/tubes/{tube_public_id}",
                 "/tubes/{tube_public_id}"})
    Tube tube(@PathVariable("tube_public_id") String tpid) throws Exception;

    @GetMapping({"/public/tubes/{tube_public_id}/events",
                 "/tubes/{tube_public_id}/events"})
    Events tubeEvents(@PathVariable("tube_public_id") String tpid);

    @GetMapping({"/public/tubes/{tube_public_id}/likes",
                 "/tubes/{tube_public_id}/likes"})
    Events tubeLikes(@PathVariable("tube_public_id") String tpid);

    @GetMapping({"/public/tubes/{tube_public_id}/comments",
                 "/tubes/{tube_public_id}/comments"})
    Events tubeComments(@PathVariable("tube_public_id") String tpid);

    @GetMapping({"/public/tubes/{tube_public_id}/subscriptions",
                 "/tubes/{tube_public_id}/subscriptions"})
    Events tubeSubscriptions(@PathVariable("tube_public_id") String tpid);
  }

  public interface ExternalInfo {
    @GetMapping({"/info/video/{ext_video_id}"})
    VideoInfo extInfoVideo(@PathVariable("ext_video_id") String evid);

    @GetMapping({"/info/channel/{ext_channel_id}"})
    ChannelInfo extInfoChannel(@PathVariable("ext_channel_id") String ecid);
  }
}
