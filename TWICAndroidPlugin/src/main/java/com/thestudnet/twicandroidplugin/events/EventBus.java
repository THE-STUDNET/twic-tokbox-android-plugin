package com.thestudnet.twicandroidplugin.events;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * INTERACTIVE LAYER
 * Created by Baptiste PHILIBERT on 20/04/15.
 */
public class EventBus {

    private static Bus instance;

    public static Bus getInstance() {
        if(instance == null) {
            Bus bus = new Bus(ThreadEnforcer.ANY);
            instance = bus;
            return instance;
        }
        else {
            return instance;
        }
    }

}
