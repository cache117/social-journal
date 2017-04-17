package edu.byu.cs456.journall.social_journal.post;

import android.graphics.Bitmap;
import android.util.Base64;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.ByteArrayOutputStream;
import java.util.Date;

/**
 * Created by cstaheli on 4/11/2017.
 */

@IgnoreExtraProperties
public class ImagePost extends Post {
    public String photoUrl;
    public String imageUrl;
    private static final String LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif";


    public ImagePost() {
    }

    public ImagePost(String userId, Date date, String photoUrl, String imageUrl) {
        super(userId, date);
        this.photoUrl = photoUrl;
        this.imageUrl = imageUrl;
    }

    public ImagePost(String userId, Date date, String photoUrl) {
        this(userId, date, photoUrl, LOADING_IMAGE_URL);
    }

//    @Override
//    public String toString() {
//        String imageAsString = bitMapToString(image);
//        String preface = "THIS IS A BITMAP"; //insert this before the bits in a bitmap
//        return preface + imageAsString;
//    }
//
//    private String bitMapToString(Bitmap bitmap) {
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
//        byte[] b = baos.toByteArray();
//        String temp = Base64.encodeToString(b, Base64.DEFAULT);
//        return temp;
//    }
}