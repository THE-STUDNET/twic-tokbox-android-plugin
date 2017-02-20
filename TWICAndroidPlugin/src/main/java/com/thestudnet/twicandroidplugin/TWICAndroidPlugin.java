package com.thestudnet.twicandroidplugin;

import android.content.Context;

/**
 * INTERACTIVE LAYER
 * Created by Baptiste PHILIBERT on 14/02/2017.
 */

public class TWICAndroidPlugin {

    private static Context mAppContext;

    public void setAppContext(Context context) {
        mAppContext = context;
    }

    public Context getAppContext() {
        return mAppContext;
    }

}
