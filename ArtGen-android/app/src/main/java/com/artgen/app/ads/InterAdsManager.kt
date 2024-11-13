package com.artgen.app.ads

import android.content.Context
import com.artgen.app.data.local.AppDataStore
import com.artgen.app.data.remote.RemoteConfig

class InterAdsManager(
    private val context: Context,
    private val dataStore: AppDataStore,
    private val remoteConfig: RemoteConfig
) {}
