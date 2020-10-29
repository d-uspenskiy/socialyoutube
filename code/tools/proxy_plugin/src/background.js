var LOG = console.log.bind(console)
var BACKEND_HOST = "localhost:1880";
var BACKEND_URL = `http://${BACKEND_HOST}`;
var LOGIN_RESPONSE_PAGE = `${BACKEND_HOST}/login/response.html`;

class WSConnector {
  constructor(hnd) {
    this.hnd_ = hnd;
    this.socket_ = new WebSocket("ws://localhost:8000/ws");
    this.socket_.onmessage = this.onMessage_.bind(this);
    this.socket_.onclose = this.onClose_.bind(this);
    this.socket_.onopen = this.onOpen_.bind(this);
  }

  send(data) {
      this.socket_.send(data);
  }

  onMessage_(event) {
      this.hnd_(event.data);
  }

  onOpen_() {
      LOG("WSConnector::onOpen_");
  }

  onClose_() {
      LOG("WSConnector::onClose_");
  }
}

class ExtendedHistoryProcessor {
  constructor(hnd) {
    this.hnd_ = hnd;
    this.history_ = null;
    this.likes_ = null;
  }

  fire(requester) {
    requester('/feed/history?pbj=1').then(data => {
      this.history_ = parseHistory(data);
      this.process_();
    });
    requester('/playlist?list=LL&pbj=1').then(data => {
      this.likes_ = parseLikes(data);
      this.process_();
    });
  }

  process_() {
    if (this.history_ && this.likes_) {
      LOG(this.history_);
      LOG(this.likes_);
      var channels = {};
      var videos = {};
      var watch = [];
      var like = [];
      var add_video = function(v) {
        if (!(v.id in videos)) {
          var c = v.channel;
          videos[v.id] = {
            id: v.id,
            thumbnails: v.thumbnails,
            title: v.title,
            duration: v.duration,
            channel_id: c.id
          };
          if (!(c.id in channels)) {
            channels[c.id] = c;
          }
        }
      };
      for (var h of this.history_.items) {
        add_video(h);
        watch.push(h.id);
      }
      for (var l of this.likes_) {
        add_video(l);
        like.push(l.id);
      }
      this.hnd_({
        channel: Object.values(channels),
        video: Object.values(videos),
        watch: watch,
        like: like,
        fresh_watch_count: this.history_.fresh
      });
    }
  }
}

class LikeStateProcessor {
  constructor(requester) {
    this.requester_ = requester;
  }

  process(videos) {
    var data = {likes:[], dislikes:[], neutrals:[]};
    var that = this;
    return new Promise((hnd) => {
      function impl(idx, rsp) {
        if (idx > -1) {
          var dest = data.neutrals;
          var ls = parseLikeState(rsp);
          LOG(ls);
          switch(ls) {
            case -1: dest = data.likes; break;
            case 1: dest = data.dislikes; break;
          }
          dest.push(videos[idx]);
        }
        if (++idx < videos.length) {
          that.requester_(`/watch?v=${videos[idx]}&pbj=1`).then((r) => impl(idx, r));
        } else {
          hnd(data);
        }
      }
      impl(-1);
    });
  }
}

class Core {
  constructor(bootstrap) {
    LOG("Core::Core");
    this.token_ = null;
    this.pending_ = [];
    this.bootstrap_ = bootstrap;
    this.rest_ = new REST(BACKEND_URL);
    var serializer = new JSONSerializer([RemoteError, RpcMessage, InvokePayload, Object]);
    this.rpc_ = new Rpc((rpc_hnd) => {
      return new Endpoint((endpoint_hnd) => {
        this.fromPageHnd_ = endpoint_hnd;
        return {send: this.sendToPage_.bind(this)};
      }, rpc_hnd, serializer);
    });
    this.iface_ = this.rpc_.stub(InjectedIface);
    this.ws_ = new WSConnector(this.wsMessage_.bind(this));
    this.port_ = null;
    this.selfInfo_ = null;
    chrome.tabs.onActivated.addListener(
      (info) => chrome.tabs.get(info.tabId, 
                               (tab) => this.checkTab_(tab)));

    chrome.tabs.onUpdated.addListener((tId, changeInf, tab) => {
      if (safeGet(changeInf, "status")) {
        this.checkTab_(tab);
      }
    });

    chrome.runtime.onConnect.addListener(this.onConnect.bind(this));

    chrome.tabs.query({url: "*://www.youtube.com/*", status: "complete"}, (tabs) => {
      for (var t of tabs) {
        this.checkTab_(t);
      }
    });
    chrome.storage.local.get('token', (items) => {
      this.token_ = safeGet(items, 'token');
      console.log("token loaded", this.token_);
    });
  }

  onConnect(port) {
    LOG("Core::onConnect", port);
    port.onMessage.addListener(this.pageMessage_.bind(this, port));
    port.onDisconnect.addListener(() => {
      console.log("port is reseted");
      this.port_ = null;
      this.selfInfo_ = null;
    })
    this.port_ = port;
    this.iface_.getValue().then((info) => {
      if (info) {
        this.setSelfInfo_(info);
      } else {
        this.getUrl_("/feed/my_videos?pbj=1").then((rsp) => {
          var info = parseSelfInfo(rsp);
          this.iface_.setValue(info);
          this.setSelfInfo_(info);
        });
      }
    });
  }

  setSelfInfo_(info) {
    console.log("Self info is set", info);
    this.selfInfo_ = info;
    for (var cmd of this.pending_) {
      cmd();
    }
    this.pending_ = [];
  }

  wsMessage_(data) {
    data = JSON.parse(data);
    LOG("Core::wsMessage_", data.cmd);
    var cmds = data.cmd.split(" ");
    var command = cmds[0];
    var responder = this.defaultResponseHandler_.bind(this, data.req_id, command);
    switch(command) {
      case 'history':
        this.processRequest_(this.defaultResponseHandler_.bind(this, data.req_id, "history_old"), '/feed/history?pbj=1', parseHistoryOld);
        break;
      case 'comments':
        this.processRequest_(responder, '/feed/history/comment_history?pbj=1', parseComments);
        break;
      case 'likes':
        this.processRequest_(responder, '/playlist?list=LL&pbj=1', parseLikes);
        break;
      case 'subscriptions':
        this.processRequest_(responder, '/feed/channels?pbj=1', parseSubscriptions);
        break;
      case 'like_state': {
        new LikeStateProcessor(this.getUrl_.bind(this)).process(cmds.splice(1)).then(
          (rsp) => {
            LOG("like_state", data);
            this.defaultResponseHandler_(data.req_id, 'like_state', rsp);
          });
        }
        break;
      case 'login':
        chrome.tabs.query({active: true}, (tabs) => {
          var active_tid = tabs.length ? tabs[0].id : -1;
          chrome.tabs.create({ url: `${BACKEND_URL}/login/index.html?state=` + encodeURIComponent(`active|${active_tid}`)});
        });
        break;
      case 'ehistory':
        new ExtendedHistoryProcessor(resp => {
          var hash = 0;
          for(var i of chain(resp.watch, resp.like)) {
            hash = murmurhash3_32_gc(i, hash);
          }
          this.defaultResponseHandlerImpl_(data.req_id, "history", resp, hash);
        }).fire(url => this.getUrl_(url));
        break;
      default:
        this.sendResponse_(data.req_id, `Unknown command '${data.cmd}'`);
        break;
    }
  }

  processRequest_(responder, url, processor) {
    var cmd = () => this.getUrl_(url).then((data) => responder(processor(data)));
    if (this.selfInfo_ == null) {
      this.pending_.push(cmd);
    } else {
      cmd();
    }
  }

  processError_(req_id, rsp) {
    return false;
    console.log("processError_", rsp);
    if (rsp.exc) {
      this.sendToBackend_('/error', rsp);
    }
    if (rsp.exc || rsp.reloaded) {
      this.sendResponse_(req_id, rsp.exp || "RELOAD");
      return true;
    }
    return false;
  }

  defaultResponseHandlerImpl_(req_id, endpoint, response, hash) {
    if (!this.processError_(req_id, response)) {
      this.sendToBackend_(`/${endpoint}`,
                         {hash:hash, self: this.selfInfo_, version: 1, data: response});
      var data = {};
      data[endpoint] = response;
      this.sendResponse_(req_id, data);
    }
  }

  defaultResponseHandler_(req_id, endpoint, response) {
    this.defaultResponseHandlerImpl_(req_id, endpoint, response, Math.floor(Math.random() * 1000000));
  }

  sendToBackend_(path, data) {
    this.rest_.send(
      null, "POST", `/backend/gateway/yt${path}`, "application/json",
      JSON.stringify(data), {Authorization: `Bearer ${this.token_}`});
  }

  sendResponse_(req_id, rsp) {
    this.ws_.send(JSON.stringify({req_id: req_id, resp: JSON.stringify(rsp)}));
  }

  sendToPage_(msg) {
    console.log("sendToPage_", msg);
    this.port_.postMessage(msg);
  }

  pageMessage_(port, msg) {
    LOG("Core::pageMessage_", msg);
    this.fromPageHnd_(msg);
  }

  inject_(tid) {
    LOG("inject_", tid, safeLen(this.bootstrap_));
    callInjectedCode(tid,
                     this.bootstrap_,
                     (res) => console.log(res[0] ? "NEW INJECTION" : "ALREADY INJECTED"));
  }

  checkTab_(tab) {
    if (tab.status == "complete") {
      if (safeStr(tab.url).includes(LOGIN_RESPONSE_PAGE)) {
        var url = new URL(tab.url);
        if ((url.host + url.pathname) == LOGIN_RESPONSE_PAGE) {
          var sp = new URLSearchParams(url.hash.substr(1));
          this.token_ = sp.get("id_token");
          chrome.storage.local.set({token: this.token_});
          console.log("Token updated", this.token_);
          var state = safeStr(sp.get("state")).split("|");
          if (state.length == 2 && state[0] == "active") {
            var close_tid = tab.id;
            var active_tid = parseInt(state[1]);
            setTimeout(() => {
              chrome.tabs.update(active_tid, {active: true});
              chrome.tabs.remove(close_tid);
            });
          }
          return;
        }
      }

      if (safeStr(tab.url).includes("://www.youtube.com/")) {
        LOG("Found on", tab.id);
        this.inject_(tab.id);
      }
    }
  }

  getUrl_(path) {
    LOG("Requesting", path);
    return new Promise((hnd) => {
      this.iface_.get(path).then((data) => {
        LOG("response length", data.length);
        var obj = null;
        try {
          obj = JSON.parse(data);
        } catch (e) {
          LOG(e);
        }
        hnd(obj == null || reloadRequired(obj) ? null : obj);
      });
    });
  }
}

var core = null;

function initCore(datas) {
  if (safeLen(datas) > 0) {
    var boot_data = [datas[2], datas.splice(datas.length - 1, 1)[0]].join("\n");
    var content_data = datas.join("\n").replace(/"/g, '\\"').replace(/\n/g, '\\n');
    var log_func = "var LOG=console.log.bind(console);";
    var bootstrap = `function NAMESPACE() {
      ${log_func}
      var BACKEND_URL="${BACKEND_URL}";
      var CONTENT_DATA="${log_func}\\n${content_data}";
      ${boot_data}
    }
    NAMESPACE();`;
    core = new Core(bootstrap);
  }
}

joinFiles(
  ["./jst/tools.js",
   "./jst/json_serializer.js",
   "./jst/pipe_communicator.js",
   "./jst/rpc.js",
   "./jst/endpoint.js",
   "./injections/iface.js",
   "./injections/injection.js",
   "./injections/bootstrap.js"],
  initCore);
