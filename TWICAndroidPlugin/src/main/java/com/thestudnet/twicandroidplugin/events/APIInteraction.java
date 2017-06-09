package com.thestudnet.twicandroidplugin.events;

import com.squareup.otto.Produce;

import java.util.ArrayList;

/**
 * INTERACTIVE LAYER
 * Created by Baptiste PHILIBERT on 20/04/15.
 */
public class APIInteraction {

    public enum Type {
        ON_HANGOUT_DATA_RECEIVED(0),
        ON_HANGOUT_USERS_RECEIVED(1),
        ON_TOKBOX_DATA_RECEIVED(2),
        ON_USER_CONNECTION_STATE_CHANGED(3);

        private int value;

        private Type (int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private static APIInteraction instance;
    public static APIInteraction getInstance() {
        if(instance == null) {
            instance = new APIInteraction();
        }
        return instance;
    }

    public class OnAPIInteractionEvent {
        private Type type;
        private ArrayList<? extends Object> data;
        public OnAPIInteractionEvent(Type type, ArrayList<? extends Object> data) {
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
        EventBus.getInstance().post(new OnAPIInteractionEvent(type, data));
    }

}
