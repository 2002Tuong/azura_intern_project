package com.bloodpressure.app.screen.bloodsugar

import android.view.ViewGroup
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
import androidx.compose.foundation.lazy.items
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
import com.bloodpressure.app.ads.CardItemNativeAd
import com.bloodpressure.app.data.model.BloodSugarOption
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
fun BloodSugarScreen(
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit = {},
    onItemOptionClick: (BloodSugarOption) -> Unit,
    onNavigateToPremium: () -> Unit,
    viewModel: BloodSugarFeatureViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val adsManager = LocalAdsManager.current
    LaunchedEffect(true) {
        adsManager.loadAddRecordNativeAd()
        adsManager.loadHistoryNativeAd()
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
            alarmType = AlarmType.BLOOD_SUGAR,
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
                    text = stringResource(id = R.string.blood_sugar),
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
                    if (uiState.hasBloodSugarAlarm) {
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
            BloodSugarContent(
                modifier = Modifier.fillMaxSize(),
                onBloodSugarOptionClick = onItemOptionClick,
                uiState = uiState
            )
        }
    }
}

@Composable
fun BloodSugarContent(
    modifier: Modifier,
    onBloodSugarOptionClick: (BloodSugarOption) -> Unit,
    uiState: BloodSugarFeatureViewModel.UiState
) {
    val adManager = LocalAdsManager.current
    val nativeAd by adManager.bloodSugarNativeAd.collectAsStateWithLifecycle()
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
                BloodSugarOptionItem(
                    bloodSugarOption = BloodSugarOption.ADD,
                    onBloodSugarOptionClick = onBloodSugarOptionClick
                )
            }

            if (uiState.isHistoryAvailable) {

                item {
                    BloodSugarOptionItem(
                        bloodSugarOption = BloodSugarOption.TRENDS,
                        onBloodSugarOptionClick = onBloodSugarOptionClick
                    )
                }
                item {
                    BloodSugarOptionItem(
                        bloodSugarOption = BloodSugarOption.HISTORY,
                        onBloodSugarOptionClick = onBloodSugarOptionClick
                    )
                }
            }
        }
    }


}

@Composable
fun BloodSugarOptionItem(
    bloodSugarOption: BloodSugarOption,
    onBloodSugarOptionClick: (BloodSugarOption) -> Unit,
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
                    painter = painterResource(id = bloodSugarOption.iconRes),
                    contentDescription = "",
                    contentScale = ContentScale.None
                )
                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = stringResource(id = bloodSugarOption.contentRes),
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
                onClick = { onBloodSugarOptionClick(bloodSugarOption) }) {
                Text(
                    text = stringResource(id = bloodSugarOption.titleRes), style = TextStyle(
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
