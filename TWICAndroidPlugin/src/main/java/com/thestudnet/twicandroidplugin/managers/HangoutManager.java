package com.thestudnet.twicandroidplugin.managers;

import android.content.ContentValues;

import com.thestudnet.twicandroidplugin.libs.JsonManager;

import org.json.JSONObject;

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

    public boolean getRule(String rule) {
        // TODO : remove before moving to production !
//        if(rule.equals(HangoutManager.HANGOUT_ACTIONAUTOPUBLISHCAMERA)) return false;
//        if(rule.equals(HangoutManager.HANGOUT_ACTIONAUTOPUBLISHMICROPHONE)) return false;
        JSONObject rules = HangoutManager.getInstance().getSettingsForKey(HangoutManager.HANGOUT_OPTIONSKEY, "rules");
        if(rules != null) {
            return rules.optBoolean(rule, false);
        }
        else {
            return false;
        }
    }

    /*
    public boolean hasAutoPublishCamera(String userId) {
        return HangoutManager.getInstance().getActionSetting(HangoutManager.HANGOUT_ACTIONAUTOPUBLISHCAMERA);
    }

    public boolean hasAutoPublishMicrophone(String userId) {
        return HangoutManager.getInstance().getActionSetting(HangoutManager.HANGOUT_ACTIONAUTOPUBLISHMICROPHONE);
    }
    */

}
