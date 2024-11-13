package com.emopass.antitheftalarm.service;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.content.ContextCompat;

import com.emopass.antitheftalarm.service.receiver.RestarterBroadcastReceiver;
import com.emopass.antitheftalarm.utils.Config;
import com.emopass.antitheftalarm.utils.PreferencesHelper;

public class BackgroundManager {

    private static final int period = 5 * 1000;//5 minutes;
    public static final String TYPE_ALARAM = "type alarm manager";
    public static final String START_SERVICE_FROM_AM = "start service FromAM";

    private final Context context;
    private static BackgroundManager instance;

    private BackgroundManager(Context context) {
        this.context = context;
    }

    public static BackgroundManager getInstance(Context context) {
        if (instance == null) {
            instance = new BackgroundManager(context);
        }
        return instance;
    }

    public boolean canStartService() {
        if (PreferencesHelper.getInt(PreferencesHelper.DETECTION_TYPE, Config.DETECTION_NONE) != Config.DETECTION_NONE
                || PreferencesHelper.getBoolean(PreferencesHelper.ENABLE_KIDZONE, false)) {
//            if (!isServiceRunning(AntiTheftService.class)) {
//                return true;
//            }
            return true;
        }
        return false;
    }

    public boolean isServiceRunning(Class<?> serviceClass) {
        checkContext();
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void startService(Class<?> serviceClass) {
        checkContext();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(context, new Intent(context, serviceClass));
        } else {
            context.startService(new Intent(context, serviceClass));
        }
    }

    public void stopAlarmManager() {
        checkContext();
        Intent intent = new Intent(context, RestarterBroadcastReceiver.class);
        intent.putExtra(TYPE_ALARAM, START_SERVICE_FROM_AM);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 95374, intent, PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    public void startAlarmManager() {
        checkContext();
        Intent intent = new Intent(context, RestarterBroadcastReceiver.class);
        intent.putExtra(TYPE_ALARAM, START_SERVICE_FROM_AM);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 95374, intent, PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + period, pendingIntent);

    }

    private void checkContext() {
        if (context == null)
            throw new RuntimeException("Context can not be null: Initialize context first");
    }
}
