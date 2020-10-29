var PARSER_VERSION = 1;
var DEBUG = true;
var LOG = DEBUG ? console.log.bind(console) : function() {};

function dataResponse(data) {
  return data[1].response
}

function responseContent(data) {
  return dataResponse(data).contents;
}

function sectionsContent(data) {
  return responseContent(data).twoColumnBrowseResultsRenderer.tabs[0].tabRenderer.content.sectionListRenderer.contents
}

function reloadRequired(data) {
  LOG("realoadRequired", data);
  return data.hasOwnProperty("reload");
}

function endpointUrl(ep) {
  return ep.commandMetadata.webCommandMetadata.url;
}

function extractParams(url) {
  var params = {};
  for(var p of url.split("?")[1].split("&")) {
    var part = p.split("=");
    params[part[0]] = part[1];
  }
  return params;
}

function processBaseContentEx(data, processor) {
  var section_idx = 0;
  for (var s of sectionsContent(data)) {
    for (var i of safeGet(s, "itemSectionRenderer", {contents:[]}).contents) {
      processor(i, section_idx);
    }
    ++section_idx;
  }
}

function processBaseContent(data, itemParser) {
  var items = [];
  processBaseContentEx(data, it => pushNotNull(items, itemParser(it)));
  return items;
}

function extractVideoTitle(title) {
  return title.runs ? title.runs[0].text : title.simpleText;
}

function parseDuration(dur) {
  var res = 0;
  for (var p of dur.split(":")) {
    res = res * 60 + parseInt(p);
  }
  return res;
}

function parseChannelInfo(item) {
  var info = item.ownerText.runs[0];
  return {id: info.navigationEndpoint.browseEndpoint.browseId,
          title: info.text,
          thumbnail: item.channelThumbnailSupportedRenderers.channelThumbnailWithLinkRenderer.thumbnail.thumbnails[0]};
}

function parseVideoItem(item) {
  console.log(item);
  return (item.lengthText != null) ?
    {
      thumbnails: item.thumbnail.thumbnails,
      title: extractVideoTitle(item.title),
      duration: parseDuration(item.lengthText.simpleText),
      id: item.videoId
    } : null;
}

function parseHistoryItemOld(uniqueIds, raw_item) {
  var item = raw_item.videoRenderer;
  var wdur = item.navigationEndpoint.watchEndpoint.startTimeSeconds;
  if (wdur == null) {
    for (var o of item.thumbnailOverlays) {
      if (safeGet(safeGet(o, "thumbnailOverlayResumePlaybackRenderer", {}), "percentDurationWatched", 100) != 100) {
        return null;
      }
    }
  } else if (wdur < 60) {
    return null;
  }
  var r = parseVideoItem(item);
  if (r && trueAdd(uniqueIds, r.id)) {
    r.channel = parseChannelInfo(item);
    return r;
  }
  return null;
}

function parseHistoryItem(raw_item, ) {
  var item = raw_item.videoRenderer;
  var short = false;
  var wdur = item.navigationEndpoint.watchEndpoint.startTimeSeconds;
  if (wdur == null) {
    for (var o of item.thumbnailOverlays) {
      if (safeGet(safeGet(o, "thumbnailOverlayResumePlaybackRenderer", {}), "percentDurationWatched", 100) != 100) {
        short = true;
        break;
      }
    }
  } else if (wdur < 60) {
    short = true;
  }
  var r = parseVideoItem(item);
  if (r) {
    r.channel = parseChannelInfo(item);
    if (short) {
      r.short = true;
    }
  }
  return r;
}

function parseCommentItem(raw_item) {
  var rend = raw_item.commentHistoryEntryRenderer;
  var txt = "";
  for (var r of rend.content.runs) {
    txt += r.text;
  }
  var params = extractParams(endpointUrl(rend.summary.runs[0].navigationEndpoint));
  return {text: txt, video_id: params.v, id: params.lc};
}

function parseHistoryOld(data) {
  LOG("parseHistoryOld", data);
  return processBaseContent(data, parseHistoryItemOld.bind(null, new Set()));
}

function parseHistory(data) {
  LOG("parseHistory", data);
  var items = [];
  var fresh_count = null;
  processBaseContentEx(data, (it, section) => {
    if (fresh_count === null && section == 2) {
      fresh_count = items.length;
    }
    pushNotNull(items, parseHistoryItem(it));
  });
  return {items: items, fresh: fresh_count};
}

function parseComments(data) {
  LOG("parseComments", data);
  return processBaseContent(data, parseCommentItem);
}

function parseLikes(data) {
  LOG("parseLikes", data);
  return parseVideoList(data);
}

function parseLikeState(data) {
  //i.response.contents.twoColumnWatchNextResults.results.results.contents[0].videoPrimaryInfoRenderer.sentimentBarRenderer.likeStatus
  //dateText
  LOG("parseLikeState", data);
  var found = false;
  for (var i of data) {
    try {
      found = true;
      for (var b of i.response.contents.twoColumnWatchNextResults.results.results.contents[0].videoPrimaryInfoRenderer.videoActions.menuRenderer.topLevelButtons) {
        var br = b.toggleButtonRenderer;
        if (br && br.isToggled) {
          LOG(br);
          switch(br.defaultServiceEndpoint.likeEndpoint.status) {
            case 'LIKE': return 1;
            case 'DISLIKE': return -1; 
          }
        }
      }
    } catch(e) {
    }
  }
  if (!found) {
    throw new Error("No state found");
  }
  return 0;
}

function channelInfo(id, name, thumbnails) {
  return {channel_id: id, title: name, thumbnails: thumbnails};
}

function parseSubscriptionsShelf(shelf) {
  var result = []
  for (var i of shelf.shelfRenderer.content.expandedShelfContentsRenderer.items) {
    var item = i.channelRenderer;
    result.push(channelInfo(item.channelId, item.title.simpleText, item.thumbnail.thumbnails));
  }
  return result;
}

function parseSubscriptions(data) {
  LOG("parseSubscriptions", data);
  return processBaseContent(data, parseSubscriptionsShelf)[0];
}

function parseVideoList(data) {
  LOG("parseVideoList", data);
  var items = [];
  for (var v of sectionsContent(data)[0].itemSectionRenderer.contents[0].playlistVideoListRenderer.contents) {
    var info = v.playlistVideoRenderer;
    var item = parseVideoItem(info);
    if (item) {
      item.channel = {id: info.shortBylineText.runs[0].navigationEndpoint.browseEndpoint.browseId, title: "UNKNOWN"};
      items.push(item);
    }
  }
  return items;
}

function parseSelfInfo(data) {
  LOG("parseSelfInfo");
  var r = dataResponse(data).header.c4TabbedHeaderRenderer;
  return channelInfo(r.channelId, r.title, r.avatar.thumbnails);
}