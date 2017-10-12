package edu.byu.cs456.journall.social_journal.models.post;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Date;

/**
 * A class for a WebView version of Facebook Posts
 *
 * @deprecated Facebook now has it's own class for this, FacebookPost. The Facebook API
 * is used to make this better.
 */

@IgnoreExtraProperties
@SuppressWarnings({"unused", "WeakerAccess"})
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
