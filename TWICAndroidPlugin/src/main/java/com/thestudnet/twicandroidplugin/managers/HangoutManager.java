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

    /*
 {
 "autoPublishCamera":[{"roles":["academic","instructor"]}],
 "autoPublishMicrophone":false,
 "archive":[{"roles":["admin","super_admin","academic","instructor"]}],
 "raiseHand":[{"roles":["student"]}],
 "publish":[{"roles":["admin","super_admin","academic","instructor"]}],
 "askDevice":[{"roles":["admin","super_admin","academic","instructor"]}],
 "askScreen":[{"roles":["admin","super_admin","academic","instructor"]}],
 "forceMute":[{"roles":["admin","super_admin","academic","instructor"]}],
 "forceUnpublish":[{"roles":["admin","super_admin","academic","instructor"]}],
 "kick":[{"roles":["admin","super_admin","academic"]}]}
*/

/*
 - kick => permet de savoir si l'user a le droit de kick,
 - askDevice => permet de savoir si l'utilisateur à le droit de demander à un autre qu'il partage micro OU camera
 - askScreen => permet de savoir si l'utilisateur à le droit de demander à un autre qu'il partage son écran.

 - archive => permet de savoir si l'utilisateur à le droit de lancer/stopper l'enregistrement du hangout
 - raiseHand => permet de savoir si l'utilisateur a le droit de demander le partage de sa camera/micro
 - publish => permet de savoir si l'utilisateur à le droit de publier sa camera/son micro
 - autoPublishCamera => permet de savoir si l'utilisateur doit automatiquement publier sa camera.
 - autoPublishMicrophone => permet de savoir si l'utilisateur doit automatiquement publier son micro.
 - forceMute => Permet de savoir si l'utilisateur peut mute un autre.
 - forceUnpublish => permet de savoir si l'utilisateur peut forcer un autre à couper sa camera/micro/partage d'écran.
 PS: A noter que si l'utilisateur n'a pas le droit "publish" et que autoPublishCamera OU autoPublishMicrophone est 'ok', la camera ou le micro de l'utilisateur sont publiés au lancement du hangout.
 PS2: les droits "askDevice" et "askScreen" sont également ceux qui autorise un user à accepter / refuser la demande de partage d'un autre utilisateur
*/

/*
 direct message => pas de droit ( tout le monde a le droit de chatter )
 request for camera => askDevice
 request for micro => askDevice
 request for screen => askScreen
 kick => kick
*/

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
//        if(rule.equals(HangoutManager.HANGOUT_ACTIONPUBLISH)) return false;
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
