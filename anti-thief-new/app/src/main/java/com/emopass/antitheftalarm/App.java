package com.emopass.antitheftalarm;

import android.app.Activity;
import android.app.Application;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;

import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.ads.control.admob.Admob;
import com.ads.control.admob.AppOpenManager;
import com.ads.control.ads.VioAdmob;
import com.ads.control.application.VioAdmobMultiDexApplication;
import com.ads.control.config.AdjustConfig;
import com.ads.control.config.VioAdmobConfig;
import com.emopass.antitheftalarm.service.AntiTheftService;
import com.emopass.antitheftalarm.service.BackgroundManager;
import com.emopass.antitheftalarm.service.jobScheduler.JobSchedulerService;
import com.emopass.antitheftalarm.ui.BaseActivity;
import com.emopass.antitheftalarm.ui.splash.SplashActivity;
import com.emopass.antitheftalarm.utils.PreferencesHelper;
import com.google.android.gms.ads.AdActivity;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.FirebaseApp;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.ArrayList;
import java.util.List;

public class App extends VioAdmobMultiDexApplication implements Application.ActivityLifecycleCallbacks {

    private static App instance;
    private boolean forceLockScreen;
    private static List<BaseActivity> activityList;

    // To detect app lifecycle state
    private int activityReferences = 0;
    private boolean isActivityChangingConfigurations = false;

    public boolean isForceLockScreen() {
        return forceLockScreen;
    }

    public void setForceLockScreen(boolean forceLockScreen) {
        this.forceLockScreen = forceLockScreen;
    }

    public static App getInstace() {
        if (instance == null)
            instance = new App();
        return instance;
    }

    private void setInstance(App instance) {
        App.instance = instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (instance == null)
            setInstance(App.this);
        PreferencesHelper.init(this);
        if (BackgroundManager.getInstance(this).canStartService()) {
            BackgroundManager.getInstance(this).startService(AntiTheftService.class);
        }
//        startService(new Intent(this, KeepLiveService.class));
        startJobScheduler();
        activityList = new ArrayList<>();

        // To detect the app lifecycle
        registerActivityLifecycleCallbacks(this);

        MobileAds.initialize(this);
        initAds();
        initRemoteConfig();


    }

    public void doForCreate(BaseActivity activity) {
        activityList.add(activity);
    }

    public void doForFinish(BaseActivity activity) {
        activityList.remove(activity);
    }

    public BaseActivity getTopActivity() {
        if (activityList.isEmpty())
            return null;
        return activityList.get(activityList.size() - 1);
    }

    private void startJobScheduler() {
        if (Build.VERSION.SDK_INT >= 21) {
            JobInfo.Builder builder = new JobInfo.Builder(1, new ComponentName(this, JobSchedulerService.class));
            builder.setPeriodic(1000);
            builder.setPersisted(true);
            ((JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE)).schedule(builder.build());
        }

    }

    private void initRemoteConfig() {
        FirebaseRemoteConfigSettings setting = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(10L)
                .build();
        FirebaseRemoteConfig.getInstance().setConfigSettingsAsync(setting);
        FirebaseRemoteConfig.getInstance().setDefaultsAsync(R.xml.remote_config_defaults);
    }

    private void initAds() {
        String systemEnvironment;
        if(BuildConfig.is_debug) {
            systemEnvironment = VioAdmobConfig.ENVIRONMENT_DEVELOP;
        } else {
            systemEnvironment = VioAdmobConfig.ENVIRONMENT_PRODUCTION;
        }

        vioAdmobConfig = new VioAdmobConfig(this, VioAdmobConfig.PROVIDER_ADMOB, systemEnvironment);
        vioAdmobConfig.setMediationProvider(VioAdmobConfig.PROVIDER_ADMOB);
        AdjustConfig adjustConfig = new AdjustConfig("adjustToken");
        vioAdmobConfig.setAdjustConfig(adjustConfig);
        vioAdmobConfig.setIdAdResume(getString(R.string.app_open_resume));
        AppOpenManager.getInstance().setSplashAdId(getString(R.string.app_open_splash));
        listTestDevice.add("EC25F576DA9B6CE74778B268CB87E431");
        vioAdmobConfig.setListDeviceTest(listTestDevice);
        Admob.getInstance().setFan(true);
        VioAdmob.getInstance().init(this, vioAdmobConfig, false);
        AppOpenManager.getInstance().disableAppResumeWithActivity(AdActivity.class);
        AppOpenManager.getInstance().disableAppResumeWithActivity(SplashActivity.class);
        //Admob.getInstance().setOpenActivityAfterShowInterAds(true);

    }
    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        if (++activityReferences == 1 && !isActivityChangingConfigurations) {
            // App enters foreground
            // Not show ads when coming back from the permission settings and the activity is not Splash Activity
            if (!PreferencesHelper.isCheckingPermission() && !(activity instanceof SplashActivity)) {
                //AppOpenManager.getInstance().showAdOpenApp(activity, () -> {});
                Log.d("Application", "Foreground");
            }
            PreferencesHelper.setCheckingPermission(false);
        }
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        isActivityChangingConfigurations = activity.isChangingConfigurations();
        if (--activityReferences == 0 && !isActivityChangingConfigurations) {
            // App enters background
            //Log.d("Application", "Background");
        }
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }
}
