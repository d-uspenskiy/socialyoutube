<template>
  <div class='event-item'>
    <a v-if='event.video' :href="'http://www.youtube.com/watch?v=' + event.video.id" class='image-container'>
      <img :src="event.video.thumbnailUrl">
      <span>{{ event.video.duration }}</span>
    </a>
    <router-link v-if='event.video' :to="'/watch?v=' + event.video.id" class='header'>{{ event.video.title }}</router-link>
    <br>
    <a :href="'http://www.youtube.com/channel/' + event.channel.id"> {{ event.channel.title }}</a>
    <div v-if='event.video && event.extra'>
      <br>
      {{ event.extra.text }}
      <a :href="'http://www.youtube.com/watch?v=' + event.video.id + '&lc=' + event.extra.id">Link</a>
    </div>
    <div v-if='showTube'>
      <br>
      <router-link :to="'/tubes/' + event.tube.id">{{ event.tube.title }}</router-link>
    </div>
  </div>
</template>

<script lang='ts'>
  import {Vue, Component, Prop} from 'vue-property-decorator'
  import {Data} from './data.ts'

  @Component
  export default class Comp extends Vue {
    @Prop({required: true})
    private event!:Data.Event;

    @Prop({required: true})
    private showTube!:boolean;
  };
</script>

<style scoped>
  .event-item a {
    text-decoration: none;
    display: table;
  }

  .event-item {
    clear: both;
    float: none;
    padding-top: 10px;
  }

  .event-item .header {
    font-weight: bold;
  }

  .image-container {
    position: relative;
    vertical-align: top;
    float: left;
  }

  .image-container span {
    font-weight: normal;
    position: absolute;
    bottom: 0px;
    right: 0px;
    color: white;
    background: rgba(0, 0, 0, 0.7);
    padding-left: 3px;
    padding-right: 3px;
    font-size: 12px;
    margin: 2px;
  }
</style>
