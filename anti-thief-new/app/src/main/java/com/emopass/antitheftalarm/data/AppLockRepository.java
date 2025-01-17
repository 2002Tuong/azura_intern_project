package com.emopass.antitheftalarm.data;

import android.app.ActivityManager;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;

import com.emopass.antitheftalarm.model.TaskInfo;
import com.emopass.antitheftalarm.ui.password.PasswordActivity;
import com.emopass.antitheftalarm.utils.SystemUtil;


import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class AppLockRepository {

    public static Flowable<String> getForegroundObservableApp(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return getForegroundObservableHigherLollipop(context);
        } else {
            return getForegroundObservableLowerLollipop(context);
        }
    }


    @NotNull
    private static Flowable<String> getForegroundObservableHigherLollipop(Context context) {
        return Flowable
                .interval(100, TimeUnit.MILLISECONDS)
                .filter(aLong -> SystemUtil.isUsageAccessAllowed(context))
                .map(aLong -> {
                    UsageEvents.Event usageEvent = null;

                    UsageStatsManager mUsageStatsManager = (UsageStatsManager) context.getSystemService(Service.USAGE_STATS_SERVICE);
                    Long time = System.currentTimeMillis();

                    UsageEvents usageEvents = mUsageStatsManager.queryEvents(time - 10000, time);
                    UsageEvents.Event event = new UsageEvents.Event();
                    while (usageEvents.hasNextEvent()) {
                        usageEvents.getNextEvent(event);
                        if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                            usageEvent = event;
                        }
                    }
                    return usageEvent;
                })
                .filter(event -> event != null)
                .filter(event -> event.getClassName() != null)
                .filter(event -> !event.getClassName().contains(PasswordActivity.class.getSimpleName()))
                .map(event -> {
                    if (event.getPackageName() != null) {
                        return event.getPackageName();
                    }
                    return "";
                })
                .distinctUntilChanged();
    }

    private static Flowable<String> getForegroundObservableLowerLollipop(Context context) {
        return Flowable.interval(100, TimeUnit.MILLISECONDS)
                .map(aLong -> {
                    ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                    ComponentName componentName = mActivityManager.getRunningTasks(1).get(0).topActivity;
                    return componentName;
                })
                .filter(event -> !event.getClassName().contains(PasswordActivity.class.getSimpleName()))
                .map(ComponentName::getPackageName)
                .distinctUntilChanged();
    }

    private static Flowable<String> getBackgroundObservable(Context context) {
        return Flowable.interval(100, TimeUnit.MILLISECONDS)
                .map(aLong -> {
                    String backgroundApp = null;

                    UsageStatsManager mUsageStatsManager = (UsageStatsManager) context.getSystemService(Service.USAGE_STATS_SERVICE);
                    Long time = System.currentTimeMillis();

                    UsageEvents usageEvents = mUsageStatsManager.queryEvents(time - 1000 * 3600, time);
                    UsageEvents.Event event = new UsageEvents.Event();
                    while (usageEvents.hasNextEvent()) {
                        usageEvents.getNextEvent(event);
                        if (event.getEventType() == UsageEvents.Event.MOVE_TO_BACKGROUND) {
                            backgroundApp = event.getPackageName();
                        }
                    }
                    return backgroundApp;
                })
                .distinctUntilChanged();
    }

    @NotNull
    public static Disposable fetchInstalledAppList(Context context, RxCallback<List<TaskInfo>> callback) {
        return Single.create(emitter -> {
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> resolveInfoList = context.getPackageManager().queryIntentActivities(mainIntent, 0);

            List<TaskInfo> taskInfoList = new ArrayList<>();
            List<String> listAppLaucher = getHomesLauncher(context);
            for (ResolveInfo resolveInfo : resolveInfoList) {
                if (!resolveInfo.activityInfo.packageName.equals(context.getPackageName())
                        && !listAppLaucher.contains(resolveInfo.activityInfo.packageName)) {
                    ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(resolveInfo.activityInfo.packageName, 0);
                    TaskInfo taskInfo = new TaskInfo(context, appInfo);
                    if (!taskInfoList.contains(taskInfo))
                        taskInfoList.add(taskInfo);
                }
            }
            emitter.onSuccess(taskInfoList);
        })
                .delay(1000L, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object -> callback.onSuccess((List<TaskInfo>) object));
    }

    private static List<String> getHomesLauncher(Context context) {
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
}
