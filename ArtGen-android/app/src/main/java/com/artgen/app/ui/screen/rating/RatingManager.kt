package com.artgen.app.ui.screen.rating

import androidx.appcompat.app.AppCompatActivity
import com.artgen.app.BuildConfig
import com.artgen.app.data.local.AppDataStore
import com.artgen.app.data.remote.RemoteConfig
import com.artgen.app.log.Logger
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.model.ReviewErrorCode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.component.KoinComponent

class RatingManager(
    private val activity: AppCompatActivity,
    private val dataStore: AppDataStore,
    remoteConfig: RemoteConfig
) : KoinComponent {

    private val manager = ReviewManagerFactory.create(activity)
    private var reviewInfo: ReviewInfo? = null
    private val ratingConfig = remoteConfig.getRatingAppConfig()
    private var _waitingToShowRate: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val waitingToShowRate get() = _waitingToShowRate

    fun canShowRate(isExitApp: Boolean): Boolean {
        return if (BuildConfig.DEBUG) {
            true
        } else {
            if (isExitApp) {
                !dataStore.isRated && dataStore.exitAppCount in ratingConfig.exitAppRatingSession
            } else {
                !dataStore.isRated && dataStore.savePhotoCount in ratingConfig.savePictureRatingSession
            }
        }

    }

    fun updateWaitingRateStatus(isWaiting: Boolean) {
        _waitingToShowRate.update { isWaiting }
    }

    suspend fun updateShowRatedStatus() {
        dataStore.setRatedStatus(true)
    }

    fun requestReviewFlow() {
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // We got the ReviewInfo object
                reviewInfo = task.result

            } else {
                // There was some problem, log or handle the error code.
                @ReviewErrorCode val reviewErrorCode = (task.exception as? ReviewException)?.errorCode
                Logger.d("qvk: $reviewErrorCode")
            }
        }
    }

    fun requestReviewStore(onComplete: () -> Unit) {
        if (reviewInfo == null) {
            onComplete.invoke()
            return
        }
        val flow = manager.launchReviewFlow(activity, reviewInfo!!)

        flow.addOnCompleteListener {
            onComplete.invoke()
        }
    }
}