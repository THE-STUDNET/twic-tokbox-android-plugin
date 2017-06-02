package com.thestudnet.twicandroidplugin.models;

import android.content.ContentValues;

import java.util.Map;

public class GenericModel {

    private String id = "";
    private String title = "";
    private String subTitle = "";
    private String imageUrl;
    private String serializedId = "";
    private ContentValues contentValues = new ContentValues();

    public GenericModel(ContentValues values) {
        this.contentValues = values;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSerializedId() {
        return serializedId;
    }

    public String getContentValue(String key) {
        if(this.contentValues.containsKey(key)) {
            return this.contentValues.getAsString(key);
        }
        else {
            return "";
        }
    }

    public void addContentValue(String key, String value) {
        this.contentValues.put(key, value);
    }

    public void clearValues() {
        this.contentValues.clear();
    }

    public String[] getKeySet() {
        String[] ketSet = new String[this.contentValues.size()];
        int i = 0;
        for (Map.Entry<String, Object> entry : this.contentValues.valueSet()) {
            ketSet[i] = entry.getKey();
            i++;
        }
        return ketSet;
    }

}
