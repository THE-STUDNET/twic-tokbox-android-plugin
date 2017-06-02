package com.thestudnet.twicandroidplugin.events;

import com.squareup.otto.Produce;

import java.util.ArrayList;

/**
 * INTERACTIVE LAYER
 * Created by Baptiste PHILIBERT on 20/04/15.
 */
public class FragmentInteraction {

    public enum Type {
        ON_BACK(0),
        ON_SHOW_USER_DIALOG(1),
        ON_SHOW_VIDEO_DETAILS_FRAGMENT(2);

        private int value;

        private Type (int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private static FragmentInteraction instance;
    public static FragmentInteraction getInstance() {
        if(instance == null) {
            instance = new FragmentInteraction();
        }
        return instance;
    }

    public class OnFragmentInteractionEvent {
        private Type type;
        private ArrayList<? extends Object> data;
        public OnFragmentInteractionEvent(Type type, ArrayList<? extends Object> data) {
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
        EventBus.getInstance().post(new OnFragmentInteractionEvent(type, data));
    }

}
