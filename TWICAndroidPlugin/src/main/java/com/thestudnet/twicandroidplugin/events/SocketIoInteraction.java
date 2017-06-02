package com.thestudnet.twicandroidplugin.events;

import com.squareup.otto.Produce;

import java.util.ArrayList;

/**
 * INTERACTIVE LAYER
 * Created by Baptiste PHILIBERT on 20/04/15.
 */
public class SocketIoInteraction {

    public enum Type {
        ON_EVALUATION_CLICKED(0),
        ON_QUALITY_CLICKED(1),
        ON_DOCUMENTS_CLICKED(2),
        ON_TRACEABILITY_CLICKED(3),
        ON_LOGOUT(4),
        ON_ROOM_CLICKED(5),
        ON_PERSON_CLICKED(6),
        ON_DOCUMENT_CLICKED(7),
        ON_ROOM_CLICKED_TRACEABILITY(8),
        ON_ROOM_CLICKED_QUALITY(9),
        ON_SYNC_CLICKED(10),
        ON_DISPLAY_DASHBOARD(11),
        ON_BACK_PRESSED(12),
        ON_EMAIL_CLICKED(13),
        ON_SHOW_LAST_RECORD(14),
        ON_EMAIL_CREATE_CLICKED(15),
        ON_EMAIL_DETAILS_CLICKED(16),
        ON_EMAIL_DETAILS_SHOW_IMAGE(17),
        ON_SHOW_HISTORY(18);

        private int value;

        private Type (int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private static SocketIoInteraction instance;
    public static SocketIoInteraction getInstance() {
        if(instance == null) {
            instance = new SocketIoInteraction();
        }
        return instance;
    }

    public class OnSocketIoInteractionEvent {
        private Type type;
        private ArrayList<? extends Object> data;
        public OnSocketIoInteractionEvent(Type type, ArrayList<? extends Object> data) {
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
        EventBus.getInstance().post(new OnSocketIoInteractionEvent(type, data));
    }

}
