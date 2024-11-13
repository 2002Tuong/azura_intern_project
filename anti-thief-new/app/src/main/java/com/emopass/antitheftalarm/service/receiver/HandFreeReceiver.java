package com.emopass.antitheftalarm.service.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.emopass.antitheftalarm.service.AntiTheftService;
import com.emopass.antitheftalarm.utils.Config;

import static android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY;
import static android.content.Intent.ACTION_HEADSET_PLUG;

/**
 * Receives headphone connect and disconnect intents so that music may be paused when headphones
 * are disconnected
 */
public class HandFreeReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    if (isInitialStickyBroadcast()) {
      return;
    }

    boolean plugged, unplugged;

    if (ACTION_HEADSET_PLUG.equals(intent.getAction())) {
      unplugged = intent.getIntExtra("state", -1) == 0;
      plugged = intent.getIntExtra("state", -1) == 1;
    } else {
      unplugged = plugged = false;
    }

    boolean becomingNoisy = ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction());

    if (unplugged || becomingNoisy) {
      Intent mIntent = new Intent(context, AntiTheftService.class);
      mIntent.setAction(Config.ActionIntent.ACTION_START_ALARM_HANDSFREE);
      context.startService(mIntent);
    } else if (plugged) {
      // Headset plugged
    }
  }

  public final void onCreate(Context context) {
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(ACTION_HEADSET_PLUG);
    intentFilter.addAction(ACTION_AUDIO_BECOMING_NOISY);
    context.registerReceiver(this, intentFilter);
  }

  public final void onDestroy(Context context) {
    if (context != null) {
      context.unregisterReceiver(this);
    }
  }
}