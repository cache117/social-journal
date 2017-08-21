package edu.byu.cs456.journall.social_journal.models.post;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Date;

/**
 * A representation of an Image Post.
 */

@IgnoreExtraProperties
public class ImagePost extends Post {
    public String photoUrl;
    public String imageUrl;
    public String key;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ImagePost imagePost = (ImagePost) o;

        if (!photoUrl.equals(imagePost.photoUrl)) return false;
        if (!userId.equals(imagePost.userId)) return false;
        return key.equals(imagePost.key);

    }

    @Override
    public int hashCode() {
        int result = photoUrl.hashCode();
        result = 31 * result + userId.hashCode();
        result = 31 * result + key.hashCode();
        return result;
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
