package com.bloodpressure.app.screen.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bloodpressure.app.R
import com.bloodpressure.app.utils.LocalAdsManager
import org.koin.androidx.compose.koinViewModel

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    onNavigateNext: (String) -> Unit,
    viewModel: SplashScreenViewModel = koinViewModel()
) {
    val splashScreenUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val adManager = LocalAdsManager.current

    LaunchedEffect(splashScreenUiState.isOnboardingCompleted) {
        if (splashScreenUiState.isOnboardingCompleted) {
            adManager.loadExitAppNativeAdIfNeeded()
        }
    }

    LaunchedEffect(key1 = Unit, block = {
        adManager.loadBannerAds()
    })

    splashScreenUiState.nextScreenRoute?.let { nextRoute ->
        LaunchedEffect(nextRoute) {
            onNavigateNext(nextRoute)
        }
    }

    if (splashScreenUiState.isLoadingAds) {
        LoadingAdsDialog()
    }

    Splash(modifier = modifier, splashScreenUiState)
}

@Composable
fun Splash(
    modifier: Modifier = Modifier,
    uiState: SplashScreenViewModel.UiState
) {
    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.img_bg_splash_screen),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = R.drawable.img_heart),
                contentDescription = null,
                tint = Color.Unspecified
            )

            Text(
                text = stringResource(id = R.string.app_name),
                modifier = Modifier.padding(top = 10.dp),
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        AnimatedVisibility(
            visible = uiState.isLoading,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (!uiState.isPurchased) {
                    Text(
                        text = stringResource(id = R.string.splash_loading_message),
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                LinearProgressIndicator(
                    modifier = Modifier
                        .padding(bottom = 24.dp, top = 8.dp)
                        .width(100.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }
        }
    }
}

@Composable
@Preview
fun SplashScreenPreview() {
    Splash(uiState = SplashScreenViewModel.UiState())
}
