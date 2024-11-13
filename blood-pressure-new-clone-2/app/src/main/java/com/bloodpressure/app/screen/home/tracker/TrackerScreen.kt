package com.bloodpressure.app.screen.home.tracker

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bloodpressure.app.R
import com.bloodpressure.app.ui.component.TopAppBarAction
import com.bloodpressure.app.ui.theme.GrayScale900
import com.bloodpressure.app.utils.LocalAdsManager
import com.bloodpressure.app.utils.Logger
import com.google.android.gms.ads.nativead.NativeAd
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackerScreen(
    modifier: Modifier = Modifier,
    onPremiumClick: () -> Unit,
    onTrackerClick: (TrackerType) -> Unit,
    onAlarmClick: () -> Unit,
    viewModel: TrackerViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(id = R.string.cw_tracker).replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(
                            Locale.US
                        ) else it.toString()
                    },

                    style = TextStyle(
                        fontSize = 20.sp,
                        lineHeight = 30.sp,
                        fontWeight = FontWeight(800),
                        color = GrayScale900,
                    )
                )
            },
            actions = {
                TopAppBarAction(
                    isPurchased = uiState.isPurchased,
                    onSetAlarmClick = onAlarmClick,
                    onNavigateToPremium = onPremiumClick
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            )
        )


        TrackerContent(
            modifier = Modifier
                .weight(1f),
            uiState = uiState,
            onTrackerClick = onTrackerClick
        )

    }
}

@Composable
fun TrackerContent(
    modifier: Modifier = Modifier,
    onTrackerClick: (TrackerType) -> Unit,
    uiState: TrackerViewModel.UiState,
) {
    LazyVerticalGrid(
        modifier = Modifier.padding(top = 16.dp),
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(
            8.dp
        )
    ) {
        item(TrackerType.BLOOD_PRESSURE) {
            BpCard(
                uiState = uiState
            ) {
                onTrackerClick(TrackerType.BLOOD_PRESSURE)
            }
        }

        item(TrackerType.HEART_RATE) {
            HeartRateCard(
                uiState = uiState
            ) {
                onTrackerClick(TrackerType.HEART_RATE)
            }
        }
        items(
            listOf(
                TrackerType.WEIGHT_BMI,
                TrackerType.BLOOD_SUGAR,
                TrackerType.WATER_REMINDER
            )
        ) {
            SimpleTrackerCard(
                trackerType = it,
                onClick = { onTrackerClick(it) }
            )
        }
    }
}

@Composable
fun BpCard(
    modifier: Modifier = Modifier,
    uiState: TrackerViewModel.UiState,
    onClick: (TrackerType) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .aspectRatio(0.80f)
            .clip(shape = RoundedCornerShape(8.dp))
            .clickable { onClick(TrackerType.BLOOD_PRESSURE) },

        contentAlignment = Alignment.Center
    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(id = R.drawable.blood_pressure_card),
            contentDescription = "",
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${if (uiState.lastBpRecord != null) uiState.lastBpRecord.systolic else "--"}",
                        style = TextStyle(
                            fontSize = 18.sp,
                            lineHeight = 28.sp,
                            fontWeight = FontWeight(700),
                            color = Color.White,
                        )
                    )

                    Text(
                        text = "sys",
                        style = TextStyle(
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight(400),
                            color = Color.White,
                        )
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${if (uiState.lastBpRecord != null) uiState.lastBpRecord.diastolic else "--"}",
                        style = TextStyle(
                            fontSize = 18.sp,
                            lineHeight = 28.sp,
                            fontWeight = FontWeight(700),
                            color = Color.White,
                        )
                    )

                    Text(
                        text = "dia",
                        style = TextStyle(
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight(400),
                            color = Color.White,
                        )
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${if (uiState.lastBpRecord != null) uiState.lastBpRecord.pulse else "--"}",
                        style = TextStyle(
                            fontSize = 18.sp,
                            lineHeight = 28.sp,
                            fontWeight = FontWeight(700),
                            color = Color.White,
                        )
                    )

                    Text(
                        text = "pul",
                        style = TextStyle(
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight(400),
                            color = Color.White,
                        )
                    )
                }
            }


            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = stringResource(id = R.string.blood_pressure),
                style = TextStyle(
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight(700),
                    color = Color.White,
                )
            )
        }
    }
}

@Composable
fun HeartRateCard(
    modifier: Modifier = Modifier,
    uiState: TrackerViewModel.UiState,
    onClick: (TrackerType) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .aspectRatio(0.80f)
            .clip(shape = RoundedCornerShape(8.dp))
            .clickable { onClick(TrackerType.HEART_RATE) },

        contentAlignment = Alignment.Center
    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(id = R.drawable.heart_rate_card),
            contentDescription = "",
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "${if (uiState.lastHeartRateRecord != null) uiState.lastHeartRateRecord.heartRate else "--"}",
                    style = TextStyle(
                        fontSize = 18.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight(700),
                        color = Color.White,
                    )
                )

                Text(
                    text = "bpm",
                    style = TextStyle(
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight(400),
                        color = Color.White,
                    )
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = stringResource(id = R.string.heart_rate),
                style = TextStyle(
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight(700),
                    color = Color.White,
                )
            )
        }
    }
}

@Composable
fun SimpleTrackerCard(
    modifier: Modifier = Modifier,
    trackerType: TrackerType,
    onClick: (TrackerType) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .aspectRatio(1.2f)
            .background(color = Color.White, shape = RoundedCornerShape(8.dp))
            .clickable { onClick(trackerType) },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            modifier = Modifier.size(48.dp),
            painter = painterResource(id = trackerType.iconRes),
            contentDescription = "",
            contentScale = ContentScale.Crop
        )

        Text(
            text = stringResource(id = trackerType.titleRes),
            modifier = Modifier.padding(top = 16.dp),
            style = TextStyle(
                fontSize = 15.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight(700),
                color = Color.Black,
            )
        )
    }
}

