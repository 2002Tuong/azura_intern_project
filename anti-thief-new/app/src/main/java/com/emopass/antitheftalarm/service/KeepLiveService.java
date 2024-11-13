package com.emopass.antitheftalarm.service;

import android.app.IntentService;
import android.content.Intent;

import androidx.annotation.Nullable;

public class KeepLiveService extends IntentService {

    public KeepLiveService() {
        super("KeepLiveService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (BackgroundManager.getInstance(this).canStartService()) {
            BackgroundManager.getInstance(this).startService(AntiTheftService.class);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        startService(new Intent(this, KeepLiveService.class));
    }
}
