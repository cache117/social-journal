package edu.byu.cs456.journall.social_journal.post;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Date;

/**
 * Created by cstaheli on 4/17/2017.
 */

@IgnoreExtraProperties
public class WebPost extends Post {
    public String url;

    public WebPost() {
    }

    public WebPost(String userId, Date date, String url) {
        super(userId, date);
        this.url = url;
    }

    @Override
    public String toString() {
        return url;
    }
}
