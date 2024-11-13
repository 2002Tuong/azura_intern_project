package com.bloodpressure.app.screen.onboarding

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerScope
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bloodpressure.app.R
import com.bloodpressure.app.ads.MediumNativeAd
import com.bloodpressure.app.ads.MediumNativeCtaTopAd
import com.bloodpressure.app.tracking.TrackingManager
import com.bloodpressure.app.utils.LocalAdsManager
import com.bloodpressure.app.utils.LocalRemoteConfig
import com.bloodpressure.app.utils.navigationBarsPaddingIfNeed
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    onNavigateToMain: () -> Unit,
    onNavigateUp: () -> Unit,
    viewModel: OnboardingViewModel = koinViewModel(),
    hideNavigationBar: () -> Unit
) {
    val isPurchased by viewModel.isPurchased.collectAsStateWithLifecycle()
    LaunchedEffect(key1 = Unit, block = {
        hideNavigationBar.invoke()
    })
    BackHandler(true, onNavigateUp)
//    LaunchedEffect(Unit){
//        adsManager.loadHomeNativeAd()
//    }
    Box(modifier = modifier.fillMaxSize()) {
        if (isPurchased) {
            OnboardingScreenForProUser(
                modifier = modifier,
                onNavigateToMain = onNavigateToMain
            )
        } else {
            OnboardingScreenForFreeUser(
                modifier = modifier,
                onNavigateToMain = onNavigateToMain
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreenForFreeUser(
    modifier: Modifier = Modifier,
    onNavigateToMain: () -> Unit,
) {
    val adsManager = LocalAdsManager.current
    val nativeAd1 by adsManager.onboardingNativeAd1.collectAsState()
    val nativeAd2 by adsManager.onboardingNativeAd2.collectAsState()
    val nativeAd3 by adsManager.onboardingNativeAd3.collectAsState()
    val isCtaTop = LocalRemoteConfig.current.adsConfig.ctaTopOnboarding

    var page = 0
    Column(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPaddingIfNeed()
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            val pageState = rememberPagerState { 3 }
            val coroutineScope = rememberCoroutineScope()

            HorizontalPager(
                state = pageState,
                pageContent = {index ->
                    page = index
                    when (index) {
                        0 -> OnboardingPage1ForFreeUser()
                        1 -> OnboardingPage2ForFreeUser()
                        else -> OnboardingPage3ForProUser()
                    }
                }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomEnd),
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
                    Text(text = text, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color(0xFFFCFCFC))
        ) {
            val nativeAd = when (page) {
                0 -> nativeAd1
                1 -> nativeAd2
                else -> nativeAd3
            }
            if (nativeAd != null) {
                if (isCtaTop) {
                    MediumNativeCtaTopAd(
                        nativeAd = nativeAd,
                        modifier = Modifier
                            .padding(top = 4.dp, start = 8.dp, end = 8.dp, bottom = 4.dp)
                            .fillMaxWidth(),
                        onLoadAd = { TrackingManager.logOnboardingNativeAdStartShowEvent() }
                    )
                } else {
                    MediumNativeAd(
                        nativeAd = nativeAd,
                        modifier = Modifier
                            .padding(top = 4.dp, start = 8.dp, end = 8.dp, bottom = 4.dp)
                            .fillMaxWidth(),
                        onLoadAd = { TrackingManager.logOnboardingNativeAdStartShowEvent() }
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(272.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreenForProUser(
    modifier: Modifier = Modifier,
    onNavigateToMain: () -> Unit,
) {
    Box(modifier = modifier.fillMaxSize()) {
        val pageState = rememberPagerState { 3 }
        val coroutineScope = rememberCoroutineScope()

        HorizontalPager(
            state = pageState,
            pageContent = {index ->
                when (index) {
                    0 -> OnboardingPage1()
                    1 -> OnboardingPage2()
                    else -> OnboardingPage3()
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomEnd),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PageIndicator(pageIndex = pageState.currentPage, pageCount = 3)

            val text = if (pageState.currentPage == 2) {
                stringResource(id = R.string.cw_get_started)
            } else {
                stringResource(id = R.string.cw_next)
            }
            TextButton(
                onClick = {
                    coroutineScope.launch {
                        if (pageState.currentPage == 2) {
                            onNavigateToMain()
                        } else {
                            pageState.animateScrollToPage(pageState.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier
                    .wrapContentWidth()
                    .padding(top = 19.dp, start = 16.dp, end = 16.dp, bottom = 24.dp)
            ) {
                Text(text = text, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun PageIndicator(modifier: Modifier = Modifier, pageIndex: Int, pageCount: Int) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        (0 until pageCount).forEach { index ->
            val isSelected = pageIndex == index
            val color = if (isSelected) Color(0xFF1892FA) else Color(0xFFC4CACF)
            Box(
                modifier = Modifier
                    .padding(5.dp)
                    .size(10.dp)
                    .background(color = color, shape = CircleShape)
            )
        }
    }
}

@Composable
fun OnboardingPage1ForFreeUser(
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.img_bg_onboarding1),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        0.4f to Color.White.copy(alpha = 0f),
                        0.75f to Color.White,
                        1f to Color.White
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_heart),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 24.dp, top = 58.dp)
                        .size(186.dp, 119.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .align(Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Column(
                        modifier = Modifier
                            .width(84.dp)
                            .background(
                                color = Color.White,
                                shape = CircleShape
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(id = R.string.cw_systolic),
                            modifier = Modifier.padding(top = 8.dp),
                            style = MaterialTheme.typography.bodySmall
                        )

                        Text(
                            text = "109",
                            color = Color(0xFFF95721),
                            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Spacer(modifier = Modifier.width(24.dp))

                    Column(
                        modifier = Modifier
                            .width(84.dp)
                            .background(
                                color = Color.White,
                                shape = CircleShape
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(id = R.string.cw_diastolic),
                            modifier = Modifier.padding(top = 8.dp),
                            style = MaterialTheme.typography.bodySmall
                        )

                        Text(
                            text = "82",
                            color = Color(0xFF1892FA),
                            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Spacer(modifier = Modifier.width(24.dp))

                    Column(
                        modifier = Modifier
                            .width(80.dp)
                            .background(
                                color = Color.White,
                                shape = CircleShape
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(id = R.string.cw_pulse),
                            modifier = Modifier.padding(top = 8.dp),
                            style = MaterialTheme.typography.bodySmall
                        )

                        Text(
                            text = "109",
                            color = Color(0xFF53B69F),
                            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(bottom = 48.dp, start = 16.dp, end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.onboarding_title1),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Start
                )

                Text(
                    text = stringResource(id = R.string.onboarding_des1),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun OnboardingPage1(
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.img_bg_onboarding1),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        0.4f to Color.White.copy(alpha = 0f),
                        0.75f to Color.White,
                        1f to Color.White
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_heart),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 24.dp, top = 70.dp)
                        .statusBarsPadding()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                        .align(Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Column(
                        modifier = Modifier
                            .width(84.dp)
                            .background(
                                color = Color.White,
                                shape = CircleShape
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(id = R.string.cw_systolic),
                            modifier = Modifier.padding(top = 8.dp),
                            style = MaterialTheme.typography.bodySmall
                        )

                        Text(
                            text = "109",
                            color = Color(0xFFF95721),
                            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Spacer(modifier = Modifier.width(24.dp))

                    Column(
                        modifier = Modifier
                            .width(84.dp)
                            .background(
                                color = Color.White,
                                shape = CircleShape
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(id = R.string.cw_diastolic),
                            modifier = Modifier.padding(top = 8.dp),
                            style = MaterialTheme.typography.bodySmall
                        )

                        Text(
                            text = "82",
                            color = Color(0xFF1892FA),
                            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Spacer(modifier = Modifier.width(24.dp))

                    Column(
                        modifier = Modifier
                            .width(80.dp)
                            .background(
                                color = Color.White,
                                shape = CircleShape
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(id = R.string.cw_pulse),
                            modifier = Modifier.padding(top = 8.dp),
                            style = MaterialTheme.typography.bodySmall
                        )

                        Text(
                            text = "109",
                            color = Color(0xFF53B69F),
                            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(bottom = 120.dp, start = 24.dp, end = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.onboarding_title1),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = stringResource(id = R.string.onboarding_des1),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun OnboardingPage2ForFreeUser(
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.img_bg_onboarding2),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        0.4f to Color.White.copy(alpha = 0f),
                        0.75f to Color.White,
                        1f to Color.White
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                Box(
                    modifier = Modifier.padding(start = 32.dp, top = 62.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_onboarding2_chart),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(bottom = 50.dp, end = 60.dp)
                            .size(212.dp, 185.dp),
                    )

                    Image(
                        painter = painterResource(id = R.drawable.img_premium_heart),
                        contentDescription = null,
                        modifier = Modifier
                            .size(142.dp, 76.dp)
                            .align(Alignment.BottomEnd)
                    )
                }
            }

            Column(
                modifier = Modifier.padding(bottom = 48.dp, start = 16.dp, end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.onboarding_title2),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Start
                )

                Text(
                    text = stringResource(id = R.string.onboarding_des2),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun OnboardingPage2(
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.img_bg_onboarding2),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        0.4f to Color.White.copy(alpha = 0f),
                        0.75f to Color.White,
                        1f to Color.White
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .padding(start = 32.dp, top = 70.dp)
                        .statusBarsPadding()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_onboarding2_chart),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(bottom = 79.dp, end = 42.dp)
                            .size(240.dp, 209.dp),
                    )

                    Image(
                        painter = painterResource(id = R.drawable.img_premium_heart),
                        contentDescription = null,
                        modifier = Modifier
                            .size(179.dp, 97.dp)
                            .align(Alignment.BottomEnd)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .padding(bottom = 120.dp, start = 24.dp, end = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.onboarding_title2),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = stringResource(id = R.string.onboarding_des2),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun OnboardingPage3ForProUser(
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.img_bg_onboarding3),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        0.4f to Color.White.copy(alpha = 0f),
                        0.75f to Color.White,
                        1f to Color.White
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .width(220.dp)
                        .padding(start = 24.dp, top = 46.dp)
                        .background(color = Color.White, shape = CircleShape)
                ) {
                    Text(
                        text = stringResource(id = R.string.bp_info_title_at_a_normal_bp_range),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF57A368),
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 24.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .width(220.dp)
                        .padding(start = 72.dp, top = 8.dp)
                        .background(color = Color.White, shape = CircleShape)
                ) {
                    Text(
                        text = stringResource(id = R.string.bp_info_title_what_is_bp),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF7C1D2C),
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 24.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .width(220.dp)
                        .padding(start = 24.dp, top = 8.dp)
                        .background(color = Color.White, shape = CircleShape)
                ) {
                    Text(
                        text = stringResource(id = R.string.bp_info_title_how_to_measure_bp),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF1892FA),
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 24.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.padding(bottom = 48.dp, start = 16.dp, end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.onboarding_title3),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Start
                )

                Text(
                    text = stringResource(id = R.string.onboarding_des3),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun OnboardingPage3(
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.img_bg_onboarding3),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        0.4f to Color.White.copy(alpha = 0f),
                        0.75f to Color.White,
                        1f to Color.White
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .width(220.dp)
                        .padding(start = 24.dp, top = 70.dp)
                        .statusBarsPadding()
                        .background(color = Color.White, shape = CircleShape)
                ) {
                    Text(
                        text = stringResource(id = R.string.bp_info_title_at_a_normal_bp_range),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF57A368),
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 24.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .width(220.dp)
                        .padding(start = 72.dp, top = 24.dp)
                        .background(color = Color.White, shape = CircleShape)
                ) {
                    Text(
                        text = stringResource(id = R.string.bp_info_title_what_is_bp),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF7C1D2C),
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 24.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .width(220.dp)
                        .padding(start = 24.dp, top = 24.dp)
                        .background(color = Color.White, shape = CircleShape)
                ) {
                    Text(
                        text = stringResource(id = R.string.bp_info_title_how_to_measure_bp),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF1892FA),
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 24.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.padding(bottom = 120.dp, start = 24.dp, end = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.onboarding_title3),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = stringResource(id = R.string.onboarding_des3),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
