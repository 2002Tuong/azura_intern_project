package com.bloodpressure.app.screen.bmi.history

import android.view.ViewGroup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bloodpressure.app.R
import com.bloodpressure.app.ads.LargeNativeAd
import com.bloodpressure.app.data.model.BMIRecord
import com.bloodpressure.app.screen.bmi.add.BMIType
import com.bloodpressure.app.screen.bmi.historyandstatistics.HeightUnitPickerDialog
import com.bloodpressure.app.screen.bmi.historyandstatistics.WeightUnitPickerDialog
import com.bloodpressure.app.screen.heartrate.add.GenderType
import com.bloodpressure.app.screen.heartrate.detail.DateRangeSelection
import com.bloodpressure.app.screen.heartrate.history.EmptyHeartRateRecords
import com.bloodpressure.app.ui.theme.BloodPressureAndroidTheme
import com.bloodpressure.app.ui.theme.GrayScale600
import com.bloodpressure.app.ui.theme.GrayScale700
import com.bloodpressure.app.ui.theme.GrayScale900
import com.bloodpressure.app.utils.HeightUnit
import com.bloodpressure.app.utils.LocalAdsManager
import com.bloodpressure.app.utils.LocalRemoteConfig
import com.bloodpressure.app.utils.WeightUnit
import com.bloodpressure.app.utils.navigationBarsPaddingIfNeed
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BMIHistoryScreen(
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit,
    onItemCLick:(Long) -> Unit,
    viewModel: BMIHistoryViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val adView by LocalAdsManager.current.homeBannerAd.collectAsStateWithLifecycle()
    val shouldShowNative = LocalRemoteConfig.current.adsConfig.shouldShowHistoryNativeAd
    val nativeAd by LocalAdsManager.current.historyNativeAd.collectAsStateWithLifecycle()

    if(uiState.showWeightUnitDialog) {
        WeightUnitPickerDialog(
            weightUnit = uiState.weightUnit,
            onValueSaved = {
                viewModel.setWeightUnit(it)
                viewModel.showWeightUnitDialog(false)
            },
            onDismissRequest = { viewModel.showWeightUnitDialog(false)})
    }

    if(uiState.showHeightUnitDialog) {
        HeightUnitPickerDialog(
            heightUnit = uiState.heightUnit,
            onValueSaved = {
                viewModel.setHeightUnit(it)
                viewModel.showHeightUnitDialog(false)
            },
            onDismissRequest = { viewModel.showHeightUnitDialog(false) })
    }

    Column(modifier = modifier.fillMaxSize().navigationBarsPaddingIfNeed()) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(id = R.string.weight_bmi_history),
                    style = TextStyle(
                        fontSize = 18.sp,
                        lineHeight = 28.sp,
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
                .background(Color(0xFFECEDEF))
        )

        DateRangeSelection(
            onDateRangeChanged = { startDate, endDate ->
                viewModel.setFilteredRecords(
                    startDate, endDate
                )
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        ) {
            Card(
                modifier = Modifier
                    .height(40.dp)
                    .weight(3f),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(width = 1.dp, color = GrayScale600),
                onClick = { viewModel.showWeightUnitDialog(true) }
            ) {
                Row(
                    modifier = Modifier.padding(10.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        stringResource(R.string.weight_unit_select_text, uiState.weightUnit.value),
                        style = TextStyle(
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight(400),
                            color = Color.Black,
                        )
                    )

                    Icon(
                        painter = painterResource(id = R.drawable.ic_edit),
                        contentDescription = ""
                    )
                }
            }


            Spacer(
                modifier = Modifier
                    .weight(1f)
                    .background(color = GrayScale700)
            )

            Card(
                modifier = Modifier
                    .height(40.dp)
                    .weight(3f)
                    .wrapContentWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(width = 1.dp, color = GrayScale600),
                onClick = { viewModel.showHeightUnitDialog(true) }
            ) {
                Row(
                    modifier = Modifier.padding(10.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(
                            R.string.height_unit_select_text,
                            uiState.heightUnit.value
                        ),
                        style = TextStyle(
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight(400),
                            color = Color.Black,
                        )
                    )

                    Icon(
                        painter = painterResource(id = R.drawable.ic_edit),
                        contentDescription = ""
                    )
                }
            }
        }

        BMIHistoryContent(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            records = uiState.filteredRecords,
            onItemClick = onItemCLick,
            weightUnit = uiState.weightUnit,
            heightUnit = uiState.heightUnit
        )

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
fun BMIHistoryContent(
    modifier: Modifier = Modifier,
    records: Map<String, List<BMIRecord>>,
    onItemClick: (Long) -> Unit,
    weightUnit: WeightUnit,
    heightUnit: HeightUnit
) {
    if(records.isEmpty()) {
        EmptyHeartRateRecords(modifier = modifier)
    }else {
        BMIRecordsList(
            modifier = modifier,
            records = records,
            onItemClick = onItemClick,
            weightUnit = weightUnit,
            heightUnit = heightUnit
        )
    }
}
