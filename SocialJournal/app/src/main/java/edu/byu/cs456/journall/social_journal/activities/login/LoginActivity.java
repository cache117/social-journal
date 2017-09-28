package edu.byu.cs456.journall.social_journal.activities.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.TwitterAuthProvider;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import edu.byu.cs456.journall.social_journal.R;
import edu.byu.cs456.journall.social_journal.activities.main.MainActivity;

/**
 * A login screen that offers login via Facebook.
 *
 * @author Cache Staheli
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "Login";
    private CallbackManager mCallbackManager;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private TwitterLoginButton mLoginButton;
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        PreferenceManager.setDefaultValues(this, R.xml.pref_data_sync, false);
        initializeFirebaseAuth();
        new FacebookAuthMethods().initializeFacebookLogin();
        new TwitterAuthMethods().initializeTwitterLogin();
    }

    private void initializeFirebaseAuth() {
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Intent mainActivity = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(mainActivity);
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
//                    signOut();
                }
                // ...
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void useSocialNetwork(String key) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, true);
        editor.apply();
    }

    private class FacebookAuthMethods {

        void initializeFacebookLogin() {
            // Initialize Facebook Login button
            mCallbackManager = CallbackManager.Factory.create();
            LoginButton loginButton = (LoginButton) findViewById(R.id.button_facebook_login);
            loginButton.setReadPermissions("email", "public_profile", "user_posts", "user_photos");
            loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    Log.d(TAG, "facebook:onSuccess:" + loginResult);
                    handleFacebookAccessToken(loginResult.getAccessToken());
                }

                @Override
                public void onCancel() {
                    Log.d(TAG, "facebook:onCancel");
                    // ...
                }

                @Override
                public void onError(FacebookException error) {
                    Log.w(TAG, "facebook:onError", error);
                }
            });
        }

        private class FacebookOnCompleteListener implements OnCompleteListener<AuthResult> {
            private boolean linkToExisting;

            public FacebookOnCompleteListener(boolean linkToExisting) {
                this.linkToExisting = linkToExisting;
            }

            private String getTagText() {
                if (linkToExisting) {
                    return "linkWithCredential";
                } else {
                    return "signInWithCredential";
                }
            }

            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(TAG, getTagText() + ":onComplete:" + task.isSuccessful());

                if (task.isSuccessful()) {
                    Log.d(TAG, getTagText() + ":success");
                    useSocialNetwork("connect_to_facebook");
                } else {
                    Log.d(TAG, getTagText() + ":failure");
                }
            }
        }

        private void handleFacebookAccessToken(AccessToken token) {
            Log.d(TAG, "handleFacebookAccessToken:" + token);

            AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
            if (mAuth.getCurrentUser() == null) {
                mAuth.signInWithCredential(credential)
                        .addOnCompleteListener(LoginActivity.this, new FacebookOnCompleteListener(false));
            } else {
                mAuth.getCurrentUser()
                        .linkWithCredential(credential)
                        .addOnCompleteListener(LoginActivity.this, new FacebookOnCompleteListener(true));
            }
        }
    }

    private class TwitterAuthMethods {

        void initializeTwitterLogin() {
            Twitter.initialize(LoginActivity.this);
            mLoginButton = (TwitterLoginButton) findViewById(R.id.login_button);
            mLoginButton.setCallback(new Callback<TwitterSession>() {
                @Override
                public void success(Result<TwitterSession> result) {
                    // Do something with result, which provides a TwitterSession for making API calls
                    Log.d(TAG, "twitterLogin:success" + result);
                    handleTwitterSession(result.data);
                }

                @Override
                public void failure(TwitterException exception) {
                    Log.w(TAG, "twitterLogin:failure", exception);
                }
            });
        }

        private class TwitterOnCompleteListener implements OnCompleteListener<AuthResult> {
            private boolean linkToExisting;

            public TwitterOnCompleteListener(boolean linkToExisting) {
                this.linkToExisting = linkToExisting;
            }

            private String getTagText() {
                if (linkToExisting) {
                    return "linkWithCredential";
                } else {
                    return "signInWithCredential";
                }
            }

            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(TAG, getTagText() + ":onComplete:" + task.isSuccessful());

                if (task.isSuccessful()) {
                    Log.d(TAG, getTagText() + ":success");
                    useSocialNetwork("connect_to_twitter");
                } else {
                    Log.d(TAG, getTagText() + ":failure");
                }
            }
        }

        private void handleTwitterSession(TwitterSession session) {
            AuthCredential credential = TwitterAuthProvider.getCredential(session.getAuthToken().token, session.getAuthToken().secret);
            if (mAuth.getCurrentUser() == null) {
                mAuth.signInWithCredential(credential)
                        .addOnCompleteListener(LoginActivity.this, new TwitterOnCompleteListener(false));
            } else {
                mAuth.getCurrentUser()
                        .linkWithCredential(credential)
                        .addOnCompleteListener(LoginActivity.this,  new TwitterOnCompleteListener(true));
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result to the Twitter login button.
        mLoginButton.onActivityResult(requestCode, resultCode, data);
    }

    public void signOut() {
        mAuth.signOut();
        LoginManager.getInstance().logOut();
//        TwitterCore.getInstance().getSessionManager().clearActiveSession();
    }
}

