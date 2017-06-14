package com.thestudnet.twicandroidplugin.adapters;

/**
 * INTERACTIVE LAYER
 * Created by Baptiste PHILIBERT on 09/04/2017.
 */

public class UserAction {

    public enum Type {
        ALLOW_USER_TO_SHARE_HIS_CAMERA(0),
        ASK_USER_TO_SHARE_HIS_CAMERA(1),
        ALLOW_USER_TO_SHARE_HIS_MICROPHONE(2),
        ASK_USER_TO_SHARE_HIS_MICROPHONE(3),
        ALLOW_USER_TO_SHARE_HIS_SCREEN(4),
        ASK_USER_TO_SHARE_HIS_SCREEN(5),
        FORCE_USER_TO_UNPUBLISH_SCREEN(6),
        FORCE_USER_TO_UNPUBLISH_CAMERA(7),
        FORCE_USER_TO_UNPUBLISH_MICROPHONE(8),
        KICK_USER(9);

        private int value;

        private Type (int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public UserAction(String text, Type type) {
        this.text = text;
        this.type = type;
    }

    private String text;
    private Type type;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}
