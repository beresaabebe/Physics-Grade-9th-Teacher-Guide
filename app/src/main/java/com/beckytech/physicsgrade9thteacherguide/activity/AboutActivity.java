package com.beckytech.physicsgrade9thteacherguide.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.beckytech.physicsgrade9thteacherguide.R;
import com.beckytech.physicsgrade9thteacherguide.adapter.AboutAdapter;
import com.beckytech.physicsgrade9thteacherguide.contents.AboutImages;
import com.beckytech.physicsgrade9thteacherguide.contents.AboutName;
import com.beckytech.physicsgrade9thteacherguide.contents.AboutUrlContents;
import com.beckytech.physicsgrade9thteacherguide.model.AboutModel;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AboutActivity extends AppCompatActivity implements AboutAdapter.OnLinkClicked {
    private final AboutImages images = new AboutImages();
    private final AboutName name = new AboutName();
    private final AboutUrlContents urlContents = new AboutUrlContents();
    List<AboutModel> modelList;
    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        MobileAds.initialize(this, initializationStatus -> {
        });
        adaptiveAds();
        allContents();
    }

    private void allContents() {
        ImageButton back_btn = findViewById(R.id.ib_back);
        back_btn.setOnClickListener(v -> onBackPressed());
        back_btn.setColorFilter(ContextCompat.getColor(this, R.color.white));
        String str = "About us";
        TextView title = findViewById(R.id.tv_title);
        title.setText(str);
        title.setTextColor(ContextCompat.getColor(this, R.color.white));

        WebView webView = findViewById(R.id.webView);
        webView.loadUrl("file:///android_asset/about.html");

        TextView version = findViewById(R.id.version_tv);
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getBaseContext().getPackageName(),0);
            version.setText(String.format(Locale.ENGLISH, " %s", info.versionName));
        }
        catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        shareBtn();

        RecyclerView recyclerView = findViewById(R.id.recycler_about);
        getData();
        AboutAdapter adapter = new AboutAdapter(modelList, this);
        recyclerView.setAdapter(adapter);
    }

    private void shareBtn() {
        ImageView imageView = findViewById(R.id.imageView);
        imageView.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            String url = "https://play.google.com/store/apps/details?id="+getPackageName();
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, R.string.app_name);
            intent.putExtra(Intent.EXTRA_TEXT, "Sharing is caring! Share me with others so that they can also use me.\n"+url);
            startActivity(Intent.createChooser(intent, "Share via"));
        });
    }

    private void getData() {
        modelList = new ArrayList<>();
        for (int i = 0; i < name.name.length; i++) {
            modelList.add(new AboutModel(images.images[i],
                    name.name[i], urlContents.url[i]));
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

    @Override
    public void linkClicked(AboutModel model) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(model.getUrl()));
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }
}