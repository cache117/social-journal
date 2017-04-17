package edu.byu.cs456.journall.social_journal.post;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Date;
import java.util.Map;

/**
 * Created by cstaheli on 4/11/2017.
 */

@IgnoreExtraProperties
public class FacebookPost extends Post {
    public String postId;
    public String url;

    public FacebookPost() {

    }

    public FacebookPost(String userId, Date date, String postId, String url) {
        this(userId, date, postId);
        this.url = url;
    }
    public FacebookPost(String userId, Date date, String postId) {
        super(userId, date);
        this.postId = postId;
    }

    @Override
    public String toString() {
        if (url == null) {
            return "<iframe src=\"https://www.facebook.com/plugins/post.php?href=https%3A%2F%2Fwww.facebook.com%2FStudentProblems%2Fposts%2F1184336055026459%3A0&width=500\" width=\"500\" height=\"589\" style=\"border:none;overflow:hidden\" scrolling=\"no\" frameborder=\"0\" allowTransparency=\"true\"></iframe>";
        }
        else
        {
            return url;
        }
    }
}
