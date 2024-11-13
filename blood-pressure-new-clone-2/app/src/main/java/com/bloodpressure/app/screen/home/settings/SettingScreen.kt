package com.bloodpressure.app.screen.home.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bloodpressure.app.R
import com.bloodpressure.app.ui.component.RatingDialog
import com.bloodpressure.app.ui.theme.GrayScale900
import com.bloodpressure.app.utils.LocalShareController
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    modifier: Modifier = Modifier,
    onLanguageClick: () -> Unit,
    onPremiumBannerClick: () -> Unit,
    onAlarmClick: () -> Unit,
    onAboutMeClick: () -> Unit,
    viewModel: SettingViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val shareController = LocalShareController.current

    if (uiState.shouldShowRating) {
        RatingDialog(
            onDismissRequest = { viewModel.onRateShown() },
            onRateClick = { rate ->
                if (rate > 4) {
                    shareController.openStore()
                } else {
                    shareController.sendFeedback()
                }
            }
        )
    }

    uiState.shareUri?.let { shareUri ->
        LaunchedEffect(shareUri) {
            shareController.shareFile(shareUri)
            viewModel.clearShareUri()
        }
    }

    Surface(modifier = modifier.fillMaxSize()) {
        Column(modifier = modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Text(
                        stringResource(id = R.string.cw_setting),
                        style = TextStyle(
                            fontSize = 20.sp,
                            lineHeight = 30.sp,
                            fontWeight = FontWeight(800),
                            color = GrayScale900,
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                if (!uiState.isPurchased) {
                    PremiumBannerItem(
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                        onClick = onPremiumBannerClick
                    )
                }

                Card(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {

                    SettingItem(
                        iconRes = R.drawable.ic_about_me,
                        label = stringResource(R.string.about_me),
                        onClick = onAboutMeClick
                    )

                    SettingDivider()

                    SettingItem(
                        iconRes = R.drawable.ic_alarm_small,
                        label = stringResource(R.string.alarm),
                        onClick = onAlarmClick
                    )

                    SettingDivider()

                    SettingItem(
                        iconRes = R.drawable.ic_setting_language,
                        label = stringResource(id = R.string.setting_language),
                        onClick = onLanguageClick
                    )

                    SettingDivider()

                    SettingItem(
                        iconRes = R.drawable.ic_setting_feedback,
                        label = stringResource(id = R.string.setting_feedback),
                        onClick = { shareController.sendFeedback() }
                    )

                    SettingDivider()

                    SettingItem(
                        iconRes = R.drawable.ic_setting_rate_us,
                        label = stringResource(id = R.string.setting_rate_us),
                        onClick = { viewModel.rateApp() }
                    )

                    SettingDivider()

                    SettingItem(
                        iconRes = R.drawable.ic_setting_share,
                        label = stringResource(id = R.string.setting_share),
                        onClick = { shareController.shareApp() }
                    )

                    SettingDivider()

                    SettingItem(
                        iconRes = R.drawable.ic_terms_services,
                        label = stringResource(id = R.string.term_of_service),
                        onClick = {
                            shareController.openTermOfService()
                        }
                    )

//                    val launcher = rememberLauncherForActivityResult(
//                        contract = CreateCsvContract(),
//                        onResult = { uri ->
//                            if (uri != null) {
//                                viewModel.exportData(uri)
//                            }
//                        }
//                    )
//                    SettingItem(
//                        iconRes = R.drawable.ic_export,
//                        label = stringResource(id = R.string.setting_export),
//                        onClick = {
//                            launcher.launch(System.currentTimeMillis().toString())
//                        }
//                    )
                }
            }
        }
    }
}

@Composable
fun PremiumBannerItem(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable { onClick() }
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_bg_banner_premium),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(id = R.string.setting_banner_premium_title),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(start = 24.dp)
            )

            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_right),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.padding(end = 24.dp)
            )
        }
    }
}

@Composable
fun SettingItem(
    modifier: Modifier = Modifier,
    iconRes: Int,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = label,
            modifier = Modifier.padding(top = 16.dp, bottom = 16.dp, start = 16.dp),
            tint = Color.Unspecified
        )

        Text(
            text = label,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 12.dp)
        )

        Icon(
            painter = painterResource(id = R.drawable.ic_chevron_right),
            contentDescription = null,
            modifier = Modifier.padding(end = 16.dp)
        )
    }
}

@Composable
fun SettingDivider() {
    Divider(
        modifier = Modifier.padding(horizontal = 12.dp),
        color = Color(0xFFF4F4F5)
    )
}

@Preview
@Composable
fun SettingScreenPreview() {
    SettingScreen(onLanguageClick = {}, onPremiumBannerClick = {}, onAlarmClick = {}, onAboutMeClick = {})
}
