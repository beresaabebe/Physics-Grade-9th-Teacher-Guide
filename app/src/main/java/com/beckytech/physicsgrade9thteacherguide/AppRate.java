package com.beckytech.physicsgrade9thteacherguide;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.gms.tasks.Task;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;

public class AppRate {
    private final static int DAYS_UNTIL_PROMPT = 3;
    private final static int LAUNCHES_UNTIL_PROMPT = 5;

    public static void app_launched(Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences("apprater", 0);
        if (prefs.getBoolean("dontshowagain", false)) {
            return;
        }

        SharedPreferences.Editor editor = prefs.edit();
        long launch_count = prefs.getLong("launch_count", 0) + 1;
        editor.putLong("launch_count", launch_count);

        long date_firstLaunch = prefs.getLong("date_firstlaunch", 0);
        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong("date_firstlaunch", date_firstLaunch);
        }

        if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
            if (System.currentTimeMillis() >= date_firstLaunch + (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
                showRateDialog(activity, editor);
            }
        }
        editor.apply();
    }

    public static void showRateDialog(final Activity activity, final SharedPreferences.Editor editor) {
        new MaterialAlertDialogBuilder(activity)
                .setTitle("Rate " + activity.getString(R.string.app_name))
                .setMessage("If you enjoy using this app, please take a moment to rate it. Thanks for your support!")
                .setPositiveButton("Rate Now", (dialog, which) -> {
                    ReviewManager manager = ReviewManagerFactory.create(activity);
                    Task<ReviewInfo> request = manager.requestReviewFlow();
                    request.addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            ReviewInfo reviewInfo = task.getResult();
                            Task<Void> flow = manager.launchReviewFlow(activity, reviewInfo);
                            flow.addOnCompleteListener(task1 -> {
                                editor.putBoolean("dontshowagain", true);
                                editor.apply();
                            });
                        }
                    });
                })
                .setNeutralButton("Remind Me Later", (dialog, which) -> dialog.dismiss())
                .setNegativeButton("No, Thanks", (dialog, which) -> {
                    if (editor != null) {
                        editor.putBoolean("dontshowagain", true);
                        editor.apply();
                    }
                    dialog.dismiss();
                })
                .show();
    }
}
