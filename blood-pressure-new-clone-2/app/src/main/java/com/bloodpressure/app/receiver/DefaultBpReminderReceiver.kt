package com.bloodpressure.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.StringRes
import com.bloodpressure.app.R
import com.bloodpressure.app.data.model.Record
import com.bloodpressure.app.data.repository.BpRepository
import com.bloodpressure.app.fcm.NotificationController
import com.bloodpressure.app.screen.UriPattern
import com.bloodpressure.app.screen.home.info.InfoItemType
import com.bloodpressure.app.screen.record.BpType
import com.bloodpressure.app.utils.DefaultReminderManager
import com.bloodpressure.app.utils.DefaultReminderManager.Companion.DEFAULT_BP_REGULARLY1_REMINDER_ID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Calendar
import java.util.concurrent.TimeUnit

class DefaultBpReminderReceiver : BroadcastReceiver(), KoinComponent {
    private val receiverScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val reminderManager: DefaultReminderManager by inject()
    private val notificationController: NotificationController by inject()

    override fun onReceive(context: Context, p1: Intent?) {
        val categoryOfReminder = p1?.getIntExtra(KEY, -1)
        val bpRepository = reminderManager.bpRepository
        receiverScope.launch {
            when (categoryOfReminder) {
                CategoryDefaultReminder.BPREGULARLY_AT_6AM.value -> processRegularlyReminder(
                    context,
                    CategoryDefaultReminder.BPREGULARLY_AT_6AM
                ) {
                    reminderManager.scheduleDefaultBpRegularlyReminder(
                        CategoryDefaultReminder.BPREGULARLY_AT_6AM.defaultHour,
                        CategoryDefaultReminder.BPREGULARLY_AT_6AM.defaultMinute,
                        CategoryDefaultReminder.BPREGULARLY_AT_6AM.value,
                        DEFAULT_BP_REGULARLY1_REMINDER_ID
                    )
                }

                CategoryDefaultReminder.BPREGULARLY_AT_11AM.value -> processRegularlyReminder(
                    context,
                    CategoryDefaultReminder.BPREGULARLY_AT_11AM
                ){
                    reminderManager.scheduleDefaultBpRegularlyReminder(
                        CategoryDefaultReminder.BPREGULARLY_AT_11AM.defaultHour,
                        CategoryDefaultReminder.BPREGULARLY_AT_11AM.defaultMinute,
                        CategoryDefaultReminder.BPREGULARLY_AT_11AM.value,
                        DefaultReminderManager.DEFAULT_BP_REGULARLY2_REMINDER_ID
                    )
                }

                CategoryDefaultReminder.BPREGULARLY_AT_5PM.value -> processRegularlyReminder(
                    context,
                    CategoryDefaultReminder.BPREGULARLY_AT_5PM
                ){
                    reminderManager.scheduleDefaultBpRegularlyReminder(
                        CategoryDefaultReminder.BPREGULARLY_AT_5PM.defaultHour,
                        CategoryDefaultReminder.BPREGULARLY_AT_5PM.defaultMinute,
                        CategoryDefaultReminder.BPREGULARLY_AT_5PM.value,
                        DefaultReminderManager.DEFAULT_BP_REGULARLY3_REMINDER_ID
                    )
                }

                CategoryDefaultReminder.BPLEVEL.value -> processBpLevelReminder(
                    context,
                    bpRepository
                ){
                    reminderManager.scheduleDefaultBpLevelReminder()
                }

                CategoryDefaultReminder.BPREVIEW.value -> processBpReviewReminder(context){
                    reminderManager.scheduleDefaultBpReviewReminder()
                }
                CategoryDefaultReminder.BPADD.value -> processBpAddReminder(context, bpRepository){
                    reminderManager.scheduleDefaultBpAddReminder()
                }
                else -> null
            }
        }
    }

    private fun processRegularlyReminder(
        context: Context,
        category: CategoryDefaultReminder,
        onComplete: () -> Unit = {}
    ) {
        notificationController.showDefaultBpRegularlyReminderNotification(
            context = context,
            title = context.getString(category.notificationTitle),
            content = if (category.notificationContent != 0) context.getString(category.notificationContent) else ""
        )
        onComplete()
    }

    private suspend fun processBpLevelReminder(
        context: Context,
        bpRepository: BpRepository,
        onComplete: () -> Unit = {}
    ) {
        val latestRecord: Record? = bpRepository.getLatestRecord().first()
        if (latestRecord != null) {
            val bpLevelAlarm = when (latestRecord.type) {
                BpType.HYPOTENSION -> BpLevelReminder.LOW
                BpType.NORMAL -> BpLevelReminder.NORMAL
                BpType.ELEVATED -> BpLevelReminder.ELEVATED
                else -> BpLevelReminder.HIGH
            }
            val distanceFromLatestRecordToNow = System.currentTimeMillis() - latestRecord.createdAt
            if (distanceFromLatestRecordToNow <= A_DAY_IN_MILLIS_FORM) {
                notificationController.showDefaultBpLevelReminderNotification(
                    context = context,
                    title = context.getString(bpLevelAlarm.notification, latestRecord.systolic),
                    btnString = context.getString(bpLevelAlarm.btnString),
                    content = "",
                    uri = bpLevelAlarm.deepLinkScreen
                )
            }
        }
        onComplete()
    }

    private fun processBpReviewReminder(context: Context, onComplete: () -> Unit = {}) {
        val calendar = Calendar.getInstance()
        val toDay = calendar.get(Calendar.DAY_OF_WEEK)
        if (toDay == Calendar.MONDAY) {
            notificationController.showDefaultBpReviewReminderNotification(
                context = context,
                title = context.getString(CategoryDefaultReminder.BPREVIEW.notificationTitle),
                content = ""
            )
        }
        onComplete()
    }

    private suspend fun processBpAddReminder(
        context: Context,
        bpRepository: BpRepository,
        onComplete: () -> Unit = {}
    ) {
        val latestRecord: Record? = bpRepository.getLatestRecord().first()
        if (latestRecord != null) {
            val distanceFromLatestRecordToNow = System.currentTimeMillis() - latestRecord.createdAt
            if (distanceFromLatestRecordToNow > A_DAY_IN_MILLIS_FORM && distanceFromLatestRecordToNow <= 7 * A_DAY_IN_MILLIS_FORM) {
                val distanceInFormDay = TimeUnit.MILLISECONDS.toDays(distanceFromLatestRecordToNow)
                notificationController.showDefaultBpAddReminderNotification(
                    context = context,
                    title = context.getString(
                        CategoryDefaultReminder.BPADD.notificationTitle,
                        distanceInFormDay
                    ),
                    content = ""
                )
            }
        }
        onComplete()
    }

    companion object {
        const val KEY = "CATEGORY"
        const val A_DAY_IN_MILLIS_FORM = 86400000L
    }
}

enum class CategoryDefaultReminder(
    val value: Int,
    @StringRes val notificationTitle: Int,
    @StringRes val notificationContent: Int,
    val defaultHour: Int,
    val defaultMinute: Int
) {
    BPREGULARLY_AT_6AM(
        0,
        R.string.bp_regularly_reminder_1_title,
        R.string.bp_regularly_reminder_1_subtitle,
        6,
        0
    ),
    BPLEVEL(1, R.string.bp_level_normal_reminder, 0, 20, 0),
    BPREVIEW(2, R.string.bp_review_reminder, 0, 8, 0),
    BPADD(3, R.string.bp_add_reminder, 0, 22, 0),
    BPREGULARLY_AT_11AM(4, R.string.bp_regularly_reminder_2_title, 0, 11, 0),
    BPREGULARLY_AT_5PM(
        5,
        R.string.bp_regularly_reminder_3_title,
        R.string.bp_regularly_reminder_3_subtitle,
        17,
        0
    )
}

enum class BpLevelReminder(
    @StringRes val notification: Int,
    @StringRes val btnString: Int,
    val deepLinkScreen: String
) {
    LOW(R.string.bp_level_low_reminder, R.string.record, UriPattern.ADD_RECORD),
    NORMAL(
        R.string.bp_level_normal_reminder,
        R.string.explore,
        UriPattern.BP_INFO + "?feature=${InfoItemType.BLOOD_PRESSURE}"
    ),
    HIGH(R.string.bp_level_high_reminder, R.string.record, UriPattern.ADD_RECORD),
    ELEVATED(R.string.bp_level_elevated_reminder, R.string.record, UriPattern.ADD_RECORD),
}

enum class ReminderMode(val value: String) {
    SIMPLE("simple"),
    FULL("full")
}