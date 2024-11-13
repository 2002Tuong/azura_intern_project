package com.ads.control;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.ads.control.app.R;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;

import java.util.Date;

public class AppOpenManager {

    private static AppOpenManager appOpenManager;
    private AppOpenAd appOpenAd = null;
    private static boolean isShowingAd = false;
    private long loadTime = 0;
    private AppOpenAd.AppOpenAdLoadCallback loadCallback;
    private Context context;
    private AdCloseListener adCloseListener;

    public interface AdCloseListener {
        void onAdClosed();
    }

    public static AppOpenManager getInstance() {
        if (appOpenManager == null)
            appOpenManager = new AppOpenManager();
        return appOpenManager;
    }

    public void init(Context context) {
        this.context = context;
        fetchAd();
    }

    public void showAdOpenApp(Activity activity, AdCloseListener adCloseListener) {
        this.adCloseListener = adCloseListener;
        showAdIfAvailable(activity);
    }

    /**
     * Request an ad
     */
    public void fetchAd() {
        // Have unused ad, no need to fetch another.
        if (isAdAvailable()) {
            return;
        }

        loadCallback = new AppOpenAd.AppOpenAdLoadCallback() {

            @Override
            public void onAdLoaded(@NonNull AppOpenAd ad) {
                AppOpenManager.this.appOpenAd = ad;
                AppOpenManager.this.loadTime = (new Date()).getTime();
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                // Handle the error.
            }

        };
        AdRequest request = getAdRequest();
        AppOpenAd.load(
                context, context.getString(R.string.admob_open_app), request,
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback);
    }

    /**
     * Creates and returns ad request.
     */
    private AdRequest getAdRequest() {
        return new AdRequest.Builder().build();
    }

    /**
     * Utility method that checks if ad exists and can be shown.
     */
    public boolean isAdAvailable() {
        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4);
    }

    /**
     * Utility method to check if ad was loaded more than n hours ago.
     */
    private boolean wasLoadTimeLessThanNHoursAgo(long numHours) {
        long dateDifference = (new Date()).getTime() - this.loadTime;
        long numMilliSecondsPerHour = 3600000;
        return (dateDifference < (numMilliSecondsPerHour * numHours));
    }

    /**
     * Shows the ad if one isn't already showing.
     */
    private void showAdIfAvailable(Activity activity) {
        // Only show ad if there is not already an app open ad currently showing
        // and an ad is available.
        if (!isShowingAd && isAdAvailable()) {

            FullScreenContentCallback fullScreenContentCallback =
                    new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            // Set the reference to null so isAdAvailable() returns false.
                            AppOpenManager.this.appOpenAd = null;
                            isShowingAd = false;
                            fetchAd();
                            if (adCloseListener != null)
                                adCloseListener.onAdClosed();
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(AdError adError) {
                            fetchAd();
                            if (adCloseListener != null)
                                adCloseListener.onAdClosed();
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                            isShowingAd = true;
                        }
                    };

            appOpenAd.setFullScreenContentCallback(fullScreenContentCallback);
            appOpenAd.show(activity);

        } else {
            fetchAd();
            if (adCloseListener != null)
                adCloseListener.onAdClosed();
        }
    }
}
