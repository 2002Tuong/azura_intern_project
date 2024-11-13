package com.bloodpressure.app.ads

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.postDelayed
import com.bloodpressure.app.screen.splash.LoadingAdsDialog

class LoadingAdsDialog: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoadingAdsDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        Handler(Looper.getMainLooper()).postDelayed(delayInMillis = 800L) {
            finish()
        }
    }
}