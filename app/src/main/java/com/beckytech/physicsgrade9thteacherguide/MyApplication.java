package com.beckytech.physicsgrade9thteacherguide;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.appopen.AppOpenAd;

import java.util.Date;

public class MyApplication extends Application implements Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {

    public interface OnAdEventListener {
        void onAdDismissed();
        void onAdFailed();
    }

    private AppOpenAdManager appOpenAdManager;
    private Activity currentActivity;
    private OnAdEventListener onAdEventListener;

    public void setOnAdEventListener(OnAdEventListener listener) {
        this.onAdEventListener = listener;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.registerActivityLifecycleCallbacks(this);

        MobileAds.initialize(this, initializationStatus -> {});

        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        appOpenAdManager = new AppOpenAdManager();
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onStart(owner);
        appOpenAdManager.showAdIfAvailable(currentActivity);
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {}

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        currentActivity = activity;
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        currentActivity = activity;
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {}

    @Override
    public void onActivityStopped(@NonNull Activity activity) {}

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {}

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        currentActivity = null;
    }

    private class AppOpenAdManager {
        private static final String LOG_TAG = "AppOpenAdManager";
        private AppOpenAd appOpenAd = null;
        private boolean isLoadingAd = false;
        private boolean isShowingAd = false;
        private boolean isFirstLaunch = true;
        private long loadTime = 0;
        private final long launchTime;

        public AppOpenAdManager() {
            launchTime = System.currentTimeMillis();
        }

        private void loadAd(Context context) {
            if (isLoadingAd || isAdAvailable()) {
                return;
            }

            isLoadingAd = true;
            AdRequest request = new AdRequest.Builder().build();
            AppOpenAd.load(
                    context,
                    context.getString(R.string.app_open_ads_unit_id),
                    request,
                    new AppOpenAd.AppOpenAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull AppOpenAd ad) {
                            appOpenAd = ad;
                            isLoadingAd = false;
                            loadTime = (new Date()).getTime();
                            Log.d(LOG_TAG, "onAdLoaded.");
                            
                            // If it's first launch, show it immediately when loaded, with a 7-second cutoff
                            long timeSinceLaunch = System.currentTimeMillis() - launchTime;
                            if (isFirstLaunch && currentActivity != null && timeSinceLaunch < 7000) {
                                showAdIfAvailable(currentActivity);
                                isFirstLaunch = false;
                            } else {
                                isFirstLaunch = false; // Ad loaded too late, don't auto-show
                            }
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            isLoadingAd = false;
                            Log.d(LOG_TAG, "onAdFailedToLoad: " + loadAdError.getMessage());
                            isFirstLaunch = false; // Stop trying first launch logic on failure
                            if (onAdEventListener != null) {
                                onAdEventListener.onAdFailed();
                            }
                        }
                    });
        }

        private boolean wasLoadTimeLessThanNHoursAgo(long numHours) {
            long dateDifference = (new Date()).getTime() - loadTime;
            long numMilliSecondsPerHour = 3600000;
            return (dateDifference < (numMilliSecondsPerHour * numHours));
        }

        private boolean isAdAvailable() {
            return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4);
        }

        public void showAdIfAvailable(@NonNull final Activity activity) {
            if (isShowingAd) {
                Log.d(LOG_TAG, "The app open ad is already showing.");
                return;
            }

            if (!isAdAvailable()) {
                Log.d(LOG_TAG, "The app open ad is not ready yet.");
                loadAd(activity);
                return;
            }

            appOpenAd.setFullScreenContentCallback(
                    new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            appOpenAd = null;
                            isShowingAd = false;
                            Log.d(LOG_TAG, "onAdDismissedFullScreenContent.");
                            if (onAdEventListener != null) {
                                onAdEventListener.onAdDismissed();
                            }
                            loadAd(activity);
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                            appOpenAd = null;
                            isShowingAd = false;
                            Log.d(LOG_TAG, "onAdFailedToShowFullScreenContent: " + adError.getMessage());
                            if (onAdEventListener != null) {
                                onAdEventListener.onAdFailed();
                            }
                            loadAd(activity);
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                            isShowingAd = true;
                            Log.d(LOG_TAG, "onAdShowedFullScreenContent.");
                        }
                    });

            appOpenAd.show(activity);
        }
    }
}
