package com.emopass.antitheftalarm.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.emopass.antitheftalarm.App;

public class NetworkUtils {
    private static Boolean isOnline(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) (context.getSystemService(Context.CONNECTIVITY_SERVICE));
        NetworkInfo info = null;
        if(connectivityManager != null) {
            info = connectivityManager.getActiveNetworkInfo();
        }
        return info != null && info.isConnected();
    }

    public static Boolean isInternetAvailable() {
        return isOnline(App.getInstace().getApplicationContext());
    }
}
