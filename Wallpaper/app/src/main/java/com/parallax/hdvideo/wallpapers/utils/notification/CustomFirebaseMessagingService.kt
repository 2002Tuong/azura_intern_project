package com.parallax.hdvideo.wallpapers.utils.notification

import android.content.Intent
import androidx.core.os.bundleOf
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.parallax.hdvideo.wallpapers.extension.toJson
import com.parallax.hdvideo.wallpapers.services.log.EventNotification
import com.parallax.hdvideo.wallpapers.services.log.TrackingSupport
import com.parallax.hdvideo.wallpapers.services.worker.FCMWorker
import com.parallax.hdvideo.wallpapers.utils.Logger
import java.util.Calendar

class CustomFirebaseMessagingService : FirebaseMessagingService() {

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Logger.d(TAG, "onMessageReceived: ${remoteMessage.data.isNotEmpty()}")
        val remoteNotification = remoteMessage.notification
        val remoteData = remoteMessage.data
        val remoteTitle = remoteNotification?.title ?: remoteData[NotificationUtils.TITLE]
        val remoteBody = remoteNotification?.body ?: remoteData[NotificationUtils.BODY]
        val remoteImage = remoteNotification?.imageUrl?.toString() ?: remoteData[NotificationUtils.IMAGE]
        if (remoteTitle != null) {
            try {
                val checkSuccess = NotificationUtils.push(
                    bundleOf(NotificationUtils.TITLE to remoteTitle,
                        NotificationUtils.MESSAGE to remoteBody,
                        NotificationUtils.DATA to remoteData.toJson,
                        NotificationUtils.ACTION to FCMWorker.TAGS)
                    , image = remoteImage
                )
                if (!checkSuccess) {
                    val cal = Calendar.getInstance()
                    val time = cal.timeInMillis
                    val hour = cal.get(Calendar.HOUR_OF_DAY)
                    if (hour in 22..23) {
                        cal.add(Calendar.DAY_OF_MONTH, 1)
                    }
                    cal.set(Calendar.HOUR_OF_DAY, 8)
                    val delay = cal.timeInMillis - time
                    remoteData[NotificationUtils.TITLE] = remoteTitle
                    if (remoteBody != null) remoteData[NotificationUtils.MESSAGE] = remoteBody
                    if (remoteImage != null) remoteData[NotificationUtils.IMAGE] = remoteImage
                    FCMWorker.schedule(delay, remoteData.toJson)
                }
            } catch (e: Exception) {

            }
        } else {

        }
    }

    override fun handleIntent(intent: Intent) {
        super.handleIntent(intent)
        val bundle = intent.extras ?: return
        Logger.d(TAG, "handleIntent: ${bundle.keySet()}")
        val idObject = bundle.getString(NotificationUtils.OBJECT_ID)
        val idWall = bundle.getString(NotificationUtils.Wall_ID)
        val idCate = bundle.getString(NotificationUtils.CATE_ID)
        TrackingSupport.recordEventOnlyFirebase(EventNotification.NotificationReceiveAll)
        when {
            !idObject.isNullOrEmpty() && !idObject.isNullOrBlank() -> {
                val dataList = idObject.split(":")
                if (dataList.size > 1) {
                    when {
                        dataList[0].startsWith("keysearch") -> {
                            Logger.d(TAG, "keysearch", dataList[1])
                            TrackingSupport.recordEventOnlyFirebase(EventNotification.NotificationReceiveFcm.nameEvent + NotificationUtils.FCM_KEY_SEARCH)
                        }
                        dataList[0].startsWith("hashtag") -> {
                            Logger.d(TAG, "hashtag", dataList[1])
                            TrackingSupport.recordEventOnlyFirebase(EventNotification.NotificationReceiveFcm.nameEvent + NotificationUtils.FCM_HASHTAG)
                        }
                        dataList[0].startsWith("moreapp") -> {
                            Logger.d(TAG, "moreapp", dataList[1])
                            TrackingSupport.recordEventOnlyFirebase(EventNotification.NotificationReceiveFcm.nameEvent + NotificationUtils.FCM_MORE_APP)
                        }
                    }
                }
            }
            idWall != null -> {
                // navigate to detail screen
                Logger.d(TAG, "navigate to detail")
                TrackingSupport.recordEventOnlyFirebase(EventNotification.NotificationReceiveFcm.nameEvent + NotificationUtils.FCM_DETAIL)
            }
            idCate != null -> {
                // navigate to category screen
                Logger.d(TAG, "navigate to category")
                TrackingSupport.recordEventOnlyFirebase(EventNotification.NotificationReceiveFcm.nameEvent + NotificationUtils.FCM_CATEGORY)
            }
        }

    }

    override fun onNewToken(token: String) {
        Logger.d(TAG, "Refreshed token: $token")

    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }
}