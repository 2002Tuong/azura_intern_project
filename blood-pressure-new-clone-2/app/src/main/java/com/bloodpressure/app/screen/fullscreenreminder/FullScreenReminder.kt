package com.bloodpressure.app.screen.fullscreenreminder

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bloodpressure.app.R
import com.bloodpressure.app.ui.theme.Blue800
import com.bloodpressure.app.ui.theme.Blue900
import com.bloodpressure.app.ui.theme.BlueFB
import com.bloodpressure.app.ui.theme.Green800
import com.bloodpressure.app.utils.getActivity
import com.bloodpressure.app.utils.getTimeFormattedString
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import java.util.Calendar


@Composable
fun ReminderContent(
    modifier: Modifier = Modifier,
    type: Int,
    viewModel: FullScreenReminderViewModel = koinViewModel(),
    openBloodPressure: () -> Unit,
    openHeartRate: () -> Unit,
    openBloodSugar: () -> Unit,
    openApp: () -> Unit,
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val calendar = Calendar.getInstance()
    var currentTimeMillis by remember {
        mutableStateOf(System.currentTimeMillis())
    }

    LaunchedEffect(key1 = currentTimeMillis) {
        while (true) {
            delay(1000L)
            currentTimeMillis = System.currentTimeMillis()
            calendar.timeInMillis = currentTimeMillis
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .padding(top = 72.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = calendar.getTimeFormattedString("HH:mm"),
                style = TextStyle(
                    fontSize = 72.sp,
                    fontWeight = FontWeight(700),
                    color = Color(0xFFFFFFFF),
                    textAlign = TextAlign.Center,
                ),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                text = calendar.getTimeFormattedString("EEEE dd MMMM"),
                modifier = Modifier.align(Alignment.CenterHorizontally),
                style = TextStyle(
                    fontSize = 20.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight(600),
                    color = Color(0xFFFFFFFF),
                    textAlign = TextAlign.Center,
                )
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .align(Alignment.BottomCenter),
            verticalArrangement = Arrangement.Bottom

        ) {
            FilledIconButton(
                onClick = {
                    context.getActivity()?.finish()
                },
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(bottom = 4.dp)
                    .size(24.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color.Gray,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }

            Card(
                modifier = modifier.clickable {
                    openApp()
                },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(Color.White)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .background(color = BlueFB),
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 6.dp)
                                .size(24.dp)
                                .align(Alignment.CenterVertically)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF2935DD),
                                            Color(0xFF38ACFA)
                                        )
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )
                        ) {
                            Image(
                                modifier = Modifier
                                    .wrapContentSize()
                                    .padding(2.dp),
                                painter = painterResource(id = R.drawable.ic_blood_pressure_widget),
                                contentDescription = null
                            )
                        }


                        Text(
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .weight(1f, fill = true)
                                .padding(vertical = 12.dp),
                            text = stringResource(id = R.string.blood_pressure),
                            color = Color.White,
                            textAlign = TextAlign.Start,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .padding(end = 16.dp)
                                .wrapContentWidth(),
                            text = Calendar.getInstance()
                                .getTimeFormattedString("EEE dd MMM, yyyy"),
                            color = Color.White,
                            textAlign = TextAlign.End,
                            fontSize = 14.sp
                        )

                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_right),
                            contentDescription = null,
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .padding(end = 16.dp)
                                .wrapContentSize(),
                            tint = Color.White
                        )
                    }

                    Box(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        if (type == FULLSCREEN_REMINDER_TYPE_1) {
                            if (uiState.bpRecord == null && uiState.hrRecord == null)
                                Reminder1NoData(
                                    openBloodPressure = openBloodPressure,
                                    openHeartRate = openHeartRate,
                                    openBloodSugar = openBloodSugar
                                )
                            else
                                Reminder1WithData(
                                    uiState = uiState,
                                    openBloodPressure = openBloodPressure,
                                    openHeartRate = openHeartRate,
                                    openBloodSugar = openBloodSugar
                                )
                        } else {
                            if (uiState.bpRecord == null && uiState.hrRecord == null)
                                Reminder2NoData(
                                    openBloodPressure = openBloodPressure,
                                    openHeartRate = openHeartRate,
                                    openBloodSugar = openBloodSugar,
                                )
                            else
                                Reminder2WithData(uiState, openBloodPressure, openHeartRate)
                        }

                    }

                }
            }

        }
    }

}


@Composable
fun HorizontalButton(
    modifier: Modifier,
    icon: Int,
    text: Int,
    backgroundColor: Color = Color.White,
    textColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(8.dp)
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .wrapContentSize(),
                tint = Color.Unspecified
            )

            Text(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .weight(1f, fill = true),
                text = stringResource(id = text),
                textAlign = TextAlign.Center,
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun VerticalButton(
    modifier: Modifier,
    icon: Int,
    text: Int,
    backgroundColor: Color,
    textColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Image(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(28.dp),
                painter = painterResource(id = icon),
                contentDescription = null
            )
            Text(
                modifier = Modifier.padding(top = 4.dp),
                text = stringResource(id = text),
                color = textColor,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                textAlign = TextAlign.Center,
                fontSize = 14.sp
            )

        }
    }

}

@Composable
fun RecordButton(
    modifier: Modifier,
    onClick: () -> Unit,
    border: Dp = 8.dp
) {
    Button(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(border),
        colors = ButtonDefaults.buttonColors(
            containerColor = Blue800
        )
    ) {
        Row {
            Icon(imageVector = Icons.Outlined.Add, contentDescription = null)

            Text(
                modifier = Modifier.align(Alignment.CenterVertically),
                text = stringResource(id = R.string.record),
                textAlign = TextAlign.Center,
                fontSize = 18.sp
            )
        }

    }
}

@Composable
fun Reminder1NoData(
    modifier: Modifier = Modifier,
    openBloodPressure: () -> Unit,
    openHeartRate: () -> Unit,
    openBloodSugar: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                modifier = Modifier.size(width = 104.dp, height = 60.dp),
                painter = painterResource(id = R.drawable.img_blood_sugar_article),
                contentDescription = null,
                contentScale = ContentScale.FillBounds
            )

            Text(
                modifier = Modifier.padding(horizontal = 8.dp),
                text = stringResource(id = R.string.blood_sugar_article_title),
                maxLines = 2,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max)
                .padding(top = 12.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            VerticalButton(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable {
                        openBloodPressure()
                    },
                R.drawable.ic_blood_pressure_widget,
                R.string.blood_pressure,
                Blue900,
                Color(0xFF1892FA)
            )
            VerticalButton(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable {
                        openHeartRate()
                    },
                R.drawable.ic_heart_rate,
                R.string.heart_rate,
                Color(0xFFFEECE6),
                Color(0xFFF95721)
            )
            VerticalButton(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable {
                        openBloodSugar()
                    },
                R.drawable.ic_blood_sugar,
                R.string.blood_sugar,
                Color(0x1A8BC34A),
                Green800
            )
        }
    }
}

@Preview
@Composable
fun PreviewReminder1WithData(modifier: Modifier = Modifier) {
    Reminder1WithData(uiState = FullScreenReminderViewModel.UiState(),
        openBloodPressure = { /*TODO*/ }, openHeartRate = {}, openBloodSugar = {})
}

@Preview
@Composable
fun PreviewReminder2WithData(modifier: Modifier = Modifier) {
    Reminder2WithData(uiState = FullScreenReminderViewModel.UiState(),
        openBloodPressure = { /*TODO*/ }, openHeartRate = {})
}

@Composable
fun Reminder1WithData(
    modifier: Modifier = Modifier,
    uiState: FullScreenReminderViewModel.UiState,
    openBloodPressure: () -> Unit,
    openHeartRate: () -> Unit,
    openBloodSugar: () -> Unit,
) {
    Box(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max)
                .padding(top = 12.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(26.dp)
        ) {
            Column(
                modifier = Modifier
                    .clickable { openBloodPressure() }
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        color = Blue900,
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                HorizontalButton(
                    modifier = Modifier,
                    icon = R.drawable.ic_blood_pressure_widget,
                    text = R.string.blood_pressure,
                    Blue900,
                    Color(0xFF1892FA)
                )

                Divider(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    color = Color(0xFFB4DCFE)
                )

                if (uiState.bpRecord == null) {
                    EmptyDataCard(
                        modifier = Modifier.clickable {
                            openBloodPressure()
                        },
                        text = stringResource(id = R.string.common_add_at_least_1_record),
                        color = Color(0xFF1892FA),
                        onClick = { openBloodPressure() }
                    )
                } else {
                    InformationCard(
                        Modifier
                            .background(
                                color = Blue900,
                                shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                            )
                            .fillMaxWidth()
                            .clickable {
                                openBloodPressure()
                            }
                            .padding(8.dp),
                        Calendar.getInstance().apply {
                            timeInMillis = uiState.bpRecord.createdAt
                        }.getTimeFormattedString("MMM hh:mm a"),
                        uiState.bpRecord.diastolic.toString(),
                        uiState.bpRecord.typeName,
                        Color(0xFF1892FA)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .clickable { openHeartRate() }
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        color = Color(0xFFFEECE6),
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                HorizontalButton(
                    modifier = Modifier,
                    icon = R.drawable.ic_heart_rate,
                    text = R.string.heart_rate,
                    Color(0xFFFEECE6),
                    Color(0xFFF95721)
                )

                Divider(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    color = Color(0xFFFDDDD3)
                )

                if (uiState.hrRecord == null)
                    EmptyDataCard(
                        modifier = Modifier.clickable {
                            openHeartRate()
                        },
                        text = stringResource(id = R.string.common_add_at_least_1_record),
                        color = Color(0xFFF95721),
                        onClick = { openHeartRate() }
                    )
                else
                    InformationCard(
                        Modifier
                            .background(
                                color = Color(0xFFFEECE6),
                                shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                            )
                            .fillMaxWidth()
                            .clickable {
                                openHeartRate()
                            }
                            .padding(8.dp),
                        Calendar.getInstance().apply {
                            timeInMillis = uiState.hrRecord.createdAt
                        }.getTimeFormattedString("MMM hh:mm a"),
                        uiState.hrRecord.heartRate.toString() + " BPM",
                        uiState.hrRecord.typeName,
                        Color(0xFFF95721)
                    )
            }
        }
    }
}

@Composable
fun EmptyDataCard(modifier: Modifier = Modifier, text: String, color: Color, onClick: () -> Unit) {
    Column(
        modifier = modifier.padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier,
            text = text,
            color = color,
            fontWeight = FontWeight(400),
            textAlign = TextAlign.Start,
            fontSize = 12.sp,
            lineHeight = 16.sp,
        )

        Button(
            modifier = Modifier.defaultMinSize(40.dp, 16.dp),
            onClick = {
                onClick()
            },
            colors = buttonColors(containerColor = color),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Text(text = stringResource(id = R.string.record))
            Icon(
                painter = painterResource(id = R.drawable.ic_plus),
                contentDescription = ""
            )
        }
    }
}

@Composable
fun Reminder2NoData(
    modifier: Modifier = Modifier,
    openBloodPressure: () -> Unit,
    openHeartRate: () -> Unit,
    openBloodSugar: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        Text(
            modifier = Modifier.padding(8.dp),
            text = stringResource(id = R.string.fullscreen_reminder_capture_first_record),
            style = TextStyle(
                fontSize = 15.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight(700),
                color = Color(0xFF191D30),
            )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color(0xFFE8F4FE),
                    shape = RoundedCornerShape(size = 16.dp)
                )
                .clickable {
                    openBloodPressure()
                }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Image(
                    modifier = Modifier
                        .size(28.dp),
                    painter = painterResource(id = R.drawable.ic_blood_pressure),
                    contentDescription = null
                )
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp),
                    text = stringResource(id = R.string.blood_pressure),
                    color = Color(0xFF1892FA),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    textAlign = TextAlign.Start,
                    fontSize = 14.sp
                )

                Button(
                    modifier = Modifier.defaultMinSize(40.dp, 16.dp),
                    onClick = { openBloodPressure() },
                    colors = buttonColors(containerColor = Color(0xFF1892FA)),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(text = stringResource(id = R.string.record))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_plus),
                        contentDescription = ""
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .fillMaxWidth()
                .background(
                    color = Color(0xFFFEECE6),
                    shape = RoundedCornerShape(size = 16.dp)
                )
                .clickable {
                    openHeartRate()
                }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Image(
                    modifier = Modifier
                        .size(28.dp),
                    painter = painterResource(id = R.drawable.ic_heart_rate),
                    contentDescription = null
                )
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp),
                    text = stringResource(id = R.string.heart_rate),
                    color = Color(0xFFF95721),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    textAlign = TextAlign.Start,
                    fontSize = 14.sp
                )

                Button(
                    modifier = Modifier.defaultMinSize(40.dp, 16.dp),
                    onClick = { openHeartRate() },
                    colors = buttonColors(containerColor = Color(0xFFF95721)),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(text = stringResource(id = R.string.record))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_plus),
                        contentDescription = ""
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color(0xFFE8F4FE),
                    shape = RoundedCornerShape(size = 16.dp)
                )
                .clickable {
                    openBloodSugar()
                }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Image(
                    modifier = Modifier
                        .size(28.dp),
                    painter = painterResource(id = R.drawable.ic_blood_sugar),
                    contentDescription = null
                )
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp),
                    text = stringResource(id = R.string.blood_sugar),
                    color = Color(0xFFF6C504),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    textAlign = TextAlign.Start,
                    fontSize = 14.sp
                )

                Button(
                    modifier = Modifier.defaultMinSize(40.dp, 16.dp),
                    onClick = { openBloodPressure() },
                    colors = buttonColors(containerColor = Color(0xFFF6C504)),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(text = stringResource(id = R.string.record))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_plus),
                        contentDescription = ""
                    )
                }
            }
        }

    }
}


@Composable
fun Reminder2WithData(
    uiState: FullScreenReminderViewModel.UiState,
    openBloodPressure: () -> Unit,
    openHeartRate: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            if (uiState.bpRecord != null) {
                InformationCard(
                    Modifier
                        .background(
                            color = Blue900,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .weight(1f)
                        .clickable {
                            openBloodPressure()
                        }
                        .padding(8.dp),
                    text1 = Calendar.getInstance().apply {
                        timeInMillis = uiState.bpRecord.createdAt
                    }.getTimeFormattedString("MMM hh:mm a"),
                    text2 = String.format(
                        "%d/%d mmHg",
                        uiState.bpRecord.systolic,
                        uiState.bpRecord.diastolic
                    ),
                    textColor = Color(0xFF1892FA)
                )
            } else {
                EmptyDataCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            openBloodPressure()
                        },
                    text = stringResource(id = R.string.common_add_at_least_1_record),
                    color = Color(0xFF1892FA),
                    onClick = { openBloodPressure() }
                )
            }

            Spacer(modifier = Modifier.size(8.dp))
            if (uiState.hrRecord != null) {
                InformationCard(
                    Modifier
                        .background(
                            color = Color(0xFFFEECE6),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .weight(1f)
                        .clickable {
                            openHeartRate()
                        }
                        .padding(8.dp),
                    text1 = Calendar.getInstance().apply {
                        timeInMillis = uiState.hrRecord.createdAt
                    }.getTimeFormattedString("MMM hh:mm a"),
                    text2 = uiState.hrRecord.heartRate.toString() + " BPM",
                    textColor = Color(0xFFF95721)
                )
            } else {
                EmptyDataCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            openHeartRate()
                        },
                    text = stringResource(id = R.string.common_add_at_least_1_record),
                    color = Color(0xFFF95721),
                    onClick = { openHeartRate() }
                )
            }
        }

        Text(
            modifier = Modifier.padding(vertical = 16.dp),
            text = stringResource(id = R.string.fullscreen_reminder_time_to_record_health),
            style = TextStyle(
                fontSize = 15.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight(700),
                color = Color(0xFF191D30),
            )
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { openBloodPressure() },
            colors = buttonColors(
                containerColor = Color(0xFFECEDEF),
                contentColor = Color(0xFF191D30)
            )
        ) {
            Icon(painter = painterResource(id = R.drawable.ic_clock), contentDescription = "")
            Text(
                text = "5:00 PM",
                modifier = Modifier.padding(horizontal = 4.dp),
                fontWeight = FontWeight(700)
            )
            Icon(painter = painterResource(id = R.drawable.ic_plus), contentDescription = "")
        }

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            onClick = { openBloodPressure() },
            colors = buttonColors(
                containerColor = Color(0xFF1892FA),
                contentColor = Color.White
            )
        ) {
            Text(
                text = stringResource(id = R.string.record),
                modifier = Modifier.padding(horizontal = 4.dp),
            )
            Icon(painter = painterResource(id = R.drawable.ic_plus), contentDescription = "")
        }

    }
}

@Composable
fun InformationCard(
    modifier: Modifier = Modifier,
    text1: String,
    text2: String,
    text3: String = "",
    textColor: Color
) {
    Column(
        modifier = modifier
    ) {
        Text(text = text1, color = textColor, fontSize = 12.sp)

        Text(
            modifier = Modifier.padding(top = 4.dp),
            text = text2,
            color = textColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        if (text3.isNotEmpty())
            Text(text = text3, color = textColor, fontSize = 12.sp)
    }
}

const val FULLSCREEN_REMINDER_TYPE_1 = 1
const val FULLSCREEN_REMINDER_TYPE_2 = 2