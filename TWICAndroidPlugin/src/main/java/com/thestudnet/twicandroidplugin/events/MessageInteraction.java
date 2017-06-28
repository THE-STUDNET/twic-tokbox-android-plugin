package com.thestudnet.twicandroidplugin.events;

import com.squareup.otto.Produce;

import java.util.ArrayList;

/**
 * INTERACTIVE LAYER
 * Created by Baptiste PHILIBERT on 27/06/2017.
 */

public class MessageInteraction {

    public enum Type {
        ON_MESSAGES_LOADED(0),
        ON_LATEST_MESSAGES_LOADED(1),
        ON_HISTORICAL_MESSAGES_LOADED(2);

        private int value;

        private Type (int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private static MessageInteraction instance;
    public static MessageInteraction getInstance() {
        if(instance == null) {
            instance = new MessageInteraction();
        }
        return instance;
    }

    public class OnMessageInteractionEvent {
        private Type type;
        private ArrayList<? extends Object> data;
        public OnMessageInteractionEvent(Type type, ArrayList<? extends Object> data) {
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
        EventBus.getInstance().post(new OnMessageInteractionEvent(type, data));
    }

}
