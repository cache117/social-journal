package edu.byu.cs456.journall.social_journal.post;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Date;

/**
 * Created by cstaheli on 4/10/2017.
 */

@IgnoreExtraProperties
public abstract class Post {
    public String userId;
    public Date date;

    public Post() {

    }

    protected Post(String userId, Date date) {
        this.userId = userId;
        this.date = date;
    }
}
