package com.thestudnet.twicandroidplugin.managers;

import android.content.ContentValues;

import com.thestudnet.twicandroidplugin.libs.JsonManager;

/**
 * INTERACTIVE LAYER
 * Created by Baptiste PHILIBERT on 27/04/2017.
 */

public class SettingsManager extends JsonManager {

    public static String SETTINGS_APIKEY                 = "api";
    public static String SETTINGS_DMSKEY                 = "dms";
    public static String SETTINGS_FIREBASEKEY            = "firebase";
    public static String SETTINGS_WSKEY                  = "ws";
    public static String SETTINGS_TOKBOXAPIKEY           = "tokbox_api_key";
    public static String SETTINGS_USERIDKEY              = "user_id";
    public static String SETTINGS_HANGOUTIDKEY           = "hangout_id";
    public static String SETTINGS_AUTHTOKENKEY           = "auth_token";
    public static String SETTINGS_AUTHORIZATIONHEADERKEY = "authorization_header";
    public static String SETTINGS_PROTOCOLKEY            = "protocol";
    public static String SETTINGS_PATHSKEY               = "paths";
    public static String SETTINGS_PATHS_JSONRPCKEY       = "jsonrpc";
    public static String SETTINGS_DOMAINKEY              = "domain";
    public static String SETTINGS_URLKEY                 = "url";
    public static String SETTINGS_PORTKEY                = "port";
    public static String SETTINGS_SECUREKEY              = "secure";

    private static SettingsManager instance;
    public SettingsManager() {
    }
    public static SettingsManager getInstance() {
        if(instance == null) {
            SettingsManager minstance = new SettingsManager();
            instance = minstance;
            instance.contentValues = new ContentValues();
            return instance;
        } else {
            return instance;
        }
    }

}
