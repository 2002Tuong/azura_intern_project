package com.bloodpressure.app.screen.updateapp

import com.bloodpressure.app.data.local.AppDataStore
import com.bloodpressure.app.data.remote.RemoteConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.bloodpressure.app.BuildConfig

class AppUpdateChecker(
    private val remoteConfig: RemoteConfig,
    private val dataStore: AppDataStore
) {

    /**
     * Version condition: The app's version must be smaller than [RemoteConfig]'s version
     *
     * To make sure after updating the dialog will not show to the user
     *
     * Example: App's version = 1.0.0, remote's version = 1.0.0 and allowed update
     * => After update, app's version still = 1.0.0 => dialog still show => Bad UX
     */
    suspend fun checkUpdate(): UpdateType {
        return withContext(Dispatchers.IO) {
            val appUpdate = remoteConfig.getAppUpdate()
            if (appUpdate.status == 0) {
                return@withContext UpdateType.Updated
            }

            val currentVersion = getVersionValue(BuildConfig.VERSION_NAME)
            val updateToVersion = getVersionValue(appUpdate.maxVersionShow.orEmpty())
            if (currentVersion >= updateToVersion) {
                return@withContext UpdateType.Updated
            }

            val isForceUpdate = appUpdate.status == 2 && !dataStore.isPurchased
            return@withContext UpdateType.RequireUpdate(
                title = appUpdate.title.orEmpty(),
                message = appUpdate.message.orEmpty(),
                url = appUpdate.updateUrl.orEmpty(),
                forceUpdate = isForceUpdate
            )
        }
    }

    private fun getVersionValue(versionName: String): Int {
        return try {
            val numArray = versionName.split(".")
            return numArray[0].toInt() * 10000 + numArray[1].toInt() * 100 + numArray[2].toInt()
        } catch (e: Exception) {
            0
        }
    }

    sealed interface UpdateType {
        object Updated : UpdateType

        data class RequireUpdate(
            val title: String,
            val message: String,
            val url: String,
            val forceUpdate: Boolean
        ) : UpdateType
    }
}
