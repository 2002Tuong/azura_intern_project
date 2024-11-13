package com.bloodpressure.app.screen.record

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bloodpressure.app.R
import com.bloodpressure.app.ads.SmallNativeAd
import com.bloodpressure.app.screen.alarm.AlarmType
import com.bloodpressure.app.screen.alarm.SetAlarmDialog
import com.bloodpressure.app.screen.heartrate.add.AgePickerDialog
import com.bloodpressure.app.screen.heartrate.add.GenderPickerDialog
import com.bloodpressure.app.screen.home.tracker.TrackerType
import com.bloodpressure.app.screen.record.note.NoteDialog
import com.bloodpressure.app.ui.component.BloodButton
import com.bloodpressure.app.ui.component.Picker
import com.bloodpressure.app.ui.component.TimePickerDialog
import com.bloodpressure.app.ui.component.TopAppBarAction
import com.bloodpressure.app.ui.theme.GrayScale700
import com.bloodpressure.app.utils.LocalAdsManager
import com.bloodpressure.app.utils.LocalTextFormatter
import com.bloodpressure.app.utils.navigationBarsPaddingIfNeed
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecordScreen(
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit,
    onNavigateToAddNote: () -> Unit,
    onNavigateToPremium: () -> Unit,
    title: String,
    onSaveRecord: () -> Unit = {},
    viewModel: AddRecordViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.shouldNavigateUp) {
        if (uiState.shouldNavigateUp) {
            onNavigateUp()
        }
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

    if (uiState.showSetAlarmDialog) {
        SetAlarmDialog(
            alarmType = AlarmType.BLOOD_PRESSURE,
            onDismissRequest = { viewModel.showSetReminder(false) },
            onSave = { alarmRecord ->
                viewModel.insertRecord(alarmRecord)
                viewModel.showSetReminder(false)
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
            value = uiState.genderType,
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


    Column(modifier = modifier) {
        TopAppBar(
            title = { Text(text = title) },
            actions = {
                TopAppBarAction(
                    isPurchased = uiState.isPurchased,
                    onSetAlarmClick = { viewModel.showSetReminder(true) },
                    onNavigateToPremium = onNavigateToPremium,
                    additionalAction = {
                        uiState.recordId?.let {
                            TextButton(onClick = { viewModel.confirmDelete() }) {
                                Text(text = stringResource(id = R.string.cw_delete))
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

        AddRecordContent(
            onSaveClick = {
                onSaveRecord.invoke()
                viewModel.save()
            },
            onTimeChanged = viewModel::setTime,
            onDateChanged = viewModel::setDate,
            onSystolicValueChanged = viewModel::setSystolic,
            onDiastolicValueChanged = viewModel::setDiastolic,
            onPulseValueChanged = viewModel::setPulse,
            onSelectedNotesChanged = viewModel::setNotes,
            onAddNoteClick = onNavigateToAddNote,
            onEditAgeClick = { viewModel.showAgeDialog(true) },
            onEditGenderClick = { viewModel.showGenderDialog(true) },
            uiState = uiState
        )
    }
}

@Composable
fun AddRecordContent(
    modifier: Modifier = Modifier,
    onSaveClick: () -> Unit,
    onTimeChanged: (String) -> Unit,
    onDateChanged: (String) -> Unit,
    onSystolicValueChanged: (Int) -> Unit,
    onDiastolicValueChanged: (Int) -> Unit,
    onPulseValueChanged: (Int) -> Unit,
    onSelectedNotesChanged: (Set<String>) -> Unit,
    onAddNoteClick: () -> Unit,
    onEditAgeClick: () -> Unit,
    onEditGenderClick: () -> Unit,
    uiState: AddRecordViewModel.UiState
) {
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
                    BpTypeItem(bpType = uiState.selectedBpType)

                    BpTypeIndicator(
                        bpType = uiState.selectedBpType,
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
                            }
                    ) {
                        SpinnerSelection(
                            modifier = Modifier
                                .wrapContentHeight()
                                .weight(1f),
                            value = uiState.systolic,
                            onValueChanged = onSystolicValueChanged,
                            title = stringResource(id = R.string.cw_systolic),
                            unit = "mmHg",
                            range = 20..300,
                            selectedTextColor = Color(0xFFF95721)
                        )

                        Spacer(modifier = modifier.width(16.dp))

                        SpinnerSelection(
                            modifier = Modifier
                                .wrapContentHeight()
                                .weight(1f),
                            value = uiState.diastolic,
                            onValueChanged = onDiastolicValueChanged,
                            title = stringResource(id = R.string.cw_diastolic),
                            unit = "mmHg",
                            range = 20..300,
                            selectedTextColor = Color(0xFF1892FA)
                        )

                        Spacer(modifier = modifier.width(16.dp))

                        SpinnerSelection(
                            modifier = Modifier
                                .wrapContentHeight()
                                .weight(1f),
                            value = uiState.pulse,
                            onValueChanged = onPulseValueChanged,
                            title = stringResource(id = R.string.cw_pulse),
                            unit = "bpm",
                            range = 20..300,
                            selectedTextColor = Color(0xFF53B69F)
                        )
                    }

                    DateTimeSelection(
                        modifier = Modifier.padding(top = 24.dp),
                        onTimeChanged = onTimeChanged,
                        onDateChanged = onDateChanged,
                        onSelectedNotesChanged = onSelectedNotesChanged,
                        onAddNoteClick = onAddNoteClick,
                        time = uiState.time,
                        date = uiState.date,
                        notes = uiState.notes,
                        title = "Bp Notes"
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
                                stringResource(R.string.age_edit_btn_text, uiState.age), Modifier.padding(end = 4.dp),
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
                                text = stringResource(
                                    R.string.gender_edit_btn_text,
                                    stringResource(id = uiState.genderType.nameRes)
                                ),
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
                        modifier = Modifier
                            .fillMaxWidth(),
                        text = stringResource(id = R.string.cw_save),
                        onClick = onSaveClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }

        if (!uiState.isPurchased) {
            val nativeAd by LocalAdsManager.current.addRecordNativeAd.collectAsStateWithLifecycle()
            if (nativeAd != null) {
                SmallNativeAd(nativeAd = nativeAd!!, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
fun BpTypeItem(modifier: Modifier = Modifier, bpType: BpType) {
    var shouldShowBpTypeInfos by remember { mutableStateOf(false) }
    if (shouldShowBpTypeInfos) {
        BpTypesDialog { shouldShowBpTypeInfos = false }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable { shouldShowBpTypeInfos = true },
        colors = CardDefaults.cardColors(containerColor = bpType.color)
    ) {
        Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(id = bpType.nameRes),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                color = Color.White
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
fun BpTypeIndicator(modifier: Modifier = Modifier, bpType: BpType) {
    val bpTypes by remember { mutableStateOf(BpType.values()) }
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        val text =
            "Sys ${bpType.systolic.first} - ${bpType.systolic.last} or Dia ${bpType.diastolic.first}-${bpType.diastolic.last}"
        Text(text = text)

        Row(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
                .height(6.dp)
        ) {
            bpTypes.forEach {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    if (it == bpType) {
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
                .padding(top = 8.dp)
                .fillMaxWidth()
                .height(8.dp)
        ) {
            bpTypes.map { it.color }.forEachIndexed { index, color ->
                val shape = when (index) {
                    0 -> RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)
                    BpType.values().size - 1 -> RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)
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


@Composable
fun SpinnerSelection(
    modifier: Modifier = Modifier,
    value: Int,
    onValueChanged: (Int) -> Unit,
    title: String,
    unit: String,
    range: IntRange,
    selectedTextColor: Color
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)

        Text(text = unit, style = MaterialTheme.typography.bodySmall)

        Card(
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(width = 1.dp, color = Color(0xFFECEDEF)),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Picker(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                currentValue = value,
                onValueChanged = onValueChanged,
                items = range.toList(),
                selectedTextColor = selectedTextColor
            )
        }
    }
}

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimeSelection(
    modifier: Modifier = Modifier,
    onTimeChanged: (String) -> Unit,
    onDateChanged: (String) -> Unit,
    onAddNoteClick: () -> Unit,
    onSelectedNotesChanged: (Set<String>) -> Unit,
    title: String,
    time: String,
    date: String,
    notes: Set<String>
) {
    val textFormatter = LocalTextFormatter.current
    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(is24Hour = true)

    if (showTimePicker) {
        TimePickerDialog(
            onCancel = { showTimePicker = false },
            onConfirm = {
                val selectedTime =
                    textFormatter.formatTime(timePickerState.hour, timePickerState.minute)
                onTimeChanged(selectedTime)
                showTimePicker = false
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }

    var showDatePicker by remember { mutableStateOf(false) }
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        val confirmEnabled = derivedStateOf { datePickerState.selectedDateMillis != null }
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            onDateChanged(textFormatter.formatDate(it))
                        }
                        showDatePicker = false
                    },
                    enabled = confirmEnabled.value
                ) {
                    Text(text = stringResource(id = R.string.cw_ok))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                    }
                ) {
                    Text(text = stringResource(id = R.string.cw_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    var shouldShowNoteDialog by remember { mutableStateOf(false) }
    if (shouldShowNoteDialog) {
        NoteDialog(
            onDismissRequest = { shouldShowNoteDialog = false },
            onNotesChanged = {
                onSelectedNotesChanged(it)
                shouldShowNoteDialog = false
            },
            onAddNoteClick = {
                shouldShowNoteDialog = false
                onAddNoteClick()
            },
            notes = notes,
            title = title
        )
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(id = R.string.date_time),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                modifier = Modifier.clickable {
                    shouldShowNoteDialog = true
                },
                verticalAlignment = Alignment.CenterVertically
            ) {
                val noteText = if (notes.isEmpty()) {
                    stringResource(id = R.string.add_note)
                } else {
                    stringResource(id = R.string.number_note, notes.size.toString())
                }
                Text(text = noteText)
                Icon(
                    painter = painterResource(id = R.drawable.ic_plus),
                    contentDescription = null,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clickable {
                        showDatePicker = true
                    },
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(width = 1.dp, Color(0xFFECEDEF))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = date,
                        modifier = Modifier
                            .padding(start = 8.dp, top = 10.dp, bottom = 10.dp)
                            .fillMaxWidth()
                            .weight(1f)
                    )

                    Icon(
                        painter = painterResource(id = R.drawable.ic_calendar),
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clickable {
                        showTimePicker = true
                    },
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(width = 1.dp, Color(0xFFECEDEF))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = time,
                        modifier = Modifier
                            .padding(vertical = 10.dp, horizontal = 8.dp)
                            .fillMaxWidth()
                            .weight(1f)
                    )

                    Icon(
                        painter = painterResource(id = R.drawable.ic_clock),
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }
        }
    }
}
