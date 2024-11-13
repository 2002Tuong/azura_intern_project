package com.artgen.app.ui.screen.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.artgen.app.R
import com.artgen.app.ads.MediumNativeAdWrapper
import com.artgen.app.ads.NativeAdWrapper
import com.artgen.app.utils.LocalAdsManager
import com.artgen.app.utils.LocalRemoteConfig
import com.artgen.app.utils.navigationBarsPaddingIfNeed
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    onNavigateToMain: () -> Unit,
    onNavigateUp: () -> Unit,
    onPageChanged: (Int) -> Unit,
    viewModel: OnboardingViewModel = koinViewModel()
) {
    BackHandler(true, onNavigateUp)

    OnboardingScreenForFreeUser(
        modifier = modifier.fillMaxSize(),
        onPageChanged = onPageChanged,
        onNavigateToMain = onNavigateToMain
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreenForFreeUser(
    modifier: Modifier = Modifier,
    onPageChanged: (Int) -> Unit,
    onNavigateToMain: () -> Unit,
) {

    val adsManager = LocalAdsManager.current
    val nativeAd1 by adsManager.onboardingNativeAd.collectAsStateWithLifecycle()
    val nativeAd2 by adsManager.onboardingNativeAd2.collectAsStateWithLifecycle()
    val nativeAd3 by adsManager.onboardingNativeAd3.collectAsStateWithLifecycle()
    Box(modifier = modifier.fillMaxSize()) {
        val pageState = rememberPagerState()
        val coroutineScope = rememberCoroutineScope()

        HorizontalPager(pageCount = 3, state = pageState) { index ->
            when (index) {
                0 -> OnboardingPage1(nativeAd = nativeAd1)
                1 -> OnboardingPage2(nativeAd = nativeAd2)
                else -> OnboardingPage3(nativeAd = nativeAd3)
            }
            onPageChanged(index)
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Bottom
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PageIndicator(
                    pageIndex = pageState.currentPage,
                    pageCount = 3,
                    modifier = Modifier
                        .padding(start = 11.dp)
                        .fillMaxWidth()
                        .weight(1f)
                )

                val text = if (pageState.currentPage == 2) {
                    stringResource(id = R.string.cw_get_started)
                } else {
                    stringResource(id = R.string.cw_next)
                }
                Box(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .wrapContentWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .clickable {
                            coroutineScope.launch {
                                if (pageState.currentPage == 2) {
                                    onNavigateToMain()
                                } else {
                                    pageState.animateScrollToPage(pageState.currentPage + 1)
                                }
                            }
                        }
                ) {
                    Text(
                        text = text.uppercase(),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Spacer(
                modifier = Modifier
                    .navigationBarsPaddingIfNeed()
                    .height(260.dp)
            )
        }
    }
}

@Composable
fun OnboardingPage3(
    modifier: Modifier = Modifier,
    nativeAd: NativeAdWrapper? = null
) {
    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.onboarding_3),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Text(
            text = stringResource(R.string.modern_trends),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .padding(bottom = 333.dp)
                .fillMaxWidth()
                .padding(start = 16.dp)
                .align(Alignment.BottomCenter)
        )
        if (nativeAd != null) {
            MediumNativeAdWrapper(
                nativeAd = nativeAd,
                modifier = Modifier
                    .navigationBarsPaddingIfNeed()
                    .padding(4.dp)
                    .align(Alignment.BottomEnd)
                    .fillMaxWidth()
                    .wrapContentHeight(),
                onLoadAd = {}
            )
        }
    }
}

@Composable
fun OnboardingPage2(
    modifier: Modifier = Modifier,
    nativeAd: NativeAdWrapper? = null
) {
    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.onboarding_2),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Text(
            text = stringResource(R.string.advanced_ai_technology),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .padding(bottom = 333.dp)
                .fillMaxWidth()
                .padding(start = 16.dp)
                .align(Alignment.BottomCenter)
        )
        if (nativeAd != null) {
            MediumNativeAdWrapper(
                nativeAd = nativeAd,
                modifier = Modifier
                    .navigationBarsPaddingIfNeed()
                    .padding(4.dp)
                    .align(Alignment.BottomEnd)
                    .fillMaxWidth()
                    .wrapContentHeight(),
                onLoadAd = {}
            )
        }
    }
}

@Composable
fun OnboardingPage1(
    modifier: Modifier = Modifier,
    nativeAd: NativeAdWrapper? = null
) {
    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.onboarding_1),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Text(
            text = stringResource(R.string.explore_various_styles),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .padding(bottom = 333.dp)
                .fillMaxWidth()
                .padding(start = 16.dp)
                .align(Alignment.BottomCenter),
        )

        if (nativeAd != null) {
            MediumNativeAdWrapper(
                nativeAd = nativeAd,
                modifier = Modifier
                    .navigationBarsPaddingIfNeed()
                    .padding(4.dp)
                    .align(Alignment.BottomEnd)
                    .fillMaxWidth()
                    .wrapContentHeight(),
                onLoadAd = {}
            )
        }
    }
}

@Composable
fun OnboardingPage(
    modifier: Modifier = Modifier,
    titleRes: Int,
    drawable: Int,
) {

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {

        Image(
            painter = painterResource(id = drawable),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Text(
            text = stringResource(id = titleRes),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .padding(bottom = 380.dp)
                .fillMaxWidth()
                .padding(start = 16.dp)
                .align(Alignment.BottomCenter),
        )
    }
}

@Composable
fun PageIndicator(modifier: Modifier = Modifier, pageIndex: Int, pageCount: Int) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        (0 until pageCount).forEach { index ->
            val isSelected = pageIndex == index
            val color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                Color(0xFF7884BA)
            }
            Box(
                modifier = Modifier
                    .padding(5.dp)
                    .size(10.dp)
                    .background(color = color, shape = CircleShape)
            )
        }
    }
}
