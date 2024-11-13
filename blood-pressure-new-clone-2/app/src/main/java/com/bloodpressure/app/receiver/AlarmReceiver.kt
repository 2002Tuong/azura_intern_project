package com.bloodpressure.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bloodpressure.app.R
import com.bloodpressure.app.data.model.AlarmRecord
import com.bloodpressure.app.fcm.NotificationController
import com.bloodpressure.app.utils.AlarmingManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.KoinContextHandler

class AlarmReceiver : BroadcastReceiver(), KoinComponent {

    private val receiverScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val notificationController: NotificationController by inject()

    override fun onReceive(context: Context, intent: Intent) {

        val alarmId = intent.getLongExtra("alarmId", -1)

        val koinContext = KoinContextHandler.getOrNull()
        if (koinContext != null) {
            val alarmingManager = koinContext.get<AlarmingManager>()

            receiverScope.launch {
                val alarmRecord: AlarmRecord? =
                    alarmingManager.alarmRepository.getRecordById(alarmId).first()

                if (alarmRecord != null) {

                    if (alarmRecord.soundEnabled) {
                        alarmingManager.playSound(R.raw.beep)
                    }

                    if (alarmRecord.vibrateEnabled) {
                        alarmingManager.vibrateNotification()
                    }

                    notificationController.showAlarmNotification(context, alarmRecord)

                }
            }
        }


    }

}
