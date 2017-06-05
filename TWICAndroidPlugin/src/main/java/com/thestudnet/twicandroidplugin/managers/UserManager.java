package com.thestudnet.twicandroidplugin.managers;

import android.content.ContentValues;

import com.thestudnet.twicandroidplugin.libs.JsonManager;

/**
 * INTERACTIVE LAYER
 * Created by Baptiste PHILIBERT on 27/04/2017.
 */

public class UserManager extends JsonManager {

    //API Attributes
    public static String USER_AMBASSADORKEY       = "ambassador";
    public static String USER_AVATARKEY           = "avatar";
    public static String USER_BACKGROUNDKEY       = "background";
    public static String USER_BIRTHDATEKEY        = "birth_date";
    public static String USER_CONTACTSTATEKEY     = "contact_state";
    public static String USER_CONTACTSKEY         = "contacts_count";
    public static String USER_EMAILKEY            = "email";
    public static String USER_FIRSTNAMEKEY        = "firstname";
    public static String USER_GENDERKEY           = "gender";
    public static String USER_HASEMAILNOTIFIERKEY = "has_email_notifier";
    public static String USER_IDKEY               = "id";
    public static String USER_INTERESTKEY         = "interest";
    public static String USER_LASTNAMEKEY         = "lastname";
    public static String USER_NATIONALITYKEY      = "nationality";
    public static String USER_SHORTNAMEKEY        = "short_name";
    public static String USER_NICKNAMEKEY         = "nickname";
    public static String USER_ORGANIZATIONIDKEY   = "organization_id";
    public static String USER_ORIGINKEY           = "origin";
    public static String USER_POSITIONKEY         = "position";
    public static String USER_ROLESKEY            = "roles";

    //LOCAL Attributes
    public static String USER_LOCAL_CONNECTIONSTATEKEY  = "connection_state";
    public static String USER_LOCAL_ASKCAMERA           = "ask_camera";
    public static String USER_LOCAL_ASKMICROPHONE       = "ask_microphone";
    public static String USER_LOCAL_ASKSCREEN           = "ask_screen";
    public static String USER_LOCAL_ACTIONSKEY          = "actions";
    public static String USER_LOCAL_ACTIONTITLEKEY      = "action_title";
    public static String USER_LOCAL_ACTIONIMAGEKEY      = "action_image";
    public static String USER_LOCAL_ACTIONISADMINKEY    = "is_admin";

    private static UserManager instance;
    public UserManager() {
    }
    public static UserManager getInstance() {
        if(instance == null) {
            UserManager minstance = new UserManager();
            instance = minstance;
            instance.contentValues = new ContentValues();
            return instance;
        } else {
            return instance;
        }
    }

    public String getCurrentUserId() {
        return this.getRawValueForKey(SettingsManager.getInstance().getRawValueForKey(SettingsManager.SETTINGS_USERIDKEY));
    }

    public boolean hasPublishPermission(String userId) {
        return true;
    }

}
