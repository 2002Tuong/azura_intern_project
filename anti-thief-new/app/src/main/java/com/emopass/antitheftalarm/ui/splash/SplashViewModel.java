package com.emopass.antitheftalarm.ui.splash;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.ads.control.admob.AppOpenManager;
import com.ads.control.funtion.AdCallback;
import com.emopass.antitheftalarm.BuildConfig;
import com.emopass.antitheftalarm.data.remote.RemoteConfig;
import com.emopass.antitheftalarm.utils.AdsUtils;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.LoadAdError;

public class SplashViewModel extends ViewModel {

    private final MutableLiveData<Boolean> _navigateToMainScreenState = new MutableLiveData<>(false);
    public LiveData<Boolean> navigateToMainScreenState = _navigateToMainScreenState;

    void loadAppOpen(Context context) {
        AppOpenManager manager = AppOpenManager.getInstance();
        Boolean adsEnable = RemoteConfig.getInstance().isAdsEnable();
        String version = RemoteConfig.getInstance().getVersionEnableAds();
        AdsUtils.isAdsEnabled = adsEnable;
        Log.d("AdsRemote", version);
        Log.d("AdsRemote", adsEnable.toString());
        if(adsEnable) {
            manager.loadOpenAppAdSplash(context,
                    TIME_DELAY_IN_MILLIS,
                    TIMEOUT_IN_MILLIS,
                    true,
                    new AdCallback() {
                        @Override
                        public void onAdFailedToLoad(@Nullable LoadAdError i) {
                            super.onAdFailedToLoad(i);
                            _navigateToMainScreenState.postValue(true);
                            Log.d("Splash", "call fail");
                            showToast("call fail", context);
                        }

                        @Override
                        public void onNextAction() {
                            super.onNextAction();
                            _navigateToMainScreenState.postValue(true);
                            manager.showAdIfAvailable(true);
                            Log.d("Splash", "call onnext");
                            showToast("call onnext", context);
                        }

                        @Override
                        public void onAdLoaded() {
                            super.onAdLoaded();
                            _navigateToMainScreenState.postValue(true);
                            Log.d("Splash", "call AdLoaded");
                            showToast("call AdLoaded", context);
                        }

                        @Override
                        public void onAdClosed() {
                            super.onAdClosed();
                            _navigateToMainScreenState.postValue(true);
                            Log.d("Splash", "call Closed");
                            showToast("call Closed", context);
                        }

                        @Override
                        public void onAdSplashReady() {
                            super.onAdSplashReady();
                            _navigateToMainScreenState.postValue(true);
                            Log.d("Splash", "call Ready");
                            showToast("call Ready", context);
                        }

                        @Override
                        public void onInterstitialShow() {
                            super.onInterstitialShow();
                            Log.d("Splash", "call show");
                        }
                    }
            );
        }else {
            _navigateToMainScreenState.postValue(true);
        }


    }

    public void checkShowOpenAdSplashLoadFail(Activity activity) {
        AppOpenManager.getInstance().onCheckShowAppOpenSplashWhenFail(
                (AppCompatActivity) activity,
                new AdCallback() {
                    @Override
                    public void onNextAction() {
                        super.onNextAction();
                        _navigateToMainScreenState.postValue(true);
                        Log.d("Splash", "call open next");
                        showToast("call open next", activity);
                    }

                    @Override
                    public void onAdFailedToLoad(@Nullable LoadAdError i) {
                        super.onAdFailedToLoad(i);
                        _navigateToMainScreenState.postValue(true);
                        Log.d("Splash", "call open AdFailed to load");
                        showToast("call open AdFailed to load", activity);
                    }

                    @Override
                    public void onAdFailedToShow(@Nullable AdError adError) {
                        super.onAdFailedToShow(adError);
                        _navigateToMainScreenState.postValue(true);
                        Log.d("Splash", "call open AdFailed to show");
                        showToast("call open AdFailed to show", activity);
                    }
                },
                3
        );
    }

    private void showToast(String message, Context context) {
        if(BuildConfig.is_debug) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

    public static Long TIMEOUT_IN_MILLIS = 10000L;
    public static Long TIME_DELAY_IN_MILLIS = 5000L;
}
