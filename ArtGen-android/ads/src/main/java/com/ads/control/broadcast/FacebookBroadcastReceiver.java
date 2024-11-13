package com.ads.control.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.facebook.appevents.AppEventsLogger;
import com.facebook.appevents.FlushResult;

public class FacebookBroadcastReceiver extends BroadcastReceiver {
    private FacebookBroadcastReceiverCallback callback;

    public void setFacebookBroadcastReceiver(FacebookBroadcastReceiverCallback callback){
        this.callback = callback;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(AppEventsLogger.ACTION_APP_EVENTS_FLUSHED)) {
            FlushResult result = (FlushResult) intent.getSerializableExtra(AppEventsLogger.APP_EVENTS_EXTRA_FLUSH_RESULT);
            callback.callback(result);
        }
    }

    public interface FacebookBroadcastReceiverCallback {
        void callback(FlushResult result);
    }
}
