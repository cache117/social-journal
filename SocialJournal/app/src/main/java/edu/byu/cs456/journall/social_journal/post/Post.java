package edu.byu.cs456.journall.social_journal.post;

import android.support.annotation.NonNull;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Comparator;
import java.util.Date;

/**
 * Created by cstaheli on 4/10/2017.
 */

@IgnoreExtraProperties
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
