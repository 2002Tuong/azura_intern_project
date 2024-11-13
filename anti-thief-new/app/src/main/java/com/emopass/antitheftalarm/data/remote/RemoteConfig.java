package com.emopass.antitheftalarm.data.remote;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.emopass.antitheftalarm.BuildConfig;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

public class RemoteConfig {
    private static RemoteConfig instance = null;
    private static String KEY_VERSION_ENABLE_ADS = "version_enable_ads";
    private String localVersion = BuildConfig.VERSION_NAME;
    private String remoteVersion = FirebaseRemoteConfig.getInstance().getString(KEY_VERSION_ENABLE_ADS);

    public MutableLiveData<Boolean> isFetchComplete = new MutableLiveData<>();
    public static RemoteConfig getInstance() {
        if(instance == null) {
            instance = new RemoteConfig();
            return instance;
        }
        return instance;
    }

    public String getVersionEnableAds() {
        remoteVersion = FirebaseRemoteConfig.getInstance().getString(KEY_VERSION_ENABLE_ADS);
        return remoteVersion;
    }

    public void fetch(Activity activity) {
        FirebaseRemoteConfig.getInstance().fetchAndActivate().addOnCompleteListener(activity, new OnCompleteListener<Boolean>() {
            @Override
            public void onComplete(@NonNull Task<Boolean> task) {
                isFetchComplete.postValue(true);
                getVersionEnableAds();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                isFetchComplete.postValue(true);
            }
        }).addOnCanceledListener(new OnCanceledListener() {
            @Override
            public void onCanceled() {
                isFetchComplete.postValue(true);
            }
        });
    }

    public Boolean isAdsEnable() {
        return appVersionToInt(localVersion) <= appVersionToInt(remoteVersion);
    }

    static public Integer appVersionToInt(String appVersion) {
        String[] listNumber = appVersion.split("\\.");
        Integer res = 0;
        for (int i = 0; i< listNumber.length; i++) {
            if(i == 0) {
                res += toIntSafety(listNumber[i]) * 100;
            }else if(i == 1) {
                res += toIntSafety(listNumber[i] ) * 10;
            } else if(i == 2) {
                res += toIntSafety(listNumber[i]);
            }
        }

        return res;
    }

    static public Integer toIntSafety(String number) {
        if(number.isEmpty()) {
            return 0;
        }else {
            return Integer.parseInt(number);
        }
    }
}
