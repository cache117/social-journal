package edu.byu.cs456.journall.social_journal;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.byu.cs456.journall.social_journal.post.FacebookPost;
import edu.byu.cs456.journall.social_journal.post.ImagePost;
import edu.byu.cs456.journall.social_journal.post.NotePost;
import edu.byu.cs456.journall.social_journal.post.Post;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private final String TAG = MainActivity.class.getCanonicalName();
    static final int SELECT_IMAGE = 1;
    static final int SELECT_DATE = 2;
    static final int ADD_NOTE = 3;

    private static final String LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif";

    private RecyclerView myRecyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private List<Post> posts;
    private FirebaseDatabase mDatabase;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private String mPhotoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = initializeToolbar();

        //Javascript that https://developers.facebook.com/docs/plugins/embedded-posts said was necessary
        //It is not necessary when clicking "embed" on a post but it might be needed
        //if we change the way we embed pictures to use the Graph API
//        WebView init = (WebView) findViewById(R.id.init_js);
//        String initData = "<div id=\"fb-root\"></div>\n" +
//                "<script>(function(d, s, id) {\n" +
//                "  var js, fjs = d.getElementsByTagName(s)[0];\n" +
//                "  if (d.getElementById(id)) return;\n" +
//                "  js = d.createElement(s); js.id = id;\n" +
//                "  js.src = \"//connect.facebook.net/en_US/sdk.js#xfbml=1&version=v2.8&appId=121029951766180\";\n" +
//                "  fjs.parentNode.insertBefore(js, fjs);\n" +
//                "}(document, 'script', 'facebook-jssdk'));</script>";
//        init.loadData(initData, "text/html", null);

        //Funny post about stress
//        WebView firstPost = (WebView) findViewById(R.id.post1);
//        String data = "<iframe src=\"https://www.facebook.com/plugins/post.php?href=https%3A%2F%2Fwww.facebook.com%2FStudentProblems%2Fposts%2F1184336055026459%3A0&width=500\" width=\"500\" height=\"589\" style=\"border:none;overflow:hidden\" scrolling=\"no\" frameborder=\"0\" allowTransparency=\"true\"></iframe>";
//        firstPost.loadDataWithBaseURL("https://www.facebook.com/", data, "text/html", "utf-8", null);

        initializeDrawer(toolbar);
        initializeNavigationView();

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();
        if (mFirebaseUser != null) {
            syncUserProfiles();
            if (mFirebaseUser.getPhotoUrl() != null) {
                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            }
            onBoarding();
            posts = getFacebookPosts();
            getPostsFromDatabase();

            setUpRecyclerView();
        }
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

        adapter = new MyAdapter(posts, this);
        myRecyclerView.setAdapter(adapter);
    }

    private void syncUserProfiles() {
        String uid = mFirebaseUser.getUid();
        DatabaseReference users = mDatabase.getReference("/users");
        users.child(uid).child("email").setValue(mFirebaseUser.getEmail());
        users.child(uid).child("name").setValue(mFirebaseUser.getDisplayName());
        for (UserInfo userInfo : mFirebaseUser.getProviderData()) {
            if (!userInfo.getProviderId().equals("firebase")) {
                users.child(uid).child(userInfo.getProviderId().split("\\.")[0]).setValue(userInfo.getUid());
            }
        }
    }

    private void onBoarding() {
        final String uid = mFirebaseUser.getUid();
        mDatabase.getReference("/users").child(uid).child("onboarding").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    importExistingPosts(uid);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void importExistingPosts(final String uid) {
        /* make the API call */
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/feed",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        /* handle the result */
                        Log.d(TAG, response.toString());
                        try {
                            JSONArray data = (JSONArray) response.getJSONObject().get("data");
                            if (data.length() != 0) {
                                int length = data.length() > 2 ? 2 : data.length();
                                for (int i = 0; i < length; ++i) {
                                    JSONObject row = data.getJSONObject(i);
                                    String createdTime = (String) row.get("created_time");

                                    String postId = (String) row.get("id");
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH);
                                    Date date = dateFormat.parse(createdTime);
                                    FacebookPost post = new FacebookPost(uid, date, postId);
                                    mDatabase.getReference("/posts/" + uid + "/facebook_posts").push().setValue(post);
                                }
                                mDatabase.getReference("/users").child(uid).child("onboarding").setValue(true);
                            } else {
                                throw new Exception("This app needs access to posts, and isn't getting them");
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
        ).executeAsync();
    }

    /**
     * Initializes posts displayed on home page
     *
     * @return List of Strings. Strings represent each post.
     */
    private List<Post> getFacebookPosts() {
        List<Post> listOfPosts = new ArrayList<>();
        FacebookPost post1 = new FacebookPost();
        post1.url = "<iframe src=\"https://www.facebook.com/plugins/post.php?href=https%3A%2F%2Fwww.facebook.com%2FStudentProblems%2Fposts%2F1184336055026459%3A0&width=500\" width=\"500\" height=\"589\" style=\"border:none;overflow:hidden\" scrolling=\"no\" frameborder=\"0\" allowTransparency=\"true\"></iframe>";
        listOfPosts.add(post1);
        FacebookPost post2 = new FacebookPost();
        post2.url = "<iframe src=\"https://www.facebook.com/plugins/post.php?href=https%3A%2F%2Fwww.facebook.com%2Fverycleanfunnypics%2Fposts%2F1564340500243860%3A0&width=500\" width=\"500\" height=\"502\" style=\"border:none;overflow:hidden\" scrolling=\"no\" frameborder=\"0\" allowTransparency=\"true\"></iframe>";
        listOfPosts.add(post2);
        FacebookPost post3 = new FacebookPost();
        post3.url = "<iframe src=\"https://www.facebook.com/plugins/post.php?href=https%3A%2F%2Fwww.facebook.com%2Fmcall2%2Fposts%2F10154420774477759&width=500\" width=\"500\" height=\"607\" style=\"border:none;overflow:hidden\" scrolling=\"no\" frameborder=\"0\" allowTransparency=\"true\"></iframe>";
        listOfPosts.add(post3);
        FacebookPost post4 = new FacebookPost();
        post4.url = "<iframe src=\"https://www.facebook.com/plugins/post.php?href=https%3A%2F%2Fwww.facebook.com%2Fmcall2%2Fposts%2F10154301387472759&width=500\" width=\"500\" height=\"442\" style=\"border:none;overflow:hidden\" scrolling=\"no\" frameborder=\"0\" allowTransparency=\"true\"></iframe>";
        listOfPosts.add(post4);
        FacebookPost post5 = new FacebookPost();
        post5.url = "<iframe src=\"https://www.facebook.com/plugins/post.php?href=https%3A%2F%2Fwww.facebook.com%2Fmcall2%2Ftimeline%2Fstory%3Fut%3D32%26wstart%3D-2051193600%26wend%3D2147483647%26hash%3D10151102807557759%26pagefilter%3D3%26ustart%3D1&width=500\" width=\"500\" height=\"249\" style=\"border:none;overflow:hidden\" scrolling=\"no\" frameborder=\"0\" allowTransparency=\"true\"></iframe>";
        listOfPosts.add(post5);

//        listOfPosts.add("<iframe src=\"https://www.facebook.com/plugins/post.php?href=https%3A%2F%2Fwww.facebook.com%2FStudentProblems%2Fposts%2F1184336055026459%3A0&width=500\" width=\"500\" height=\"589\" style=\"border:none;overflow:hidden\" scrolling=\"no\" frameborder=\"0\" allowTransparency=\"true\"></iframe>");
//        listOfPosts.add("<iframe src=\"https://www.facebook.com/plugins/post.php?href=https%3A%2F%2Fwww.facebook.com%2Fverycleanfunnypics%2Fposts%2F1564340500243860%3A0&width=500\" width=\"500\" height=\"502\" style=\"border:none;overflow:hidden\" scrolling=\"no\" frameborder=\"0\" allowTransparency=\"true\"></iframe>");
//        listOfPosts.add("<iframe src=\"https://www.facebook.com/plugins/post.php?href=https%3A%2F%2Fwww.facebook.com%2Fmcall2%2Fposts%2F10154420774477759&width=500\" width=\"500\" height=\"607\" style=\"border:none;overflow:hidden\" scrolling=\"no\" frameborder=\"0\" allowTransparency=\"true\"></iframe>");
//        listOfPosts.add("<iframe src=\"https://www.facebook.com/plugins/post.php?href=https%3A%2F%2Fwww.facebook.com%2Fmcall2%2Fposts%2F10154301387472759&width=500\" width=\"500\" height=\"442\" style=\"border:none;overflow:hidden\" scrolling=\"no\" frameborder=\"0\" allowTransparency=\"true\"></iframe>");
//        listOfPosts.add("<iframe src=\"https://www.facebook.com/plugins/post.php?href=https%3A%2F%2Fwww.facebook.com%2Fmcall2%2Ftimeline%2Fstory%3Fut%3D32%26wstart%3D-2051193600%26wend%3D2147483647%26hash%3D10151102807557759%26pagefilter%3D3%26ustart%3D1&width=500\" width=\"500\" height=\"249\" style=\"border:none;overflow:hidden\" scrolling=\"no\" frameborder=\"0\" allowTransparency=\"true\"></iframe>");

        return listOfPosts;
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
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            openSettings();
            return true;
        }
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
//            case R.id.nav_show_calendar:
//                showCalendar();
//                //send to calendar page
//                break;
            case R.id.nav_settings:
                //send to settings page
                openSettings();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
                        adapter.notifyDataSetChanged();
                    }
                }
                break;
        }
    }


    private void putImageInStorage(StorageReference storageReference, Uri uri, final String key) {
        storageReference.putFile(uri).addOnCompleteListener(MainActivity.this,
                new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            String uid = mFirebaseUser.getUid();
                            @SuppressWarnings("VisibleForTests")
                            ImagePost image = new ImagePost(uid, new Date(), mPhotoUrl, task.getResult().getMetadata().getDownloadUrl().toString());

                            mDatabase.getReference().child("post").child(uid).child("images").child(key).setValue(image);
                        } else {
                            Log.w(TAG, "Image upload task was not successful.", task.getException());
                        }
                    }
                });
    }

    private void addNewNote(String noteTitle, String newNote) {
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        if (user != null) {
            final String uid = user.getUid();
            NotePost post = new NotePost(uid, new Date(), noteTitle, newNote);
            mDatabase.getReference("/posts/" + uid + "/notes").push().setValue(post);
            Toast.makeText(getApplicationContext(), "Adding Note", Toast.LENGTH_LONG).show();
//            posts.add(0, post.toString());

            Log.d(TAG, "Note is " + post.toString());
        }

    }

    private void addNewImage(Intent data) {
        final Uri uri = data.getData();
        Log.d(TAG, "Uri: " + uri.toString());
        String uid = mFirebaseUser.getUid();
        ImagePost tempPost = new ImagePost(uid, new Date(), mPhotoUrl);
        FirebaseDatabase
                .getInstance()
                .getReference("posts")
                .child(uid)
                .child("images")
                .push()
                .setValue(tempPost, new DatabaseReference.CompletionListener() {
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
        Toast.makeText(getApplicationContext(), day + "/" + month + "/" + year, Toast.LENGTH_LONG).show();
    }

    private String generateFacebookPostHTML(String userId, String postId) throws UnsupportedEncodingException {
        //String iframeFront = String.format("<iframe src=\"https://www.facebook.com/plugins/post.php?href=%s", generateFacebookPostUrl(userId, postId), );
        return "https://www.facebook.com/" + userId + "/posts/" + postId;
    }

    private String generateFacebookPostUrl(String userId, String postId) throws UnsupportedEncodingException {
        return URLEncoder.encode("https://www.facebook.com/" + userId + "/posts/" + postId, "UTF-8");
    }

    public void getPostsFromDatabase() {
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        if (user != null) {
            final String uid = user.getUid();
            mDatabase.getReference("posts/" + uid + "/facebook_posts").addChildEventListener(new FacebookChildEventListener());
            mDatabase.getReference("posts/" + uid + "/images").addChildEventListener(new ImageChildEventListener());
            mDatabase.getReference("posts/" + uid + "/notes").addChildEventListener(new NoteChildEventListener());
        }
    }

    private class FacebookChildEventListener implements ChildEventListener {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Post post = dataSnapshot.getValue(FacebookPost.class);
            MainActivity.this.posts.add(0, post);
            MainActivity.this.adapter.notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Post post = dataSnapshot.getValue(FacebookPost.class);
            MainActivity.this.posts.set(MainActivity.this.posts.indexOf(post), post);
            MainActivity.this.adapter.notifyDataSetChanged();
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            Post post = dataSnapshot.getValue(FacebookPost.class);
            MainActivity.this.posts.remove(post);
            MainActivity.this.adapter.notifyDataSetChanged();
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }

    private class ImageChildEventListener implements ChildEventListener {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Post post = dataSnapshot.getValue(ImagePost.class);
            MainActivity.this.posts.add(0, post);
            MainActivity.this.adapter.notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Post post = dataSnapshot.getValue(ImagePost.class);
            MainActivity.this.posts.set(MainActivity.this.posts.indexOf(post), post);
            MainActivity.this.adapter.notifyDataSetChanged();
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            Post post = dataSnapshot.getValue(ImagePost.class);
            MainActivity.this.posts.remove(post);
            MainActivity.this.adapter.notifyDataSetChanged();
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }

    private class NoteChildEventListener implements ChildEventListener {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Post post = dataSnapshot.getValue(NotePost.class);
            MainActivity.this.posts.add(0, post);
            MainActivity.this.adapter.notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Post post = dataSnapshot.getValue(NotePost.class);
            MainActivity.this.posts.set(MainActivity.this.posts.indexOf(post), post);
            MainActivity.this.adapter.notifyDataSetChanged();
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            Post post = dataSnapshot.getValue(NotePost.class);
            MainActivity.this.posts.remove(post);
            MainActivity.this.adapter.notifyDataSetChanged();
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }
}
