package com.thestudnet.twicandroidplugin.events;

import com.squareup.otto.Produce;

import java.util.ArrayList;

/**
 * INTERACTIVE LAYER
 * Created by Baptiste PHILIBERT on 20/04/15.
 */
public class TokBoxInteraction {

    public enum Type {
        ON_SUBSCRIBER_ADDED(0),
        ON_PUBLISHER_ADDED(1),
        ON_SUBSCRIBER_REMOVED(2),
        ON_PUBLISHER_REMOVED(3);

        private int value;

        private Type (int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private static TokBoxInteraction instance;
    public static TokBoxInteraction getInstance() {
        if(instance == null) {
            instance = new TokBoxInteraction();
        }
        return instance;
    }

    public class OnTokBoxInteractionEvent {
        private Type type;
        private ArrayList<? extends Object> data;
        public OnTokBoxInteractionEvent(Type type, ArrayList<? extends Object> data) {
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
        EventBus.getInstance().post(new OnTokBoxInteractionEvent(type, data));
    }

}
