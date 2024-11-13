package com.bloodpressure.app.screen.waterreminder.history

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bloodpressure.app.R
import com.bloodpressure.app.ads.LargeNativeAd
import com.bloodpressure.app.data.model.WaterCupRecord
import com.bloodpressure.app.screen.heartrate.detail.DateRangeSelection
import com.bloodpressure.app.screen.heartrate.history.EmptyHeartRateRecords
import com.bloodpressure.app.ui.component.TopAppBarAction
import com.bloodpressure.app.ui.theme.GrayScale800
import com.bloodpressure.app.ui.theme.GrayScale900
import com.bloodpressure.app.ui.theme.Green800
import com.bloodpressure.app.utils.LocalAdsManager
import com.bloodpressure.app.utils.LocalRemoteConfig
import com.bloodpressure.app.utils.navigationBarsPaddingIfNeed
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterHistoryScreen(
    onNavigateUp: () -> Unit,
    onViewAllHistory: () -> Unit,
    onNavigateToPremium: () -> Unit,
    onReminderItemClick: () -> Unit,
    viewModel: WaterHistoryViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val adView by LocalAdsManager.current.homeBannerAd.collectAsStateWithLifecycle()
    val shouldShowNative = LocalRemoteConfig.current.adsConfig.shouldShowHistoryNativeAd
    val nativeAd by LocalAdsManager.current.historyNativeAd.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPaddingIfNeed()
    ) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(id = R.string.statistics),
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight(800),
                        color = GrayScale900,
                    )
                )
            },
            navigationIcon = {
                IconButton(onClick = {
                    onNavigateUp()
                }) {
                    Icon(
                        painter = rememberVectorPainter(image = Icons.Default.ArrowBack),
                        contentDescription = null
                    )
                }
            },
            actions = {
                TopAppBarAction(
                    isPurchased = uiState.isPurchased,
                    onSetAlarmClick = onReminderItemClick,
                    onNavigateToPremium = onNavigateToPremium
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            ),
        )

        Spacer(
            modifier = Modifier
                .height(1.dp)
                .fillMaxWidth()
                .background(Color(0xFFECEDEF))
        )

        DateRangeSelection(
            onDateRangeChanged = { startDate, endDate ->
                viewModel.setFilteredRecords(
                    startDate, endDate
                )
            }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(16.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            WaterHistoryContent(
                uiState.isMl,
                uiState.waterToday,
                uiState.filteredRecords,
                uiState.goal,
                onViewAllHistory = onViewAllHistory
            )
        }

        if (!uiState.isPurchased) {
            if (shouldShowNative) {
                if (nativeAd != null) {
                    LargeNativeAd(nativeAd = nativeAd!!, modifier = Modifier.fillMaxWidth())
                }
            } else {
                if (adView != null) {
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
}

@Composable
fun WaterHistoryContent(
    isMl: Boolean,
    waterToday: Int,
    records: List<WaterCupRecord>,
    goal: Int,
    onViewAllHistory: (() -> Unit)? = null,
    isFull: Boolean = false
) {

    Column(
        modifier = Modifier
            .wrapContentHeight()
            .verticalScroll(rememberScrollState())
    ) {

        if (records.isEmpty()) {
            EmptyHeartRateRecords()
        } else {
            Card(shape = RoundedCornerShape(8.dp)) {
                Column(
                    modifier = Modifier
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp),
                ) {
                    Text(
                        text = stringResource(id = R.string.statistics),
                        style = TextStyle(
                            fontSize = 18.sp,
                            lineHeight = 28.sp,
                            fontWeight = FontWeight(700),
                            color = GrayScale900,
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Today",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    fontWeight = FontWeight(400),
                                    color = GrayScale800,
                                )
                            )
                            Text(
                                text = "$waterToday ${if (isMl) "ml" else "oz"}",
                                style = TextStyle(
                                    fontSize = 24.sp,
                                    lineHeight = 32.sp,
                                    fontWeight = FontWeight(700),
                                    color = Color.Blue,
                                )
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Goal",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    fontWeight = FontWeight(400),
                                    color = GrayScale800,
                                )
                            )
                            Text(
                                text = "$goal ${if (isMl) "ml" else "oz"}",
                                style = TextStyle(
                                    fontSize = 24.sp,
                                    lineHeight = 32.sp,
                                    fontWeight = FontWeight(700),
                                    color = Green800,
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFF4F4F5),
                        thickness = 1.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    WaterStatisticChart(
                        chartData = records,
                        goal = goal,
                        isMl = isMl
                    )

                    WaterRecordList(
                        isMl = isMl,
                        records = records,
                        onViewAllHistory = onViewAllHistory,
                        isFull = isFull
                    )
                }
            }
        }

    }

}