package com.thestudnet.twicandroidplugin.communication;

/**
 * INTERACTIVE LAYER
 * Created by Baptiste PHILIBERT on 08/04/2014.
 */
public class ErrorMessage {

    private String error = "";
    public String getError() {
        return error;
    }

    private String message = "";
    public String getMessage() {
        return message;
    }

    private int code = -1;
    public int getCode() {
        return code;
    }

    public ErrorMessage(String error, String message) {
        this.error = error;
        this.message = message;
    }

    public ErrorMessage(String error, String message, int code) {
        this.error = error;
        this.message = message;
        this.code = code;
    }

}
