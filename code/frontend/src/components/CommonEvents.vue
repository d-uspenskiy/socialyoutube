<template>
  <div>
    <b-form-select 
      @input="onTubeChanged"
      :value="tubes.active"
      :options="tubes.options"
      value-field="id"
      text-field="title"></b-form-select>
    <history-event v-for="event in events" v-bind:showTube="true" v-bind:event="event" v-bind:key="event.videoId"></history-event>
  </div>
</template>

<script lang='ts'>
  import {Vue, Component, Prop} from 'vue-property-decorator'
  import {Data} from './data.ts'
  import HistoryEventComponent from './HistoryEvent.vue'

  export interface Model {
    getTubes():Promise<Data.Tube[]>;
    getEvents(tubeId:string):Promise<Data.Event[]>;
  }

  type TubeState = {
    active:string;
    options:Data.Tube[];
  };

  @Component({
    components: {'history-event': HistoryEventComponent}
  })
  export default class Comp extends Vue {
    @Prop()
    private model!:Model;

    private tubes:TubeState = {active: "", options:[]};
    private events:Data.Event[] = [];

    created() {
      this.model.getTubes().then((tubes) => {
        if (tubes && tubes.length > 0) {
          this.tubes = {active: tubes[0].id, options: tubes};
        } else {
          this.tubes = {active: "", options: []};
        }
      });
    }

    onTubeChanged(tubeId:string) {
      this.tubes.active = tubeId;
      this.model.getEvents(tubeId).then((events) => this.events = events);
    }
  };
</script>