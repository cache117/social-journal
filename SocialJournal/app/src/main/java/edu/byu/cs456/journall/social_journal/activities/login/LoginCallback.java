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
 * A class that implements the common functionality of logging into a Firebase supported
 * authentication provider.
 *
 * @author Cache Staheli
 */
public class LoginCallback implements OnCompleteListener<AuthResult> {
    private final boolean linkToExisting;
    private SharedPreferences sharedPreferences;
    private final String socialNetworkKey;
    private static final String TAG = LoginCallback.class.getCanonicalName();

    /**
     * Instantiates the LoginCallback with the Context, a flag indicating whether or not to link
     * to an existing account, and a socialNetworkKey that represents the key of the preference in
     * DataSync preferences.
     *
     * @param context          the context of the log in.
     * @param linkToExisting   a flag that indicates whether or not this a new log in, or whether it
     *                         should link to the existing user already logged in.
     * @param socialNetworkKey the key for the social network associated with this login.
     */
    public LoginCallback(Context context, boolean linkToExisting, String socialNetworkKey) {
        this.linkToExisting = linkToExisting;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.socialNetworkKey = socialNetworkKey;
    }

    /**
     * Sets the preference for the given social network to true.
     *
     * @param key the key for the social network in the DataSync preferences.
     */
    private void useSocialNetwork(String key) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, true);
        editor.apply();
    }

    @Override
    public void onComplete(@NonNull Task<AuthResult> task) {
        String tagText;
        if (linkToExisting) {
            tagText = "linkWithCredential";
        } else {
            tagText = "signInWithCredential";
        }

        Log.d(TAG, tagText + ":onComplete:" + task.isSuccessful());

        if (task.isSuccessful()) {
            Log.d(TAG, tagText + ":success");
            useSocialNetwork(socialNetworkKey);
        } else {
            Log.d(TAG, tagText + ":failure");
        }
    }
}
