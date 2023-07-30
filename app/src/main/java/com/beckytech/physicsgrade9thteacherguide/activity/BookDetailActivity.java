package com.beckytech.physicsgrade9thteacherguide.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.beckytech.physicsgrade9thteacherguide.R;
import com.beckytech.physicsgrade9thteacherguide.model.Model;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;
import java.util.List;

public class BookDetailActivity extends AppCompatActivity {
    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);
        allContents();
        adaptiveAds();
        shareBtn();
    }

    private void shareBtn() {
        ImageView share_btn = findViewById(R.id.share_btn_image);
        share_btn.setColorFilter(ContextCompat.getColor(this, R.color.white));
        share_btn.setOnClickListener(view -> {
            String url = "https://play.google.com/store/apps/details?id=";
            Intent intent = new Intent(Intent.ACTION_SEND).putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
                    .putExtra(Intent.EXTRA_TEXT, "Sharing is caring, but not my toothbrush. Share me and let others also use me.\n"+
                            url+getPackageName())
                    .setType("text/plain");
            startActivity(Intent.createChooser(intent, "Share with"));
        });
        share_btn.setColorFilter(ContextCompat.getColor(this, R.color.white));
    }

    private void allContents() {
        ImageButton back_btn = findViewById(R.id.back_book_detail);
        back_btn.setOnClickListener(v -> onBackPressed());
        back_btn.setColorFilter(ContextCompat.getColor(this, R.color.white));

        Intent intent = getIntent();
        Model model = (Model) intent.getSerializableExtra("data");

        TextView title = findViewById(R.id.title_book_detail);
        title.setSelected(true);
        title.setText(model.getTitle());
        title.setTextColor(Color.WHITE);

        TextView subTitle = findViewById(R.id.sub_title_book_detail);
        subTitle.setText(model.getSubTitle());
        subTitle.setTextColor(Color.WHITE);

        PDFView pdfView = findViewById(R.id.pdfView);

        int start = model.getStartPage();
        int end = model.getEndPage();

        List<Integer> list = new ArrayList<>();

        for (int i = start; i <= end; i++) {
            list.add(i);
        }

        int[] array = new int[list.size()];

        for (int j = 1; j < array.length; j++) {
            array[j] = list.get(j);
        }

        pdfView.fromAsset("phy9.pdf")
                .pages(array)
                .enableSwipe(true)
                .swipeHorizontal(false)
                .spacing(10)
                .enableDoubletap(true)
                .scrollHandle(new DefaultScrollHandle(this))
                .load();
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
        //Determine the screen width to use for the ad width.
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        //you can also pass your selected width here in dp
        int adWidth = (int) (widthPixels / density);

        //return the optimal size depends on your orientation (landscape or portrait)
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
}