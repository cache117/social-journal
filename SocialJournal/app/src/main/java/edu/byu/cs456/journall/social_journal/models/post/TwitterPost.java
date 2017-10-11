package edu.byu.cs456.journall.social_journal.models.post;

import com.twitter.sdk.android.core.models.Tweet;

import java.text.ParseException;
import java.util.Date;

/**
 * A wrapper for Tweets to make them easier to store and retrieve in firebase.
 */

public class TwitterPost extends Post {
    public Tweet tweet;

    public TwitterPost(String userId, Date date, Tweet tweet) throws ParseException {
        super(userId, date);
        this.tweet = tweet;
    }
}
