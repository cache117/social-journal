package edu.byu.cs456.journall.social_journal.activities.main;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.instagram.instagramapi.engine.InstagramEngine;
import com.instagram.instagramapi.exceptions.InstagramException;
import com.instagram.instagramapi.interfaces.InstagramAPIResponseCallback;
import com.instagram.instagramapi.objects.IGPagInfo;
import com.instagram.instagramapi.objects.IGUser;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.StatusesService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.byu.cs456.journall.social_journal.R;
import edu.byu.cs456.journall.social_journal.activities.login.LoginActivity;
import edu.byu.cs456.journall.social_journal.activities.note.AddNote;
import edu.byu.cs456.journall.social_journal.activities.calendar.JournalCalendar;
import edu.byu.cs456.journall.social_journal.activities.preferences.SettingsActivity;
import edu.byu.cs456.journall.social_journal.models.post.TwitterPost;
import edu.byu.cs456.journall.social_journal.views.SocialJournalAdapter;
import edu.byu.cs456.journall.social_journal.models.post.FacebookPost;
import edu.byu.cs456.journall.social_journal.models.post.ImagePost;
import edu.byu.cs456.journall.social_journal.models.post.NotePost;
import edu.byu.cs456.journall.social_journal.models.post.Post;
import edu.byu.cs456.journall.social_journal.models.post.PostComparatorByDate;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private final String TAG = MainActivity.class.getCanonicalName();
    static final int SELECT_IMAGE = 1;
    static final int SELECT_DATE = 2;
    static final int ADD_NOTE = 3;
    static final int INSTAGRAM = 4;

    private static final String LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif";

    private RecyclerView myRecyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private List<Post> posts;
    private FirebaseDatabase mDatabase;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private String mPhotoUrl;
    private InstagramEngine instagramEngine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = initializeToolbar();

        initializeDrawer(toolbar);
        initializeNavigationView();

        Twitter.initialize(this);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser != null) {
            mDatabase = FirebaseDatabase.getInstance();
            syncUserProfiles();
            if (mFirebaseUser.getPhotoUrl() != null) {
                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            }

            posts = new ArrayList<>();
            attachPostListeners();
            onBoarding();

            setUpRecyclerView();
        }
    }

    private boolean isUsingInstagram() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPref.getBoolean("connect_to_instagram", false);
    }

    private boolean isUsingFacebook() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPref.getBoolean("connect_to_facebook", false);
    }

    private boolean isUsingTwitter() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPref.getBoolean("connect_to_twitter", false);
    }

    private Toolbar initializeToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);
        return toolbar;
    }

    private void initializeDrawer(Toolbar toolbar) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void initializeNavigationView() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void setUpRecyclerView() {
        myRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        myRecyclerView.setHasFixedSize(false);

        layoutManager = new LinearLayoutManager(this);
        myRecyclerView.setLayoutManager(layoutManager);

        adapter = new SocialJournalAdapter(posts, this);
        myRecyclerView.setAdapter(adapter);
    }

    private void syncUserProfiles() {
        String uid = mFirebaseUser.getUid();
        final DatabaseReference user = mDatabase.getReference("/users").child(uid);
        user.child("email").setValue(mFirebaseUser.getEmail());
        user.child("name").setValue(mFirebaseUser.getDisplayName());
        setDisplayName(mFirebaseUser.getDisplayName());
        for (UserInfo userInfo : mFirebaseUser.getProviderData()) {
            if (!userInfo.getProviderId().equals("firebase")) {
                user.child(userInfo.getProviderId().split("\\.")[0]).setValue(userInfo.getUid());
            }
        }
//        if (isUsingInstagram()) {
//            if (instagramEngine == null) {
//                setupInstagram();
//            }
//            instagramEngine.getUserDetails(new InstagramUserIdCallback(user));
//        }
    }

    private void setDisplayName(String displayName) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putString("pref_display_name", displayName);
        editor.apply();
    }

    private class InstagramUserIdCallback implements InstagramAPIResponseCallback<IGUser> {
        private DatabaseReference user;

        InstagramUserIdCallback(DatabaseReference user) {
            this.user = user;
        }

        @Override
        public void onResponse(IGUser responseObject, IGPagInfo pageInfo) {
            String instagramId = responseObject.getId();
            user.child("instagram").setValue(instagramId);
        }

        @Override
        public void onFailure(InstagramException exception) {
            Log.w("MainActivity", "Exception:" + exception.getMessage());
        }
    }

    private void onBoarding() {
        final String uid = mFirebaseUser.getUid();
        DatabaseReference user = mDatabase.getReference("/users").child(uid);
        if (isUsingFacebook()) {
            user.child("facebook_onboarding").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        importExistingFacebookPosts(uid);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        if (isUsingTwitter()) {
            user.child("twitter_onboarding").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        Log.d(TAG, "Importing Twitter");
                        importExistingTwitterPosts(uid);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
//        if (isUsingInstagram()) {
//            Log.d(TAG, "Here");
//            user.child("instagram_onboarding").addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    if (!dataSnapshot.exists()) {
//
//                    }
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//
//                }
//            });
//        }
    }

    private void importExistingTwitterPosts(final String uid) {
        TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
        StatusesService statusesService = twitterApiClient.getStatusesService();
        statusesService
                .userTimeline(TwitterCore
                                .getInstance()
                                .getSessionManager()
                                .getActiveSession()
                                .getUserId(),
                        null, null, null, null, null, null, null, null)
                .enqueue(new ImportExistingTwitterPostsCallback(uid));
    }

    private class ImportExistingTwitterPostsCallback extends Callback<List<Tweet>> {
        private String uid;

        ImportExistingTwitterPostsCallback(String uid) {
            this.uid = uid;
        }

        @Override
        public void success(Result<List<Tweet>> result) {
            Log.d(TAG, "Importing Twitter");
            try {
                for (Tweet tweet : result.data) {
                    TwitterPost post = new TwitterPost(uid, getDateFromTweet(tweet), tweet.id);
                    mDatabase.getReference("/posts").child(uid).child("tweets").push().setValue(post);
                }

                mDatabase.getReference("/users").child(uid).child("twitter_onboarding").setValue(true);
            } catch (ParseException e) {
                Toast.makeText(getApplicationContext(), "Something went wrong with importing your existing posts", Toast.LENGTH_SHORT).show();
                Log.w(TAG, e);
            }
        }

        @Override
        public void failure(TwitterException exception) {
            Log.w(TAG, exception);
        }
    }

    private void importExistingFacebookPosts(final String uid) {
        /* make the API call */
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/feed?fields=id,created_time,message,story,full_picture,type",
                null,
                HttpMethod.GET,
                new ImportExistingFacebookPostsCallback(uid)
        ).executeAsync();
    }

    private class ImportExistingFacebookPostsCallback implements GraphRequest.Callback {
        private String uid;

        ImportExistingFacebookPostsCallback(String uid) {
            this.uid = uid;
        }

        @Override
        public void onCompleted(GraphResponse response) {
            Log.d(TAG, response.toString());
            try {
                JSONArray data = (JSONArray) response.getJSONObject().get("data");
                if (data.length() != 0) {
                    int length = data.length() > 25 ? 25 : data.length();
                    for (int i = 0; i < length; ++i) {
                        JSONObject row = data.getJSONObject(i);
                        Log.d("OBJECT_ID", row.toString());
                        FacebookPost post = getFacebookPostFromRow(row);
                        mDatabase.getReference("/posts").child(uid).child("facebook_posts").push().setValue(post);
                    }
                    mDatabase.getReference("/users").child(uid).child("facebook_onboarding").setValue(true);
                } else {
                    throw new Exception("This app needs access to facebook posts, and isn't getting them");
                }

            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Something went wrong with importing your existing posts", Toast.LENGTH_SHORT).show();
                Log.w(TAG, e);
            }
        }

        @NonNull
        private FacebookPost getFacebookPostFromRow(JSONObject row) throws JSONException, ParseException {
            String postId = row.getString("id");
            Date date = getFacebookDate(row.getString("created_time"));
            String message = null;
            try {
                message = row.getString("message");
                Log.d("OBJECT_ID", row.toString());
            } catch (JSONException ignored) {

            }
            String story = null;
            try {
                story = row.getString("story");
            } catch (JSONException ignored) {

            }
            String pictureUrl = null;
            try {
                pictureUrl = row.getString("full_picture");
            } catch (JSONException ignored) {

            }
            String type = null;
            try {
                type = row.getString("type");

            } catch (JSONException ignored) {

            }
            FacebookPost facebookPost = new FacebookPost(uid, date, postId, message, story);
            facebookPost.photoUrl = MainActivity.this.mPhotoUrl;
            facebookPost.attachmentUrl = pictureUrl;
            facebookPost.type = type;
            return facebookPost;
        }
    }

    /*private class ImportExistingInstagramPostsCallback implements InstagramAPIResponseCallback<ArrayList<IGMedia>> {
        private String uid;

        public ImportExistingInstagramPostsCallback(String uid) {
            this.uid = uid;
        }

        @Override
        public void onResponse(ArrayList<IGMedia> responseObject, IGPagInfo pageInfo) {
            for (IGMedia media : responseObject) {
                IGMedia temp = media;
            }
        }

        @Override
        public void onFailure(InstagramException exception) {

        }
    }*/

    private Date getFacebookDate(String createdTime) throws ParseException {
        final String FACEBOOK_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
        SimpleDateFormat dateFormat = new SimpleDateFormat(FACEBOOK_DATE_FORMAT, Locale.ENGLISH);
        return dateFormat.parse(createdTime);
    }

    private static Date getDateFromTweet(Tweet tweet) throws ParseException {
        // e.g. Sat Sep 16 17:54:56 +0000 2017
        final String TWITTER_DATE_FORMAT = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat dateFormat = new SimpleDateFormat(TWITTER_DATE_FORMAT, Locale.ENGLISH);
        return dateFormat.parse(tweet.createdAt);
    }

    /**
     * Initializes posts displayed on home page
     *
     * @return List of Strings. Strings represent each post.
     */
    /*private List<Post> getFacebookPosts() {
        List<Post> listOfPosts = new ArrayList<>();
//        Posts.add(post5);

        return listOfPosts;
    }*/
    private Date getDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        return calendar.getTime();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        if (id == R.id.action_settings) {
//            openSettings();
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_home:
                navigateHome();
                // send to home
                break;
            case R.id.nav_add_note:
                addNote();
                //send to note page
                break;
            case R.id.nav_add_picture:
                addPicture();
                //send to picture page
                break;
            case R.id.nav_show_calendar:
                showCalendar();
                //send to calendar page
                break;
            case R.id.nav_settings:
                //send to settings page
                openSettings();
                break;
            case R.id.nav_log_out:
                // Log the user out and send them to log in
                logOut();
                break;
//            case R.id.instagram_action:
//                setupInstagram();
//                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /*private void setupInstagram() {
        instagramEngine = InstagramEngine.getInstance(MainActivity.this);

        String[] scopes = {InstagramKitLoginScope.BASIC, InstagramKitLoginScope.COMMENTS};

        Intent intent = new Intent(this, InstagramAuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);

        intent.putExtra(InstagramEngine.TYPE, InstagramEngine.TYPE_LOGIN);
        //add scopes if you want to have more than basic access
        intent.putExtra(InstagramEngine.SCOPE, scopes);

        startActivityForResult(intent, INSTAGRAM);
    }*/

    private void logOut() {
        Log.d(TAG, "Logging Out");
        mFirebaseAuth.signOut();
        if (isUsingFacebook()) {
            LoginManager.getInstance().logOut();
        }
//        if (isUsingInstagram() && instagramEngine != null) {
//            instagramEngine.logout(MainActivity.this, 0);
//        }
        if (isUsingTwitter()) {
            TwitterCore.getInstance().getSessionManager().clearActiveSession();
        }
        Intent home = new Intent(this, LoginActivity.class);
        startActivity(home);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SELECT_IMAGE:
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        addNewImage(data);
                    }
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    Toast.makeText(getApplicationContext(), "Cancelled", Toast.LENGTH_SHORT).show();
                }
                break;
            case SELECT_DATE:
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        int year = data.getIntExtra("YEAR", -1);
                        int month = data.getIntExtra("MONTH", -1);
                        int day = data.getIntExtra("DAY", -1);
                        if (year != -1 && month != -1 && day != -1) {
                            navigateToDay(year, month, day);
                        }
                    }
                }
                break;
            case ADD_NOTE:
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        String noteTitle = data.getStringExtra("TITLE");
                        String newNote = data.getStringExtra("NOTE");
                        if (noteTitle != null && !noteTitle.isEmpty() && newNote != null && !newNote.isEmpty()) {
                            addNewNote(noteTitle, newNote);
                        } else if (newNote != null && !newNote.isEmpty()) {
                            addNewNote(null, newNote);
                        }
                    }
                }
                break;
            /*case INSTAGRAM:
                if (resultCode == RESULT_OK) {

                    Bundle bundle = data.getExtras();

                    if (bundle.containsKey(InstagramKitConstants.kSessionKey)) {

                        IGSession session = (IGSession) bundle.getSerializable(InstagramKitConstants.kSessionKey);

                        Toast.makeText(MainActivity.this, "Woohooo!!! User trusts you :) " + session.getAccessToken(),
                                Toast.LENGTH_LONG).show();

                        instagramEngine.getUserDetails(new InstagramAPIResponseCallback<IGUser>() {
                            @Override
                            public void onResponse(IGUser responseObject, IGPagInfo pageInfo) {

                                Toast.makeText(MainActivity.this, "Username: " + responseObject.getUsername(),
                                        Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onFailure(InstagramException exception) {
                                Log.v("SampleActivity", "Exception:" + exception.getMessage());
                            }
                        });

                        instagramEngine.getMediaForUser(new InstagramAPIResponseCallback<ArrayList<IGMedia>>() {
                            @Override
                            public void onResponse(ArrayList<IGMedia> responseObject, IGPagInfo pageInfo) {
                                Toast.makeText(MainActivity.this, "Size: " + responseObject.size(),
                                        Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onFailure(InstagramException exception) {
                                Log.v("MainActivity", "Exception:" + exception.getMessage());
                            }
                        });
                    }

                }
                break;*/
        }
    }

    private void addNewNote(String noteTitle, String newNote) {
        final String uid = mFirebaseUser.getUid();
        NotePost post = new NotePost(uid, new Date(), noteTitle, newNote);
        mDatabase.getReference("/posts/" + uid + "/notes").push().setValue(post);
        toast("Adding Note");
        Log.d(TAG, "Note is " + post.toString());
    }

    private void toast(String toast) {
        Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_LONG).show();
    }

    private void addNewImage(Intent data) {
//        File file = new File(String.valueOf(data.getData()));
//        Date modifiedDate = new Date(file.lastModified());
//        Bitmap bitmap = null;
//        try {
//            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        String[] projection = new String[]{
//                MediaStore.Images.Media._ID,
//                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
//                MediaStore.Images.Media.DATE_TAKEN
//        };
//        Cursor cur = MediaStore.Images.Media.query(this.getContentResolver(), data.getData(), projection);
//        if (cur.moveToFirst())
//        {
//            long date = cur.getLong(cur.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN));
//            Calendar calendar = Calendar.getInstance();
//            calendar.setTimeInMillis(date);
//            modifiedDate = calendar.getTime();
//        }
        final Uri uri = data.getData();
        Log.d(TAG, "Uri: " + uri.toString());
        String uid = mFirebaseUser.getUid();
        ImagePost tempPost = new ImagePost(uid, new Date(), mPhotoUrl);
        DatabaseReference databaseReference = FirebaseDatabase
                .getInstance()
                .getReference("posts")
                .child(uid)
                .child("images")
                .push();
        tempPost.key = databaseReference.getKey();
        databaseReference.setValue(tempPost, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    String key = databaseReference.getKey();
                    StorageReference storageReference = FirebaseStorage
                            .getInstance()
                            .getReference(mFirebaseUser.getUid())
                            .child(key)
                            .child(uri.getLastPathSegment());
                    putImageInStorage(storageReference, uri, key);
                }
            }
        });
    }

    private void putImageInStorage(final StorageReference storageReference, Uri uri, final String key) {
        storageReference.putFile(uri).addOnCompleteListener(MainActivity.this,
                new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            String uid = mFirebaseUser.getUid();
                            @SuppressWarnings("VisibleForTests")
                            ImagePost image = new ImagePost(uid, new Date(), mPhotoUrl, task.getResult().getMetadata().getDownloadUrl().toString());
                            image.key = key;

                            mDatabase.getReference().child("posts").child(uid).child("images").child(key).setValue(image);
                        } else {
                            Log.w(TAG, "Image upload task was not successful.", task.getException());
                        }
                    }
                });
    }

    private void openSettings() {
        Intent settings = new Intent(this, SettingsActivity.class);
        startActivity(settings);
    }

    private void showCalendar() {
        Intent calendar = new Intent(this, JournalCalendar.class);
        startActivityForResult(calendar, SELECT_DATE);
    }

    private void addPicture() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, SELECT_IMAGE);
    }

    private void addNote() {
        Intent newNote = new Intent(this, AddNote.class);
        startActivityForResult(newNote, ADD_NOTE);
    }

    private void navigateHome() {
        Intent home = new Intent(this, MainActivity.class);
        startActivity(home);
    }

    private void navigateToDay(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, 0, 0, 0);
        int closestPost = getIndexOfClosestPost(calendar.getTime());
        Toast.makeText(getApplicationContext(), day + "/" + month + "/" + year, Toast.LENGTH_LONG).show();
        layoutManager.scrollToPosition(closestPost);
    }

    private int getIndexOfClosestPost(Date wantedDate) {
        double closestDistance = Double.MAX_VALUE;
        int closestIndex = -1;
        for (int i = 0; i < posts.size(); ++i) {
            Post post = posts.get(i);
            Date date = post.date;
            double distance = Math.abs(date.getTime() - wantedDate.getTime());
            if (distance < closestDistance) {
                closestDistance = distance;
                closestIndex = i;
            }
        }
        return closestIndex;
    }

    public void attachPostListeners() {
        final String uid = mFirebaseUser.getUid();
        mDatabase.getReference("posts/" + uid + "/facebook_posts").addChildEventListener(new FacebookChildEventListener());
        mDatabase.getReference("posts/" + uid + "/images").addChildEventListener(new ImageChildEventListener());
        mDatabase.getReference("posts/" + uid + "/notes").addChildEventListener(new NoteChildEventListener());
        //mDatabase.getReference("posts/" + uid + "/instagram_posts").addChildEventListener(new InstagramChildEventListener());
        mDatabase.getReference("posts/" + uid + "/tweets").addChildEventListener(new TwitterChildEventListener());
    }

    private abstract class BaseChildEventListener implements ChildEventListener {

        protected abstract Post getPostFromDataSnapshot(DataSnapshot dataSnapshot);

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Post post = getPostFromDataSnapshot(dataSnapshot);
            MainActivity.this.posts.add(post);
            Collections.sort(MainActivity.this.posts, new PostComparatorByDate());
            MainActivity.this.adapter.notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Post post = getPostFromDataSnapshot(dataSnapshot);
            int index = MainActivity.this.posts.indexOf(post);
            if (index != -1) {
                MainActivity.this.posts.set(index, post);
            } else {
                MainActivity.this.posts.add(post);
            }
            Collections.sort(MainActivity.this.posts, new PostComparatorByDate());
            MainActivity.this.adapter.notifyDataSetChanged();
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            Post post = getPostFromDataSnapshot(dataSnapshot);
            MainActivity.this.posts.remove(post);
            Collections.sort(MainActivity.this.posts, new PostComparatorByDate());
            MainActivity.this.adapter.notifyDataSetChanged();
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }

    private class FacebookChildEventListener extends BaseChildEventListener {
        @Override
        protected Post getPostFromDataSnapshot(DataSnapshot dataSnapshot) {
            return dataSnapshot.getValue(FacebookPost.class);
        }
    }

    private class ImageChildEventListener extends BaseChildEventListener {
        @Override
        protected Post getPostFromDataSnapshot(DataSnapshot dataSnapshot) {
            return dataSnapshot.getValue(ImagePost.class);
        }
    }

    private class NoteChildEventListener extends BaseChildEventListener {

        @Override
        protected Post getPostFromDataSnapshot(DataSnapshot dataSnapshot) {
            return dataSnapshot.getValue(NotePost.class);
        }
    }

//    private class InstagramChildEventListener extends BaseChildEventListener {
//
//        @Override
//        protected Post getPostFromDataSnapshot(DataSnapshot dataSnapshot) {
//            // TODO create InstagramPost class
//            return dataSnapshot.getValue(FacebookPost.class);
//        }
//    }

    private class TwitterChildEventListener extends BaseChildEventListener {

        @Override
        protected Post getPostFromDataSnapshot(DataSnapshot dataSnapshot) {
            return dataSnapshot.getValue(TwitterPost.class);
        }
    }
}
