package com.bloodpressure.app.screen.bloodsugar.history

import android.view.ViewGroup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import com.bloodpressure.app.R
import com.bloodpressure.app.ads.LargeNativeAd
import com.bloodpressure.app.screen.alarm.AlarmType
import com.bloodpressure.app.screen.alarm.SetAlarmDialog
import com.bloodpressure.app.screen.bloodsugar.add.SelectState
import com.bloodpressure.app.screen.bloodsugar.type.ALL_TYPE
import com.bloodpressure.app.screen.bloodsugar.type.BloodSugarStateType
import com.bloodpressure.app.screen.heartrate.detail.DateRangeSelection
import com.bloodpressure.app.ui.component.TopAppBarAction
import com.bloodpressure.app.ui.theme.GrayScale600
import com.bloodpressure.app.ui.theme.GrayScale900
import com.bloodpressure.app.utils.BloodSugarUnit
import com.bloodpressure.app.utils.LocalAdsManager
import com.bloodpressure.app.utils.LocalRemoteConfig
import com.bloodpressure.app.utils.navigationBarsPaddingIfNeed
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BloodSugarHistoryScreen(
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit,
    onNavigateToRecordDetail: (Long) -> Unit,
    onNavigateToPremium: () -> Unit,
    viewModel: BloodSugarStatisticViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showMenu by remember { mutableStateOf(false) }
    var bloodSugarUnit by remember { mutableStateOf(uiState.bloodSugarUnit) }
    val shouldShowNative = LocalRemoteConfig.current.adsConfig.shouldShowHistoryNativeAd
    val nativeAd by LocalAdsManager.current.historyNativeAd.collectAsStateWithLifecycle()
    val adView by LocalAdsManager.current.homeBannerAd.collectAsStateWithLifecycle()

    if (uiState.showSetAlarmDialog) {
        SetAlarmDialog(
            alarmType = AlarmType.BLOOD_SUGAR,
            onDismissRequest = { viewModel.showSetReminder(false) },
            onSave = { alarmRecord ->
                viewModel.insertRecord(alarmRecord)
                viewModel.showSetReminder(false)
            }
        )
    }

    Column(modifier = modifier.fillMaxSize().navigationBarsPaddingIfNeed()) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(id = R.string.history),
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
            actions = {
                TopAppBarAction(
                    isPurchased = uiState.isPurchased,
                    onSetAlarmClick = { viewModel.showSetReminder(true) },
                    onNavigateToPremium = onNavigateToPremium
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
            }
        )

        Spacer(
            modifier = Modifier
                .height(1.dp)
                .fillMaxWidth()
                .background(Color(0xFFECEDEF))
        )

        DateRangeSelection(
            modifier = Modifier.height(40.dp),
            onDateRangeChanged = { startDate, endDate ->
                viewModel.updateDateFilter(startDate, endDate)
                viewModel.setFilteredBloodSugarRecords(bloodSugarUnit)
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(Color.White)
                .padding(start = 16.dp, end = 16.dp, bottom = 4.dp),
        ) {
            val itemList = listOf(ALL_TYPE) + BloodSugarStateType.values().toList()
            var stateSelected by remember { mutableStateOf(itemList[0]) }
            SelectState(
                modifier = Modifier
                    .weight(1f),
                onSaveState = { stateType ->
                    stateSelected = stateType
                    viewModel.updateUnitFilter(stateType)
                    viewModel.setFilteredBloodSugarRecords(bloodSugarUnit)
                },
                state = stateSelected,
                itemList = itemList
            )

            Spacer(modifier = Modifier.width(8.dp))

            ExposedDropdownMenuBox(
                modifier = Modifier
                    .weight(1f),
                expanded = showMenu,
                onExpandedChange = { showMenu = it },
            ) {
                TextField(
                    value = bloodSugarUnit.value,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = showMenu)
                    },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .menuAnchor()
                        .background(Color.White)
                        .border(
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(width = 1.dp, color = GrayScale600)
                        )
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = showMenu,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White),
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        onClick = {
                            bloodSugarUnit = BloodSugarUnit.MILLIMOLES_PER_LITRE
                            viewModel.setBloodSugarUnit(bloodSugarUnit)
                            showMenu = false
                        },
                        text = {
                            Text(text = BloodSugarUnit.MILLIMOLES_PER_LITRE.value)
                        }
                    )
                    DropdownMenuItem(
                        onClick = {
                            bloodSugarUnit = BloodSugarUnit.MILLIGRAMS_PER_DECILITER
                            viewModel.setBloodSugarUnit(bloodSugarUnit)
                            showMenu = false
                        },
                        text = {
                            Text(text = BloodSugarUnit.MILLIGRAMS_PER_DECILITER.value)
                        }
                    )
                }
            }
        }

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .weight(1f)
                .padding(8.dp)
                .background(color = Color.White, shape = RoundedCornerShape(8.dp)),
            content = {

                items(uiState.filteredRecords) {
                    BloodSugarRecordItem(
                        record = it,
                        onClick = onNavigateToRecordDetail,
                        bloodSugarUnit = bloodSugarUnit
                    )
                }

            }
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
