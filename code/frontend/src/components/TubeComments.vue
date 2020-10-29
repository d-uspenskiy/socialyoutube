<template>
  <div>
    <tube-header v-bind:tubeId="tubeId" v-bind:activeHeader="activeHeader"></tube-header>
    <history-event v-for="comment in comments" v-bind:showTube="false" v-bind:event="comment" v-bind:key="comment.videoId"></history-event>
  </div>
</template>

<script lang='ts'>
  import {Vue, Component, Prop} from 'vue-property-decorator'
  import HistoryEventComponent from './HistoryEvent.vue'
  import TubeHeader from './TubeHeader.vue'
  import {HeaderId} from './TubeHeader.vue'
  import {Data} from './data.ts'

  export interface Model {
    getComments(tubeId:string):Promise<Data.Event[]>;
  }

  @Component({
    components: {
      'tube-header': TubeHeader,
      'history-event': HistoryEventComponent
    }
  })
  export default class Comp extends Vue {
    @Prop({required: true})
    private tubeId!:string;
    @Prop({required: true})
    private model!:Model;

    private comments:Data.Event[] = [];

    private readonly activeHeader:HeaderId = HeaderId.COMMENTS;

    created() {
      this.model.getComments(this.tubeId).then((events) => {this.comments = events});
    }
  }
</script>