package com.artgen.app.ui.screen.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.artgen.app.R
import com.artgen.app.ui.screen.rating.RatingDialog
import com.artgen.app.ui.theme.Neutral900
import com.artgen.app.utils.LocalAdsManager
import com.artgen.app.utils.LocalRatingManager
import com.artgen.app.utils.LocalShareController
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllStylesScreen(
    onNavigateUp: () -> Unit,
    onDoneClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StylePickerViewModel = koinViewModel()
) {

    val adsManager = LocalAdsManager.current
    val nativeAd by adsManager.allStyleNativeAd.collectAsStateWithLifecycle()

    BackHandler(true) {
        adsManager.loadAllStyleNativeAd(true)
        onNavigateUp()
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val ratingManager = LocalRatingManager.current
    val shareController = LocalShareController.current
    val waitingToShowRate by ratingManager.waitingToShowRate.collectAsStateWithLifecycle()

    if (waitingToShowRate && ratingManager.canShowRate(isExitApp = false)) {
        RatingDialog(
            onDismissRequest = { ratingManager.updateWaitingRateStatus(false) },
            onRateClick = { rateStar, feedback ->
                ratingManager.updateWaitingRateStatus(false)
                if (rateStar >= 4) {
                    ratingManager.requestReviewStore {}
                } else {
                    shareController.sendFeedback(feedback)
                }
            }
        )
    }

    val selectedArtStyle = remember(uiState.selectedArtStyle) {
        mutableStateOf(uiState.selectedArtStyle)
    }

    Column(modifier = modifier.fillMaxSize()) {
        CenterAlignedTopAppBar(
            title = {
                Text(text = stringResource(R.string.all_styles))
            },
            navigationIcon = {
                IconButton(onClick = {
                    adsManager.loadAllStyleNativeAd(true)
                    onNavigateUp()
                }) {
                    Icon(imageVector = Icons.Outlined.Close, contentDescription = null)
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        selectedArtStyle.value?.let {
                            viewModel.setSelectedArtStyle(it)
                        }
                        adsManager.loadAllStyleNativeAd(true)
                        onDoneClick()
                    }
                ) {
                    Icon(imageVector = Icons.Outlined.Done, contentDescription = null)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Neutral900
            )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            SelectStyle(
                uiState = uiState,
                nativeAd = nativeAd,
                selectedArtStyle = selectedArtStyle.value,
                onSelectStyle = {
                    selectedArtStyle.value = it
                }
            )
        }
    }
}
