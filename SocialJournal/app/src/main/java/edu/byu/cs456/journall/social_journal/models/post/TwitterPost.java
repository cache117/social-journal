package edu.byu.cs456.journall.social_journal.models.post;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Date;

/**
 * A wrapper for Tweets to make them easier to store and retrieve in firebase.
 */

@IgnoreExtraProperties
@SuppressWarnings({"unused", "WeakerAccess"})
public class TwitterPost extends Post {
    public long tweetId;

    public TwitterPost() {
    }

    public TwitterPost(String userId, Date date, long tweetId) {
        super(userId, date);
        this.tweetId = tweetId;
    }
}
