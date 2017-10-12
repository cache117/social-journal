package edu.byu.cs456.journall.social_journal.models.post;

import android.support.annotation.NonNull;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Date;

/**
 * Contains the basic information about posts.
 */

@IgnoreExtraProperties
@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class Post implements Comparable<Post> {
    public String userId;
    public Date date;

    public Post() {

    }

    protected Post(String userId, Date date) {
        this.userId = userId;
        this.date = date;
    }

    @Override
    public int compareTo(@NonNull Post o) {
        return date.compareTo(o.date);
    }
}
