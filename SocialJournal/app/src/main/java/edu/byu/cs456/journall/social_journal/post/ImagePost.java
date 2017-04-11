package edu.byu.cs456.journall.social_journal.post;

import android.graphics.Bitmap;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Date;

/**
 * Created by cstaheli on 4/11/2017.
 */

@IgnoreExtraProperties
public class ImagePost extends Post {
    public Bitmap image;

    public ImagePost() {
    }

    public ImagePost(String userId, Date date, Bitmap image) {
        super(userId, date);
        this.image = image;
    }
}
