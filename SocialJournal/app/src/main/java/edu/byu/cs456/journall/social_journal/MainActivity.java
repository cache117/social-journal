package edu.byu.cs456.journall.social_journal;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.byu.cs456.journall.social_journal.post.FacebookPost;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private final String TAG = MainActivity.class.getCanonicalName();
    static final int SELECT_IMAGE = 1;
    static final int SELECT_DATE = 2;
    static final int ADD_NOTE = 3;

    private RecyclerView myRecyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private List<String> posts = getPosts();
    private StorageReference mStorageRef;
    private FirebaseDatabase mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        myRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        myRecyclerView.setHasFixedSize(false);

        layoutManager = new LinearLayoutManager(this);
        myRecyclerView.setLayoutManager(layoutManager);

        //posts = getPosts();

        adapter = new MyAdapter(posts, this);
        myRecyclerView.setAdapter(adapter);

        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance();
        syncUserProfiles();
        onBoarding();
    }

    private void syncUserProfiles() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            DatabaseReference users = FirebaseDatabase.getInstance().getReference("/users");
            users.child(uid).child("email").setValue(user.getEmail());
            users.child(uid).child("name").setValue(user.getDisplayName());
            for (UserInfo userInfo : user.getProviderData()) {
                if (!userInfo.getProviderId().equals("firebase")) {
                    users.child(uid).child(userInfo.getProviderId().split("\\.")[0]).setValue(userInfo.getUid());
                }
            }
        }
    }


    private void onBoarding() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            final String uid = user.getUid();
            FirebaseDatabase.getInstance().getReference("/users").child(uid).child("onboarding").addListenerForSingleValueEvent(new ValueEventListener() {
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
                            int length = data.length() > 5 ? 5 : data.length();
                            for (int i = 0; i < length; ++i)
                            {
                                JSONObject row = data.getJSONObject(i);
                                String createdTime = (String) row.get("created_time");

                                String postId = (String) row.get("id");
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH);
                                Date date = dateFormat.parse(createdTime);
                                FacebookPost post = new FacebookPost(uid, date, postId);
                                FirebaseDatabase.getInstance().getReference("/posts").push().setValue(post);
                            }
                        FirebaseDatabase.getInstance().getReference("/users").child(uid).child("onboarding").setValue(true);

                        } catch (JSONException | ParseException e) {
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
    private List<String> getPosts() {
        List<String> listOfPosts = new ArrayList<String>();

        listOfPosts.add("<iframe src=\"https://www.facebook.com/plugins/post.php?href=https%3A%2F%2Fwww.facebook.com%2FStudentProblems%2Fposts%2F1184336055026459%3A0&width=500\" width=\"500\" height=\"589\" style=\"border:none;overflow:hidden\" scrolling=\"no\" frameborder=\"0\" allowTransparency=\"true\"></iframe>");
        listOfPosts.add("<iframe src=\"https://www.facebook.com/plugins/post.php?href=https%3A%2F%2Fwww.facebook.com%2Fverycleanfunnypics%2Fposts%2F1564340500243860%3A0&width=500\" width=\"500\" height=\"502\" style=\"border:none;overflow:hidden\" scrolling=\"no\" frameborder=\"0\" allowTransparency=\"true\"></iframe>");
        listOfPosts.add("<iframe src=\"https://www.facebook.com/plugins/post.php?href=https%3A%2F%2Fwww.facebook.com%2Fmcall2%2Fposts%2F10154420774477759&width=500\" width=\"500\" height=\"607\" style=\"border:none;overflow:hidden\" scrolling=\"no\" frameborder=\"0\" allowTransparency=\"true\"></iframe>");
        listOfPosts.add("<iframe src=\"https://www.facebook.com/plugins/post.php?href=https%3A%2F%2Fwww.facebook.com%2Fmcall2%2Fposts%2F10154301387472759&width=500\" width=\"500\" height=\"442\" style=\"border:none;overflow:hidden\" scrolling=\"no\" frameborder=\"0\" allowTransparency=\"true\"></iframe>");
        listOfPosts.add("<iframe src=\"https://www.facebook.com/plugins/post.php?href=https%3A%2F%2Fwww.facebook.com%2Fmcall2%2Ftimeline%2Fstory%3Fut%3D32%26wstart%3D-2051193600%26wend%3D2147483647%26hash%3D10151102807557759%26pagefilter%3D3%26ustart%3D1&width=500\" width=\"500\" height=\"249\" style=\"border:none;overflow:hidden\" scrolling=\"no\" frameborder=\"0\" allowTransparency=\"true\"></iframe>");

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
            case R.id.nav_show_calendar:
                showCalendar();
                //send to calendar page
                break;
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
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                            addNewImage(bitmap);
                            adapter.notifyDataSetChanged();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
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

    private void addNewNote(String noteTitle, String newNote) {
        Toast.makeText(getApplicationContext(), "Adding Note", Toast.LENGTH_LONG).show();
        String titleAndBody;
        Log.d("NOTE", "noteTitle is " + noteTitle);
        if (noteTitle != null) {
            titleAndBody = noteTitle + "(@)" + newNote;
        } else {
            titleAndBody = newNote;
        }
        posts.add(0, titleAndBody);
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
//        Intent pictures = new Intent(this, AddPicture.class);
//        startActivity(pictures);
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_IMAGE);
    }

    private void addNote() {
        Intent newNote = new Intent(this, AddNote.class);
        startActivityForResult(newNote, ADD_NOTE);
    }

    private void navigateHome() {
        Intent home = new Intent(this, MainActivity.class);
        startActivity(home);
    }

    private void addNewImage(Bitmap image) {
        String imageAsString = BitMapToString(image);
        String preface = "THIS IS A BITMAP"; //insert this before the bits in a bitmap
        posts.add(0, preface + imageAsString);
        Log.d("Debug", "imageAsString == " + imageAsString);
        Toast.makeText(getApplicationContext(), "Adding Image", Toast.LENGTH_LONG).show();
    }

    private void navigateToDay(int year, int month, int day) {
        Toast.makeText(getApplicationContext(), day + "/" + month + "/" + year, Toast.LENGTH_LONG).show();
    }

    private String BitMapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String temp = Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    private String generateFacebookPostHTML(String userId, String postId) throws UnsupportedEncodingException {
        //String iframeFront = String.format("<iframe src=\"https://www.facebook.com/plugins/post.php?href=%s", generateFacebookPostUrl(userId, postId), );
        return "https://www.facebook.com/" + userId + "/posts/" + postId;
    }

    private String generateFacebookPostUrl(String userId, String postId) throws UnsupportedEncodingException {
        return URLEncoder.encode("https://www.facebook.com/" + userId + "/posts/" + postId, "UTF-8");
    }
}
