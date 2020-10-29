<template>
  <div>
    <tube-header v-bind:tubeId="tubeId" v-bind:activeHeader="activeHeader"></tube-header>
    <history-event v-for="subscription in subscriptions" v-bind:showTube="false" v-bind:event="subscription" v-bind:key="subscription.channel.id"></history-event>
  </div>
</template>

<script lang='ts'>
  import {Vue, Component, Prop} from 'vue-property-decorator'
  import HistoryEventComponent from './HistoryEvent.vue'
  import TubeHeader from './TubeHeader.vue'
  import {HeaderId} from './TubeHeader.vue'
  import {Data} from './data.ts'

  export interface Model {
    getSubscriptions(tubeId:string):Promise<Data.Event[]>;
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

    private subscriptions:Data.Event[] = [];

    private readonly activeHeader:HeaderId = HeaderId.SUBSCRIPTIONS;

    created() {
      this.model.getSubscriptions(this.tubeId).then((events) => {this.subscriptions = events});
    }
  }
</script>