package com.thestudnet.twicandroidplugin.managers;

import android.util.Log;
import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Executor;

/**
 * INTERACTIVE LAYER
 * Created by Baptiste PHILIBERT on 27/04/2017.
 */

public class FirebaseClient {

    private static final String TAG = "com.thestudnet.twicandroidplugin.managers." + FirebaseClient.class.getSimpleName();

    private static FirebaseClient instance;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth firebaseAuth;
    private String token;
    private String hangoutConnectedKey;

    public FirebaseClient() {}

    public static FirebaseClient getInstance() {
        if(instance == null) {
            instance = new FirebaseClient();
            instance.firebaseDatabase = FirebaseDatabase.getInstance();
            instance.firebaseAuth = FirebaseAuth.getInstance();
        }

        try {
            JSONObject firebaseSettings = new JSONObject(SettingsManager.getInstance().getRawValueForKey(SettingsManager.SETTINGS_FIREBASEKEY));
            String token = firebaseSettings.optString(SettingsManager.SETTINGS_TOKENKEY);
            instance.token = token;
        }catch ( JSONException e ){
            Log.e( TAG, e.getLocalizedMessage() );
        }

        return instance;
    }

    public void register(){
        if( FirebaseAuth.getInstance().getCurrentUser() == null ){
            Log.d(TAG, "Not logged => signin");
            signIn();
        }else{
            Log.d(TAG, "Already logged => updateDatabase");
            updateDatabase();
        }
    }

    public void signIn(){
        firebaseAuth.signInWithCustomToken( token )
            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCustomToken:success");
                        FirebaseClient.getInstance().updateDatabase();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCustomToken:failure", task.getException());
                    }
                }
            });
    }

    public void updateDatabase(){
        Log.d(TAG, "updateDatabase");
        String user_id = SettingsManager.getInstance().getRawValueForKey(SettingsManager.SETTINGS_USERIDKEY);
        String hangout_id = SettingsManager.getInstance().getRawValueForKey(SettingsManager.SETTINGS_HANGOUTIDKEY);
        // Set current hangout for user.
        DatabaseReference user_hgt_ref = firebaseDatabase.getReference( "current_hangout/" + user_id );
        user_hgt_ref.setValue( hangout_id );
        // Add user in hangout user's list.
        DatabaseReference hgt_ref = firebaseDatabase.getReference( "hangouts/" + hangout_id + "/connecteds" ).push();
        hangoutConnectedKey = hgt_ref.getKey();
        Log.d(TAG, "hangoutConnectedsKey: "+hangoutConnectedKey);
        hgt_ref.setValue( user_id );
    }

    public void unregisterFirebaseClient() {
        Log.d(TAG, "signOut");
        String user_id = SettingsManager.getInstance().getRawValueForKey(SettingsManager.SETTINGS_USERIDKEY);
        String hangout_id = SettingsManager.getInstance().getRawValueForKey(SettingsManager.SETTINGS_HANGOUTIDKEY);
        // Remove hangout from user current hangout
        firebaseDatabase.getReference("current_hangout/" + user_id).removeValue();
        // Remove user from hangout connected users.
        Log.d(TAG, "GettingConnectedsKey: "+hangoutConnectedKey);
        if( hangoutConnectedKey != null ){
            firebaseDatabase.getReference("hangouts/" + hangout_id + "/connecteds").child(hangoutConnectedKey).removeValue();
        }
        // Sign out.
        FirebaseAuth.getInstance().signOut();
    }

}
