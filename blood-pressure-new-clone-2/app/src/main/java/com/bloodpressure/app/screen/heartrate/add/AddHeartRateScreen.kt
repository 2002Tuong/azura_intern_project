package com.bloodpressure.app.screen.heartrate.add

import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bloodpressure.app.R
import com.bloodpressure.app.ads.SmallNativeAd
import com.bloodpressure.app.data.model.HeartRateRecord
import com.bloodpressure.app.screen.alarm.AlarmType
import com.bloodpressure.app.screen.alarm.SetAlarmDialog
import com.bloodpressure.app.screen.home.tracker.TrackerType
import com.bloodpressure.app.screen.record.DateTimeSelection
import com.bloodpressure.app.ui.component.BloodButton
import com.bloodpressure.app.ui.component.TopAppBarAction
import com.bloodpressure.app.ui.theme.GrayScale700
import com.bloodpressure.app.ui.theme.GrayScale900
import com.bloodpressure.app.utils.LocalAdsManager
import com.bloodpressure.app.utils.LocalRemoteConfig
import com.bloodpressure.app.utils.navigationBarsPaddingIfNeed
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHeartRateScreen(
    modifier: Modifier = Modifier,
    heartRate: Int? = null,
    onNavigateUp: () -> Unit,
    onNavigateToAddNote: () -> Unit,
    onNavigateToPremium: () -> Unit,
    onSaveRecord: (HeartRateRecord) -> Unit,
    viewModel: AddHeartRateViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val adsManager = LocalAdsManager.current
    val adView by LocalAdsManager.current.homeBannerAd.collectAsStateWithLifecycle()
    val shouldShowNative = LocalRemoteConfig.current.adsConfig.shouldShowAddRecordNativeAd

    LaunchedEffect(uiState.shouldNavigateUp) {
        if (uiState.shouldNavigateUp) {
            onNavigateUp()
        }
    }

    LaunchedEffect(heartRate) {
        if (heartRate != null) {
            viewModel.setHeartRate(heartRate)
        }
    }

    LaunchedEffect(uiState.didClickSave) {

        if (uiState.didClickSave) {

            if (uiState.heartRateRecord != null) {
                adsManager.showAddFeatureInterAds(TrackerType.HEART_RATE) {
                    onSaveRecord(uiState.heartRateRecord!!)
                }
            }
        }
    }

    if (uiState.showSetAlarmDialog) {
        SetAlarmDialog(
            alarmType = AlarmType.HEART_RATE,
            onDismissRequest = { viewModel.showSetReminder(false) },
            onSave = { alarmRecord ->
                viewModel.insertRecord(alarmRecord)
                viewModel.showSetReminder(false)
            }
        )
    }

    if (uiState.shouldShowDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.clearConfirmDelete() },
            text = { Text(text = stringResource(id = R.string.delete_record_des)) },
            title = { Text(text = stringResource(id = R.string.delete_record_title)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteRecord()
                    viewModel.clearConfirmDelete()
                }) {
                    Text(text = stringResource(id = R.string.cw_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.clearConfirmDelete() }) {
                    Text(text = stringResource(id = R.string.cw_cancel))
                }
            }
        )
    }

    if (uiState.showAgeDialog || uiState.forceShowAgeDialog) {
        AgePickerDialog(
            value = uiState.age,
            onValueChanged = { viewModel.setAge(it) },
            onDismissRequest = { viewModel.showAgeDialog(false) },
            onValueSaved = {
                viewModel.setAge(it)
                viewModel.showAgeDialog(false)
                viewModel.forceShowAgeDialog(false)

                if (!uiState.isGenderInputted) {
                    viewModel.forceShowGenderDialog(true)
                }
            },
            dismissOnClickOutside = !uiState.forceShowAgeDialog
        )
    }

    if (uiState.showGenderDialog || uiState.forceShowGenderDialog) {
        GenderPickerDialog(
            value = uiState.genderType ?: GenderType.OTHERS,
            onValueChanged = { viewModel.setGender(it) },
            onDismissRequest = { viewModel.showGenderDialog(false) },
            onValueSaved = {
                viewModel.setGender(it)
                viewModel.showGenderDialog(false)
                viewModel.forceShowGenderDialog(false)
            },
            dismissOnClickOutside = !uiState.forceShowGenderDialog
        )
    }

    AnimatedVisibility(
        visible = uiState.showAddSuccessDialog,
        enter = fadeIn() + expandIn()
    ) {
        AddSuccessDialog(onDismissRequest = { viewModel.showAddSuccessDialog(false) })
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPaddingIfNeed()
    ) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.new_record),
                    style = TextStyle(
                        fontSize = 18.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight(700),
                        color = GrayScale900,
                    )
                )
            },
            actions = {
                TopAppBarAction(
                    isPurchased = uiState.isPurchased,
                    onSetAlarmClick = { viewModel.showSetReminder(true) },
                    onNavigateToPremium = onNavigateToPremium,
                    additionalAction = {
                        uiState.recordId?.let {
                            TextButton(onClick = { viewModel.confirmDelete() }) {
                                Text(
                                    text = stringResource(R.string.delete),
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp,
                                        fontWeight = FontWeight(400),
                                        color = Color(0xFFF95721),
                                        textAlign = TextAlign.Right,
                                    )
                                )
                            }
                        }
                    }
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

        AddHeartRateContent(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            onSaveClick = viewModel::saveRecord,
            onTimeChanged = viewModel::setTime,
            onDateChanged = viewModel::setDate,
            onHeartRateValueChanged = viewModel::setHeartRate,
            onSelectedNotesChanged = viewModel::setNotes,
            onAddNoteClick = onNavigateToAddNote,
            onEditAgeClick = { viewModel.showAgeDialog(true) },
            onEditGenderClick = { viewModel.showGenderDialog(true) },
            uiState = uiState
        )

        if (!uiState.isPurchased) {
            if (shouldShowNative) {
                val nativeAd by LocalAdsManager.current.addRecordNativeAd.collectAsStateWithLifecycle()
                if (nativeAd != null) {
                    SmallNativeAd(nativeAd = nativeAd!!, modifier = Modifier.fillMaxWidth())
                }
            } else if (adView != null){
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
}

@Composable
private fun AddHeartRateContent(
    modifier: Modifier = Modifier,
    onSaveClick: () -> Unit,
    onTimeChanged: (String) -> Unit,
    onDateChanged: (String) -> Unit,
    onHeartRateValueChanged: (Int) -> Unit,
    onSelectedNotesChanged: (Set<String>) -> Unit,
    onAddNoteClick: () -> Unit,
    onEditAgeClick: () -> Unit,
    onEditGenderClick: () -> Unit,
    uiState: AddHeartRateViewModel.UiState
) {

    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPaddingIfNeed()
    ) {
        var isScrollEnabled by remember { mutableStateOf(true) }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .verticalScroll(state = rememberScrollState(), enabled = isScrollEnabled)
        ) {
            Card(
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    HeartRateTypeItem(heartRateType = uiState.selectedHeartRateType)

                    HeartRateTypeIndicator(
                        heartRateType = uiState.selectedHeartRateType,
                        modifier = Modifier.padding(top = 12.dp)
                    )

                    Row(
                        modifier = Modifier
                            .padding(top = 24.dp)
                            .fillMaxWidth()
                            .pointerInput(Unit) {
                                val currentContext = currentCoroutineContext()
                                awaitPointerEventScope {
                                    while (currentContext.isActive) {
                                        awaitPointerEvent()
                                        isScrollEnabled =
                                            currentEvent.type == PointerEventType.Release
                                    }
                                }
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Spacer(modifier = modifier.weight(1f))

                        SpinnerValueSelection(
                            modifier = Modifier
                                .wrapContentHeight()
                                .width(88.dp),
                            value = uiState.heartRate,
                            onValueChanged = onHeartRateValueChanged,
                            range = 40..220,
                            selectedTextColor = Color(0xFF1892FA),
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 16.dp)
                                .fillMaxHeight()
                        ) {
                            Text(
                                text = stringResource(R.string.bpm),
                                style = TextStyle(
                                    fontSize = 18.sp,
                                    lineHeight = 28.sp,
                                    fontWeight = FontWeight(700),
                                    color = GrayScale900,
                                )
                            )
                        }

                    }

                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        text = when (uiState.selectedHeartRateType) {
                            HeartRateType.SLOW -> stringResource(R.string.your_heart_rate_too_low)
                            HeartRateType.NORMAL -> stringResource(R.string.your_heart_rate_remains_normal)
                            else -> stringResource(R.string.your_heart_rate_high)
                        },
                        style = TextStyle(
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight(400),
                            color = GrayScale900,
                            textAlign = TextAlign.Center,
                        )
                    )

                    DateTimeSelection(
                        modifier = Modifier,
                        onTimeChanged = onTimeChanged,
                        onDateChanged = onDateChanged,
                        onSelectedNotesChanged = onSelectedNotesChanged,
                        onAddNoteClick = onAddNoteClick,
                        time = uiState.time,
                        date = uiState.date,
                        notes = uiState.notes,
                        title = stringResource(R.string.heart_rate_notes)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {

                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 8.dp)
                                .clickable { onEditAgeClick() },
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "Age: ${uiState.age}", Modifier.padding(end = 4.dp),
                                style = TextStyle(
                                    fontSize = 15.sp,
                                    lineHeight = 22.sp,
                                    fontWeight = FontWeight(400),
                                    color = Color(0xFF8C8E97),
                                )
                            )

                            Icon(
                                painter = painterResource(id = R.drawable.ic_edit),
                                contentDescription = ""
                            )
                        }

                        Spacer(
                            modifier = Modifier
                                .width(1.dp)
                                .background(color = GrayScale700)
                        )

                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 8.dp)
                                .clickable { onEditGenderClick() },
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                modifier = Modifier.padding(end = 4.dp),
                                text = "Gender: ${context.getString(uiState.genderType.nameRes)}",
                                style = TextStyle(
                                    fontSize = 15.sp,
                                    lineHeight = 22.sp,
                                    fontWeight = FontWeight(400),
                                    color = Color(0xFF8C8E97),
                                )
                            )

                            Icon(
                                painter = painterResource(id = R.drawable.ic_edit),
                                contentDescription = ""
                            )
                        }
                    }

                    BloodButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.save),
                        onClick = onSaveClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun HeartRateTypeItem(modifier: Modifier = Modifier, heartRateType: HeartRateType) {
    var shouldShowHeartRateTypeInfos by remember { mutableStateOf(false) }
    if (shouldShowHeartRateTypeInfos) {
        HeartRateTypesDialog { shouldShowHeartRateTypeInfos = false }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable { shouldShowHeartRateTypeInfos = true },
        colors = CardDefaults.cardColors(containerColor = heartRateType.color),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(id = heartRateType.nameRes),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight(700),
            )

            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                modifier = Modifier.padding(end = 12.dp),
                tint = Color.White
            )
        }
    }
}

@Composable
fun HeartRateTypeIndicator(modifier: Modifier = Modifier, heartRateType: HeartRateType) {
    val heartRateTypes by remember { mutableStateOf(HeartRateType.values()) }
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Resting Heart Rate 60-100 BPM",
            style = TextStyle(
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight(400),
                color = GrayScale900,
                textAlign = TextAlign.Center,
            )
        )

        Row(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth()
        ) {
            heartRateTypes.forEach {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    if (it == heartRateType) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_bottom),
                            contentDescription = null
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        ) {
            heartRateTypes.map { it.color }.forEachIndexed { index, color ->
                val shape = when (index) {
                    0 -> RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)
                    HeartRateType.values().size - 1 -> RoundedCornerShape(
                        topEnd = 8.dp,
                        bottomEnd = 8.dp
                    )

                    else -> RoundedCornerShape(0.dp)
                }
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .background(color, shape)
                )
            }
        }
    }
}

@Preview
@Composable
fun AddRecordScreenPreview() {
    AddHeartRateContent(
        onSaveClick = {},
        onTimeChanged = {},
        onDateChanged = {},
        onHeartRateValueChanged = {},
        onSelectedNotesChanged = {},
        onAddNoteClick = {},
        onEditAgeClick = {},
        onEditGenderClick = {},
        uiState = AddHeartRateViewModel.UiState()
    )
}
