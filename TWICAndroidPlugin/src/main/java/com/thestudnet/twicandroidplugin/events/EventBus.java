package com.thestudnet.twicandroidplugin.events;

import com.squareup.otto.Bus;

/**
 * INTERACTIVE LAYER
 * Created by Baptiste PHILIBERT on 20/04/15.
 */
public class EventBus {

    private static Bus instance;

    public static Bus getInstance() {
        if(instance == null) {
            Bus bus = new Bus();
            instance = bus;
            return instance;
        }
        else {
            return instance;
        }
    }

}
