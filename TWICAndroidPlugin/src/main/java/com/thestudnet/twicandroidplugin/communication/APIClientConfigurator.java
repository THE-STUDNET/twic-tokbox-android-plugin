package com.thestudnet.twicandroidplugin.communication;

import com.thestudnet.twicandroidplugin.managers.SettingsManager;
import com.thetransactioncompany.jsonrpc2.client.ConnectionConfigurator;

import java.net.HttpURLConnection;

/**
 * INTERACTIVE LAYER
 * Created by Baptiste PHILIBERT on 04/05/2017.
 */

public class APIClientConfigurator implements ConnectionConfigurator {

    @Override
    public void configure(HttpURLConnection connection) {
        // add custom HTTP header
        connection.addRequestProperty(
                SettingsManager.getInstance().getSettingsForKey(SettingsManager.SETTINGS_APIKEY).optString(SettingsManager.SETTINGS_AUTHORIZATIONHEADERKEY, ""),
                SettingsManager.getInstance().getSettingsForKey(SettingsManager.SETTINGS_APIKEY).optString(SettingsManager.SETTINGS_AUTHTOKENKEY, "")
        );
    }
}
