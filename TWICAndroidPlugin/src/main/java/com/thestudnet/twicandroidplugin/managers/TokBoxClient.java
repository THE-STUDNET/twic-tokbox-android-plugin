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
import com.thestudnet.twicandroidplugin.R;
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
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * INTERACTIVE LAYER
 * Created by Baptiste PHILIBERT on 27/04/2017.
 */

public class TokBoxClient implements Session.SessionListener, Session.ConnectionListener, Publisher.PublisherListener, Subscriber.VideoListener, Session.SignalListener, Session.ArchiveListener {

    private static final String TAG = "com.thestudnet.twicandroidplugin.managers." + TokBoxClient.class.getSimpleName();

    public static String SIGNALTYPE_CAMERAAUTHORIZATION           = "hgt_camera_authorization";
    public static String SIGNALTYPE_CANCELCAMERAAUTHORIZATION     = "hgt_cancel_camera_authorization";
    public static String SIGNALTYPE_CANCELMICROPHONEAUTHORIZATION = "hgt_cancel_microphone_authorization";
    public static String SIGNALTYPE_MICROPHONEAUTHORIZATION       = "hgt_microphone_authorization";
    public static String SIGNALTYPE_CAMERAREQUESTED               = "hgt_camera_requested";
    public static String SIGNALTYPE_MICROPHONEREQUESTED           = "hgt_microphone_requested";
    public static String SIGNALTYPE_FORCEMUTESTREAM               = "hgt_force_mute_stream";
    public static String SIGNALTYPE_FORCEUNMUTESTREAM             = "hgt_force_unmute_stream";
    public static String SIGNALTYPE_KICKUSER                      = "hgt_kick_user";
    public static String SIGNALTYPE_FORCEUNPUBLISHSTREAM          = "hgt_force_unpublish_stream";
    public static String SIGNALTYPE_FORCEUNPUBLISHSCREEN          = "hgt_force_unpublish_screen";
    public static String SIGNALTYPE_SCREENREQUESTED               = "hgt_screen_requested";
    public static String SIGNALTYPE_CANCELSCREENAUTHORIZATION     = "hgt_cancel_screen_authorization";
    public static String SIGNALTYPE_SCREENAUTHORIZATION           = "hgt_screen_authorization";


    private Session session;
    private AtomicBoolean isConnected = new AtomicBoolean(false);

    public AtomicBoolean isArchiving = new AtomicBoolean(false);

    public Publisher getPublisher() {
        return publisher;
    }

    public LinkedHashMap<String, LinkedHashMap<String, Subscriber>> getSubscribers() {
        return subscribers;
    }

    private Publisher publisher;
    private LinkedHashMap<String, LinkedHashMap<String, Subscriber>> subscribers = new LinkedHashMap<>();

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
            Iterator<LinkedHashMap<String, Subscriber>> iterator = this.subscribers.values().iterator();
            while (iterator.hasNext()) {
                LinkedHashMap<String, Subscriber> users = iterator.next();
                Iterator<Subscriber> subscribers = users.values().iterator();
                while (subscribers.hasNext()) {
                    Subscriber subscriber = subscribers.next();
                    session.unsubscribe(subscriber);
                    subscriber.destroy();
                }
            }
//            Iterator<Subscriber> iterator = this.subscribers.values().iterator();
//            while (iterator.hasNext()) {
//                Subscriber subscriber = iterator.next();
//                session.unsubscribe(subscriber);
//                subscriber.destroy();
//            }
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

    }

    public void publish(boolean video, boolean audio) {
        if(this.session != null) {
            if(this.publisher != null) {
                this.publisher.setPublishVideo(video);
                this.publisher.setPublishAudio(audio);
//                this.session.publish(this.publisher);
            }
            else {
                publisher = new Publisher.Builder(TWICAndroidPlugin.getInstance().getContext()).build();
                publisher.setPublisherListener(this);
                publisher.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);
                this.publisher.setPublishVideo(video);
                this.publisher.setPublishAudio(audio);
                this.session.publish(this.publisher);
            }
        }
    }

    public void unpublish() {
        if(this.session != null && this.publisher != null) {
            this.session.unpublish(this.publisher);
        }
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

            // TODO : Write in firebase user is connected

            // Register "hangout.join" event with API
            APIClient.getInstance().registerEventName(APIClient.HANGOUT_EVENT_JOIN);

            // Stop listening to "sessionConnected" tokbox event
            this.isConnected.set(true);

            // Check publish (and auto-publish) permissions
            this.checkAutoPublishPermissions();

            TokBoxInteraction.getInstance().FireEvent(TokBoxInteraction.Type.ON_SESSION_CONNECTED, null);
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

        unregisterSubscriberStream(stream);
    }

    private void checkAutoPublishPermissions() {
        publisher = new Publisher.Builder(TWICAndroidPlugin.getInstance().getContext()).build();
        publisher.setPublisherListener(this);
        publisher.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);

        if(HangoutManager.getInstance().getRule(HangoutManager.HANGOUT_ACTIONAUTOPUBLISHCAMERA)) {
            this.publisher.setPublishVideo(true);
            this.publisher.setPublishAudio(true);
        }
        else if(HangoutManager.getInstance().getRule(HangoutManager.HANGOUT_ACTIONAUTOPUBLISHMICROPHONE)) {
            this.publisher.setPublishVideo(false);
            this.publisher.setPublishAudio(true);
        }
        this.session.publish(this.publisher);
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

        // CHECK USER
        if(user != null && user.has("id") && !"".equals(user.optString("id"))) {
            String userId = user.optString("id");
            // Check if user is in users list
            if(UserManager.getInstance().containsKey(userId)) {
                // YES
                // Check if user is YOU
                if(!UserManager.getInstance().getCurrentUserId().equals(userId)) {
                    // NO
                    // Set user connection state to "connected"
                    UserManager.getInstance().setConnectionState(true, userId);
                    // Set user connection
                    UserManager.getInstance().addOrReplaceUserConnection(userId, connection);
                    APIInteraction.getInstance().FireEvent(APIInteraction.Type.ON_USER_CONNECTION_STATE_CHANGED, null);
                    // Add "User joined" notification message in conversation panel
                    MessagesManager.getInstance().insertAutomaticMessage(TWICAndroidPlugin.getInstance().getContext().getString(R.string.message_user_joined, UserManager.getInstance().getDisplayName(userId)), userId, true);
                    // Check if YOU are asking for camera permission
                    if(UserManager.getInstance().isUserAskingPermission(UserManager.USER_LOCAL_ASKCAMERA, UserManager.getInstance().getCurrentUserId())) {
                        // Send tokbox signal "hgt_camera_authorization" to this user
                        this.sendSignal(TokBoxClient.SIGNALTYPE_CAMERAAUTHORIZATION, userId);
                    }
                    // Check if YOU are asking for micro permission
                    if(UserManager.getInstance().isUserAskingPermission(UserManager.USER_LOCAL_ASKMICROPHONE, UserManager.getInstance().getCurrentUserId())) {
                        // Send tokbox signal "hgt_microphone_authorization" to this user
                        this.sendSignal(TokBoxClient.SIGNALTYPE_MICROPHONEAUTHORIZATION, userId);
                    }
                }
            }
            else {
                // NO
                // Get User from API
                APIClient.getInstance().getNewUsers(userId, connection);
            }
        }

        // CHECK ARCHIVE
        // Check hangout "record" option
        JSONObject options = HangoutManager.getInstance().getSettingsForKey(HangoutManager.HANGOUT_OPTIONSKEY);
        if(options.optBoolean("record", false)) {
            // TRUE
            // Check if hangout "nb_user_autorecord" is defined
            int nb_user_autorecord = options.optInt("record", -1);
            if(nb_user_autorecord != -1) {
                // TRUE
                // Check if "nb_user_autorecord" == users connected count
                if(nb_user_autorecord == UserManager.getInstance().getTotalConnectedUsersCount()) {
                    // TRUE
                    // Start hangout recording by calling API
                    APIClient.getInstance().startArchiving();
                }
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
            Log.e(TAG, "onConnectionDestroyed: exception : " + e.getLocalizedMessage());
        }

        if(user != null && user.has("id") && !"".equals(user.optString("id"))) {
            String userId = user.optString("id");
            // Check if user is in users list
            if(UserManager.getInstance().containsKey(userId)) {
                // YES
                // Check if user is YOU
                if(!UserManager.getInstance().getCurrentUserId().equals(userId)) {
                    // NO
                    // Remove user connection
                    LinkedHashMap<String, Connection> userConnections = UserManager.getInstance().removeUserConnection(userId, connection);
                    if(userConnections == null || (userConnections != null && userConnections.size() == 0)) {
                        // Set user connection state to "disconnected"
                        UserManager.getInstance().setConnectionState(false, userId);
                        APIInteraction.getInstance().FireEvent(APIInteraction.Type.ON_USER_CONNECTION_STATE_CHANGED, null);

                        // Add "User leave" notification message in conversation panel
                        // TODO => with disconnect reason ?
                        MessagesManager.getInstance().insertAutomaticMessage(TWICAndroidPlugin.getInstance().getContext().getString(R.string.message_user_left, UserManager.getInstance().getDisplayName(userId)), userId, true);
                    }
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
            this.publisher.destroy();
            this.publisher = null;

            ArrayList<GenericModel> list = new ArrayList<>(1);
            ContentValues contentValues = new ContentValues();
            contentValues.put("id", stream.getStreamId());
            list.add(new GenericModel(contentValues));
            TokBoxInteraction.getInstance().FireEvent(TokBoxInteraction.Type.ON_PUBLISHER_REMOVED, list);

            JSONObject user = null;
            try {
                user = new JSONObject(stream.getConnection().getData());
                // TODO ? Check stream destroyed reason => Reason == "forceUnpublished"
                MessagesManager.getInstance().insertAutomaticMessage(TWICAndroidPlugin.getInstance().getContext().getString(R.string.message_user_stream_destroyed, UserManager.getInstance().getDisplayName(user.optString("id"))), user.optString("id"), true);
            }
            catch (JSONException e) {
                Log.e(TAG, "onStreamDestroyed: exception : " + e.getLocalizedMessage());
            }
        }
    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {
        Log.d(TAG, "onError: Error (" + opentokError.getMessage() + ") in publisher");
    }

    /**************** END PUBLISHER ****************/


    /**************** SUBSCRIBERS ****************/

    private void registerSubscriberStream(Stream stream) {
        JSONObject user = null;

        try {
            user = new JSONObject(stream.getConnection().getData());
        }
        catch (JSONException e) {
            Log.e(TAG, "registerSubscriberStream: exception : " + e.getLocalizedMessage());
        }

        if(user != null && user.has("id") && !"".equals(user.optString("id"))) {
            String userId = user.optString("id");

            Subscriber mSubscriber = new Subscriber.Builder(TWICAndroidPlugin.getInstance().getContext(), stream).build();
            mSubscriber.getView().setTag(userId);

            mSubscriber.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);
            LinkedHashMap<String, Subscriber> userSubscribers = this.subscribers.get(userId);
            if(userSubscribers == null) {
                this.subscribers.put(userId, new LinkedHashMap<String, Subscriber>());
            }
            userSubscribers = this.subscribers.get(userId);
            userSubscribers.put(mSubscriber.getStream().getStreamId(), mSubscriber);

//            this.subscribers.put(userId, mSubscriber);

            mSubscriber.setVideoListener(this);

            session.subscribe(mSubscriber);

            ArrayList<GenericModel> list = new ArrayList<>(1);
            ContentValues contentValues = new ContentValues();
            contentValues.put("id", userId);
            list.add(new GenericModel(contentValues));
            TokBoxInteraction.getInstance().FireEvent(TokBoxInteraction.Type.ON_SUBSCRIBER_ADDED, list);
        }

        /*
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
        */
    }

    private void unregisterSubscriberStream(Stream stream) {
        JSONObject user = null;

        try {
            user = new JSONObject(stream.getConnection().getData());
        } catch (JSONException e) {
            Log.e(TAG, "registerSubscriberStream: exception : " + e.getLocalizedMessage());
        }

        if (user != null && user.has("id") && !"".equals(user.optString("id"))) {
            String userId = user.optString("id");

            if(this.subscribers.containsKey(userId)) {
                LinkedHashMap<String, Subscriber> userSubscribers = this.subscribers.get(userId);
                Subscriber subscriber = userSubscribers.get(stream.getStreamId());
                if(subscriber != null) {
                    this.session.unsubscribe(subscriber);
                    subscriber.destroy();
                    userSubscribers.remove(stream.getStreamId());
                    if(userSubscribers.size() == 0) {
                        this.subscribers.remove(userId);
                    }
                    ArrayList<GenericModel> list = new ArrayList<>(1);
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("id", userId);
                    list.add(new GenericModel(contentValues));
                    TokBoxInteraction.getInstance().FireEvent(TokBoxInteraction.Type.ON_SUBSCRIBER_REMOVED, list);
                }
//                this.session.unsubscribe(this.subscribers.get(userId));
//                this.subscribers.get(userId).destroy();
//                this.subscribers.remove(userId);

//                ArrayList<GenericModel> list = new ArrayList<>(1);
//                ContentValues contentValues = new ContentValues();
//                contentValues.put("id", userId);
//                list.add(new GenericModel(contentValues));
//                TokBoxInteraction.getInstance().FireEvent(TokBoxInteraction.Type.ON_SUBSCRIBER_REMOVED, list);
            }
        }
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




    /**************** ARCHIVE ****************/
    @Override
    public void onArchiveStarted(Session session, String archiveId, String archiveName) {
        // Set archiving flag
        this.isArchiving.set(true);
        // Throw event
        TokBoxInteraction.getInstance().FireEvent(TokBoxInteraction.Type.ON_ARCHIVE_STARTED, null);
        // Add "Recording started" notification message in conversation panel
        MessagesManager.getInstance().insertAutomaticMessage(TWICAndroidPlugin.getInstance().getContext().getString(R.string.message_user_archive_started), "", true);
    }

    @Override
    public void onArchiveStopped(Session session, String archiveId) {
        // Set archiving flag
        this.isArchiving.set(false);
        // Throw event
        TokBoxInteraction.getInstance().FireEvent(TokBoxInteraction.Type.ON_ARCHIVE_STOPPED, null);
        // Add "Recording stopped" notification message in conversation panel
        MessagesManager.getInstance().insertAutomaticMessage(TWICAndroidPlugin.getInstance().getContext().getString(R.string.message_user_archive_stopped), "", true);
    }

    /**************** END ARCHIVE ****************/




    /**************** SIGNALING ****************/

    public void broadcastSignal(String signalName) {
        if(this.session != null) {
            this.session.sendSignal(signalName, null);
        }
    }

    public void sendSignal(String signalName, String toUserId) {
//        if(this.session != null && this.subscribers != null && this.subscribers.size() > 0) {
//            Subscriber to = this.subscribers.get(toUserId);
//            if(to != null && to.getStream() != null && to.getStream().getConnection() != null) {
//                this.session.sendSignal(signalName, null, to.getStream().getConnection());
//            }
//        }
        if(this.session != null && UserManager.getInstance().getUserConnection(toUserId) != null) {
            LinkedHashMap<String, Connection> userConnections = UserManager.getInstance().getUserConnection(toUserId);
            for(Connection connection : userConnections.values()) {
                this.session.sendSignal(signalName, null, connection);
            }
        }
    }

    @Override
    public void onSignalReceived(Session session, String type, String data, Connection connection) {
        Log.d(TAG, "onSignalReceived: type = " + type + " , data = " + data);

        JSONObject user = null;
        try {
            user = new JSONObject(connection.getData());
        } catch (JSONException e) {
            Log.e(TAG, "onSignalReceived: exception : " + e.getLocalizedMessage());
        }

        if (user != null && user.has("id") && !"".equals(user.optString("id"))) {
            ArrayList<String> list = new ArrayList<>(2);
            list.add(type);
            list.add(user.optString("id"));
            TokBoxInteraction.getInstance().FireEvent(TokBoxInteraction.Type.ON_SIGNAL_RECEIVED, list);
        }

        /*
        if(SIGNALTYPE_CAMERAAUTHORIZATION.equals(type)) {
        }
        else if(SIGNALTYPE_CANCELCAMERAAUTHORIZATION.equals(type)) {
        }
        else if(SIGNALTYPE_CANCELMICROPHONEAUTHORIZATION.equals(type)) {
        }
        else if(SIGNALTYPE_MICROPHONEAUTHORIZATION.equals(type)) {
            ArrayList<String> list = new ArrayList<>(1);
            list.add(SIGNALTYPE_CANCELMICROPHONEAUTHORIZATION);
            TokBoxInteraction.getInstance().FireEvent(TokBoxInteraction.Type.ON_SIGNAL_RECEIVED, list);
        }
        else if(SIGNALTYPE_CAMERAREQUESTED.equals(type)) {
        }
        else if(SIGNALTYPE_MICROPHONEREQUESTED.equals(type)) {
        }
        else if(SIGNALTYPE_FORCEMUTESTREAM.equals(type)) {
        }
        else if(SIGNALTYPE_FORCEUNMUTESTREAM.equals(type)) {
        }
        */
    }

    /**************** END SIGNALING ****************/
}
