package edu.byu.cs456.journall.social_journal.views;

import android.content.Context;
import android.graphics.Point;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.CompactTweetView;
import com.twitter.sdk.android.tweetui.TweetUtils;

import java.util.List;

import edu.byu.cs456.journall.social_journal.R;
import edu.byu.cs456.journall.social_journal.models.post.FacebookPost;
import edu.byu.cs456.journall.social_journal.models.post.ImagePost;
import edu.byu.cs456.journall.social_journal.models.post.NotePost;
import edu.byu.cs456.journall.social_journal.models.post.Post;
import edu.byu.cs456.journall.social_journal.models.post.TwitterPost;
import edu.byu.cs456.journall.social_journal.models.post.WebPost;

/**
 * A class to support putting various posts into a RecyclerView
 *
 * @author Michael Call
 * @author Cache Staheli
 */
public class SocialJournalAdapter extends RecyclerView.Adapter<SocialJournalAdapter.ViewHolder> {

    /**
     * @deprecated This shouldn't really be used. This was a precursor for Facebook posts. Those now
     * are implemented separately using the Facebook API.
     */
    private final static int WEB_POST = 0;
    private final static int TEXT_POST = 1;
    private final static int IMAGE_POST = 2;
    private final static int FACEBOOK_POST = 3;
    private final static int TWITTER_POST = 4;

    private List<Post> mDataset;
    private final Context mContext;
    private static final String TAG = SocialJournalAdapter.class.getCanonicalName();


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder

    /**
     * ViewHolder is a parent class to all other ViewHolder objects
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(View v) {
            super(v);
        }
    }

    /**
     * WebViewHolder is for Facebook posts and inserts an iframe into a WebView.
     *
     * @deprecated This shouldn't really be used. This was a precursor for Facebook posts. Those now
     * are implemented separately using the Facebook API.
     */
    private static class WebViewHolder extends SocialJournalAdapter.ViewHolder {
        final WebView mWebView;

        WebViewHolder(View v) {
            super(v);
            mWebView = (WebView) v.findViewById(R.id.web_view);
        }
    }

    /**
     * NoteViewHolder is for notes made in the app by the user.
     * Contains a Title, a Date, and a Body
     */
    private static class NoteViewHolder extends SocialJournalAdapter.ViewHolder {
        final TextView mTitle;
        final TextView mDate;
        final TextView mBody;

        NoteViewHolder(View v) {
            super(v);
            mTitle = (TextView) v.findViewById(R.id.text_view_title);
            mDate = (TextView) v.findViewById(R.id.text_view_date);
            mBody = (TextView) v.findViewById(R.id.text_view_body);
        }
    }

    /**
     * ImageViewHolder is for images inserted from the device
     */
    private static class ImageViewHolder extends SocialJournalAdapter.ViewHolder {
        final ImageView mImageView;

        ImageViewHolder(View v) {
            super(v);
            mImageView = (ImageView) v.findViewById(R.id.image_view);
        }
    }

    /**
     * A class to hold the information for a facebook post, since there isn't a delivered one.
     */
    private static class FacebookViewHolder extends SocialJournalAdapter.ViewHolder {
        final ImageView mProfilePicture;
        final TextView mStatus;
        final TextView mDate;
        final TextView mMessage;
        final ImageView mAttachmentImage;

        FacebookViewHolder(View v) {
            super(v);
            mProfilePicture = (ImageView) v.findViewById(R.id.facebook_post_profile_picture);
            mStatus = (TextView) v.findViewById(R.id.facebook_post_status);
            mDate = (TextView) v.findViewById(R.id.facebook_post_date);
            mMessage = (TextView) v.findViewById(R.id.facebook_post_message);
            mAttachmentImage = (ImageView) v.findViewById(R.id.facebook_post_image);
        }
    }

    /**
     * A wrapper class for Tweets. This assists in displaying Tweets natively in a RecyclerView.
     */
    private static class TwitterViewHolder extends SocialJournalAdapter.ViewHolder {
        final CardView mCardView;

        TwitterViewHolder(View v) {
            super(v);
            mCardView = (CardView) v.findViewById(R.id.twitter_card_view);
        }
    }

    /**
     * Instantiates this RecyclerView Adapter with a list of posts and the Context of the
     * RecyclerView (probably MainActivity).
     *
     * @param myDataset the list of posts to use for the Adapter.
     * @param context   the Context for the RecyclerView. This is required by various aspects of the
     *                  RecyclerView and the Adapter.
     */
    public SocialJournalAdapter(List<Post> myDataset, Context context) {
        mDataset = myDataset;
        mContext = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public SocialJournalAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case WEB_POST:
                View webView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.web_view, parent, false);
                return new WebViewHolder(webView);
            case TEXT_POST:
                View noteView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.text_view, parent, false);
                return new NoteViewHolder(noteView);
            case IMAGE_POST:
                View imageView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.image_view, parent, false);
                return new ImageViewHolder(imageView);
            case FACEBOOK_POST:
                View facebookView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.facebook_post, parent, false);
                return new FacebookViewHolder(facebookView);
            case TWITTER_POST:
                View tweetView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.twitter_post, parent, false);
                return new TwitterViewHolder(tweetView);
            default:
                return null;
        }
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        Post post = mDataset.get(position);

        switch (holder.getItemViewType()) {
            //facebook iframe
            case WEB_POST:
                handleWebPost(holder, post);
                break;
            //text note
            case TEXT_POST:
                handleNotePost(holder, post);
                break;
            //image
            case IMAGE_POST:
                handleImagePost(holder, post);
                break;
            //Facebook post
            case FACEBOOK_POST:
                handleFacebookPost(holder, post);
                break;
            //Tweet
            case TWITTER_POST:
                handleTweet(holder, post);
                break;
        }
    }

    /**
     * Handles adding a tweet to the view.
     *
     * @param holder the holder for hte tweet.
     * @param post   the post that contains the Id for the tweet.
     * @see CompactTweetView
     * @see TweetUtils#loadTweet(long, Callback)
     * @see Tweet
     */
    private void handleTweet(ViewHolder holder, Post post) {
        final TwitterViewHolder twitterViewHolder = (TwitterViewHolder) holder;
        TwitterPost twitterPost = (TwitterPost) post;
        TweetUtils.loadTweet(twitterPost.tweetId, new Callback<Tweet>() {
            @Override
            public void success(Result<Tweet> result) {
                //Unlike the other posts, we have to add a delivered TweetView to the card view.
                twitterViewHolder.mCardView.addView(new CompactTweetView(mContext, result.data));
            }

            @Override
            public void failure(TwitterException exception) {
                Log.d(TAG, "Failed to handle tweet properly in adapter");
            }
        });
    }

    /**
     * Handles adding a Facebook post to the view.
     *
     * @param holder the holder for the post.
     * @param post   the post that contains the information about the Facebook post.
     */
    private void handleFacebookPost(ViewHolder holder, Post post) {
        FacebookViewHolder facebookViewHolder = (FacebookViewHolder) holder;
        FacebookPost facebookPost = (FacebookPost) post;
        facebookViewHolder.mDate.setText(facebookPost.date.toString());
        facebookViewHolder.mStatus.setText(facebookPost.story);
        facebookViewHolder.mMessage.setText(facebookPost.message);
        String downloadUrl = facebookPost.photoUrl;
        if (downloadUrl != null) {
            Glide.with(facebookViewHolder.mProfilePicture.getContext())
                    .load(downloadUrl)
                    .into(facebookViewHolder.mProfilePicture);
        }
        String attachmentUrl = facebookPost.attachmentUrl;
        if (attachmentUrl != null) {
            Glide.with(facebookViewHolder.mAttachmentImage.getContext())
                    .load(attachmentUrl)
                    .into(facebookViewHolder.mAttachmentImage);
        }
    }

    /**
     * Handles adding a Facebook Iframe Web post to the view.
     *
     * @param holder the holder for the post.
     * @param post   the post that contains the information about the Facebook post.
     * @deprecated This shouldn't really be used. This was a precursor for Facebook posts. Those now
     * are implemented separately using the Facebook API.
     */
    private void handleWebPost(ViewHolder holder, Post post) {
        WebViewHolder webViewHolder = (WebViewHolder) holder;
        WebPost webPost = (WebPost) post;
        webViewHolder.mWebView.setInitialScale(getScale());
        webViewHolder.mWebView.loadDataWithBaseURL("https://facebook.com", webPost.toString(), "text/html", "utf-8", null);
    }

    /**
     * Handles adding a user note to the view.
     *
     * @param holder the holder for the note.
     * @param post   the post that contains the information about the note.
     */
    private void handleNotePost(ViewHolder holder, Post post) {
        NoteViewHolder noteViewHolder = (NoteViewHolder) holder;
        NotePost notePost = (NotePost) post;
        String title = notePost.title;
        String body = notePost.body;
        String date = notePost.date.toString();
        if (title != null) {
            noteViewHolder.mTitle.setText(title);
        }
        noteViewHolder.mDate.setText(date);
        noteViewHolder.mBody.setText(body);
    }

    /**
     * Handles adding an image to the view. This will download the image.
     *
     * @param holder the holder for the image.
     * @param post   the post that contains the image url for the image.
     * @see SocialJournalAdapter.ImageDownloadOnCompleteListener
     */
    private void handleImagePost(ViewHolder holder, Post post) {
        final ImageViewHolder imageViewHolder = (ImageViewHolder) holder;
        ImagePost imagePost = (ImagePost) post;
        try {
            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imagePost.imageUrl);
            storageReference.getDownloadUrl().addOnCompleteListener(new ImageDownloadOnCompleteListener(imageViewHolder));
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Trying to get a reference for an image that is not loaded. Try again");
        }
    }

    /**
     * Used for downloading images to be displayed in ImagePosts.
     *
     * @see SocialJournalAdapter#handleImagePost(ViewHolder, Post)
     */
    private class ImageDownloadOnCompleteListener implements OnCompleteListener<Uri> {
        private final ImageViewHolder imageViewHolder;

        ImageDownloadOnCompleteListener(ImageViewHolder imageViewHolder) {
            this.imageViewHolder = imageViewHolder;
        }

        @Override
        public void onComplete(@NonNull Task<Uri> task) {
            if (task.isSuccessful()) {
                String downloadUrl = task.getResult().toString();
                Glide.with(imageViewHolder.mImageView.getContext())
                        .load(downloadUrl)
                        .into(imageViewHolder.mImageView);
            } else {
                Log.w(TAG, "Getting download url was not successful.", task.getException());
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        Post item = mDataset.get(position);
        if (item instanceof WebPost) {
            return WEB_POST;
        } else if (item instanceof FacebookPost) {
            return FACEBOOK_POST;
        } else if (item instanceof TwitterPost) {
            return TWITTER_POST;
        } else if (item instanceof ImagePost) {
            return IMAGE_POST;
        } else {
            return TEXT_POST;
        }
    }

    /**
     * Used to set the scale for Facebook WebPosts.
     *
     * @return the integer scale value.
     * @deprecated Facebook WebPosts are deprecated. This isn't needed for Facebook Posts.
     */
    private int getScale() {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        Double val = (double) width / 484d;
        val = val * 100d;
        return val.intValue();
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
