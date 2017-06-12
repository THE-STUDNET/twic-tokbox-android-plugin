package com.thestudnet.twicandroidplugin.managers;

import android.content.ContentValues;

import com.opentok.android.Publisher;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;
import com.thestudnet.twicandroidplugin.libs.JsonManager;

import org.json.JSONObject;

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
        return SettingsManager.getInstance().getRawValueForKey(SettingsManager.SETTINGS_USERIDKEY);
    }

    public boolean isCurrentUserSharingAudio() {
        Publisher publisher = TokBoxClient.getInstance().getPublisher();
        if(publisher != null && publisher.getStream() != null) {
            return publisher.getStream().hasAudio();
        }
        return false;
    }

    public boolean isSharingAudio(String userId) {
        Subscriber subscriber = TokBoxClient.getInstance().getSubscribers().get(userId);
        if(subscriber != null && subscriber.getStream() != null) {
            return subscriber.getStream().hasAudio();
        }
        return false;
    }

    public boolean isCurrentUserSharingCamera() {
        Publisher publisher = TokBoxClient.getInstance().getPublisher();
        if(publisher != null && publisher.getStream() != null) {
            if(publisher.getStream().hasVideo() && publisher.getStream().getStreamVideoType() == Stream.StreamVideoType.StreamVideoTypeCamera) {
                return true;
            }
        }
        return false;
    }

    public boolean isSharingCamera(String userId) {
        Subscriber subscriber = TokBoxClient.getInstance().getSubscribers().get(userId);
        if(subscriber != null && subscriber.getStream() != null) {
            if(subscriber.getStream().hasVideo() && subscriber.getStream().getStreamVideoType() == Stream.StreamVideoType.StreamVideoTypeCamera) {
                return true;
            }
        }
        return false;
    }

    public boolean isCurrentUserSharingScreen() {
        Publisher publisher = TokBoxClient.getInstance().getPublisher();
        if(publisher != null && publisher.getStream() != null) {
            if(publisher.getStream().hasVideo() && publisher.getStream().getStreamVideoType() == Stream.StreamVideoType.StreamVideoTypeScreen) {
                return true;
            }
        }
        return false;
    }

    public boolean isSharingScreen(String userId) {
        Subscriber subscriber = TokBoxClient.getInstance().getSubscribers().get(userId);
        if(subscriber != null && subscriber.getStream() != null) {
            if(subscriber.getStream().hasVideo() && subscriber.getStream().getStreamVideoType() == Stream.StreamVideoType.StreamVideoTypeScreen) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @return the TOTAL number of users, except current user
     */
    public int getTotalUsersCount() {
        int count = super.contentValues.size();
        if(count > 1) {
            return count - 1;
        }
        else {
            return 0;
        }
    }

    /**
     *
     * @return the number of CONNECTED users, except current user
     */
    public int getTotalConnectedUsersCount() {
        int connectedCount = 0;
        for(String userId : UserManager.getInstance().getKeys()) {
            if (!userId.equals(UserManager.getInstance().getCurrentUserId())) {
                JSONObject user = UserManager.getInstance().getSettingsForKey(userId);
                if(user.optString(UserManager.USER_LOCAL_CONNECTIONSTATEKEY, "disconnected").equals("connected")) {
                    connectedCount++;
                }
            }
        }
        return connectedCount;
    }

}
