package com.bloodpressure.app.screen.bloodpressure

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bloodpressure.app.R
import com.bloodpressure.app.ads.CardItemNativeAd
import com.bloodpressure.app.data.model.BloodPressureOption
import com.bloodpressure.app.screen.alarm.AlarmType
import com.bloodpressure.app.screen.alarm.SetAlarmDialog
import com.bloodpressure.app.ui.component.AskSetAlarmDialog
import com.bloodpressure.app.ui.component.TopAppBarAction
import com.bloodpressure.app.ui.theme.Blue800
import com.bloodpressure.app.ui.theme.GrayScale900
import com.bloodpressure.app.utils.LocalAdsManager
import com.bloodpressure.app.utils.navigationBarsPaddingIfNeed
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BloodPressureFeatureScreen(
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit = {},
    onItemOptionClick: (BloodPressureOption) -> Unit,
    onNavigateToPremium: () -> Unit,
    viewModel: BloodPressureFeatureViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val adsManager = LocalAdsManager.current
    LaunchedEffect(true) {
        adsManager.loadAddRecordNativeAd()
    }
    if (uiState.showAskSetAlarmDialog) {
        AskSetAlarmDialog(
            onDismissRequest = { viewModel.showSetReminder(false) },
            onCancel = {
                viewModel.showAskSetAlarmDialog(false)
                onNavigateUp()
            },
            onAgreeSetAlarm = {
                viewModel.showSetReminder(true)
                viewModel.showAskSetAlarmDialog(false)
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPaddingIfNeed()
    ) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(id = R.string.blood_pressure),
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight(800),
                        color = GrayScale900,
                    )
                )
            },
            actions = {
                TopAppBarAction(
                    isPurchased = uiState.isPurchased,
                    onSetAlarmClick = { viewModel.showSetReminder(true) },
                    onNavigateToPremium = onNavigateToPremium
                )
            },
            navigationIcon = {
                IconButton(onClick = {
                    if (uiState.hasBloodPressureAlarm) {
                        onNavigateUp()
                    } else {
                        viewModel.showAskSetAlarmDialog(true)
                    }
                }) {
                    Icon(
                        painter = rememberVectorPainter(image = Icons.Default.ArrowBack),
                        contentDescription = null
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            )
        )
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            BloodPressureContent(
                modifier = Modifier.fillMaxSize(),
                onBloodPressureOptionClick = { option ->
                    if (option == BloodPressureOption.ADD && !uiState.isPurchased) {
                        adsManager.showClickAddRecordAd {
                            onItemOptionClick.invoke(option)
                        }
                    } else {
                        onItemOptionClick.invoke(option)
                    }
                },
                uiState = uiState
            )
        }
    }
}

@Composable
fun BloodPressureContent(
    modifier: Modifier,
    onBloodPressureOptionClick: (BloodPressureOption) -> Unit,
    uiState: BloodPressureFeatureViewModel.UiState
) {
    val adManager = LocalAdsManager.current
    val nativeAd by adManager.bloodPressureNativeAd.collectAsStateWithLifecycle()
    Box(
        modifier = modifier.padding(16.dp)
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            nativeAd?.let {
                item {
                    CardItemNativeAd(nativeAd = it)
                }
            }
            item {
                BloodPressureOptionItem(
                    bloodPressureOption = BloodPressureOption.ADD,
                    onBloodPressureOptionClick = onBloodPressureOptionClick
                )
            }

            if (uiState.isHistoryAvailable) {
                item {
                    BloodPressureOptionItem(
                        bloodPressureOption = BloodPressureOption.TRENDS,
                        onBloodPressureOptionClick = onBloodPressureOptionClick
                    )
                }
                item {
                    BloodPressureOptionItem(
                        bloodPressureOption = BloodPressureOption.HISTORY,
                        onBloodPressureOptionClick = onBloodPressureOptionClick
                    )
                }
            }
        }
    }


}

@Composable
fun BloodPressureOptionItem(
    bloodPressureOption: BloodPressureOption,
    onBloodPressureOptionClick: (BloodPressureOption) -> Unit,
    modifier: Modifier = Modifier
) {

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(color = Color.White, shape = RoundedCornerShape(8.dp))
    ) {
        Column(
            modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row {
                Image(
                    modifier = Modifier.size(48.dp),
                    painter = painterResource(id = bloodPressureOption.iconRes),
                    contentDescription = "",
                    contentScale = ContentScale.None
                )
                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = stringResource(id = bloodPressureOption.contentRes),
                    style = TextStyle(
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight(400),
                        color = GrayScale900,
                    )
                )
            }

            TextButton(modifier = Modifier
                .fillMaxWidth()
                .background(color = Blue800, shape = RoundedCornerShape(size = 8.dp)),
                onClick = { onBloodPressureOptionClick(bloodPressureOption) }) {
                Text(
                    text = stringResource(id = bloodPressureOption.titleRes), style = TextStyle(
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
