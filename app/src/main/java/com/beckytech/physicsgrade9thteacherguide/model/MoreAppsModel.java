package com.beckytech.physicsgrade9thteacherguide.model;

import android.graphics.drawable.Drawable;

public class MoreAppsModel {
    private String appName, url;
    private int appImage;
    private final int color;

    public MoreAppsModel(String appName, String url, int appImage, int color) {
        this.appName = appName;
        this.url = url;
        this.appImage = appImage;
        this.color = color;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getAppImage() {
        return appImage;
    }

    public void setAppImage(int appImage) {
        this.appImage = appImage;
    }

    public int getColor() {
        return color;
    }
}
