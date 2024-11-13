package com.bloodpressure.app.screen.alarm

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bloodpressure.app.R
import com.bloodpressure.app.data.model.AlarmRecord
import com.bloodpressure.app.screen.heartrate.add.SpinnerValueSelection
import com.bloodpressure.app.ui.theme.GrayScale900
import com.bloodpressure.app.utils.WeekDays
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetAlarmDialog(
    alarmType: AlarmType,
    alarmRecord: AlarmRecord? = null,
    onDismissRequest: () -> Unit,
    onSave: (AlarmRecord) -> Unit,
    onDelete: ((AlarmRecord) -> Unit)? = null
) {

    var selectedDays by remember { mutableStateOf(alarmRecord?.days ?: WeekDays.values().toList()) }
    var repeat by remember { mutableStateOf(alarmRecord?.repeat ?: true) }
    var hour by remember { mutableStateOf(alarmRecord?.hour ?: Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) }
    var minute by remember { mutableStateOf(alarmRecord?.minute ?: Calendar.getInstance().get(Calendar.MINUTE)) }
    var soundEnabled by remember { mutableStateOf(alarmRecord?.soundEnabled ?: true) }
    var vibrateEnabled by remember { mutableStateOf(alarmRecord?.vibrateEnabled ?: true) }

    Dialog(onDismissRequest = onDismissRequest, properties = DialogProperties(usePlatformDefaultWidth = false)) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(color = Color.White, shape = RoundedCornerShape(8.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Column(modifier = Modifier.fillMaxWidth()) {

                }

                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(id = R.string.set_alarm), style = TextStyle(
                            fontSize = 18.sp,
                            lineHeight = 28.sp,
                            fontWeight = FontWeight(700),
                            color = GrayScale900,
                            textAlign = TextAlign.Center,
                        )
                    )

                    if (alarmRecord != null) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(shape = CircleShape)
                                .background(color = Color(0xFFFED9CD), shape = CircleShape)
                                .align(Alignment.TopEnd)
                                .clickable {
                                    if (onDelete != null) {
                                        onDelete(alarmRecord)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                modifier = Modifier.size(20.dp),
                                painter = painterResource(id = R.drawable.ic_delete),
                                contentDescription = "",
                                contentScale = ContentScale.Inside
                            )
                        }
                    }
                }

                if (alarmRecord?.reminderTime != null) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(id = alarmRecord.reminderTime.titleRes),
                        style = TextStyle(
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight(700),
                            color = GrayScale900,
                            textAlign = TextAlign.Start
                        )
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {

                    CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
                        Checkbox(checked = repeat, onCheckedChange = { repeat = it })
                    }

                    Text(
                        modifier = Modifier.padding(start = 8.dp), text = stringResource(R.string.repeat), style = TextStyle(
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight(400),
                            color = GrayScale900,
                        )
                    )
                }

                DayOfWeekPicker(modifier = Modifier.fillMaxWidth(), selectedDays = selectedDays, onDaysSelected = { selectedDays = it })

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    SpinnerValueSelection(
                        modifier = Modifier
                            .width(88.dp),
                        value = hour,
                        onValueChanged = { hour = it },
                        range = 0..23,
                        selectedTextColor = Color(0xFF1892FA)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    SpinnerValueSelection(
                        modifier = Modifier.width(88.dp),
                        value = minute,
                        onValueChanged = { minute = it },
                        range = 0..59,
                        selectedTextColor = Color(0xFF1892FA)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(30.dp)
                ) {

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                        Text(
                            text = stringResource(R.string.sound),
                            style = TextStyle(
                                fontSize = 15.sp,
                                lineHeight = 22.sp,
                                fontWeight = FontWeight(700),
                                color = GrayScale900,
                                textAlign = TextAlign.Center,
                            )
                        )

                        CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
                            Switch(checked = soundEnabled, onCheckedChange = { soundEnabled = it })
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                        Text(
                            text = stringResource(R.string.vibrate),
                            style = TextStyle(
                                fontSize = 15.sp,
                                lineHeight = 22.sp,
                                fontWeight = FontWeight(700),
                                color = GrayScale900,
                                textAlign = TextAlign.Center,
                            )
                        )

                        CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
                            Switch(checked = vibrateEnabled, onCheckedChange = { vibrateEnabled = it })
                        }
                    }

                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    TextButton(
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                width = 1.dp,
                                color = Color(0xFF1892FA),
                                shape = RoundedCornerShape(size = 8.dp)
                            ),
                        onClick = onDismissRequest
                    ) {
                        Text(
                            text = stringResource(id = R.string.cancel), style = TextStyle(
                                fontSize = 15.sp,
                                lineHeight = 22.sp,
                                fontWeight = FontWeight(700),
                                color = Color(0xFF1892FA),
                                textAlign = TextAlign.Center,
                            )
                        )
                    }

                    TextButton(modifier = Modifier
                        .weight(1f)
                        .background(
                            color = Color(0xFF1892FA), shape = RoundedCornerShape(size = 8.dp)
                        ),
                        onClick = {

                            val newAlarmRecord = AlarmRecord(
                                repeat = repeat,
                                days = selectedDays.toList(),
                                hour = hour,
                                minute = minute,
                                soundEnabled = soundEnabled,
                                vibrateEnabled = vibrateEnabled,
                                type = alarmType,
                                createdAt = alarmRecord?.createdAt ?: System.currentTimeMillis()
                            )

                            onSave(newAlarmRecord)

                        }) {
                        Text(
                            text = stringResource(id = R.string.save), style = TextStyle(
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

    }
}

@Composable
fun DeleteConfirmDialog(
    onDismissRequest: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest, properties = DialogProperties(usePlatformDefaultWidth = false)) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(color = Color.White, shape = RoundedCornerShape(8.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.delete_confirm),
                    style = TextStyle(
                        fontSize = 18.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight(700),
                        color = GrayScale900,
                        textAlign = TextAlign.Center,
                    )
                )

                Text(
                    text = stringResource(R.string.are_you_sure_delete_this),
                    style = TextStyle(
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight(400),
                        color = GrayScale900,
                        textAlign = TextAlign.Center,
                    )
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    TextButton(
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                width = 1.dp,
                                color = Color(0xFF8C8E97),
                                shape = RoundedCornerShape(size = 8.dp)
                            ),
                        onClick = onCancel
                    ) {
                        Text(
                            text = stringResource(id = R.string.cancel),
                            style = TextStyle(
                                fontSize = 15.sp,
                                lineHeight = 22.sp,
                                fontWeight = FontWeight(700),
                                color = Color(0xFF8C8E97),
                                textAlign = TextAlign.Center,
                            )
                        )
                    }

                    TextButton(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                color = Color(0xFFF95721), shape = RoundedCornerShape(size = 8.dp)
                            ),
                        onClick = onDelete
                    ) {
                        Text(
                            text = stringResource(id = R.string.delete),
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

    }
}