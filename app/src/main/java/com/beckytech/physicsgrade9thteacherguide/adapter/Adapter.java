package com.beckytech.physicsgrade9thteacherguide.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.beckytech.physicsgrade9thteacherguide.R;
import com.beckytech.physicsgrade9thteacherguide.model.Model;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;

import java.util.ArrayList;
import java.util.List;

public class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM_VIEW_TYPE_CONTENT = 0;
    private static final int ITEM_VIEW_TYPE_AD = 1;
    private static final int AD_INTERVAL = 4;

    private final List<Object> items = new ArrayList<>();
    private final onBookClicked bookClicked;
    private final Activity activity;

    public Adapter(List<Model> list, onBookClicked bookClicked, Activity activity) {
        this.bookClicked = bookClicked;
        this.activity = activity;
        
        // Populate items with content and ad placeholders
        for (int i = 0; i < list.size(); i++) {
            items.add(list.get(i));
            if ((i + 1) % AD_INTERVAL == 0 && i != list.size() - 1) {
                items.add(null); // Placeholder for ad
            }
        }
    }

    public interface onBookClicked {
        void clickedBook(Model model);
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof Model ? ITEM_VIEW_TYPE_CONTENT : ITEM_VIEW_TYPE_AD;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_VIEW_TYPE_AD) {
            return new AdViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_native_ad, parent, false));
        }
        return new PageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == ITEM_VIEW_TYPE_CONTENT) {
            Model model = (Model) items.get(position);
            PageViewHolder contentHolder = (PageViewHolder) holder;
            contentHolder.title.setText(model.getTitle());
            contentHolder.subTitle.setText(model.getSubTitle());
            contentHolder.itemView.setOnClickListener(v -> bookClicked.clickedBook(model));
        } else {
            Object adItem = items.get(position);
            if (adItem instanceof NativeAd) {
                populateNativeAdView((NativeAd) adItem, ((AdViewHolder) holder).nativeAdView);
                ((AdViewHolder) holder).bannerContainer.setVisibility(View.GONE);
                ((AdViewHolder) holder).nativeAdView.setVisibility(View.VISIBLE);
            } else {
                loadAd((AdViewHolder) holder, position);
            }
        }
    }

    private void loadAd(AdViewHolder holder, int position) {
        AdLoader adLoader = new AdLoader.Builder(activity, activity.getString(R.string.native_ads_unit_id))
                .forNativeAd(nativeAd -> {
                    items.set(position, nativeAd);
                    holder.bannerContainer.setVisibility(View.GONE);
                    holder.nativeAdView.setVisibility(View.VISIBLE);
                    populateNativeAdView(nativeAd, holder.nativeAdView);
                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                        // Fallback to Medium Rectangle Banner
                        loadBannerAd(holder, AdSize.MEDIUM_RECTANGLE);
                    }
                })
                .build();
        adLoader.loadAd(new AdRequest.Builder().build());
    }

    private void loadBannerAd(AdViewHolder holder, AdSize adSize) {
        AdView adView = new AdView(activity);
        adView.setAdUnitId(activity.getString(R.string.banner_ad_unit_id));
        adView.setAdSize(adSize);
        holder.nativeAdView.setVisibility(View.GONE);
        holder.bannerContainer.setVisibility(View.VISIBLE);
        holder.bannerContainer.removeAllViews();
        holder.bannerContainer.addView(adView);
        adView.loadAd(new AdRequest.Builder().build());
        
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                // If Medium Rectangle fails, fallback to standard banner
                if (adSize == AdSize.MEDIUM_RECTANGLE) {
                    loadBannerAd(holder, AdSize.BANNER);
                }
            }
        });
    }

    private void populateNativeAdView(NativeAd nativeAd, NativeAdView adView) {
        adView.setMediaView(adView.findViewById(R.id.ad_media));
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

        if (adView.getHeadlineView() != null) {
            ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
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

        if (nativeAd.getPrice() == null) {
            if (adView.getPriceView() != null) adView.getPriceView().setVisibility(View.INVISIBLE);
        } else {
            if (adView.getPriceView() != null) {
                adView.getPriceView().setVisibility(View.VISIBLE);
                ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
            }
        }

        if (nativeAd.getStore() == null) {
            if (adView.getStoreView() != null) adView.getStoreView().setVisibility(View.INVISIBLE);
        } else {
            if (adView.getStoreView() != null) {
                adView.getStoreView().setVisibility(View.VISIBLE);
                ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
            }
        }

        if (nativeAd.getAdvertiser() == null) {
            if (adView.getAdvertiserView() != null) adView.getAdvertiserView().setVisibility(View.INVISIBLE);
        } else {
            if (adView.getAdvertiserView() != null) {
                adView.getAdvertiserView().setVisibility(View.VISIBLE);
                ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            }
        }

        adView.setNativeAd(nativeAd);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    protected static class PageViewHolder extends RecyclerView.ViewHolder {
        TextView title, subTitle;
        ImageView imageView;

        public PageViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            title.setSelected(true);
            subTitle = itemView.findViewById(R.id.subTitle);
            imageView = itemView.findViewById(R.id.image);
        }
    }

    protected static class AdViewHolder extends RecyclerView.ViewHolder {
        NativeAdView nativeAdView;
        FrameLayout bannerContainer;

        public AdViewHolder(@NonNull View itemView) {
            super(itemView);
            nativeAdView = itemView.findViewById(R.id.native_ad_view);
            bannerContainer = itemView.findViewById(R.id.banner_container);
        }
    }
}
