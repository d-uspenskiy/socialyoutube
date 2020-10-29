<template>
  <ul>
    <li v-for="header in headers">
      <router-link :to="'/tubes/' + tubeId + header.route">{{header.title}}</router-link>
    </li>
  </ul>
</template>

<script lang='ts'>
  import {Vue, Component, Prop} from 'vue-property-decorator'

  type Header = {
    readonly route:string;
    readonly title:string;
  }

  export enum HeaderId {
    EVENTS = 0,
    LIKES,
    COMMENTS,
    SUBSCRIPTIONS
  }

  @Component
  export default class Comp extends Vue {
    @Prop({required: true})
    private tubeId!:string;

    @Prop({required: true})
    private activeHeader!:HeaderId;

    private headers:Header[] = [
      {route: '', title:'Events'},
      {route: '/likes', title:'Likes'},
      {route: '/comments', title:'Comments'},
      {route: '/subscriptions', title:'Subscriptions'}]; 
  };
</script>

<style scoped>
ul {
  list-style-type: none;
  margin: 0;
  padding: 0;
  overflow: hidden;
  background-color: #333333;
}

li {
  float: left;
}

li a {
  display: block;
  color: white;
  text-align: center;
  padding: 16px;
  text-decoration: none;
}

li a.router-link-exact-active {
  color: red;
}

li a:hover {
  background-color: #111111;
}
</style>
