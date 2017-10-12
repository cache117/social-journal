package edu.byu.cs456.journall.social_journal.models.post;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Date;

/**
 * A representation of a Note Post.
 */

@IgnoreExtraProperties
@SuppressWarnings({"unused", "WeakerAccess"})
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

    @Override
    public String toString() {
        String titleAndBody;
        if (title != null) {
            titleAndBody = title + "(@)" + body;
        } else {
            titleAndBody = body;
        }
        return titleAndBody;
    }
}
