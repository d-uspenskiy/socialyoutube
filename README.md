# socialytube
Your watch history says lots about you. Share it with friends!

SocialYouTube collects and structures YouTube users’ activity (watches, likes, comments, subscriptions etc) and allows to share it with friends.

Implementation of such simple idea produces the following results:
- Activity history is visible to friends. This motivates users to choose videos more carefully and be more polite in their comments.
- Friends’ watch history becomes a new source of potentially interesting videos as an alternative for YouTube’s recommendations.
- Comments and likes let users know about friend’s new interests.
- Users have no need to repost liked videos any more.This saves time and avoids negative reactions from the most irritable characters.
- User’s likes/dislikes are visible to friends. This motivates users to rate videos more often. High like/dislike activity effectively filters unwanted content.
- The new kind of bloggers is emerging. They attract an audience by searching and selecting quality content.

Service functionality is based on the SocialYouTube Chrome plugin and server backed cooperation.
Each time a user visits YouTube page on PC SocialYouTube plugin reads his/her latest activity (including activity on mobile devices) and sends it to SocialYouTube server. Server updates the user's history and sends notifications to his/her friends about new likes, comments, subscriptions etc. Also while watching YouTube videos on PC SocialYouTube plugin shows additional information about friends’ likes/dislikes comments related to current video. Plugin reads this information from the SocialYouTube server. In addition to the plugin server has its own web interface for profile, notifications and friends list management.
