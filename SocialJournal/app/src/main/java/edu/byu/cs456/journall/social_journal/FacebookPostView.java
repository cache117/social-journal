package edu.byu.cs456.journall.social_journal;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import edu.byu.cs456.journall.social_journal.post.FacebookPost;

/**
 * Created by Michael on 4/15/2017.
 */

public class FacebookPostView extends LinearLayout {

    private FacebookPost facebookPost;
    private String title;
    private Bitmap image;
    private TextView titleView;
    private ImageView imageView;

    public FacebookPostView(Context context){
        super(context);
        init(context);
    }

    public FacebookPostView(Context context, AttributeSet attributeSet){
        super(context, attributeSet);
        init(context);
    }

    public FacebookPostView(Context context, AttributeSet attributeSet, int defStyle){
        super(context, attributeSet, defStyle);
        init(context);
    }

    private void init(Context context){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.facebook_post_view, this);

        title = null;
        image = null;
    }

    //I don't know if we'll use the FacebookPost object or not....
    public void setFacebookPost(FacebookPost post){
        this.facebookPost = post;
    }

    public FacebookPost getFacebookPost(){
        return facebookPost;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public String getTitle(){
        return title;
    }

    public void setImage(Bitmap image){
        this.image = image;
    }

    public Bitmap getImage(){
        return image;
    }

    @Override
    protected void onFinishInflate(){
        super.onFinishInflate();

        titleView = (TextView) this.findViewById(R.id.facebook_post_message);
        titleView.setText(title);
        imageView = (ImageView) this.findViewById(R.id.facebook_post_image);
        imageView.setImageBitmap(image);
    }
}
