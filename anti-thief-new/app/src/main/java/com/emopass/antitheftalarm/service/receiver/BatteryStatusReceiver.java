package com.emopass.antitheftalarm.service.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;

import com.emopass.antitheftalarm.service.AntiTheftService;
import com.emopass.antitheftalarm.utils.Config;
import com.emopass.antitheftalarm.utils.PreferencesHelper;

public class BatteryStatusReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        checkBatteryLevel(context, intent);
        if (action.equals(Intent.ACTION_POWER_CONNECTED)) {
            Log.d("BatteryStatusReceiver", "onReceive: ACTION_POWER_CONNECTED");
            if (PreferencesHelper.getBoolean(PreferencesHelper.NOTI_CHARGER_CONNECTED, false)) {
                Intent mIntent = new Intent(context, AntiTheftService.class);
                mIntent.setAction(Config.ActionIntent.ACTION_START_ALARM_POWER_CONNECTED);
                context.startService(mIntent);
            }
        } else if (action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
            Log.d("BatteryStatusReceiver", "onReceive: ACTION_POWER_DISCONNECTED");
            if (PreferencesHelper.getInt(PreferencesHelper.DETECTION_TYPE, Config.DETECTION_NONE) == Config.DETECTION_CHARGER) {
                Intent mIntent = new Intent(context, AntiTheftService.class);
                mIntent.setAction(Config.ActionIntent.ACTION_START_ALARM_CHARGER);
                context.startService(mIntent);
            }
        }
    }

    private void checkBatteryLevel(Context context, Intent batteryChangeIntent) {
        int currLevel = batteryChangeIntent.getIntExtra(
                BatteryManager.EXTRA_LEVEL, -1);
        if (currLevel == 100) {
            Intent mIntent = new Intent(context, AntiTheftService.class);
            mIntent.setAction(Config.ActionIntent.ACTION_START_ALARM_FULL_BATTERY);
            context.startService(mIntent);
        }
    }

    public final void onCreate(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        context.registerReceiver(this, intentFilter);
    }

    public final void onDestroy(Context context) {
        if (context != null) {
            context.unregisterReceiver(this);
        }
    }
}
