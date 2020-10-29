
import Vue from 'vue'
import VueRouter from 'vue-router'
import BootstrapVue from 'bootstrap-vue'
import axios from 'axios'
import {Data} from './components/data.ts'
import CommonEventsComponent from './components/CommonEvents.vue'
import {Model as CEModel} from './components/CommonEvents.vue'
import TubeEventsComponent from './components/TubeEvents.vue'
import {Model as TEModel} from './components/TubeEvents.vue'
import TubeCommentsComponent from './components/TubeComments.vue'
import {Model as TCModel} from './components/TubeComments.vue'
import TubeLikesComponent from './components/TubeLikes.vue'
import {Model as TLModel} from './components/TubeLikes.vue'
import TubeSubscriptionsComponent from './components/TubeSubscriptions.vue'
import {Model as TSModel} from './components/TubeSubscriptions.vue'
import VideoComponent from './components/Video.vue'
import {Model as VModel} from './components/Video.vue'

Vue.use(BootstrapVue);
Vue.use(VueRouter);

var HOST = "http://localhost:1880";
var TOKEN_KEY = "token";

var url = window.location;
var search = url.search.substr(1);
console.log(search);
var access_token = new URLSearchParams(search).get('id_token');
if (!access_token) {
  access_token = window.localStorage.getItem(TOKEN_KEY);
} else {
  window.localStorage.setItem(TOKEN_KEY, access_token);
}
if (!access_token) {
  var redirect = encodeURIComponent(`redirect|${url.href}?id_token=`);
  window.location.href = `${HOST}/login/index.html?state=${redirect}`;
}

function getJson(url:string) {
  return axios.get(`${HOST}/backend/${url}`, 
                   {headers: {'Authorization' : `Bearer ${access_token}`}});
}

function formDuration(dur:number):string {
  var result = '';
  for (var i = 0; i < 2; ++i) {
    var v = dur % 60;
    var d = Math.floor(dur / 60);
    result = (v > 9 || (!d && i) ? v.toString() : `0${v}`) + (result.length ? ':' : '') + result;
    dur = d;
  }
  return (dur ? `${dur}:`: '') + result;
}

class RootModel {
  getTubes(): Promise<Data.Tube[]> {
    return new Promise<Data.Tube[]>(hnd => {
      getJson('/public/tubes').then((response:any) => {
        var tubes:Data.Tube[] = [];
        for (var o of response.data) {
          tubes.push({id: o.id, title: o.title ? o.title : o.id});
        }
        hnd(tubes);
      });
    });
  }

  getEvents(tubeId:string): Promise<Data.Event[]> {
    return this.eventsHelper(`/public/tubes/${tubeId}/events`);
  }

  getComments(tubeId:string): Promise<Data.Event[]> {
    return this.eventsHelper(`/public/tubes/${tubeId}/comments`);
  }

  getSubscriptions(tubeId:string): Promise<Data.Event[]> {
    return this.eventsHelper(`/public/tubes/${tubeId}/subscriptions`);
  }

  getLikes(tubeId:string): Promise<Data.Event[]> {
    return this.eventsHelper(`/public/tubes/${tubeId}/likes`);
  }

  getVideoInfo(videoId:string): Promise<Data.VideoInfo> {
    return new Promise<Data.VideoInfo>(hnd => {
      getJson(`/info/video/${videoId}`).then((response:any) => {
        hnd(response.data);
      })
    })
  }

  private eventsHelper(url:string): Promise<Data.Event[]>{
    return new Promise<Data.Event[]>(hnd => {
      getJson(url).then((response:any) => {
        var events:Data.Event[] = [];
        var d = response.data;
        var channels:{[key:string]:Data.Channel} = {};
        for (var c of d.channels) {
          var thmb: any = null;
          for (var t of c.thumbnails || []) {
            if (!thmb || thmb.width == 68) {
              thmb = t;
              break;
            }
          }
          channels[c.external_id] = {id: c.external_id, title: c.title, thumbnailUrl: thmb && thmb.url};
        }
        var tubes:{[key:string]:Data.Tube} = {};
        for (var t of d.tubes) {
          tubes[t.id] = {id: t.id, title: t.title};
        }
        var videos:{[key:string]:Data.Video} = {};
        for (var v of d.videos) {
        var thmb: any = null;
          for (var t of v.thumbnails || []) {
            if (!thmb || thmb.width == 246) {
              thmb = t;
              break;
            }
          }
          videos[v.external_id] = {id: v.external_id, title: v.title || '--unnamed--', thumbnailUrl: thmb && thmb.url, channel: channels[v.channel_id], duration:formDuration(v.duration)};
        }
        for (var e of d.datas) {
          var extra = undefined;
          if (e.extra) {
            extra = { text: e.extra.text, id: e.extra.comment_id };
          }
          var vid = videos[e.video_id];
          events.push({
            video: vid,
            tube: tubes[e.tube_id],
            channel: (vid && vid.channel) || channels[e.channel_id],
            type: e.type,
            extra: extra
          });
        }
        hnd(events);
      });
    });
  }
};

class CommonEventsModel implements CEModel {
  private impl:RootModel;

  constructor(impl:RootModel) {
    this.impl = impl;
  }

  getTubes(): Promise<Data.Tube[]> {
    return this.impl.getTubes();
  }

  getEvents(tubeId:string): Promise<Data.Event[]> {
    return this.impl.getEvents(tubeId);
  }
};

class TubeEventsModel implements TEModel {
  private impl:RootModel;

  constructor(impl:RootModel) {
    this.impl = impl;
  }

  getEvents(tubeId:string): Promise<Data.Event[]> {
    return this.impl.getEvents(tubeId);
  }
};

class TubeCommentsModel implements TCModel {
  private impl:RootModel;

  constructor(impl:RootModel) {
    this.impl = impl;
  }

  getComments(tubeId:string): Promise<Data.Event[]> {
    return this.impl.getComments(tubeId);
  }
};

class TubeLikesModel implements TLModel {
  private impl:RootModel;

  constructor(impl:RootModel) {
    this.impl = impl;
  }

  getLikes(tubeId:string): Promise<Data.Event[]> {
    return this.impl.getLikes(tubeId);
  }
};

class TubeSubscriptionsModel implements TSModel {
  private impl:RootModel;

  constructor(impl:RootModel) {
    this.impl = impl;
  }

  getSubscriptions(tubeId:string): Promise<Data.Event[]> {
    return this.impl.getSubscriptions(tubeId);
  }
};

class VideoModel implements VModel {
  private impl:RootModel;

  constructor(impl:RootModel) {
    this.impl = impl;
  }

  getInfo(videoId:string): Promise<Data.VideoInfo> {
    return this.impl.getVideoInfo(videoId);
  }
}

var rootModel = new RootModel();

const router = new VueRouter({
  mode: 'history',
  routes: [
    {path: '/', component: CommonEventsComponent, props: {model: new CommonEventsModel(rootModel)}},
    {path: '/tubes/:tubeId', component: TubeEventsComponent, props: (route) => {return {tubeId: route.params.tubeId, model: new TubeEventsModel(rootModel)}}},
    {path: '/tubes/:tubeId/comments', component: TubeCommentsComponent, props: (route) => {return {tubeId: route.params.tubeId, model: new TubeCommentsModel(rootModel)}}},
    {path: '/tubes/:tubeId/likes', component: TubeLikesComponent, props: (route) => {return {tubeId: route.params.tubeId, model: new TubeLikesModel(rootModel)}}},
    {path: '/tubes/:tubeId/subscriptions', component: TubeSubscriptionsComponent, props: (route) => {return {tubeId: route.params.tubeId, model: new TubeSubscriptionsModel(rootModel)}}},
    {path: '/watch', component: VideoComponent, props: (route) => {return {videoId: route.query.v, model: new VideoModel(rootModel)}}}
  ]
});

var app = new Vue({
  router: router,
  el: '#app'
});