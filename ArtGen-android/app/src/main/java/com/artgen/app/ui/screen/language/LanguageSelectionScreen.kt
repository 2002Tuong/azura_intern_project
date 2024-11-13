package com.artgen.app.ui.screen.language

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonColors
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.artgen.app.R
import com.artgen.app.ads.LoadState
import com.artgen.app.ads.MediumNativeAdWrapper
import com.artgen.app.ads.NativeAdWrapper
import com.artgen.app.tracking.TrackingManager
import com.artgen.app.ui.screen.navigation.MainRoute
import com.artgen.app.ui.theme.Neutral800
import com.artgen.app.ui.theme.Purple800
import com.artgen.app.utils.LocalAdUtil
import com.artgen.app.utils.LocalAdsManager
import com.artgen.app.utils.LocalLanguageManager
import com.artgen.app.utils.LocalRemoteConfig
import com.artgen.app.utils.navigationBarsPaddingIfNeed
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectionScreen(
    modifier: Modifier = Modifier,
    goNext: (String) -> Unit,
    onNavigateUp: () -> Unit,
    viewModel: LanguageSelectionViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val adsManager = LocalAdsManager.current
    val nativeAd by adsManager.languageNativeAd.collectAsStateWithLifecycle()
    val adUtil = LocalAdUtil.current
    val context = LocalContext.current
    BackHandler(true, onNavigateUp)
    LaunchedEffect(Unit) {
        adUtil.loadNativeOnboard(context)
    }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(text = stringResource(id = R.string.setting_language))
            },
            actions = {
                val languageManager = LocalLanguageManager.current

                if (uiState.selectedLanguage != null)
                    IconButton(
                        onClick = {
                            uiState.selectedLanguage?.let {
                                viewModel.setLanguageSelected(it)
                                languageManager.updateLanguage(it)
                                goNext(MainRoute.ONBOARDING)
                            }
                            TrackingManager.logSelectLanguageButtonClickEvent()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Done,
                            contentDescription = null
                        )
                    }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
        )

        LanguageSelection(
            uiState = uiState,
            onItemClick = { viewModel.setSelectedLanguage(it) },
            visibleItemCounter = 6,
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
    LaunchedEffect(true) {
        adsManager.loadLanguageSettingNativeAd()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPaddingIfNeed()
    ) {
        TopAppBar(
            title = {
                Text(text = stringResource(id = R.string.setting_language))
            },
            actions = {
                val languageManager = LocalLanguageManager.current

                if (uiState.selectedLanguage != null)
                    Box(
                        modifier = Modifier
                            .wrapContentSize()
                            .padding(horizontal = 16.dp)
                            .clickable {
                                uiState.selectedLanguage?.let {
                                    viewModel.setLanguageSelected(it)
                                    languageManager.updateLanguage(it)
                                    onNavigateUp()
                                }
                            }
                    ) {
                        Image(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(id = R.drawable.ic_check),
                            contentDescription = "",
                            contentScale = ContentScale.None
                        )
                    }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Neutral800
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
            uiState = uiState,
            onItemClick = { viewModel.setSelectedLanguage(it) },
            visibleItemCounter = 20,
            nativeAd = nativeAd,
            onLoadAd = {}
        )
    }
}

@Composable
fun LanguageSelection(
    modifier: Modifier = Modifier,
    uiState: LanguageSelectionViewModel.UiState,
    onItemClick: (Language) -> Unit,
    visibleItemCounter: Int,
    onLoadAd: () -> Unit,
    nativeAd: NativeAdWrapper? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xff0A0C14))
            .navigationBarsPaddingIfNeed()
    ) {
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

        if (nativeAd != null && nativeAd.loadState != LoadState.FAILED) {
            MediumNativeAdWrapper(
                nativeAd = nativeAd,
                modifier = Modifier
                    .navigationBarsPaddingIfNeed()
                    .padding(8.dp)
                    .fillMaxWidth()
                    .wrapContentHeight(),
                onLoadAd = {},
                isCtaTop = LocalRemoteConfig.current.nativeLanguageCtaTop
            )
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
        modifier = modifier
            .clickable { onItemClick(language) },
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xff131313)),
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
                    .weight(1f),
                color = Color.White
            )

            RadioButton(selected = isSelected, onClick = { onItemClick(language) }, colors = RadioButtonDefaults.colors(unselectedColor = Purple800))
        }
    }
}

@Preview
@Composable
fun LanguageSelectionScreenPreview() {

}
