package com.thestudnet.twicandroidplugin.events;

import com.squareup.otto.Produce;

import java.util.ArrayList;

/**
 * INTERACTIVE LAYER
 * Created by Baptiste PHILIBERT on 20/04/15.
 */
public class PluginInteraction {

    public enum Type {
        IS_INITIALIZED(0);

        private int value;

        private Type (int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private static PluginInteraction instance;
    public static PluginInteraction getInstance() {
        if(instance == null) {
            instance = new PluginInteraction();
        }
        return instance;
    }

    public class OnPluginInteractionEvent {
        private Type type;
        private ArrayList<? extends Object> data;
        public OnPluginInteractionEvent(Type type, ArrayList<? extends Object> data) {
            this.type = type;
            this.data = data;
        }
        public Type getType() {
            return this.type;
        }
        public ArrayList<? extends Object> getData() {
            if(this.data == null) {
                return null;
            }
            else {
                return (ArrayList<? extends Object>) this.data.clone();
            }
        }
    }

    @Produce
    public void FireEvent(Type type, ArrayList<? extends Object> data) {
        EventBus.getInstance().post(new OnPluginInteractionEvent(type, data));
    }

}
