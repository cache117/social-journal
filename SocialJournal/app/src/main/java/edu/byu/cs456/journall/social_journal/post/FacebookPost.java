package edu.byu.cs456.journall.social_journal.post;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Date;

/**
 * Created by cstaheli on 4/11/2017.
 */

@IgnoreExtraProperties
class FacebookPost extends Post {
    public String postId;

    public FacebookPost() {

    }

    protected FacebookPost(String userId, Date date, String postId) {
        super(userId, date);
        this.postId = postId;
    }
}
