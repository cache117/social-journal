package edu.byu.cs456.journall.social_journal;

import android.content.Context;
import android.graphics.Point;
import android.net.Uri;
import android.support.annotation.NonNull;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import edu.byu.cs456.journall.social_journal.post.FacebookPost;
import edu.byu.cs456.journall.social_journal.post.ImagePost;
import edu.byu.cs456.journall.social_journal.post.NotePost;
import edu.byu.cs456.journall.social_journal.post.Post;

import com.bumptech.glide.Glide;

/**
 * Created by Michael on 3/27/2017.
 */

public class SocialJournalAdapter extends RecyclerView.Adapter<SocialJournalAdapter.ViewHolder> {
    private final static int FACEBOOK_POST = 0;
    private final static int TEXT_POST = 1;
    private final static int IMAGE_POST = 2;

    private List<Post> mDataset;
    private Context mContext;
    private static final String TAG = SocialJournalAdapter.class.getCanonicalName();

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder

    /**
     * ViewHolder is a parent class to all other ViewHolder objects
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View v) {
            super(v);
        }
    }

    /**
     * FacebookViewHolder is for Facebook posts and inserts an iframe into a WebView
     */
    public static class FacebookViewHolder extends SocialJournalAdapter.ViewHolder {
        public WebView mWebView;

        public FacebookViewHolder(View v) {
            super(v);
            mWebView = (WebView) v.findViewById(R.id.web_view);
        }
    }

    /**
     * NoteViewHolder is for notes made in the app by the user.
     * Contains a Title, a Date, and a Body
     */
    public static class NoteViewHolder extends SocialJournalAdapter.ViewHolder {
        public TextView mTextTitle;
        public TextView mTextDate;
        public TextView mTextBody;

        public NoteViewHolder(View v) {
            super(v);
            mTextTitle = (TextView) v.findViewById(R.id.text_view_title);
            mTextDate = (TextView) v.findViewById(R.id.text_view_date);
            mTextBody = (TextView) v.findViewById(R.id.text_view_body);
        }
    }

    /**
     * ImageViewHolder is for images inserted from the device
     */
    public static class ImageViewHolder extends SocialJournalAdapter.ViewHolder {
        public ImageView mImageView;

        public ImageViewHolder(View v) {
            super(v);
            mImageView = (ImageView) v.findViewById(R.id.image_view);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public SocialJournalAdapter(List<Post> myDataset, Context context) {

        mDataset = myDataset;
        mContext = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public SocialJournalAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case FACEBOOK_POST:
                View facebookView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.web_view, parent, false);
                return new FacebookViewHolder(facebookView);
            case TEXT_POST:
                View noteView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.text_view, parent, false);
                return new NoteViewHolder(noteView);
            case IMAGE_POST:
                View imageView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.image_view, parent, false);
                return new ImageViewHolder(imageView);
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
            case FACEBOOK_POST:
                handleFacebookPost(holder, post);
                break;
            //text note
            case TEXT_POST:
                handleNotePost(holder, post);
                break;
            //image
            case IMAGE_POST:
                handleImagePost(holder, post);
                break;
        }
    }

    private void handleFacebookPost(ViewHolder holder, Post post) {
        FacebookViewHolder facebookViewHolder = (FacebookViewHolder) holder;
        FacebookPost facebookPost = (FacebookPost) post;
        facebookViewHolder.mWebView.setInitialScale(getScale());
        facebookViewHolder.mWebView.loadDataWithBaseURL("https://facebook.com", facebookPost.toString(), "text/html", "utf-8", null);
    }

    private void handleNotePost(ViewHolder holder, Post post) {
        NoteViewHolder noteViewHolder = (NoteViewHolder) holder;
        NotePost notePost = (NotePost) post;
        String title = notePost.title;
        String body = notePost.body;
        String date = notePost.date.toString();
        if (title != null) {
            noteViewHolder.mTextTitle.setText(title);
        }
        noteViewHolder.mTextDate.setText(date);
        noteViewHolder.mTextBody.setText(body);
    }

    private void handleImagePost(ViewHolder holder, Post post) {
        final ImageViewHolder imageViewHolder = (ImageViewHolder) holder;
        ImagePost imagePost = (ImagePost) post;
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imagePost.imageUrl);
        storageReference.getDownloadUrl().addOnCompleteListener(new ImageDownloadOnCompleteListener(imageViewHolder));
    }

    private class ImageDownloadOnCompleteListener implements OnCompleteListener<Uri> {
        private ImageViewHolder imageViewHolder;

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
        if (item instanceof FacebookPost) {
            return FACEBOOK_POST;
        } else if (item instanceof ImagePost) {
            return IMAGE_POST;
        } else {
            return TEXT_POST;
        }
    }

    private int getScale() {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        Double val = new Double(width) / new Double(484);
        val = val * 100d;
        return val.intValue();
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
