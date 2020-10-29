<template>
  <div>
    <iframe width="560" height="315" :src="'https://www.youtube.com/embed/' + videoId"></iframe>
    <br>
    <span>Watch: {{ info.watches.total }}</span>
    <span>Likes: {{ info.likes.total }}</span>
    <span>Dislikes: {{ info.dislikes.total }}</span>
    <span>Comments: {{ info.comments.total }}</span>
  </div>
</template>

<script lang='ts'>
  import {Vue, Component, Prop} from 'vue-property-decorator'
  import {Data} from './data.ts'

  export interface Model {
    getInfo(videoId:string):Promise<Data.VideoInfo>;
  }


  @Component
  export default class Comp extends Vue {
    @Prop({required: true})
    private model!:Model;
    @Prop({required: true})
    private videoId!:string;

    private info:Data.VideoInfo = {
      tubes: [], watches: {}, likes: {}, dislikes: {}, comments: {}
    };

    created() {
      this.model.getInfo(this.videoId).then((info) => this.info = info);
    }
  };
</script>