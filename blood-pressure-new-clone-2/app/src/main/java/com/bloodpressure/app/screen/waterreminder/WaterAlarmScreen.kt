package com.bloodpressure.app.screen.waterreminder

import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bloodpressure.app.screen.alarm.AlarmType
import com.bloodpressure.app.screen.alarm.SetAlarmDialog
import com.bloodpressure.app.ui.theme.GrayScale800
import com.bloodpressure.app.ui.theme.GrayScale900
import com.bloodpressure.app.utils.LocalAdsManager
import com.bloodpressure.app.utils.formatTime
import com.bloodpressure.app.utils.navigationBarsPaddingIfNeed
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterAlarmScreen(
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit,
    viewModel: WaterAlarmViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val adView by LocalAdsManager.current.homeBannerAd.collectAsStateWithLifecycle()

    var reminderTime by remember { mutableStateOf(ReminderTime.AFTER_WAKE_UP) }
    if (uiState.showSetAlarmDialog) {
        SetAlarmDialog(
            alarmType = AlarmType.WATER_REMINDER,
            alarmRecord = uiState.listRecords.firstOrNull { it.reminderTime == reminderTime }?.alarmRecord,
            onDismissRequest = { viewModel.showSetAlarmDialog(false) },
            onSave = { alarmRecord ->
                viewModel.updateListRecord(
                    alarmRecord.copy(
                        reminderTime = reminderTime
                    ), reminderTime
                )
                viewModel.showSetAlarmDialog(false)
            }
        )
    }
    BackHandler {
        viewModel.insertRecords()
        onNavigateUp.invoke()
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPaddingIfNeed()
    ) {
        TopAppBar(
            title = {
                Column {
                    Text(text = "Reminder")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            ),
            navigationIcon = {
                IconButton(
                    onClick = {
                        viewModel.insertRecords()
                        onNavigateUp()
                    }
                ) {
                    Icon(
                        painter = rememberVectorPainter(image = Icons.Default.ArrowBack),
                        contentDescription = null
                    )
                }
            }
        )
        WaterAlarmContent(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(16.dp),
            onItemClick = { item ->
                reminderTime = item
                viewModel.showSetAlarmDialog(true)
            },
            onItemChecked = { isChecked, item ->
                viewModel.updateCheckedItem(isChecked, item)
            },
            uiState = uiState
        )
        if (!uiState.isPurchased && adView != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.BottomCenter
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxWidth(),
                    factory = {
                        adView!!.apply {
                            (parent as? ViewGroup)?.removeView(this)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun WaterAlarmContent(
    modifier: Modifier = Modifier,
    onItemClick: (ReminderTime) -> Unit,
    onItemChecked: (Boolean, WaterAlarmRecord) -> Unit,
    uiState: WaterAlarmViewModel.UiState
) {
    LazyColumn(modifier = modifier) {
        itemsIndexed(uiState.listRecords) { index, item ->
            ReminderCard(
                waterAlarmRecord = item,
                onItemClick = { onItemClick.invoke(item.reminderTime) },
                onItemChecked = { onItemChecked.invoke(it, item) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun ReminderCard(
    modifier: Modifier = Modifier,
    waterAlarmRecord: WaterAlarmRecord,
    onItemClick: () -> Unit,
    onItemChecked: (Boolean) -> Unit
) {
    Row(
        modifier = modifier
            .background(Color.White, shape = RoundedCornerShape(8.dp))
            .fillMaxWidth()
            .clickable {
                onItemClick.invoke()
            }
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(id = waterAlarmRecord.reminderTime.titleRes),
                    style = TextStyle(
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight(400),
                        color = GrayScale800,
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = waterAlarmRecord.alarmRecord.formatTime(),
                    style = TextStyle(
                        fontSize = 20.sp,
                        lineHeight = 30.sp,
                        fontWeight = FontWeight(800),
                        color = GrayScale900,
                    )
                )
            }
            Switch(
                checked = waterAlarmRecord.isChecked,
                onCheckedChange = onItemChecked,
            )
        }
    }
}
