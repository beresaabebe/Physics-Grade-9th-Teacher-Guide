package com.beckytech.physicsgrade9thteacherguide;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.beckytech.physicsgrade9thteacherguide.activity.AboutActivity;
import com.beckytech.physicsgrade9thteacherguide.activity.BookDetailActivity;
import com.beckytech.physicsgrade9thteacherguide.activity.MoreAppsActivity;
import com.beckytech.physicsgrade9thteacherguide.activity.PrivacyActivity;
import com.beckytech.physicsgrade9thteacherguide.adapter.Adapter;
import com.beckytech.physicsgrade9thteacherguide.contents.ContentEndPage;
import com.beckytech.physicsgrade9thteacherguide.contents.ContentStartPage;
import com.beckytech.physicsgrade9thteacherguide.contents.SubTitleContents;
import com.beckytech.physicsgrade9thteacherguide.contents.TitleContents;
import com.beckytech.physicsgrade9thteacherguide.model.Model;
import com.facebook.ads.Ad;
import com.facebook.ads.AdListener;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements Adapter.onBookClicked {

    private InterstitialAd mInterstitialAd;
    private final List<Model> list = new ArrayList<>();
    private final ContentStartPage startPage = new ContentStartPage();
    private final TitleContents titleContents = new TitleContents();
    private final ContentEndPage endPage = new ContentEndPage();
    private final SubTitleContents subTitleContents = new SubTitleContents();
    private DrawerLayout drawerLayout;
    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_drawer);

        AppRate.app_launched(this);
        MobileAds.initialize(this, initializationStatus -> {
        });
        setAds();
        allContents();
        adaptiveAds();
    }

    private void allContents() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name);
        drawerToggle.syncState();
        drawerToggle.getDrawerArrowDrawable().setColor(Color.WHITE);
        drawerLayout.addDrawerListener(drawerToggle);

        NavigationView navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(item -> {
            MenuOptions(item);
            return true;
        });

        View menu = navigationView.getHeaderView(0);
        ImageView back_btn = menu.findViewById(R.id.back_btn_image);
        back_btn.setOnClickListener(view -> drawerLayout.closeDrawer(GravityCompat.START));
        back_btn.setColorFilter(ContextCompat.getColor(this,R.color.white));

        ImageView share_btn = menu.findViewById(R.id.share_btn_image);
        share_btn.setOnClickListener(view -> {
            shareBtn();
        });
        share_btn.setColorFilter(ContextCompat.getColor(this, R.color.white));

        TextView nav_title = menu.findViewById(R.id.tv_title);
        nav_title.setTextColor(Color.WHITE);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        getData();
        Adapter adapter = new Adapter(list, this);
        recyclerView.setAdapter(adapter);
    }

    private void shareBtn() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        String url = "https://play.google.com/store/apps/details?id=" + getPackageName();
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
        intent.putExtra(Intent.EXTRA_TEXT, "Sharing is caring, but not my pizza. Share me and let others also use me. \n" + url);
        startActivity(Intent.createChooser(intent, "Share Via"));
    }

    private void getData() {
        list.clear();
        for (int i = 0; i < titleContents.title.length; i++) {
            list.add(new Model(titleContents.title[i],
                    subTitleContents.subTitle[i],
                    startPage.pageStart[i],
                    endPage.pageEnd[i]));
        }
    }

    private void adaptiveAds() {
        FrameLayout adContainerView = findViewById(R.id.adView_container);
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
        adView.setAdSize(adSize);
        adView.loadAd(adRequest);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    void MenuOptions(MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);
        if (item.getItemId() == R.id.action_privacy)
            startActivity(new Intent(this, PrivacyActivity.class));
        if (item.getItemId() == R.id.action_about_us) {
            if (mInterstitialAd != null) {
                mInterstitialAd.show(MainActivity.this);
                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent();

                        startActivity(new Intent(MainActivity.this, AboutActivity.class));
                        mInterstitialAd = null;
                        setAds();
                    }
                });
            } else {
                startActivity(new Intent(this, AboutActivity.class));
            }
        }

        if (item.getItemId() == R.id.action_rate) {
            AppRate.showRateDialog(this, null);
        }

        if (item.getItemId() == R.id.action_more_apps) {
            if (mInterstitialAd != null) {
                mInterstitialAd.show(MainActivity.this);
                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent();
                        startActivity(new Intent(MainActivity.this, MoreAppsActivity.class));
                        mInterstitialAd = null;
                        setAds();
                    }
                });
            } else {
                startActivity(new Intent(this, MoreAppsActivity.class));
            }
        }

        if (item.getItemId() == R.id.action_share) {
            shareBtn();
        }

        if (item.getItemId() == R.id.action_update) {
            String url = "https://play.google.com/store/apps/details?id=" + getPackageName();
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        }
        if (item.getItemId() == R.id.action_exit) {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.MyAlertDialog);
            builder.setTitle("Exit")
                    .setMessage("Do you want to close?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        finishAffinity();
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        }
    }

    @Override
    public void clickedBook(Model model) {
        if (mInterstitialAd != null) {
            mInterstitialAd.show(MainActivity.this);
            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent();
                    startActivity(new Intent(MainActivity.this, BookDetailActivity.class).putExtra("data", model));
                    mInterstitialAd = null;
                    setAds();
                }
            });
        } else {
            startActivity(new Intent(this, BookDetailActivity.class).putExtra("data", model));
        }
    }

    private void setAds() {
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(this, getString(R.string.interstitial_ads_unit_id), adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        mInterstitialAd = null;
                    }
                });
    }

    @Override
    protected void onDestroy() {
        if (adView != null)
            adView.destroy();

        super.onDestroy();
    }
}
