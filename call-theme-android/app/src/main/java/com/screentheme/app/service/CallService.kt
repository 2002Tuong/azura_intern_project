package com.screentheme.app.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.InCallService
import androidx.preference.PreferenceManager
import com.calltheme.app.ui.activity.CallActivity
import com.screentheme.app.utils.extensions.isOutgoing
import com.screentheme.app.utils.extensions.powerManager
import com.screentheme.app.utils.helpers.CallController
import com.screentheme.app.utils.helpers.FlashController
import com.screentheme.app.utils.helpers.VibrateController
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class CallService : InCallService(), KoinComponent {

    private lateinit var context: Context
    private val vibrateHelper: VibrateController by inject()
    private val flashHelper: FlashController by inject()

    override fun onCreate() {
        super.onCreate()
        context = this
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        CallController.onCallAdded(call)
        CallController.inCallService = this

        if (!powerManager.isInteractive || call.isOutgoing()) {
        }

        if (call.state == Call.STATE_RINGING) {

            if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("vibrate_preference", false)) {
                vibrateHelper.vibrateDevice()
            }

            if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("caller_flash_preference", false)) {
                flashHelper.startFlashLight()
            }

            startActivity(CallActivity.getStartIntent(this))
        }

        call.registerCallback(callCallback)
    }

    private val callCallback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            super.onStateChanged(call, state)

            if (state == Call.STATE_DIALING) {
                // Handle outgoing call state here
                val phoneNumber = call.details.handle.schemeSpecificPart
                val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumber"))
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
            if (state == Call.STATE_DISCONNECTED) {
                if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("vibrate_preference", false)) {
                    vibrateHelper.stopVibration()
                }

                if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("caller_flash_preference", false)) {
                    flashHelper.stopFlashLight()
                }
            }
            if (state == Call.STATE_ACTIVE) {
                if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("vibrate_preference", false)) {
                    vibrateHelper.stopVibration()
                }

                if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("caller_flash_preference", false)) {
                    flashHelper.stopFlashLight()
                }
            }
        }
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        CallController.onCallRemoved(call)
        call.unregisterCallback(callCallback)
    }

    override fun onCallAudioStateChanged(audioState: CallAudioState?) {
        super.onCallAudioStateChanged(audioState)
    }
}
