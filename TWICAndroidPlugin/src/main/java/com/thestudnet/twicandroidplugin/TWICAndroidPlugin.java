package com.thestudnet.twicandroidplugin;

import com.google.firebase.database.FirebaseDatabase;
import com.thestudnet.twicandroidplugin.config.IoSocketConfig;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

/**
 * INTERACTIVE LAYER
 * Created by Baptiste PHILIBERT on 14/02/2017.
 */

public class TWICAndroidPlugin {

    private FirebaseDatabase firebaseDatabase;
    public FirebaseDatabase getFirebaseDatabase() {
        return firebaseDatabase;
    }
    public void setFirebaseDatabase(FirebaseDatabase firebaseDatabase) {
        this.firebaseDatabase = firebaseDatabase;
    }

    private Socket ioSocket;
    public Socket getIoSocket() {
        return ioSocket;
    }
    public void setIoSocket(Socket ioSocket) {
        this.ioSocket = ioSocket;
    }

    private static TWICAndroidPlugin instance;
    public TWICAndroidPlugin() {
    }
    public static TWICAndroidPlugin getInstance() {
        if(instance == null) {
            TWICAndroidPlugin minstance = new TWICAndroidPlugin();
            instance = minstance;
            instance.firebaseDatabase = FirebaseDatabase.getInstance();
            try {
                instance.ioSocket = IO.socket(IoSocketConfig.SERVER_URL);
            }
            catch (URISyntaxException error) {

            }
            return instance;
        } else {
            return instance;
        }
    }

}
