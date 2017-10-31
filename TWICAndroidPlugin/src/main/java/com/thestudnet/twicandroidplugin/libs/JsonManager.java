package com.thestudnet.twicandroidplugin.libs;

import android.content.ContentValues;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Set;

/**
 * INTERACTIVE LAYER
 * Created by Baptiste PHILIBERT on 24/05/2017.
 */

public class JsonManager {

    protected ContentValues contentValues;

    private static JsonManager instance;
    public JsonManager() {
    }
    public static JsonManager getInstance() {
        if(instance == null) {
            JsonManager minstance = new JsonManager();
            instance = minstance;
            instance.contentValues = new ContentValues();
            return instance;
        } else {
            return instance;
        }
    }

    public void configure(String settings) {
        this.contentValues = new ContentValues();
        try {
            JSONObject jsonSettings = new JSONObject(settings);
            Iterator<String> iter = jsonSettings.keys();
            while (iter.hasNext()) {
                String key = iter.next();
                String value = jsonSettings.optString(key, "");
                this.contentValues.put(key, value);
            }
        }
        catch (JSONException error) {
        }
    }

    public String getRawValueForKey(String key) {
        if(this.contentValues.containsKey(key)) {
            return this.contentValues.getAsString(key);
        }
        else {
            return "";
        }
    }

    public JSONObject getSettingsForKey(String key) {
        JSONObject jsonObject = null;
        try {
            if(this.contentValues.containsKey(key)) {
                jsonObject = new JSONObject(this.contentValues.getAsString(key));
            }
        }
        catch (JSONException error) {
        }
        return jsonObject;
    }

    public JSONObject getSettingsForKey(String keyFirstLevel, String keySecondLevel) {
        return this.getSettingsForKey(keyFirstLevel).optJSONObject(keySecondLevel);
    }

    public Set<String> getKeys() {
        return this.contentValues.keySet();
    }

    public boolean containsKey(String key) {
        return this.contentValues.containsKey(key);
    }

    public void addOrReplace(String key, String value) {
        this.contentValues.put(key, value);
    }

}
