package com.thestudnet.twicandroidplugin.adapters;

/**
 * INTERACTIVE LAYER
 * Created by Baptiste PHILIBERT on 09/04/2017.
 */

public class UserAction {

    public UserAction(String text, int type) {
        this.text = text;
        this.type = type;
    }

    private String text;
    private int type;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
