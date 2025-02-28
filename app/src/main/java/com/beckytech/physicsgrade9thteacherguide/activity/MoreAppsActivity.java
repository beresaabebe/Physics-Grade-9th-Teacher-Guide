package com.beckytech.physicsgrade9thteacherguide.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
import com.facebook.ads.Ad;
import com.facebook.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class MoreAppsActivity extends AppCompatActivity implements MoreAppsAdapter.OnAppClicked {

    private final MoreAppsImage image = new MoreAppsImage();
    private final MoreAppTitle title = new MoreAppTitle();
    private final MoreAppUrl url = new MoreAppUrl();
    private final MoreAppsBgColor color = new MoreAppsBgColor();
    private AdView adView;
    private List<MoreAppsModel> list;
    private com.facebook.ads.AdView fbAds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_apps);
        adaptiveAds();
        toolBar();
        recyclerView();
        facebookAds();
    }

    private void facebookAds() {
        Random random = new Random();
        int rand = random.nextInt(100) + 1;
        LinearLayout banner_container = findViewById(R.id.banner_container);
        if (rand % 3 == 0)
            fbAds = new com.facebook.ads.AdView(this, "813997437115809_1166976755151207", com.facebook.ads.AdSize.RECTANGLE_HEIGHT_250);
        else
            fbAds = new com.facebook.ads.AdView(this, "813997437115809_1166980901817459", com.facebook.ads.AdSize.BANNER_HEIGHT_50);
        banner_container.addView(fbAds);
        fbAds.loadAd(fbAds.buildLoadAdConfig().withAdListener(new AdListener() {
            @Override
            public void onError(Ad ad, com.facebook.ads.AdError adError) {
                Log.d(MoreAppsActivity.this.getPackageName(), "onError");
            }

            @Override
            public void onAdLoaded(Ad ad) {
                Log.d(MoreAppsActivity.this.getPackageName(), "onAdLoaded");
            }

            @Override
            public void onAdClicked(Ad ad) {
                Log.d(MoreAppsActivity.this.getPackageName(), "onAdClicked");
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                Log.d(MoreAppsActivity.this.getPackageName(), "onLoggingImpression");
            }
        }).build());
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
                    url.url[i],
                    image.images[i],
                    color.color[i]));
        }
    }

    private void toolBar() {
        TextView tv_title = findViewById(R.id.tv_title);
        tv_title.setText(R.string.more_apps_for_grade_9th);
        tv_title.setTextColor(ContextCompat.getColor(this, R.color.white));
        ImageButton back_btn = findViewById(R.id.ib_back);
        back_btn.setColorFilter(ContextCompat.getColor(this, R.color.white));
        back_btn.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void adaptiveAds() {
        MobileAds.initialize(this, initializationStatus -> {
        });
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
        String url;
        if (model.getUrl() == null || Objects.equals(model.getUrl(),"")) {
            url = "https://play.google.com/store/apps/dev?id=6669279757479011928";
        }
        else {
            url = "http://play.google.com/store/apps/details?id=" + model.getUrl();
        }
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    @Override
    protected void onDestroy() {
        if (adView != null)
            adView.destroy();

        if (fbAds != null)
            fbAds.destroy();

        super.onDestroy();
    }
}