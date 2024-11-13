package com.emopass.antitheftalarm.service;

import android.app.ActivityManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;

import com.emopass.antitheftalarm.utils.PreferencesHelper;
import com.emopass.antitheftalarm.utils.ViewUtils;
import com.emopass.antitheftalarm.widget.LockViewWindowManager;

import java.util.ArrayList;
import java.util.List;

public class LockManager {
    private Context context;
    private BroadcastReceiver installUninstallReceiver;
    private String lastPackage = "";
    private UsageStatsManager usageStatsManager;
    private ActivityManager activityManager;
    private WindowManager windowManager;
    private LockViewWindowManager lockViewWindowManager;

    public LockManager(Context context) {
        this.context = context;
        this.usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        initWindowManager();
        initReceiver();
        registerInstallUninstallReceiver();
    }

    private void initWindowManager() {
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        lockViewWindowManager = new LockViewWindowManager(context);
        lockViewWindowManager.setPasswordConfirmListener(new LockViewWindowManager.PasswordConfirmListener() {
            @Override
            public void onSuccess() {
                showhideLockView(false, "");
            }

            @Override
            public void onFails(String passInput) {

            }
        });
        windowManager.addView(lockViewWindowManager, ViewUtils.setupLayoutParams());
    }

    private void showhideLockView(boolean isShow, String packageName) {
        WindowManager.LayoutParams paramsViewContent = ViewUtils.setupLayoutParams();
        lockViewWindowManager.showhideViewPassword(isShow, packageName);
        windowManager.updateViewLayout(lockViewWindowManager, paramsViewContent);
    }

    private void initReceiver() {
        installUninstallReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // TODO nothing
            }
        };
    }

    public void lockApp() {
        if (PreferencesHelper.getBoolean(PreferencesHelper.ENABLE_KIDZONE, false))
            onAppForeground(getLastPackage());
    }

    private String getLastPackage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return getPackageHigherLollipop();
        } else {
            return getPackageLowerLollipop();
        }
    }

    private String getPackageHigherLollipop() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            List<ActivityManager.RunningTaskInfo> appTasks = activityManager.getRunningTasks(1);
            if (null != appTasks && !appTasks.isEmpty()) {
                return appTasks.get(0).topActivity.getPackageName();
            }
        } else {
            long endTime = System.currentTimeMillis();
            long beginTime = endTime - 10000;
            String result = "";
            UsageEvents.Event event = new UsageEvents.Event();
            UsageEvents usageEvents = usageStatsManager.queryEvents(beginTime, endTime);
            while (usageEvents.hasNextEvent()) {
                usageEvents.getNextEvent(event);
                if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    result = event.getPackageName();
                }
            }
            if (!TextUtils.isEmpty(result)) {
                return result;
            }
        }
        return "";
    }

    private String getPackageLowerLollipop() {
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName componentName = mActivityManager.getRunningTasks(1).get(0).topActivity;
        if (componentName != null && componentName.getPackageName() != null) {
            return componentName.getPackageName();
        }
        return "";
    }

  /*  private void onAppForeground(String foregroundAppPackage) {
        Log.d("TAG", "onAppForeground: " + foregroundAppPackage);
        if (context == null || TextUtils.isEmpty(foregroundAppPackage)) return;
        List<String> listAppLocked = PreferencesHelper.getListAppKidZone();
        List<String> listHomeLaucher = getHomesLauncher();
        // add when installer app
        listAppLocked.add("com.android.packageinstaller");
        if (listAppLocked.contains(foregroundAppPackage) &&
                !listHomeLaucher.contains(foregroundAppPackage) &&
                !lastPackage.equals(foregroundAppPackage)) {
            Intent intent = new Intent(context, PasswordActivity.class);
            intent.setAction(Config.ActionIntent.ACTION_CHECK_PASSWORD_FROM_SERVICE);
            intent.putExtra(Config.KeyBundle.KEY_PACKAGE_NAME, foregroundAppPackage);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivityAllStage(context, intent);
            lastPackage = foregroundAppPackage;
        }
        if (TextUtils.isEmpty(foregroundAppPackage) || listHomeLaucher.contains(foregroundAppPackage))
            lastPackage = foregroundAppPackage;
    }*/

    private void onAppForeground(String foregroundAppPackage) {
        Log.d("TAG", "onAppForeground: " + foregroundAppPackage);
        if (context == null || TextUtils.isEmpty(foregroundAppPackage)) return;
        List<String> listAppLocked = PreferencesHelper.getListAppKidZone();
        // add when installer app
        listAppLocked.add("com.android.packageinstaller");
        if (TextUtils.isEmpty(foregroundAppPackage)) {
            return;
        }
        if (listAppLocked.contains(foregroundAppPackage)) {
            if (!lastPackage.equals(foregroundAppPackage))
                showhideLockView(true, foregroundAppPackage);
        } else {
            showhideLockView(false, "");
        }
        lastPackage = foregroundAppPackage;
    }

    private List<String> getHomesLauncher() {
        List<String> names = new ArrayList<>();
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo ri : resolveInfo) {
            names.add(ri.activityInfo.packageName);
        }
        return names;
    }

    public void registerInstallUninstallReceiver() {
        if (context != null) {
            IntentFilter installUninstallFilter = new IntentFilter();
            installUninstallFilter.addAction(Intent.ACTION_INSTALL_PACKAGE);
            installUninstallFilter.addDataScheme("package");
            context.registerReceiver(installUninstallReceiver, installUninstallFilter);
        }
    }

    public void unregisterInstallUninstallReceiver() {
        if (context != null) {
            try {
                context.unregisterReceiver(installUninstallReceiver);
            } catch (Exception e) {

            }
        }
    }
}

