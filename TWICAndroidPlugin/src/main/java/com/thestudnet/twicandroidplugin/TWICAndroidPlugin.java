package com.thestudnet.twicandroidplugin;

import android.content.Context;
import android.util.Log;

import com.squareup.otto.Subscribe;
import com.thestudnet.twicandroidplugin.events.APIInteraction;
import com.thestudnet.twicandroidplugin.events.EventBus;
import com.thestudnet.twicandroidplugin.events.PluginInteraction;
import com.thestudnet.twicandroidplugin.managers.APIClient;
import com.thestudnet.twicandroidplugin.managers.FirebaseClient;
import com.thestudnet.twicandroidplugin.managers.MessagesManager;
import com.thestudnet.twicandroidplugin.managers.SettingsManager;
import com.thestudnet.twicandroidplugin.managers.SocketIoClient;
import com.thestudnet.twicandroidplugin.managers.TokBoxClient;

/**
 * INTERACTIVE LAYER
 * Created by Baptiste PHILIBERT on 14/02/2017.
 */

public class TWICAndroidPlugin {

    private static final String TAG = "com.thestudnet.twicandroidplugin. " + TWICAndroidPlugin.class.getSimpleName();

    public Context getContext() {
        return context;
    }

    private Context context;

    private static TWICAndroidPlugin instance;
    public TWICAndroidPlugin() {
        Log.d(TAG, "TWICAndroidPlugin NEW INSTANCE CREATED AND REGISTERED ON BUS");
        EventBus.getInstance().register(this);
    }
    public static TWICAndroidPlugin getInstance() {
        if(instance == null) {
            TWICAndroidPlugin minstance = new TWICAndroidPlugin();
            instance = minstance;
            return instance;
        } else {
            return instance;
        }
    }

    public TWICAndroidPlugin initContext(Context context) {
        this.context = context;
        return this;
    }

    public TWICAndroidPlugin configure(String settings) {
        SettingsManager.getInstance().configure(settings);
        return this;
    }

    public void launch() {
        APIClient.getInstance().getHangoutData();
    }

    @Subscribe
    public void OnAPIInteraction(APIInteraction.OnAPIInteractionEvent event) {
        if(event.getType() == APIInteraction.Type.ON_HANGOUT_DATA_RECEIVED) {
            Log.d(TAG, "ON_HANGOUT_DATA_RECEIVED");

            APIClient.getInstance().getHangoutUsers();
        }
        else if(event.getType() == APIInteraction.Type.ON_HANGOUT_USERS_RECEIVED) {
            Log.d(TAG, "ON_HANGOUT_USERS_RECEIVED");

            PluginInteraction.getInstance().FireEvent(PluginInteraction.Type.IS_INITIALIZED, null);
        }
    }

    public void onResume() {
        TokBoxClient.getInstance().resumeSession();
    }

    public void onPause() {
        TokBoxClient.getInstance().pauseSession();
    }

    public void onDestroy() {
        SocketIoClient.getInstance().unregisterIoSocket();
        MessagesManager.getInstance().unregisterMessageManager();
//        FirebaseClient.getInstance().unregisterFirebaseClient();
    }

}
