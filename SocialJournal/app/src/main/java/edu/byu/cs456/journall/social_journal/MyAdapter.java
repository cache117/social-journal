package edu.byu.cs456.journall.social_journal;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
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

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private List<Post> mDataset;
    //    private List<String> mDataset;
    //private String[] mDataset;
    private Context mContext;
    private static final String TAG = MyAdapter.class.getCanonicalName();

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
     * ViewHolder0 is for Facebook posts and inserts an iframe into a WebView
     */
    public static class ViewHolder0 extends MyAdapter.ViewHolder {
        public WebView mWebView;

        public ViewHolder0(View v) {
            super(v);
            mWebView = (WebView) v.findViewById(R.id.web_view);
        }
    }

    /**
     * ViewHolder1 is for notes made in the app by the user. Contains a Title and a Body
     */
    public static class ViewHolder1 extends MyAdapter.ViewHolder {
        public TextView mTextView;

        public ViewHolder1(View v) {
            super(v);
            mTextView = (TextView) v.findViewById(R.id.text_view);
        }
    }

    /**
     * ViewHolder2 is for images inserted from the device
     */
    public static class ViewHolder2 extends MyAdapter.ViewHolder {
        public ImageView mImageView;

        public ViewHolder2(View v) {
            super(v);
            mImageView = (ImageView) v.findViewById(R.id.image_view);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(List<Post> myDataset, Context context) {

        mDataset = myDataset;
        mContext = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        //LinearLayout ll = (LinearLayout) parent;
//        View v = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.post, parent, false);
        // set the view's size, margins, paddings and layout parameters
        //...
        //v.setTextSize(20);
        switch (viewType) {
            case 0:
                View v0 = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.post, parent, false);
                return new ViewHolder0(v0);
            case 1:
                View v1 = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.text_view, parent, false);
                return new ViewHolder1(v1);
            case 2:
                View v2 = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.image_view, parent, false);
                return new ViewHolder2(v2);
            default:
                return null;
        }
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        //holder.mWebView.setText(mDataset[position]);

        //TODO not sure what to do with this yet...
        Post post = mDataset.get(position);

        switch (holder.getItemViewType()) {
            //facebook iframe
            case 0:
                ViewHolder0 holder0 = (ViewHolder0) holder;
                FacebookPost facebookPost = (FacebookPost) post;
                holder0.mWebView.setInitialScale(getScale());
                holder0.mWebView.loadDataWithBaseURL("https://facebook.com", facebookPost.toString(), "text/html", "utf-8", null);
                break;
            //text note
            case 1:
                ViewHolder1 holder1 = (ViewHolder1) holder;
                NotePost notePost = (NotePost) post;
                String title = notePost.title;
                String body = notePost.body;
//                String delimeter = "\\(@\\)";
//                String[] titleAndBody = post.split(delimeter);
//                if (titleAndBody.length == 2) {
//                    holder1.mTextView.setText(titleAndBody[0] + "\n\n" + titleAndBody[1]);
//                } else {
//                    holder1.mTextView.setText(titleAndBody[0]);
//                }
                if (title != null) {
                    holder1.mTextView.setText(title + "\n\n" + body);
                } else {
                    holder1.mTextView.setText(body);
                }
                break;
            //image
            case 2:
                final ViewHolder2 holder2 = (ViewHolder2) holder;
                ImagePost imagePost = (ImagePost) post;
                StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imagePost.imageUrl);
                storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            String downloadUrl = task.getResult().toString();
                            Glide.with(holder2.mImageView.getContext())
                                    .load(downloadUrl)
                                    .into(holder2.mImageView);
                        } else {
                            Log.w(TAG, "Getting download url was not successful.",
                                    task.getException());
                        }
                    }
                });
                break;
        }

    }

    @Override
    public int getItemViewType(int position) {
        Post item = mDataset.get(position);
        if (item instanceof FacebookPost) {
            return 0;
        } else if (item instanceof ImagePost) {
            return 2;
        } else {
            return 1;
        }

//        //if the string starts out with "<iframe" then it is HTML for a WebView
//        if (item.length() > 6 && item.substring(0, 7).equals("<iframe")) {
//            return 0;
//        }
//        //if there is no whitespace it is a bitmap for an image
//        else if (item.length() > 16 && item.substring(0, 16).equals("THIS IS A BITMAP")) {
//            return 2;
//        }
//        //if it's not a WebView or bitmap, display the string in a TextView
//        else {
//            return 1;
//        }
    }

    private int getScale() {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        Double val = new Double(width) / new Double(500);
        val = val * 100d;
        return val.intValue();
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    private Bitmap stringToBitMap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }
}
