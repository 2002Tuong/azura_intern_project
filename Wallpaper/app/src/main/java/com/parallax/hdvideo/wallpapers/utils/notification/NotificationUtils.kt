/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.parallax.hdvideo.wallpapers.utils.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.parallax.hdvideo.wallpapers.WallpaperApp
import com.parallax.hdvideo.wallpapers.BuildConfig
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.remote.RemoteConfig
import com.parallax.hdvideo.wallpapers.remote.model.WallpaperURLBuilder
import com.parallax.hdvideo.wallpapers.ui.main.MainActivity
import com.parallax.hdvideo.wallpapers.utils.AppConfiguration
import com.parallax.hdvideo.wallpapers.utils.Logger
import com.parallax.hdvideo.wallpapers.utils.other.GlideSupport
import java.util.*


object NotificationUtils {

    const val ACTION_NOTIFICATION = BuildConfig.APPLICATION_ID + ".notification"
    const val CHANNEL_ID = BuildConfig.APPLICATION_ID
    const val EXTRA_TAG = "NOTIFICATION"
    const val MESSAGE = "MESSAGE"
    const val TITLE = "TITLE"
    const val ID = "ID"
    const val DATA = "DATA"
    const val ACTION = "ACTION"
    const val SUMMARY_TEXT = "SUMMARY_TEXT"
    const val BODY = "body"
    const val IMAGE = "image"
    const val OBJECT_ID = "objectId"
    const val Wall_ID = "wallId"
    const val CATE_ID = "cateId"
    const val NAME = "name"
    const val URL = "url"
    const val HASHTAG = "hashtag"
    const val FCM_KEY_SEARCH = "keysearch"
    const val FCM_HASHTAG = "hashtag"
    const val FCM_MORE_APP = "moreapp"
    const val DAY1 = "DAY1"
    const val DAY2 = "DAY2"
    const val DAY4 = "DAY4"
    const val DAY7 = "DAY7"
    const val WEEKLY = "WEEKLY"
    const val FCM_DETAIL = "detail"
    const val FCM_CATEGORY = "category"

    @JvmStatic
    fun push(bundle: Bundle, bigPicture: Bitmap? = null, image: String? = null) : Boolean {
        val calendar = Calendar.getInstance()
        val hourInDay = calendar.get(Calendar.HOUR_OF_DAY)
        if (hourInDay >= 22 || hourInDay <= 6) return false
        val appInstance = WallpaperApp.instance
        val context = appInstance.applicationContext ?: return false
        if(!appInstance.localStorage.isOnNotification) {
            return false
        }
//        val isForeground =  isApplicationInForeground(con)
//        if (isForeground) return false
        val notificationUnitId = System.currentTimeMillis().toInt()
        bundle.putString(ID, notificationUnitId.toString() + "")

        val titleNotify = bundle.getString(TITLE)
        val messageNotify = bundle.getString(MESSAGE)
        val intentNotify = Intent()
        intentNotify.action = ACTION_NOTIFICATION
        intentNotify.setClass(context, MainActivity::class.java)
        intentNotify.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        intentNotify.putExtra(EXTRA_TAG, bundle)
        val pendingIntent = PendingIntent.getActivity(context, notificationUnitId, intentNotify, PendingIntent.FLAG_UPDATE_CURRENT)
        val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setTicker(titleNotify)
            .setContentTitle(titleNotify)
            .setContentText(messageNotify)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSound(sound)
            .setContentIntent(pendingIntent)
        val bitmap = bigPicture ?: image?.let {
            try {
                GlideSupport.getBitmap(it,
                    RequestOptions().override(AppConfiguration.widthScreenValue, AppConfiguration.heightScreenValue))
            }catch (e: Exception) {
                null
            }
        }
        if (bitmap != null) {
            val bigStyleNotify = NotificationCompat
                .BigPictureStyle()
                .bigPicture(bitmap)
            bundle.getString(SUMMARY_TEXT)?.also { bigStyleNotify.setSummaryText(it) }
            builder.setLargeIcon(bitmap)
                .setStyle(bigStyleNotify)
            builder.setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher))
                .setStyle(NotificationCompat.BigTextStyle().bigText(messageNotify))
        }
        val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(notificationManager, CHANNEL_ID)
        notificationManager.notify(notificationUnitId, builder.build())
        return true
    }

    private fun createNotificationChannel(manager: NotificationManager, channel: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nameChannel = "LiveWallpaper"
            val descriptionChannel = "Notification"
            val important = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channel, nameChannel, important)
            channel.description = descriptionChannel
            manager.createNotificationChannel(channel)
        }
    }

    //fcm related logics
    fun subscribeTopics() {
        if (!WallpaperApp.instance.localStorage.isOnNotification) return
        val curTopic = WallpaperApp.instance.localStorage.curListTopics.toMutableList()

        val curCountry = if (RemoteConfig.countryName == RemoteConfig.DEFAULT_LANGUAGE) WallpaperURLBuilder.getCountryByLang() else RemoteConfig.countryName
        val listTopic = mutableSetOf("version_${BuildConfig.VERSION_NAME}", "country_${curCountry.toUpperCase(Locale.ENGLISH)}")
        listTopic.addAll(RemoteConfig.commonData.listTopicsFCM)

        WallpaperApp.instance.localStorage.curListTopics = listTopic

        var size = curTopic.size
        var position = 0
        while (size > position) {
            val item = curTopic[position]
            if (listTopic.remove(item)) {
                curTopic.removeAt(position)
            } else {
                position++
            }
            size = curTopic.size
        }

        curTopic.forEach {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(it).addOnCompleteListener { task ->
                Logger.d("unsubscribeFromTopic", task.isSuccessful)
            }
        }

        listTopic.forEach {
            FirebaseMessaging.getInstance().subscribeToTopic(it).addOnCompleteListener { task ->
                Logger.d("subscribeToTopic", task.isSuccessful)
            }
        }
        Logger.d("subscribeTopics")
    }

    fun unsubscribeTopics() {
        WallpaperApp.instance.localStorage.curListTopics.forEach {
            val taskResult = FirebaseMessaging.getInstance().unsubscribeFromTopic(it)
            Logger.d("unsubscribeFromTopic", taskResult.isSuccessful)
        }
        WallpaperApp.instance.localStorage.curListTopics = hashSetOf()
    }
}
