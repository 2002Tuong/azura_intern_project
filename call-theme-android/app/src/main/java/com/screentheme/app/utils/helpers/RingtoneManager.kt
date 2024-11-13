package com.screentheme.app.utils.helpers

import android.content.Context
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.provider.Settings
import com.screentheme.app.models.RingtoneModel

class RingtoneController(private val context: Context) {
    private var player: MediaPlayer? = null
    private var curUri: Uri? = null

    fun getCurrentRingtoneUri(): Uri? {
        return RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE)
    }

    fun getDefaultRingtones(): List<RingtoneModel> {
        val ringtoneManager = RingtoneManager(context)
        ringtoneManager.setType(RingtoneManager.TYPE_RINGTONE)

        val ringtoneCursor = ringtoneManager.cursor
        val ringtoneList = mutableListOf<RingtoneModel>()

        while (ringtoneCursor.moveToNext()) {
            val title = ringtoneCursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
            val uri = ringtoneManager.getRingtoneUri(ringtoneCursor.position)
            val ringtoneItem = RingtoneModel(title, uri)
            ringtoneList.add(ringtoneItem)
        }

        ringtoneCursor.close()
        return ringtoneList
    }

    fun setRingtone(ringtoneUri: Uri) {
        val ringtoneManager = RingtoneManager(context)
        ringtoneManager.setType(RingtoneManager.TYPE_RINGTONE)

        val ringtoneIndex = ringtoneManager.getRingtonePosition(ringtoneUri)
        if (ringtoneIndex >= 0) {
            // Save the selected ringtone as the default for phone calls
            Settings.System.putString(
                context.contentResolver,
                Settings.System.RINGTONE,
                ringtoneManager.getRingtoneUri(ringtoneIndex).toString()
            )
        }
    }

    fun playRingtone(ringtoneUri: Uri, callback: ((RingtoneState, Uri) -> Unit)? = null) {
        if (player?.isPlaying == true) {
            // A ringtone is already playing, stop it before playing the new one
            stopRingtone()
            if (curUri != null) {
                callback?.invoke(RingtoneState.STOPPED, curUri!!)
            }
        }

        try {
            player = MediaPlayer.create(context, ringtoneUri)
            curUri = ringtoneUri
            player?.setOnCompletionListener {
                player?.release()
                player = null
            }
            player?.start()
            callback?.invoke(RingtoneState.STARTED, ringtoneUri)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopRingtone() {
        player?.stop()
        player?.release()
        player   = null
    }
}

enum class RingtoneState {
    STARTED,
    STOPPED
}