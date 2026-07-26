package com.beckytech.physicsgrade9thteacherguide.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.beckytech.physicsgrade9thteacherguide.PdfRendererManager;
import com.beckytech.physicsgrade9thteacherguide.R;
import com.beckytech.physicsgrade9thteacherguide.contents.ContentEndPage;
import com.beckytech.physicsgrade9thteacherguide.contents.ContentStartPage;
import com.beckytech.physicsgrade9thteacherguide.contents.SubTitleContents;
import com.beckytech.physicsgrade9thteacherguide.contents.TitleContents;
import com.beckytech.physicsgrade9thteacherguide.model.Model;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback;
import com.google.android.gms.tasks.Task;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BookDetailActivity extends AppCompatActivity {
    private static final String TAG = "BookDetailActivity";
    private ViewPager2 viewPager;
    private List<Model> modelList;
    private PdfRendererManager pdfRendererManager;
    private AdView collapsibleAdView;
    private RewardedAd rewardedAd;
    private RewardedInterstitialAd rewardedInterstitialAd;
    private InterstitialAd interstitialAd;
    private AppUpdateManager appUpdateManager;
    private final Random random = new Random();
    private int pageFlipCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        initData();
        initViews();
        initPdf();
        initAds();
        checkUpdate();
    }

    private void initData() {
        modelList = new ArrayList<>();
        TitleContents titleContents = new TitleContents();
        SubTitleContents subTitleContents = new SubTitleContents();
        ContentStartPage startPage = new ContentStartPage();
        ContentEndPage endPage = new ContentEndPage();

        for (int i = 0; i < titleContents.title.length; i++) {
            modelList.add(new Model(titleContents.title[i],
                    subTitleContents.subTitle[i],
                    startPage.pageStart[i],
                    endPage.pageEnd[i]));
        }
    }

    private void initViews() {
        ImageButton back_btn = findViewById(R.id.back_book_detail);
        back_btn.setOnClickListener(v -> finish());
        back_btn.setColorFilter(ContextCompat.getColor(this, R.color.white));

        ImageView share_btn = findViewById(R.id.share_btn_image);
        share_btn.setColorFilter(ContextCompat.getColor(this, R.color.white));
        share_btn.setOnClickListener(view -> {
            String url = "https://play.google.com/store/apps/details?id=" + getPackageName();
            Intent intent = new Intent(Intent.ACTION_SEND).putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
                    .putExtra(Intent.EXTRA_TEXT, "Check out this Teacher Guide for Physics Grade 9th!\n" + url)
                    .setType("text/plain");
            startActivity(Intent.createChooser(intent, "Share with"));
        });

        viewPager = findViewById(R.id.viewPager);
        ChapterPagerAdapter adapter = new ChapterPagerAdapter(modelList);
        viewPager.setAdapter(adapter);

        Intent intent = getIntent();
        Model currentModel = (Model) intent.getSerializableExtra("data");
        if (currentModel != null) {
            for (int i = 0; i < modelList.size(); i++) {
                if (modelList.get(i).getTitle().equals(currentModel.getTitle())) {
                    viewPager.setCurrentItem(i, false);
                    updateToolbar(modelList.get(i));
                    break;
                }
            }
        }

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateToolbar(modelList.get(position));
                showRewardedAd();
            }
        });
    }

    private void updateToolbar(Model model) {
        TextView title = findViewById(R.id.title_book_detail);
        title.setSelected(true);
        title.setText(model.getTitle());

        TextView subTitle = findViewById(R.id.sub_title_book_detail);
        subTitle.setText(model.getSubTitle());
    }

    private void initPdf() {
        try {
            pdfRendererManager = new PdfRendererManager(this, "phy9.pdf");
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading PDF", Toast.LENGTH_SHORT).show();
        }
    }

    private void initAds() {
        loadCollapsibleBanner();
        loadRewardedAd();
        loadRewardedInterstitialAd();
        loadInterstitialAd();
    }

    private void loadCollapsibleBanner() {
        FrameLayout adContainer = findViewById(R.id.adView_container);
        collapsibleAdView = new AdView(this);
        collapsibleAdView.setAdUnitId(getString(R.string.banner_ad_unit_id));
        adContainer.addView(collapsibleAdView);

        AdRequest adRequest = new AdRequest.Builder()
                .addNetworkExtrasBundle(com.google.ads.mediation.admob.AdMobAdapter.class, getCollapsibleBundle())
                .build();
        collapsibleAdView.setAdSize(getAdSize());
        collapsibleAdView.loadAd(adRequest);
    }

    private Bundle getCollapsibleBundle() {
        Bundle extras = new Bundle();
        extras.putString("collapsible", "bottom");
        return extras;
    }

    private AdSize getAdSize() {
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;
        int adWidth = (int) (widthPixels / density);
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
    }

    private void loadRewardedAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(this, getString(R.string.rewarded_ads_unit_id),
                adRequest, new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        rewardedAd = null;
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd ad) {
                        rewardedAd = ad;
                    }
                });
    }

    private void loadRewardedInterstitialAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedInterstitialAd.load(this, getString(R.string.rewarded_interstitial_ads_unit_id),
                adRequest, new RewardedInterstitialAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        rewardedInterstitialAd = null;
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedInterstitialAd ad) {
                        rewardedInterstitialAd = ad;
                    }
                });
    }

    private void loadInterstitialAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this, getString(R.string.interstitial_ads_unit_id),
                adRequest, new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd ad) {
                        interstitialAd = ad;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        interstitialAd = null;
                    }
                });
    }

    private void showRewardedAd() {
        pageFlipCount++;
        if (pageFlipCount % 5 != 0) return;

        int r = random.nextInt(10);
        if (r % 2 == 0) {
            if (rewardedAd != null) {
                rewardedAd.show(this, rewardItem -> loadRewardedAd());
            } else if (rewardedInterstitialAd != null) {
                rewardedInterstitialAd.show(this, rewardItem -> loadRewardedInterstitialAd());
            }
        } else {
            if (interstitialAd != null) {
                interstitialAd.show(this);
                loadInterstitialAd();
            }
        }
    }

    private void checkUpdate() {
        appUpdateManager = AppUpdateManagerFactory.create(this);
        Task<AppUpdateInfo> task = appUpdateManager.getAppUpdateInfo();
        task.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                try {
                    appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, this, 1001);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (appUpdateManager != null) {
            appUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    try {
                        appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, this, 1001);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        if (pdfRendererManager != null) pdfRendererManager.close();
        if (collapsibleAdView != null) collapsibleAdView.destroy();
        super.onDestroy();
    }

    private class ChapterPagerAdapter extends RecyclerView.Adapter<ChapterPagerAdapter.ChapterViewHolder> {
        private final List<Model> chapters;

        public ChapterPagerAdapter(List<Model> chapters) {
            this.chapters = chapters;
        }

        @NonNull
        @Override
        public ChapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chapter_container, parent, false);
            return new ChapterViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ChapterViewHolder holder, int position) {
            Model chapter = chapters.get(position);
            holder.bind(chapter);
        }

        @Override
        public int getItemCount() {
            return chapters.size();
        }

        class ChapterViewHolder extends RecyclerView.ViewHolder {
            RecyclerView recyclerView;

            public ChapterViewHolder(@NonNull View itemView) {
                super(itemView);
                recyclerView = itemView.findViewById(R.id.chapterRecyclerView);
                recyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            }

            public void bind(Model chapter) {
                PageAdapter adapter = new PageAdapter(chapter.getStartPage(), chapter.getEndPage());
                recyclerView.setAdapter(adapter);
            }
        }
    }

    private class PageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final List<Object> items;
        private static final int TYPE_PAGE = 0;
        private static final int TYPE_AD = 1;

        public PageAdapter(int start, int end) {
            items = new ArrayList<>();
            int pageCount = 0;
            for (int i = start; i <= end; i++) {
                items.add(i);
                pageCount++;
                if (pageCount % 4 == 0) {
                    items.add("AD");
                }
            }
        }

        @Override
        public int getItemViewType(int position) {
            return items.get(position) instanceof Integer ? TYPE_PAGE : TYPE_AD;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == TYPE_PAGE) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pdf_page, parent, false);
                return new PageViewHolder(view);
            } else {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ad_container, parent, false);
                return new AdViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof PageViewHolder) {
                int pageIndex = (int) items.get(position);
                ((PageViewHolder) holder).bind(pageIndex);
            } else if (holder instanceof AdViewHolder) {
                ((AdViewHolder) holder).bind(position);
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class PageViewHolder extends RecyclerView.ViewHolder {
            PhotoView imageView;

            public PageViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.pdf_page_image);
            }

            public void bind(int pageIndex) {
                Bitmap bitmap = pdfRendererManager.renderPage(pageIndex);
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }

        class AdViewHolder extends RecyclerView.ViewHolder {
            LinearLayout container;

            public AdViewHolder(@NonNull View itemView) {
                super(itemView);
                container = itemView.findViewById(R.id.ad_container);
            }

            public void bind(int position) {
                Object adItem = items.get(position);
                if (adItem instanceof NativeAd) {
                    NativeAdView adView = (NativeAdView) getLayoutInflater().inflate(R.layout.item_native_ad, null);
                    populateNativeAdView((NativeAd) adItem, adView);
                    container.removeAllViews();
                    container.addView(adView);
                } else if (adItem instanceof AdView) {
                    AdView adView = (AdView) adItem;
                    if (adView.getParent() != null) {
                        ((ViewGroup) adView.getParent()).removeView(adView);
                    }
                    container.removeAllViews();
                    container.addView(adView);
                } else {
                    loadNativeAd(container, position);
                }
            }

            private void loadNativeAd(LinearLayout container, int position) {
                AdLoader adLoader = new AdLoader.Builder(BookDetailActivity.this, getString(R.string.native_ads_unit_id))
                        .forNativeAd(nativeAd -> {
                            items.set(position, nativeAd);
                            NativeAdView adView = (NativeAdView) getLayoutInflater().inflate(R.layout.item_native_ad, null);
                            populateNativeAdView(nativeAd, adView);
                            container.removeAllViews();
                            container.addView(adView);
                        })
                        .withAdListener(new AdListener() {
                            @Override
                            public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                                loadMediumRectangle(container, position);
                            }
                        }).build();
                adLoader.loadAd(new AdRequest.Builder().build());
            }

            private void loadMediumRectangle(LinearLayout container, int position) {
                AdView adView = new AdView(BookDetailActivity.this);
                adView.setAdSize(AdSize.MEDIUM_RECTANGLE);
                adView.setAdUnitId(getString(R.string.banner_ad_unit_id));
                adView.setAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                        loadNormalBanner(container, position);
                    }

                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                        items.set(position, adView);
                    }
                });
                container.removeAllViews();
                container.addView(adView);
                adView.loadAd(new AdRequest.Builder().build());
            }

            private void loadNormalBanner(LinearLayout container, int position) {
                AdView adView = new AdView(BookDetailActivity.this);
                adView.setAdSize(AdSize.BANNER);
                adView.setAdUnitId(getString(R.string.banner_ad_unit_id));
                adView.setAdListener(new AdListener() {
                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                        items.set(position, adView);
                    }
                });
                container.removeAllViews();
                container.addView(adView);
                adView.loadAd(new AdRequest.Builder().build());
            }

            private void populateNativeAdView(NativeAd nativeAd, NativeAdView adView) {
                adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
                adView.setBodyView(adView.findViewById(R.id.ad_body));
                adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
                adView.setIconView(adView.findViewById(R.id.ad_app_icon));
                adView.setMediaView(adView.findViewById(R.id.ad_media));
                adView.setPriceView(adView.findViewById(R.id.ad_price));
                adView.setStoreView(adView.findViewById(R.id.ad_store));
                adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

                if (adView.getHeadlineView() != null) {
                    ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
                }

                if (adView.getMediaView() != null && nativeAd.getMediaContent() != null) {
                    adView.getMediaView().setMediaContent(nativeAd.getMediaContent());
                }

                if (nativeAd.getBody() == null) {
                    if (adView.getBodyView() != null) adView.getBodyView().setVisibility(View.INVISIBLE);
                } else {
                    if (adView.getBodyView() != null) {
                        adView.getBodyView().setVisibility(View.VISIBLE);
                        ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
                    }
                }

                if (nativeAd.getCallToAction() == null) {
                    if (adView.getCallToActionView() != null) adView.getCallToActionView().setVisibility(View.INVISIBLE);
                } else {
                    if (adView.getCallToActionView() != null) {
                        adView.getCallToActionView().setVisibility(View.VISIBLE);
                        ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
                    }
                }

                if (nativeAd.getIcon() == null) {
                    if (adView.getIconView() != null) adView.getIconView().setVisibility(View.GONE);
                } else {
                    if (adView.getIconView() != null) {
                        ((ImageView) adView.getIconView()).setImageDrawable(nativeAd.getIcon().getDrawable());
                        adView.getIconView().setVisibility(View.VISIBLE);
                    }
                }

                adView.setNativeAd(nativeAd);
            }
        }
    }
}
