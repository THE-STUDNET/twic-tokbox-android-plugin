package com.thestudnet.twicandroidplugin.managers;

import android.content.ContentValues;
import android.util.Log;

import com.thestudnet.twicandroidplugin.communication.APIClientConfigurator;
import com.thestudnet.twicandroidplugin.events.APIInteraction;
import com.thestudnet.twicandroidplugin.models.GenericModel;
import com.thestudnet.twicandroidplugin.utils.RandomInt;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2Session;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2SessionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * INTERACTIVE LAYER
 * Created by Baptiste PHILIBERT on 27/04/2017.
 */

public class APIClient {

    private static final String TAG = "com.thestudnet.twicandroidplugin.managers." + APIClient.class.getSimpleName();

    public static String TWIC_CONVERSATION_GETPATH        = "conversation.get";
    public static String TWIC_CONVERSATION_GETTOKENPATH   = "conversation.getToken";
    public static String TWIC_USER_GETPATH                = "user.get";
    public static String TWIC_ACTIVITY_ADDPATH            = "activity.add";
    
    public static String HANGOUT_EVENTJOIN            = "hangout.join";
    public static String HANGOUT_EVENTLEAVE           = "hangout.leave";
    public static String HANGOUT_EVENTUSERSPOKE       = "hangout.userspoke";
    public static String HANGOUT_EVENTSHARECAMERA     = "hangout.sharecamera";
    public static String HANGOUT_EVENTSHAREMICROPHONE = "hangout.sharemicrophone";
    public static String HANGOUT_EVENTMESSAGE         = "hangout.message";
    public static String HANGOUT_EVENTSTARTRECORD     = "hangout.startrecord";
    public static String HANGOUT_EVENTSTOPRECORD      = "hangout.stoprecord";

    private JSONRPC2Session client;

    private static APIClient instance;
    public APIClient() {
    }
    public static APIClient getInstance() {
        if(instance == null) {
            APIClient minstance = new APIClient();
            instance = minstance;
            // The JSON-RPC 2.0 server URL
            URL serverURL = null;
            try {

                JSONObject apiSettings = new JSONObject(SettingsManager.getInstance().getRawValueForKey(SettingsManager.SETTINGS_APIKEY));
                String protocol = apiSettings.optString(SettingsManager.SETTINGS_PROTOCOLKEY, "http");
                String hostname = apiSettings.optString(SettingsManager.SETTINGS_DOMAINKEY, "");
                JSONObject paths = new JSONObject(apiSettings.optString(SettingsManager.SETTINGS_PATHSKEY, ""));
                String path = paths.optString(SettingsManager.SETTINGS_PATHS_JSONRPCKEY);

                serverURL = new URL(protocol
                        + "://"
                        + hostname
                        + "/"
                        + path);

                instance.client = new JSONRPC2Session(serverURL);
                instance.client.setConnectionConfigurator(new APIClientConfigurator());
                instance.client.getOptions().trustAllCerts(true);
            }
            catch (JSONException e) {
                Log.e(TAG, e.getLocalizedMessage());
            }
            catch (MalformedURLException e) {
                Log.e(TAG, e.getLocalizedMessage());
            }
            return instance;
        } else {
            return instance;
        }
    }

    public void getHangoutData() {
        if(this.client != null) {
//            getHangoutDataTask task = new getHangoutDataTask();
//            if(Build.VERSION.SDK_INT >= 11/*HONEYCOMB*/) {
//                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//            }
//            else {
//                task.execute();
//            }

            new Thread() {
                public void run() {
                    // Construct request
                    int requestID = new RandomInt().nextNonNegative();
                    HashMap<String, Object> param = new HashMap<>(1);
                    //JSONObject jsonParams = new JSONObject();
                    JSONRPC2Response response = null;

                    try {
//                        jsonParams.put("id", SettingsManager.getInstance().getRawValueForKey(SettingsManager.SETTINGS_HANGOUTIDKEY));
//                        param.put("params", jsonParams);
                        param.put("id", SettingsManager.getInstance().getRawValueForKey(SettingsManager.SETTINGS_HANGOUTIDKEY));
                        JSONRPC2Request request = new JSONRPC2Request(TWIC_CONVERSATION_GETPATH, param, requestID);

                        // Send request
                        response = client.send(request);
                    }
//                    catch (JSONException e) {
//                        Log.e(TAG, e.getLocalizedMessage());
//                    }
                    catch (JSONRPC2SessionException e) {
                        Log.e(TAG, e.getLocalizedMessage());
                        Log.e(TAG, e.getMessage());
                    }

                    if(response != null && response.indicatesSuccess()) {
                        Log.d(TAG, response.getResult().toString());

                        HangoutManager.getInstance().configure(response.getResult().toString());

                        APIInteraction.getInstance().FireEvent(APIInteraction.Type.ON_HANGOUT_DATA_RECEIVED, null);
                    }
                }
            }.start();
        }
    }

//    private class getHangoutDataTask extends AsyncTask<String, Void, JSONRPC2Response> {
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            Log.e("AsyncTask", "onPreExecute");
//        }
//
//        protected JSONRPC2Response doInBackground(String... urls) {
//            // Construct request
//            int requestID = new RandomInt().nextNonNegative();
//            HashMap<String, Object> params = new HashMap<>(1);
//            params.put("id", SettingsManager.getInstance().getRawValueForKey(SettingsManager.SETTINGS_HANGOUTIDKEY));
//            JSONRPC2Request request = new JSONRPC2Request(TWIC_CONVERSATION_GETPATH, params, requestID);
//
//            // Send request
//            JSONRPC2Response response = null;
//
//            try {
//                response = client.send(request);
//            }
//            catch (JSONRPC2SessionException e) {
//                Log.e(TAG, String.valueOf(e.getCauseType()));
//            }
//            finally {
//                return response;
//            }
//        }
//
//        protected void onPostExecute(JSONRPC2Response response) {
//            if(response != null && response.indicatesSuccess()) {
//                Log.e(TAG, response.getResult().toString());
//
//                APIInteraction.getInstance().FireEvent(APIInteraction.Type.ON_HANGOUT_DATA_RECEIVED, null);
//            }
//        }
//    }

    public void getTokBoxData() {
        if(this.client != null) {
//            getTokBoxDataTask task = new getTokBoxDataTask();
//            if(Build.VERSION.SDK_INT >= 11/*HONEYCOMB*/) {
//                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//            }
//            else {
//                task.execute();
//            }

            new Thread() {
                public void run() {
                    // Construct request
                    int requestID = new RandomInt().nextNonNegative();
                    HashMap<String, Object> param = new HashMap<>(1);
                    //JSONObject jsonParams = new JSONObject();
                    JSONRPC2Response response = null;

                    try {
//                        jsonParams.put("id", SettingsManager.getInstance().getRawValueForKey(SettingsManager.SETTINGS_HANGOUTIDKEY));
//                        param.put("params", jsonParams);
                        param.put("id", SettingsManager.getInstance().getRawValueForKey(SettingsManager.SETTINGS_HANGOUTIDKEY));
                        JSONRPC2Request request = new JSONRPC2Request(TWIC_CONVERSATION_GETTOKENPATH, param, requestID);

                        // Send request
                        response = client.send(request);
                    }
//                    catch (JSONException e) {
//                        Log.e(TAG, e.getLocalizedMessage());
//                    }
                    catch (JSONRPC2SessionException e) {
                        Log.e(TAG, e.getLocalizedMessage());
                        Log.e(TAG, e.getMessage());
                    }

                    if(response != null && response.indicatesSuccess()) {
                        Log.d(TAG, response.getResult().toString());

                        ArrayList<GenericModel> list = new ArrayList<>(1);
                        ContentValues contentValues = new ContentValues();
                        net.minidev.json.JSONObject jsonResponse = (net.minidev.json.JSONObject) response.getResult();
                        contentValues.put("token", (String) jsonResponse.get("token"));
                        contentValues.put("session", (String) jsonResponse.get("session"));
                        list.add(new GenericModel(contentValues));

                        APIInteraction.getInstance().FireEvent(APIInteraction.Type.ON_TOKBOX_DATA_RECEIVED, list);
                    }
                }
            }.start();
        }
    }

//    private class getTokBoxDataTask extends AsyncTask<String, Void, JSONRPC2Response> {
//
//        protected JSONRPC2Response doInBackground(String... urls) {
//            // Construct request
//            int requestID = new RandomInt().nextNonNegative();
//            HashMap<String, Object> params = new HashMap<>(1);
//            params.put("id", SettingsManager.getInstance().getRawValueForKey(SettingsManager.SETTINGS_HANGOUTIDKEY));
//            JSONRPC2Request request = new JSONRPC2Request(TWIC_CONVERSATION_GETPATH, params, requestID);
//
//            // Send request
//            JSONRPC2Response response = null;
//
//            try {
//                response = client.send(request);
//            }
//            catch (JSONRPC2SessionException e) {
//                Log.e(TAG, e.getLocalizedMessage());
//            }
//            finally {
//                return response;
//            }
//        }
//
//        protected void onPostExecute(JSONRPC2Response response) {
//            if(response != null && response.indicatesSuccess()) {
//                Log.e(TAG, response.getResult().toString());
//
//                APIInteraction.getInstance().FireEvent(APIInteraction.Type.ON_TOKBOX_DATA_RECEIVED, null);
//            }
//        }
//    }

    public void getHangoutUsers() {
        if(this.client != null) {
//            getHangoutDataTask task = new getHangoutDataTask();
//            if(Build.VERSION.SDK_INT >= 11/*HONEYCOMB*/) {
//                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//            }
//            else {
//                task.execute();
//            }

            new Thread() {
                public void run() {
                    // Construct request
                    int requestID = new RandomInt().nextNonNegative();
                    HashMap<String, Object> param = new HashMap<>(1);
                    //JSONObject jsonParams = new JSONObject();
                    JSONRPC2Response response = null;

                    try {
//                        jsonParams.put("id", SettingsManager.getInstance().getRawValueForKey(SettingsManager.SETTINGS_HANGOUTIDKEY));
//                        param.put("params", jsonParams);
//                        param.put("id", HangoutManager.getInstance().getRawValueForKey(HangoutManager.HANGOUT_USERSKEY));
                        JSONArray jsonIds = new JSONArray(HangoutManager.getInstance().getRawValueForKey(HangoutManager.HANGOUT_USERSKEY));
                        ArrayList<String> ids = new ArrayList<>(jsonIds.length());
                        JSONRPC2Request request = new JSONRPC2Request(TWIC_USER_GETPATH, param, requestID);
                        for (int i=0; i<jsonIds.length(); i++){
                            ids.add(jsonIds.getString(i));
                        }
                        param.put("id", ids);
                        // Send request
                        response = client.send(request);
                    }
//                    catch (JSONException e) {
//                        Log.e(TAG, e.getLocalizedMessage());
//                    }
                    catch (JSONRPC2SessionException e) {
                        Log.e(TAG, e.getLocalizedMessage());
                        Log.e(TAG, e.getMessage());
                    } catch (JSONException e) {
                        Log.e(TAG, e.getLocalizedMessage());
                        Log.e(TAG, e.getMessage());
                    }

                    if(response != null && response.indicatesSuccess()) {
                        Log.d(TAG, response.getResult().toString());

                        UserManager.getInstance().configure(response.getResult().toString());

                        APIInteraction.getInstance().FireEvent(APIInteraction.Type.ON_HANGOUT_USERS_RECEIVED, null);
                    }
                }
            }.start();
        }
    }

    public void sendSessionConnected() {
        if(this.client != null) {
            new Thread() {
                public void run() {
                    // Construct request
                    int requestID = new RandomInt().nextNonNegative();
                    HashMap<String, Object> param = new HashMap<>(1);
                    //JSONObject jsonParams = new JSONObject();
                    JSONRPC2Response response = null;

                    try {
                        param.put("id", SettingsManager.getInstance().getRawValueForKey(SettingsManager.SETTINGS_HANGOUTIDKEY));
                        param.put("name", "hangout");
                        JSONRPC2Request request = new JSONRPC2Request(HANGOUT_EVENTJOIN, param, requestID);
                        // Send request
                        response = client.send(request);
                    }
//                    catch (JSONException e) {
//                        Log.e(TAG, e.getLocalizedMessage());
//                    }
                    catch (JSONRPC2SessionException e) {
                        Log.e(TAG, e.getLocalizedMessage());
                        Log.e(TAG, e.getMessage());
                    }

                    if(response != null && response.indicatesSuccess()) {
                        Log.d(TAG, response.getResult().toString());
                    }
                    else if(response != null && response.getError() != null) {
                        Log.e(TAG, response.getError().toString());
                    }
                    else {
                        Log.e(TAG, "unknown error in sendSessionConnected");
                    }
                }
            }.start();
        }
    }

    public void sendConnectionDestroyed() {
        if(this.client != null) {
            new Thread() {
                public void run() {
                    // Construct request
                    int requestID = new RandomInt().nextNonNegative();
                    HashMap<String, Object> param = new HashMap<>(1);
                    //JSONObject jsonParams = new JSONObject();
                    JSONRPC2Response response = null;

                    try {
                        param.put("id", SettingsManager.getInstance().getRawValueForKey(SettingsManager.SETTINGS_HANGOUTIDKEY));
                        param.put("name", "hangout");
                        JSONRPC2Request request = new JSONRPC2Request(HANGOUT_EVENTLEAVE, param, requestID);
                        // Send request
                        response = client.send(request);
                    }
//                    catch (JSONException e) {
//                        Log.e(TAG, e.getLocalizedMessage());
//                    }
                    catch (JSONRPC2SessionException e) {
                        Log.e(TAG, e.getLocalizedMessage());
                        Log.e(TAG, e.getMessage());
                    }

                    if(response != null && response.indicatesSuccess()) {
                        Log.d(TAG, response.getResult().toString());
                    }
                    else if(response != null && response.getError() != null) {
                        Log.e(TAG, response.getError().toString());
                    }
                    else {
                        Log.e(TAG, "unknown error in sendSessionConnected");
                    }
                }
            }.start();
        }
    }

}
