package edu.byu.cs456.journall.social_journal;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    static final int SELECT_IMAGE = 1;
    static final int SELECT_DATE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

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
        WebView firstPost = (WebView) findViewById(R.id.post1);
        String data = "<iframe src=\"https://www.facebook.com/plugins/post.php?href=https%3A%2F%2Fwww.facebook.com%2FStudentProblems%2Fposts%2F1184336055026459%3A0&width=500\" width=\"500\" height=\"589\" style=\"border:none;overflow:hidden\" scrolling=\"no\" frameborder=\"0\" allowTransparency=\"true\"></iframe>";
        firstPost.loadDataWithBaseURL("https://www.facebook.com/", data, "text/html", "utf-8", null);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
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
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    Toast.makeText(getApplicationContext(), "Cancelled", Toast.LENGTH_SHORT).show();
                }
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
        }
    }

    private void openSettings() {
        //Intent settings = new Intent(this, Settings.class);
//        startActivity(settings);
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
        startActivity(newNote);
    }

    private void navigateHome() {
        Intent home = new Intent(this, MainActivity.class);
        startActivity(home);
    }

    private void addNewImage(Bitmap image) {
        Toast.makeText(getApplicationContext(), "Inserting Picture", Toast.LENGTH_LONG).show();
    }

    private void navigateToDay(int year, int month, int day) {
        Toast.makeText(getApplicationContext(), day + "/" + month + "/" + year, Toast.LENGTH_LONG).show();
    }
}
