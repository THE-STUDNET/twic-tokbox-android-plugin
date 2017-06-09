package com.thestudnet.twicandroidplugin.managers;

import android.content.ContentValues;
import android.util.Log;

import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.Connection;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;
import com.opentok.android.SubscriberKit;
import com.squareup.otto.Subscribe;
import com.thestudnet.twicandroidplugin.TWICAndroidPlugin;
import com.thestudnet.twicandroidplugin.events.APIInteraction;
import com.thestudnet.twicandroidplugin.events.EventBus;
import com.thestudnet.twicandroidplugin.events.TokBoxInteraction;
import com.thestudnet.twicandroidplugin.models.GenericModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * INTERACTIVE LAYER
 * Created by Baptiste PHILIBERT on 27/04/2017.
 */

public class TokBoxClient implements Session.SessionListener, Session.ConnectionListener, Publisher.PublisherListener, Subscriber.VideoListener, Session.SignalListener {

    private static final String TAG = "com.thestudnet.twicandroidplugin.managers." + TokBoxClient.class.getSimpleName();

    public static String SIGNALTYPE_CAMERAAUTHORIZATION           = "hgt_camera_authorization";
    public static String SIGNALTYPE_CANCELCAMERAAUTHORIZATION     = "hgt_cancel_camera_authorization";
    public static String SIGNALTYPE_CANCELMICROPHONEAUTHORIZATION = "hgt_cancel_microphone_authorization";
    public static String SIGNALTYPE_MICROPHONEAUTHORIZATION       = "hgt_microphone_authorization";
    public static String SIGNALTYPE_CAMERAREQUESTED               = "hgt_camera_requested";
    public static String SIGNALTYPE_MICROPHONEREQUESTED           = "hgt_microphone_requested";
    public static String SIGNALTYPE_FORCEMUTESTREAM               = "hgt_force_mute_stream";
    public static String SIGNALTYPE_FORCEUNMUTESTREAM             = "hgt_force_unmute_stream";

    private Session session;
    private AtomicBoolean isConnected = new AtomicBoolean(false);

    public Publisher getPublisher() {
        return publisher;
    }

    public LinkedHashMap<String, Subscriber> getSubscribers() {
        return subscribers;
    }

    private Publisher publisher;
    private LinkedHashMap<String, Subscriber> subscribers = new LinkedHashMap<>();

    private static TokBoxClient instance;
    public TokBoxClient() {
    }
    public static TokBoxClient getInstance() {
        if(instance == null) {
            TokBoxClient minstance = new TokBoxClient();
            EventBus.getInstance().register(minstance);
            instance = minstance;
            return instance;
        } else {
            return instance;
        }
    }

    public void connectSession() {
        APIClient.getInstance().getTokBoxData();
    }

    public void pauseSession() {
        if(this.session != null) {
            this.session.onPause();
        }
    }

    public void resumeSession() {
        if(this.session != null) {
            this.session.onResume();
        }
    }

    public void disconnectSession() {

        if(this.subscribers != null && this.subscribers.size() > 0) {
            Iterator<Subscriber> iterator = this.subscribers.values().iterator();
            while (iterator.hasNext()) {
                Subscriber subscriber = iterator.next();
                session.unsubscribe(subscriber);
                subscriber.destroy();
            }
            this.subscribers = null;
        }

        if(this.publisher != null) {
            session.unpublish(publisher);
            publisher.destroy();
            publisher = null;
        }

        if (session != null) {
            session.disconnect();
        }

        EventBus.getInstance().unregister(this);
    }

    @Subscribe
    public void OnAPIInteraction(APIInteraction.OnAPIInteractionEvent event) {
        if(event.getType() == APIInteraction.Type.ON_TOKBOX_DATA_RECEIVED) {
            Log.d(TAG, "ON_TOKBOX_DATA_RECEIVED");

            if(event.getData() != null && event.getData().size() > 0) {
                GenericModel result = (GenericModel) event.getData().get(0);
                session = new Session(TWICAndroidPlugin.getInstance().getContext(), SettingsManager.getInstance().getRawValueForKey(SettingsManager.SETTINGS_TOKBOXAPIKEY), result.getContentValue("session"));
                session.setSessionListener(this);
                session.setConnectionListener(this);
                session.setSignalListener(this);
                session.connect(result.getContentValue("token"));
            }
        }
    }

    /**************** SESSION ****************/

    @Override
    public void onConnected(Session session) {
        Log.d(TAG, "onConnected: Connected to session " + session.getSessionId());

        if(this.isConnected.get() == false) {
            TokBoxInteraction.getInstance().FireEvent(TokBoxInteraction.Type.ON_SESSION_CONNECTED, null);

            // TODO : Write in firebase user is connected

            // Register "hangout.join" event with API
            APIClient.getInstance().sendUserJoin();

            // Stop listening to "sessionConnected" tokbox event
            this.isConnected.set(true);

            // Check publish (and auto-publish) permissions
            this.checkPublishPermissions();
        }
    }

    @Override
    public void onDisconnected(Session session) {
        Log.d(TAG, "onDisconnected: disconnected from session " + session.getSessionId());

        this.session = null;

        TokBoxInteraction.getInstance().FireEvent(TokBoxInteraction.Type.ON_SESSION_DISCONNECTED, null);
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Log.d(TAG, "onError: Error (" + opentokError.getMessage() + ") in session " + session.getSessionId());
    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Log.d(TAG, "onStreamReceived: New stream " + stream.getStreamId() + " in session " + session.getSessionId());
        
        registerSubscriberStream(stream);
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.d(TAG, "onStreamDropped: Stream " + stream.getStreamId() + " dropped from session " + session.getSessionId());

        if(this.subscribers.containsKey(stream.getStreamId())) {
            this.session.unsubscribe(this.subscribers.get(stream.getStreamId()));
            this.subscribers.get(stream.getStreamId()).destroy();
            this.subscribers.remove(stream.getStreamId());

            ArrayList<GenericModel> list = new ArrayList<>(1);
            ContentValues contentValues = new ContentValues();
            contentValues.put("id", stream.getStreamId());
            list.add(new GenericModel(contentValues));
            TokBoxInteraction.getInstance().FireEvent(TokBoxInteraction.Type.ON_SUBSCRIBER_REMOVED, list);
        }
    }

    private void checkPublishPermissions() {
        if(UserManager.getInstance().hasPublishPermission(UserManager.getInstance().getCurrentUserId())) {
            publisher = new Publisher.Builder(TWICAndroidPlugin.getInstance().getContext()).build();
            publisher.setPublisherListener(this);
            publisher.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);
            this.session.publish(publisher);
        }
    }

    /**************** END SESSION ****************/



    /**************** CONNECTION ****************/

    @Override
    public void onConnectionCreated(Session session, Connection connection) {
        Log.d(TAG, "onConnectionCreated: connection data = " + connection.getData());

        JSONObject user = null;

        try {
            user = new JSONObject(connection.getData());
        }
        catch (JSONException e) {
            Log.e(TAG, "onConnectionCreated: exception : " + e.getLocalizedMessage());
        }

        if(user != null && user.has("id") && !"".equals(user.optString("id"))) {
            String userId = user.optString("id");
            // Check if user is in users list
            if(UserManager.getInstance().containsKey(userId)) {
                // YES
                // Check if user is YOU
                if(!UserManager.getInstance().getCurrentUserId().equals(userId)) {
                    // NO
                    try {
                        // Set user connection state to connected
                        JSONObject updatedUser = UserManager.getInstance().getSettingsForKey(userId).put(UserManager.USER_LOCAL_CONNECTIONSTATEKEY, "connected");
                        UserManager.getInstance().addOrReplace(userId, updatedUser.toString());
                    }
                    catch (JSONException e) {
                        Log.e(TAG, "onConnectionCreated: exception : " + e.getLocalizedMessage());
                    }
                    APIInteraction.getInstance().FireEvent(APIInteraction.Type.ON_USER_CONNECTION_STATE_CHANGED, null);
                    // TODO Add "User joined" notification message in conversation panel
                }
            }
            else {
                // NO
                // Get User from API
                APIClient.getInstance().getNewUsers(userId);
            }
        }
    }

    @Override
    public void onConnectionDestroyed(Session session, Connection connection) {
        JSONObject user = null;

        try {
            user = new JSONObject(connection.getData());
        }
        catch (JSONException e) {
            Log.e(TAG, "onConnectionCreated: exception : " + e.getLocalizedMessage());
        }

        if(user != null && user.has("id") && !"".equals(user.optString("id"))) {
            String userId = user.optString("id");
            // Check if user is in users list
            if(UserManager.getInstance().containsKey(userId)) {
                // YES
                // Check if user is YOU
                if(!UserManager.getInstance().getCurrentUserId().equals(userId)) {
                    // NO
                    try {
                        // Set user connection state to disconnected
                        JSONObject updatedUser = UserManager.getInstance().getSettingsForKey(userId).put(UserManager.USER_LOCAL_CONNECTIONSTATEKEY, "disconnected");
                        UserManager.getInstance().addOrReplace(userId, updatedUser.toString());
                        // TODO : Unset user camera, microphone, screen permission demand
                    }
                    catch (JSONException e) {
                        Log.e(TAG, "onConnectionCreated: exception : " + e.getLocalizedMessage());
                    }
                    APIInteraction.getInstance().FireEvent(APIInteraction.Type.ON_USER_CONNECTION_STATE_CHANGED, null);

                    // Add "User leave" notification message in conversation panel
                    // ( with disconnect reason )
                    APIClient.getInstance().sendUserLeave(); // TODO pass disconnect reason
                }
            }
        }
    }

    /**************** END CONNECTION ****************/



    /**************** PUBLISHER ****************/

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {
        Log.d(TAG, "onStreamCreated: Own stream " + stream.getStreamId() + " created");

        // Check if user is in users list
        if(SettingsManager.getInstance().getRawValueForKey(SettingsManager.SETTINGS_USERIDKEY).equals(stream.getConnection().getData())) {
            Log.d(TAG, "onStreamCreated: user IS in the list");
        }
        else {
            Log.d(TAG, "onStreamCreated: user IS NOT in the list");
        }

        ArrayList<GenericModel> list = new ArrayList<>(1);
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", stream.getStreamId());
        list.add(new GenericModel(contentValues));
        TokBoxInteraction.getInstance().FireEvent(TokBoxInteraction.Type.ON_PUBLISHER_ADDED, list);
    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {
        Log.d(TAG, "onStreamDestroyed: Own stream " + stream.getStreamId() + " destroyed");

        if(this.publisher != null) {
            this.session.unpublish(this.publisher);
            this.publisher.destroy();
            this.publisher = null;

            ArrayList<GenericModel> list = new ArrayList<>(1);
            ContentValues contentValues = new ContentValues();
            contentValues.put("id", stream.getStreamId());
            list.add(new GenericModel(contentValues));
            TokBoxInteraction.getInstance().FireEvent(TokBoxInteraction.Type.ON_PUBLISHER_REMOVED, list);
        }
    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {
        Log.d(TAG, "onError: Error (" + opentokError.getMessage() + ") in publisher");
    }

    /**************** END PUBLISHER ****************/


    /**************** SUBSCRIBERS ****************/

    private void registerSubscriberStream(Stream stream) {
        Subscriber mSubscriber = new Subscriber.Builder(TWICAndroidPlugin.getInstance().getContext(), stream).build();
        mSubscriber.getView().setTag(stream.getStreamId());

        mSubscriber.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);
        this.subscribers.put(stream.getStreamId(), mSubscriber);

        mSubscriber.setVideoListener(this);

        session.subscribe(mSubscriber);

        ArrayList<GenericModel> list = new ArrayList<>(1);
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", stream.getStreamId());
        list.add(new GenericModel(contentValues));
        TokBoxInteraction.getInstance().FireEvent(TokBoxInteraction.Type.ON_SUBSCRIBER_ADDED, list);
    }

    @Override
    public void onVideoDataReceived(SubscriberKit subscriberKit) {

    }

    @Override
    public void onVideoDisabled(SubscriberKit subscriberKit, String s) {

    }

    @Override
    public void onVideoEnabled(SubscriberKit subscriberKit, String s) {

    }

    @Override
    public void onVideoDisableWarning(SubscriberKit subscriberKit) {

    }

    @Override
    public void onVideoDisableWarningLifted(SubscriberKit subscriberKit) {

    }

    /**************** END SUBSCRIBERS ****************/


    /**************** SIGNALING ****************/

    @Override
    public void onSignalReceived(Session session, String type, String data, Connection connection) {
        Log.d(TAG, "onSignalReceived: type = " + type + " , data = " + data);

        if(SIGNALTYPE_CAMERAAUTHORIZATION.equals(type)) {
            // TODO
        }
        else if(SIGNALTYPE_CANCELCAMERAAUTHORIZATION.equals(type)) {
            // TODO
        }
        else if(SIGNALTYPE_CANCELMICROPHONEAUTHORIZATION.equals(type)) {
            // TODO
        }
        else if(SIGNALTYPE_MICROPHONEAUTHORIZATION.equals(type)) {
            // TODO
        }
        else if(SIGNALTYPE_CAMERAREQUESTED.equals(type)) {
            // TODO
        }
        else if(SIGNALTYPE_MICROPHONEREQUESTED.equals(type)) {
            // TODO
        }
        else if(SIGNALTYPE_FORCEMUTESTREAM.equals(type)) {
            // TODO
        }
        else if(SIGNALTYPE_FORCEUNMUTESTREAM.equals(type)) {
            // TODO
        }
    }

    /**************** END SIGNALING ****************/
}
