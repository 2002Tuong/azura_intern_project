package com.bloodpressure.app.screen.language

import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bloodpressure.app.R
import com.bloodpressure.app.ads.LargeNativeAd
import com.bloodpressure.app.ads.LargeNativeCTATopAd
import com.bloodpressure.app.ads.MediumNativeAd
import com.bloodpressure.app.ads.MediumNativeCtaTopAd
import com.bloodpressure.app.screen.MainRoute
import com.bloodpressure.app.tracking.TrackingManager
import com.bloodpressure.app.utils.LocalAdsManager
import com.bloodpressure.app.utils.LocalLanguageManager
import com.bloodpressure.app.utils.LocalRemoteConfig
import com.bloodpressure.app.utils.navigationBarsPaddingIfNeed
import com.google.android.gms.ads.nativead.NativeAd
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectionScreen(
    modifier: Modifier = Modifier,
    goNext: (String) -> Unit,
    onNavigateUp: () -> Unit,
    viewModel: LanguageSelectionViewModel = koinViewModel(),
    hideNavigationBar: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val adsManager = LocalAdsManager.current
    val nativeAd by adsManager.languageNativeAd.collectAsStateWithLifecycle()
    LaunchedEffect(key1 = Unit, block = {
        hideNavigationBar.invoke()
    })

    LaunchedEffect(Unit) {
        adsManager.loadOnboardingNativeAd()
    }

    BackHandler(true, onNavigateUp)

    Column(modifier = modifier
        .fillMaxSize()
        .navigationBarsPaddingIfNeed()) {
        TopAppBar(
            title = {
                Text(text = stringResource(id = R.string.setting_language))
            },
            actions = {
                val languageManager = LocalLanguageManager.current
                if (uiState.selectedLanguage != null) {
                    IconButton(
                        onClick = {
                            uiState.selectedLanguage?.let {
                                viewModel.setLanguageSelected(it)
                                languageManager.updateLanguage(it)
                                val route = if (uiState.isOnboardingShown) {
                                    MainRoute.HOME
                                } else {
                                    MainRoute.ONBOARDING
                                }
                                goNext(route)
                            }
                            TrackingManager.logSelectLanguageButtonClickEvent()
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Done,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            ),
        )
        Divider(
            modifier = Modifier
                .height(1.dp)
                .fillMaxWidth()
                .background(color = Color(0xFFECEDEF))
        )

        LanguageSelection(
            modifier = Modifier.fillMaxSize(),
            uiState = uiState,
            onItemClick = { viewModel.setSelectedLanguage(it) },
            visibleItemCounter = 5,
            nativeAd = nativeAd,
            onLoadAd = { TrackingManager.logLanguageNativeAdStartShowEvent() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditLanguageScreen(
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit,
    viewModel: LanguageSelectionViewModel = koinViewModel()
) {
    val adsManager = LocalAdsManager.current
    val nativeAd by adsManager.languageSettingNativeAd.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val shouldShowNative = LocalRemoteConfig.current.adsConfig.shouldShowLanguageSettingNativeAd
    val adView by LocalAdsManager.current.homeBannerAd.collectAsStateWithLifecycle()
    LaunchedEffect(true) {
        adsManager.loadLanguageSettingNativeAd()
    }

    Column(modifier = modifier
        .fillMaxSize()
        .navigationBarsPaddingIfNeed()) {
        TopAppBar(
            title = {
                Text(text = stringResource(id = R.string.setting_language))
            },
            actions = {
                val languageManager = LocalLanguageManager.current
                Box(
                    modifier = Modifier
                        .wrapContentSize()
                        .clickable {
                            uiState.selectedLanguage?.let {
                                viewModel.setLanguageSelected(it)
                                languageManager.updateLanguage(it)
                                onNavigateUp()
                            }
                        }
                ) {
                    Text(
                        text = stringResource(id = R.string.cw_done),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
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

        LanguageSelection(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            uiState = uiState,
            onItemClick = { viewModel.setSelectedLanguage(it) },
            visibleItemCounter = 20,
            nativeAd = nativeAd,
            onLoadAd = {}
        )
        if (!uiState.isPurchased && adView != null && !shouldShowNative) {
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

@Composable
fun LanguageSelection(
    modifier: Modifier = Modifier,
    uiState: LanguageSelectionViewModel.UiState,
    onItemClick: (Language) -> Unit,
    visibleItemCounter: Int,
    onLoadAd: () -> Unit,
    nativeAd: NativeAd? = null,
) {
    val isCtaTop = LocalRemoteConfig.current.adsConfig.ctaTopLanguage
    Column(modifier = modifier.background(Color(0xFFFCFDFD))) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            items(uiState.languages.take(visibleItemCounter)) { language ->
                LanguageItem(
                    language = language,
                    onItemClick = onItemClick,
                    isSelected = uiState.selectedLanguage == language,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }

        if (nativeAd != null) {
            if (isCtaTop) {
                MediumNativeCtaTopAd(
                    nativeAd = nativeAd,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    onLoadAd = onLoadAd
                )
            } else {
                MediumNativeAd(
                    nativeAd = nativeAd,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    onLoadAd = onLoadAd
                )
            }
        }
    }
}

@Composable
fun LanguageItem(
    modifier: Modifier = Modifier,
    language: Language,
    onItemClick: (Language) -> Unit,
    isSelected: Boolean
) {
    Card(
        modifier = modifier.clickable { onItemClick(language) },
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = if (isSelected) {
            BorderStroke(1.dp, color = MaterialTheme.colorScheme.primary)
        } else {
            null
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = language.iconRes),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )

            Text(
                text = stringResource(id = language.nameRes),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .weight(1f)
            )

            RadioButton(selected = isSelected, onClick = { onItemClick(language) })
        }
    }
}
