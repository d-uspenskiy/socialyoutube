export namespace Data {
  export type Tube = {
    readonly id:string;
    readonly title:string;
  }

  export type Channel = {
    readonly id:string;
    readonly title:string;
    readonly thumbnailUrl:string;
  }

  export type Video = {
    readonly id:string;
    readonly title:string;
    readonly thumbnailUrl:string;
    readonly duration:string;
    readonly channel:Channel;
  }

  export type EventExtra = {
    readonly text:string;
    readonly id:string;
  }

  export type Event = {
    readonly type:string;
    readonly channel?:Channel;
    readonly tube:Tube;
    readonly video:Video;
    readonly extra?:EventExtra;
  }

  export type VideoEventData = {
    readonly total?:number;
    readonly last_tubes?:string[];
  }

  export type Comment = {
    readonly id:string;
    readonly text:string;
    readonly tube_id:string;
  }

  export type VideoCommentData = {
    readonly total?:number;
    readonly last_comments?:Comment[];
  }

  export type VideoInfo = {
    readonly tubes:Tube[];
    readonly watches:VideoEventData;
    readonly likes:VideoEventData;
    readonly dislikes:VideoEventData;
    readonly comments:VideoCommentData;
  }
}