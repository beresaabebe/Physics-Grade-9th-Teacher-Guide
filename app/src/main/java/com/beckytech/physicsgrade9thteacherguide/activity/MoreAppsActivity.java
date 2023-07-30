package com.beckytech.physicsgrade9thteacherguide.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.beckytech.physicsgrade9thteacherguide.R;
import com.beckytech.physicsgrade9thteacherguide.adapter.MoreAppsAdapter;
import com.beckytech.physicsgrade9thteacherguide.contents.MoreAppTitle;
import com.beckytech.physicsgrade9thteacherguide.contents.MoreAppUrl;
import com.beckytech.physicsgrade9thteacherguide.contents.MoreAppsBgColor;
import com.beckytech.physicsgrade9thteacherguide.contents.MoreAppsImage;
import com.beckytech.physicsgrade9thteacherguide.model.MoreAppsModel;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;
import java.util.List;

public class MoreAppsActivity extends AppCompatActivity implements MoreAppsAdapter.OnAppClicked {

    private AdView adView;

    private List<MoreAppsModel> list;
    private final MoreAppsImage image = new MoreAppsImage();
    private final MoreAppTitle title = new MoreAppTitle();
    private final MoreAppUrl url = new MoreAppUrl();
    private final MoreAppsBgColor color = new MoreAppsBgColor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_apps);
        adaptiveAds();
        toolBar();
        recyclerView();
    }

    private void recyclerView() {
        RecyclerView moreRecyclerView = findViewById(R.id.more_app_recyclerView);
        getData();
        MoreAppsAdapter moreAppsAdapter = new MoreAppsAdapter(list, this, this);
        moreRecyclerView.setAdapter(moreAppsAdapter);
    }

    private void getData() {
        list = new ArrayList<>();
        for (int i = 0; i < title.title.length; i++) {
            list.add(new MoreAppsModel(title.title[i],
                    "http://play.google.com/store/apps/details?id="+url.url[i],
                    image.images[i],
                    color.color[i]));
        }
    }

    private void toolBar() {
        TextView tv_title = findViewById(R.id.tv_title);
        tv_title.setText(R.string.more_apps_for_grade_9th);
        tv_title.setTextColor(ContextCompat.getColor(this, R.color.white));
        ImageButton back_btn = findViewById(R.id.ib_back);
        back_btn.setColorFilter(ContextCompat.getColor(this,R.color.white));
        back_btn.setOnClickListener(view -> onBackPressed());
    }

    private void adaptiveAds() {
        MobileAds.initialize(this, initializationStatus -> {});
        FrameLayout adContainerView = findViewById(R.id.adView_container);
        //Create an AdView and put it into your FrameLayout
        adView = new AdView(this);
        adContainerView.addView(adView);
        adView.setAdUnitId(getString(R.string.banner_ad_unit_id));
        loadBanner();
    }

    public AdSize getAdSize() {
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;
        int adWidth = (int) (widthPixels / density);
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
    }

    public void loadBanner() {
        AdRequest adRequest = new AdRequest.Builder().build();
        AdSize adSize = getAdSize();
        // Set the adaptive ad size to the ad view.
        adView.setAdSize(adSize);
        // Start loading the ad in the background.
        adView.loadAd(adRequest);
    }

    @Override
    public void clickedApp(MoreAppsModel model) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(model.getUrl())));
    }
}