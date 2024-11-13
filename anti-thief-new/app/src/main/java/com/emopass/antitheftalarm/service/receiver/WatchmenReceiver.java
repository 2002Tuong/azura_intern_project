package com.emopass.antitheftalarm.service.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.emopass.antitheftalarm.service.AntiTheftService;
import com.emopass.antitheftalarm.service.BackgroundManager;

public class WatchmenReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (BackgroundManager.getInstance(context).canStartService()) {
            BackgroundManager.getInstance(context).startService(AntiTheftService.class);
        }
    }
}
