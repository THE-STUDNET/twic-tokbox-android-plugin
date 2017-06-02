package com.thestudnet.twicandroidplugin.managers;

import android.content.ContentValues;

import com.thestudnet.twicandroidplugin.libs.JsonManager;

/**
 * INTERACTIVE LAYER
 * Created by Baptiste PHILIBERT on 27/04/2017.
 */

public class HangoutManager extends JsonManager {

    public static String HANGOUT_USERSKEY                    = "users";
    public static String HANGOUT_OPTIONSKEY                  = "options";
    public static String HANGOUT_ACTIONAUTOPUBLISHCAMERA     = "autoPublishCamera";
    public static String HANGOUT_ACTIONAUTOPUBLISHMICROPHONE = "autoPublishMicrophone";
    public static String HANGOUT_ACTIONARCHIVE               = "archive";
    public static String HANGOUT_ACTIONRAISEHAND             = "raiseHand";
    public static String HANGOUT_ACTIONPUBLISH               = "publish";
    public static String HANGOUT_ACTIONASKDEVICE             = "askDevice";
    public static String HANGOUT_ACTIONASKSCREEN             = "askScreen";
    public static String HANGOUT_ACTIONFORCEMUTE             = "forceMute";
    public static String HANGOUT_ACTIONFORCEUNPUSBLISH       = "forceUnpublish";
    public static String HANGOUT_ACTIONKICK                  = "kick";

    private static HangoutManager instance;
    public HangoutManager() {
    }
    public static HangoutManager getInstance() {
        if(instance == null) {
            HangoutManager minstance = new HangoutManager();
            instance = minstance;
            instance.contentValues = new ContentValues();
            return instance;
        } else {
            return instance;
        }
    }

}
