package com.emopass.antitheftalarm.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.ads.control.admob.Admob;
import com.ads.control.admob.AppOpenManager;
import com.ads.control.ads.VioAdmob;
import com.ads.control.ads.VioAdmobCallback;
import com.ads.control.ads.wrapper.ApAdError;
import com.ads.control.ads.wrapper.ApInterstitialAd;
import com.ads.control.ads.wrapper.ApNativeAd;
import com.emopass.antitheftalarm.BuildConfig;
import com.emopass.antitheftalarm.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.nativead.NativeAdOptions;

public class AdsUtils {
    private static AdsUtils instance = null;
    public String TAG = this.getClass().getName();
    public static AdsUtils getInstance() {
        if(instance == null) {
            instance = new AdsUtils();
        }
        return  instance;
    }
    public static Boolean isAdsEnabled = true;
    public static MutableLiveData<ApNativeAd> nativeLanguage = new MutableLiveData<>();
    public static MutableLiveData<Boolean> nativeLanguageLoadFail = new MutableLiveData<>();

    public static MutableLiveData<ApNativeAd> nativeOnboard1 = new MutableLiveData<>();
    public static MutableLiveData<ApNativeAd> nativeOnboard2 = new MutableLiveData<>();
    public static MutableLiveData<ApNativeAd> nativeOnboard3 = new MutableLiveData<>();
    public static MutableLiveData<Boolean> nativeOnboardLoadFail = new MutableLiveData<>();
    private int countOnBoardNativeLoading = 0;
    private AdLoader adLoaderOnBoarding = null;

    public static MutableLiveData<ApNativeAd> nativeHome = new MutableLiveData<>();
    public static MutableLiveData<Boolean> nativeHomeLoadFail = new MutableLiveData<>();

    public static MutableLiveData<ApNativeAd> nativeFunction = new MutableLiveData<>();
    public static MutableLiveData<Boolean> nativeFunctionLoadFail = new MutableLiveData<>();

    public static  MutableLiveData<ApNativeAd> nativeSetting = new MutableLiveData<>();
    public static MutableLiveData<Boolean> nativeSettingLoadFail = new MutableLiveData<>();

    private ApInterstitialAd interFunction = null;
    private static Long timeShowInterInMillis = 0L;

    public void requestNativeLanguage(Activity activity, Boolean reload) {
        if(stopLoadAds()) {
            nativeLanguageLoadFail.postValue(true);
            return;
        }

        if(nativeLanguage.getValue() != null && !reload) {
            return;
        }

        nativeLanguageLoadFail.postValue(false);
        VioAdmob.getInstance().loadNativeAdResultCallback(
            activity,
            activity.getString(R.string.native_language),
            R.layout.layout_native_ad_language,
            new VioAdmobCallback() {
                @Override
                public void onAdFailedToLoad(@Nullable ApAdError adError) {
                    super.onAdFailedToLoad(adError);
                    nativeLanguageLoadFail.postValue(true);
                    nativeLanguage.postValue(null);
                }

                @Override
                public void onNativeAdLoaded(@NonNull ApNativeAd nativeAd) {
                    super.onNativeAdLoaded(nativeAd);
                    nativeLanguage.postValue(nativeAd);
                    nativeLanguageLoadFail.postValue(false);
                }
            }
        );
    }

    public void requestNativeHome(Activity activity, Boolean reload) {
        if(stopLoadAds()) {
            nativeHomeLoadFail.postValue(true);
            return;
        }

        if(nativeHome.getValue() != null && !reload) {
            return;
        }

        nativeHomeLoadFail.postValue(false);
        VioAdmob.getInstance().loadNativeAdResultCallback(
                activity,
                activity.getString(R.string.native_language),
                R.layout.layout_native_ad_language,
                new VioAdmobCallback() {
                    @Override
                    public void onAdFailedToLoad(@Nullable ApAdError adError) {
                        super.onAdFailedToLoad(adError);
                        nativeHomeLoadFail.postValue(true);
                        nativeHome.postValue(null);
                    }

                    @Override
                    public void onNativeAdLoaded(@NonNull ApNativeAd nativeAd) {
                        super.onNativeAdLoaded(nativeAd);
                        nativeHome.postValue(nativeAd);
                        nativeHomeLoadFail.postValue(false);
                    }
                }
        );
    }

    public void requestNativeFunction(Activity activity, Boolean reload) {
        if(stopLoadAds()) {
            nativeFunctionLoadFail.postValue(true);
            return;
        }

        if(nativeFunction.getValue() != null && !reload) {
            return;
        }

        nativeFunctionLoadFail.postValue(false);
        VioAdmob.getInstance().loadNativeAdResultCallback(
                activity,
                activity.getString(R.string.native_language),
                R.layout.layout_native_ad_language,
                new VioAdmobCallback() {
                    @Override
                    public void onAdFailedToLoad(@Nullable ApAdError adError) {
                        super.onAdFailedToLoad(adError);
                        nativeFunctionLoadFail.postValue(true);
                        nativeFunction.postValue(null);
                    }

                    @Override
                    public void onNativeAdLoaded(@NonNull ApNativeAd nativeAd) {
                        super.onNativeAdLoaded(nativeAd);
                        nativeFunction.postValue(nativeAd);
                        nativeFunctionLoadFail.postValue(false);
                    }
                }
        );
    }

    public void requestNativeSetting(Activity activity, Boolean reload) {
        if(stopLoadAds()) {
            nativeSettingLoadFail.postValue(true);
            return;
        }

        if(nativeSetting.getValue() != null && !reload) {
            return;
        }

        nativeSettingLoadFail.postValue(false);
        VioAdmob.getInstance().loadNativeAdResultCallback(
                activity,
                activity.getString(R.string.native_language),
                R.layout.layout_native_ad_language,
                new VioAdmobCallback() {
                    @Override
                    public void onAdFailedToLoad(@Nullable ApAdError adError) {
                        super.onAdFailedToLoad(adError);
                        nativeSettingLoadFail.postValue(true);
                        nativeSetting.postValue(null);
                    }

                    @Override
                    public void onNativeAdLoaded(@NonNull ApNativeAd nativeAd) {
                        super.onNativeAdLoaded(nativeAd);
                        nativeSetting.postValue(nativeAd);
                        nativeSettingLoadFail.postValue(false);
                    }
                }
        );
    }

    public void requestNativeOnboard(Context context) {
        if (stopLoadAds()) {
            nativeOnboardLoadFail.postValue(true);
            return;
        }

        nativeOnboardLoadFail.postValue(false);
        countOnBoardNativeLoading = 0;
        Log.d(TAG, "loadNativeOnboard: ");
        VideoOptions videoOption = new VideoOptions.Builder()
                .setStartMuted(true)
                .build();
        NativeAdOptions adOptions = new NativeAdOptions.Builder()
                .setVideoOptions(videoOption)
                .build();

        adLoaderOnBoarding = new AdLoader.Builder(context, context.getString(R.string.native_onboarding))
                .forNativeAd(nativeAd -> {
                    if(countOnBoardNativeLoading == 0) {
                        nativeOnboard1.postValue(
                                new ApNativeAd(
                                        R.layout.layout_native_ad_language,
                                        nativeAd
                                )
                        );
                    }else if(countOnBoardNativeLoading == 1) {
                        nativeOnboard2.postValue(
                                new ApNativeAd(
                                        R.layout.layout_native_ad_language,
                                        nativeAd
                                )
                        );
                    } else if(countOnBoardNativeLoading == 2) {
                        nativeOnboard3.postValue(
                                new ApNativeAd(
                                        R.layout.layout_native_ad_language,
                                        nativeAd
                                )
                        );
                    }
                    if(adLoaderOnBoarding != null && adLoaderOnBoarding.isLoading()) {
                        countOnBoardNativeLoading ++;
                    }
                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                        AppOpenManager.getInstance().disableAppResume();
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        super.onAdFailedToLoad(loadAdError);
                        nativeOnboardLoadFail.postValue(true);
                    }
                })
                .withNativeAdOptions(adOptions)
                .build();
        adLoaderOnBoarding.loadAds(Admob.getInstance().getAdRequest(), 3);
    }

    public void initBanner(Activity activity, View view) {
        VioAdmob.getInstance().loadBannerFragment(activity, activity.getString(R.string.banner), view);
    }

    public void loadInterFunction(Context context, Boolean reload) {
        if(stopLoadAds()) {
            return;
        }

        if(isInterSelectSoundReady() && !reload) {
            return;
        }

        VioAdmob.getInstance().getInterstitialAds(context, context.getString(R.string.inter_function),
            new VioAdmobCallback() {
                @Override
                public void onInterstitialLoad(@Nullable ApInterstitialAd interstitialAd) {
                    super.onInterstitialLoad(interstitialAd);
                    interFunction = interstitialAd;
                }
            });
    }

    public void forceShowInterFunction(Context context, OnNextAction onNextAction) {
        if(isInterSelectSoundReady() && isAdsEnabled && isEnoughTime()) {
            VioAdmob.getInstance().forceShowInterstitial(context, interFunction, new VioAdmobCallback() {
                @Override
                public void onNextAction() {
                    super.onNextAction();
                    onNextAction.onNext();
                    timeShowInterInMillis = System.currentTimeMillis();
                }
            });
        }else {
            onNextAction.onNext();
        }
    }

    private Boolean stopLoadAds() {
        return !isAdsEnabled || !NetworkUtils.isInternetAvailable();
    }

    private Boolean isInterSelectSoundReady() {
        return interFunction != null && interFunction.isReady();
    }

    private Boolean isEnoughTime() {
        return System.currentTimeMillis() - timeShowInterInMillis > LIMIT_TIME_IN_MILLIS;
    }
    public interface OnNextAction {
        void onNext();
    }

    private static Long LIMIT_TIME_IN_MILLIS = 30000L;

}
