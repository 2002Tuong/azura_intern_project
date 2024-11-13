package com.emopass.antitheftalarm.service.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.emopass.antitheftalarm.service.AntiTheftService;
import com.emopass.antitheftalarm.service.BackgroundManager;

public class RestarterBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            String type = intent.getStringExtra(BackgroundManager.TYPE_ALARAM);
            if (type.contentEquals(BackgroundManager.START_SERVICE_FROM_AM)) {
                if (BackgroundManager.getInstance(context).canStartService()) {
                    BackgroundManager.getInstance(context).startService(AntiTheftService.class);
                }
                //repeat
                BackgroundManager.getInstance(context).startAlarmManager();
            }
        }
    }
}
