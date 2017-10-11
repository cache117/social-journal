package edu.byu.cs456.journall.social_journal.activities.login;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

/**
 * Created by cstaheli on 10/9/2017.
 */

public class LoginCallback implements OnCompleteListener<AuthResult> {
    private boolean linkToExisting;
    private SharedPreferences sharedPreferences;
    private String socialNetworkKey;
    public static final String TAG = LoginCallback.class.getCanonicalName();

    public LoginCallback(Context context, boolean linkToExisting, String socialNetworkKey) {
        this.linkToExisting = linkToExisting;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.socialNetworkKey = socialNetworkKey;
    }

    protected void useSocialNetwork(String key) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, true);
        editor.apply();
    }

    protected String getTagText() {
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
            useSocialNetwork(socialNetworkKey);
        } else {
            Log.d(TAG, getTagText() + ":failure");
        }
    }
}
