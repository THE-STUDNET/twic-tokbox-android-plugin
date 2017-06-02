package com.thestudnet.twicandroidplugin.managers;

import com.google.firebase.database.FirebaseDatabase;

/**
 * INTERACTIVE LAYER
 * Created by Baptiste PHILIBERT on 27/04/2017.
 */

public class FirebaseClient {

    private static final String TAG = "com.thestudnet.twicandroidplugin.managers." + FirebaseClient.class.getSimpleName();

    private FirebaseDatabase firebaseDatabase;
    public FirebaseDatabase getFirebaseDatabase() {
        return firebaseDatabase;
    }
    public void setFirebaseDatabase(FirebaseDatabase firebaseDatabase) {
        this.firebaseDatabase = firebaseDatabase;
    }

    private static FirebaseClient instance;
    public FirebaseClient() {
    }
    public static FirebaseClient getInstance() {
        if(instance == null) {
            FirebaseClient minstance = new FirebaseClient();
            instance = minstance;
            instance.firebaseDatabase = FirebaseDatabase.getInstance();
            return instance;
        } else {
            return instance;
        }
    }

}
