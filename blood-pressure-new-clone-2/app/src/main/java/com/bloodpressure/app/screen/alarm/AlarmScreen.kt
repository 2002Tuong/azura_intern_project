package com.bloodpressure.app.screen.alarm

import android.view.ViewGroup
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bloodpressure.app.R
import com.bloodpressure.app.data.model.AlarmRecord
import com.bloodpressure.app.ui.theme.GrayScale600
import com.bloodpressure.app.ui.theme.GrayScale900
import com.bloodpressure.app.utils.LocalAdsManager
import com.bloodpressure.app.utils.Logger
import com.bloodpressure.app.utils.formatTime
import com.bloodpressure.app.utils.navigationBarsPaddingIfNeed
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmScreen(
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit,
    onWaterReminderClick: () -> Unit,
    viewModel: AlarmViewModel = koinViewModel()
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val modalBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val adView by LocalAdsManager.current.homeBannerAd.collectAsStateWithLifecycle()

    if (uiState.showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.showBottomSheet(false) },
            sheetState = modalBottomSheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = Color.White,
            content = {
                ModalBottomSheetContent(onSelectAlarmType = {
                    viewModel.setSelectedAlarmType(it)
                    viewModel.showBottomSheet(false)
                    if (it != AlarmType.WATER_REMINDER) {
                        viewModel.showSetAlarmDialog(true)
                    } else {
                        onWaterReminderClick.invoke()
                    }

                })
            }
        )

    }

    if (uiState.showSetAlarmDialog && uiState.selectedAlarmType != null) {
        SetAlarmDialog(
            alarmType = uiState.selectedAlarmType!!,
            alarmRecord = uiState.alarmRecord,
            onDismissRequest = {
                viewModel.showSetAlarmDialog(false)
                viewModel.clearAlarmRecord()
                viewModel.clearSelectedAlarmType()
            },
            onSave = {

                if (uiState.isUpdateAlarmRecord) {
                    viewModel.updateRecord(it)
                } else {
                    viewModel.insertRecord(it)
                }

                viewModel.clearSelectedAlarmType()
                viewModel.clearAlarmRecord()
            },
            onDelete = {
                viewModel.showDeleteDialog(true)
            }
        )
    }

    if (uiState.showDeleteDialog) {
        DeleteConfirmDialog(
            onDismissRequest = { viewModel.showDeleteDialog(false) },
            onCancel = { viewModel.showDeleteDialog(false) },
            onDelete = {
                if (uiState.alarmRecord != null) {
                    viewModel.deleteRecord(uiState.alarmRecord!!)
                }

                viewModel.clearAlarmRecord()
                viewModel.showDeleteDialog(false)
                viewModel.showSetAlarmDialog(false)
            }
        )
    }

    Column(modifier = modifier.fillMaxSize().navigationBarsPaddingIfNeed()) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(id = R.string.alarm),
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight(700),
                        color = GrayScale900,
                    )
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            ),
            navigationIcon = {
                IconButton(onClick = { onNavigateUp() }) {
                    Icon(
                        painter = rememberVectorPainter(image = Icons.Default.ArrowBack),
                        contentDescription = null
                    )
                }
            }
        )

        Spacer(
            modifier = Modifier
                .height(1.dp)
                .fillMaxWidth()
                .background(GrayScale600)
        )

        AlarmContent(
            modifier = Modifier.fillMaxSize().weight(1f),
            uiState = uiState,
            onAddRecord = { viewModel.showBottomSheet(true) },
            onClickAlarmRecord = {

                viewModel.setAlarmRecord(it)
                viewModel.showSetAlarmDialog(true)
            }
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
private fun AlarmContent(
    modifier: Modifier = Modifier,
    uiState: AlarmViewModel.UiState,
    onAddRecord: () -> Unit,
    onClickAlarmRecord: (AlarmRecord) -> Unit
) {
    Column(
        modifier = modifier
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.allRecords) { alarmRecord ->
                    AlarmItem(alarmRecord = alarmRecord, onClick = { onClickAlarmRecord(alarmRecord) })
                }
            }

            TextButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color(0xFF1892FA), shape = RoundedCornerShape(size = 8.dp)),
                onClick = onAddRecord
            ) {
                Text(
                    text = stringResource(R.string.remind_me_to_record),
                    style = TextStyle(
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight(700),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                )
            }
        }

    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlarmItem(
    modifier: Modifier = Modifier,
    alarmRecord: AlarmRecord,
    onClick: (AlarmRecord) -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        onClick = { onClick(alarmRecord) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.remind_me_to_record),
                style = TextStyle(
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight(700),
                    color = GrayScale900,
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    modifier = Modifier.size(48.dp),
                    painter = painterResource(id = alarmRecord.type.iconRes),
                    contentDescription = "",
                    contentScale = ContentScale.None
                )

                Text(
                    text = stringResource(id = alarmRecord.type.titleRes),
                    style = TextStyle(
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight(700),
                        color = GrayScale900,
                    )
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = alarmRecord.formatTime(),
                    style = TextStyle(
                        fontSize = 32.sp,
                        lineHeight = 44.sp,
                        fontWeight = FontWeight(700),
                        color = GrayScale900,
                        textAlign = TextAlign.Center,
                    )
                )
            }

            DayOfWeekPicker(modifier = Modifier.fillMaxWidth(), selectedDays = alarmRecord.days, onDaysSelected = { })
        }
    }
}

@Composable
private fun ModalBottomSheetContent(
    modifier: Modifier = Modifier,
    onSelectAlarmType: (AlarmType) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(Color.White),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(id = R.string.remind_me_to_record),
            style = TextStyle(
                fontSize = 15.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight(700),
                color = GrayScale900,
            )
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            itemsIndexed(AlarmType.values().toList()) { index, alarmType ->
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSelectAlarmType(alarmType)
                        }) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                modifier = Modifier.size(40.dp),
                                painter = painterResource(id = alarmType.iconRes),
                                contentDescription = "",
                                contentScale = ContentScale.Inside
                            )

                            Text(
                                text = stringResource(id = alarmType.titleRes),
                                style = TextStyle(
                                    fontSize = 15.sp,
                                    lineHeight = 22.sp,
                                    fontWeight = FontWeight(700),
                                    color = GrayScale900,
                                )
                            )
                        }
                    }

                    Divider(color = Color(0xFFF4F4F5), thickness = if (index < AlarmType.values().toList().lastIndex) 1.dp else 0.dp)
                }

            }
        }
    }
}
