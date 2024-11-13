package com.bloodpressure.app.screen.bmi.listfeature

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
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
import com.bloodpressure.app.data.model.BMIOptionType
import com.bloodpressure.app.screen.alarm.AlarmType
import com.bloodpressure.app.screen.alarm.SetAlarmDialog
import com.bloodpressure.app.screen.home.settings.CreateCsvContract
import com.bloodpressure.app.ui.component.AskSetAlarmDialog
import com.bloodpressure.app.ui.component.TopAppBarAction
import com.bloodpressure.app.ui.theme.Blue800
import com.bloodpressure.app.ui.theme.GrayScale900
import com.bloodpressure.app.utils.LocalAdsManager
import com.bloodpressure.app.utils.LocalShareController
import com.bloodpressure.app.utils.navigationBarsPaddingIfNeed
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BMIListFeatureScreen(
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit,
    onBMIOptionClick:(BMIOptionType) -> Unit,
    onNavigateToPremium: () -> Unit,
    viewModel: BMIListFeatureViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val shareController = LocalShareController.current
    val adsManager = LocalAdsManager.current
    LaunchedEffect(true) {
        adsManager.loadAddRecordNativeAd()
    }

    val launcher = rememberLauncherForActivityResult(
        contract = CreateCsvContract(),
        onResult = { uri ->
            if (uri != null) {
                viewModel.exportData(uri)
            }
        }
    )

    uiState.shareUri?.let {
        LaunchedEffect(it) {
            shareController.shareFile(it)
            viewModel.clearShareUri()
        }
    }

    if(uiState.showAskSetAlarmDialog) {
        AskSetAlarmDialog(
            onDismissRequest = { viewModel.showSetAlarmDialog(false) },
            onCancel = {
                viewModel.showAskSetAlarmDialog(false)
                onNavigateUp()
            },
            onAgreeSetAlarm = {
                viewModel.showSetAlarmDialog(true)
                viewModel.showAskSetAlarmDialog(false)
            }
        )
    }

    if(uiState.showSetAlarmDialog) {
        SetAlarmDialog(
            alarmType = AlarmType.WEIGHT_BMI,
            onDismissRequest = { viewModel.showSetAlarmDialog(false) },
            onSave = {
                viewModel.insertAlarm(it)
                viewModel.showSetAlarmDialog(false)
            }
        )
    }

    BackHandler(enabled = !uiState.hasBmiAlarm) {
        if(!uiState.hasBmiAlarm) {
            viewModel.showAskSetAlarmDialog(true)
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
            .navigationBarsPaddingIfNeed()
    ) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.weight_bmi),
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight(700),
                        color = GrayScale900,
                    )
                )
            },
            colors =  TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            ),
            navigationIcon = {
                IconButton(
                    onClick = {
                        if(uiState.hasBmiAlarm) {
                            onNavigateUp()
                        } else {
                            viewModel.showAskSetAlarmDialog(true)
                        }
                    }
                ) {
                    Icon(
                        painter = rememberVectorPainter(image = Icons.Default.ArrowBack),
                        contentDescription = null
                    )
                }
            },
            actions = {
                TopAppBarAction(
                    isPurchased = uiState.isPurchased,
                    onSetAlarmClick = { viewModel.showSetAlarmDialog(true) },
                    onNavigateToPremium = onNavigateToPremium
                )
            }
        )

        BMIContent(
            modifier = Modifier.fillMaxSize().weight(1f),
            uiState = uiState,
            onBMIOptionClick = onBMIOptionClick
        )
    }
}

@Composable
fun BMIContent(
    uiState: BMIListFeatureViewModel.UiState,
    onBMIOptionClick: (BMIOptionType) -> Unit,
    modifier: Modifier = Modifier,
) {

    val adManager = LocalAdsManager.current
    val nativeAd by adManager.bmiNativeAd.collectAsStateWithLifecycle()

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
                BMIOptionItem(
                    bmiOptionType = BMIOptionType.ANALYZE,
                    onBMIOptionClick = onBMIOptionClick
                )
            }

            if(uiState.isHistoryAvailable) {
                item {
                    BMIOptionItem(
                        bmiOptionType = BMIOptionType.TRENDS,
                        onBMIOptionClick = onBMIOptionClick)
                }
                item {
                    BMIOptionItem(
                        bmiOptionType = BMIOptionType.HISTORY,
                        onBMIOptionClick = onBMIOptionClick)
                }
            }
        }
    }
}

@Composable
fun BMIOptionItem(
    bmiOptionType: BMIOptionType,
    onBMIOptionClick: (BMIOptionType) -> Unit,
    modifier: Modifier = Modifier) {

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(color = Color.White, shape = RoundedCornerShape(8.dp))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row {
                Image(
                    modifier = Modifier.size(48.dp),
                    painter = painterResource(id = bmiOptionType.iconRes),
                    contentDescription = "",
                    contentScale = ContentScale.None
                )
                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = stringResource(id = bmiOptionType.contentRes),
                    style = TextStyle(
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight(400),
                        color = GrayScale900,
                    )
                )
            }

            TextButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Blue800, shape = RoundedCornerShape(size = 8.dp)),
                onClick = { onBMIOptionClick(bmiOptionType) }
            ) {
                Text(
                    text = stringResource(id = bmiOptionType.titleRes),
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
