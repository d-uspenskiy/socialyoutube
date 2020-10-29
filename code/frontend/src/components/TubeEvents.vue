<template>
  <div>
    <tube-header v-bind:tubeId="tubeId" v-bind:activeHeader="activeHeader"></tube-header>
    <history-event v-for="event in events" v-bind:showTube="false" v-bind:event="event" v-bind:key="event.videoId"></history-event>
  </div>
</template>

<script lang='ts'>
  import {Vue, Component, Prop} from 'vue-property-decorator'
  import HistoryEventComponent from './HistoryEvent.vue'
  import TubeHeader from './TubeHeader.vue'
  import {HeaderId} from './TubeHeader.vue'
  import {Data} from './data.ts'

  export interface Model {
    getEvents(tubeId:string):Promise<Data.Event[]>;
  }

  @Component({
    components: {
      'history-event': HistoryEventComponent,
      'tube-header': TubeHeader
    }
  })
  export default class Comp extends Vue {
    @Prop({required: true})
    private tubeId!:string;
    @Prop({required: true})
    private model!:Model;

    private events:Data.Event[] = [];

    private readonly activeHeader:HeaderId = HeaderId.EVENTS;

    created() {
      this.model.getEvents(this.tubeId).then((events) => this.events = events);
    }
  }
</script>