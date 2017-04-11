package edu.byu.cs456.journall.social_journal.post;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Date;

/**
 * Created by cstaheli on 4/11/2017.
 */

@IgnoreExtraProperties
public class NotePost extends Post {
    public String title;
    public String body;

    public NotePost() {
    }

    public NotePost(String userId, Date date, String title, String body) {
        super(userId, date);
        this.title = title;
        this.body = body;
    }
}
