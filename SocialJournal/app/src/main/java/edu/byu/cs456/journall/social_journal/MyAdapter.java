package edu.byu.cs456.journall.social_journal;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Michael on 3/27/2017.
 */

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private List<String> mDataset;
    //private String[] mDataset;
    private Context mContext;

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
    public static class ViewHolder0 extends MyAdapter.ViewHolder {
        public WebView mWebView;

        public ViewHolder0(View v) {
            super(v);
            mWebView = (WebView) v.findViewById(R.id.web_view);
        }
    }

    public static class ViewHolder1 extends MyAdapter.ViewHolder {
        public TextView mTextView;

        public ViewHolder1(View v) {
            super(v);
            mTextView = (TextView) v.findViewById(R.id.text_view);
        }
    }

    public static class ViewHolder2 extends MyAdapter.ViewHolder {
        public ImageView mImageView;

        public ViewHolder2(View v) {
            super(v);
            mImageView = (ImageView) v.findViewById(R.id.image_view);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(List<String> myDataset, Context context) {

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
        switch(viewType) {
            case 0: View v0 = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.post, parent, false);
                    return new ViewHolder0(v0);
            case 1: View v1 = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.text_view, parent, false);
                    return new ViewHolder1(v1);
            case 2: View v2 = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.image_view, parent, false);
                    return new ViewHolder2(v2);
            default: return null;
        }
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        //holder.mWebView.setText(mDataset[position]);

        //TODO not sure what to do with this yet...
        String post = mDataset.get(position);


//        holder.mWebView.getSettings().setLoadWithOverviewMode(true);
//        holder.mWebView.getSettings().setUseWideViewPort(true);
        switch (holder.getItemViewType()) {
            case 0: ViewHolder0 holder0 = (ViewHolder0) holder;
                    holder0.mWebView.setInitialScale(getScale());
                    holder0.mWebView.loadDataWithBaseURL("https://facebook.com", post, "text/html", "utf-8", null);
                    break;
            case 1: ViewHolder1 holder1 = (ViewHolder1) holder;
                    holder1.mTextView.setText(post);
                    break;
            case 2: ViewHolder2 holder2 = (ViewHolder2) holder;
                    Bitmap image = StringToBitMap(post.substring(16));
                    holder2.mImageView.setImageBitmap(image);
        }

    }

    @Override
    public int getItemViewType(int position) {
        String item = mDataset.get(position);

        //if the string starts out with "<iframe" then it is HTML for a WebView
        if (item.length() > 6 && item.substring(0, 7).equals("<iframe")){
            return 0;
        }
        //if there is no whitespace it is a bitmap for an image
        else if (item.length() > 16 && item.substring(0, 16).equals("THIS IS A BITMAP")){
            return 2;
        }
        //if it's not a WebView or bitmap, display the string in a TextView
        else {
            return 1;
        }
    }

    private int getScale(){
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        Double val = new Double(width)/new Double(500);
        val = val * 100d;
        return val.intValue();
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    private Bitmap StringToBitMap(String encodedString){
        try {
            byte [] encodeByte= Base64.decode(encodedString,Base64.DEFAULT);
            Bitmap bitmap= BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch(Exception e) {
            e.getMessage();
            return null;
        }
    }
}
