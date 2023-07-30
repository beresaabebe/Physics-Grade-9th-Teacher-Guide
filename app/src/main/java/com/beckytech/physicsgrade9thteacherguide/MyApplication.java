package com.beckytech.physicsgrade9thteacherguide;

import android.app.Application;

import com.google.android.gms.ads.MobileAds;

/**
 * The Application class that manages AppOpenManager.
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        MobileAds.initialize(
                this,
                initializationStatus -> {
                });

        AppOpenManager appOpenManager = new AppOpenManager(this);
    }
}