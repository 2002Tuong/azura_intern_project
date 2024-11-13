package com.emopass.antitheftalarm.service.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.emopass.antitheftalarm.service.AntiTheftService;
import com.emopass.antitheftalarm.utils.Config;
import com.emopass.antitheftalarm.utils.PreferencesHelper;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;

public class WiFiChangeReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context context, Intent intent) {
    if (!CONNECTIVITY_ACTION.equals(intent.getAction())) {

    }
    ConnectivityManager connectivityManager =
        (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
    NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();

    if (activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
      //Toast.makeText(context, "Wifi Connected!", Toast.LENGTH_SHORT).show();
    } else {
      //Toast.makeText(context, "Wifi Not Connected!", Toast.LENGTH_SHORT).show();
      if (PreferencesHelper.getInt(PreferencesHelper.DETECTION_TYPE, Config.DETECTION_NONE)
          == Config.DETECTION_WIFI) {
        Intent mIntent = new Intent(context, AntiTheftService.class);
        mIntent.setAction(Config.ActionIntent.ACTION_START_ALARM_WIFI_DISCONNECTED);
        context.startService(mIntent);
      }
    }
  }

  public final void onCreate(Context context) {
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(CONNECTIVITY_ACTION);
    context.registerReceiver(this, intentFilter);
  }

  public final void onDestroy(Context context) {
    if (context != null) {
      context.unregisterReceiver(this);
    }
  }
}
