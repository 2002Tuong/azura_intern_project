package com.artgen.app.ui.screen.genart

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.artgen.app.R
import com.artgen.app.ads.BannerAdView
import com.artgen.app.ads.BannerAdsManager
import com.artgen.app.ui.screen.rating.RatingDialog
import com.artgen.app.ui.theme.Neutral900
import com.artgen.app.utils.LocalAdsManager
import com.artgen.app.utils.LocalRatingManager
import com.artgen.app.utils.LocalShareController
import com.artgen.app.utils.navigationBarsPaddingIfNeed
import com.google.android.gms.ads.AdView
import org.koin.androidx.compose.koinViewModel

@Composable
fun ArtGeneratorScreen(
    onNavigateToAllStyle: () -> Unit,
    onSelectAnotherImage: () -> Unit,
    onNavigateToCrop: (Uri) -> Unit,
    onNavigateToSuccess: (Uri) -> Unit,
    onNavigateToSetting: () -> Unit,
    onNavigateToPremium: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ArtGeneratorViewModel = koinViewModel()
) {
    val adsManager = LocalAdsManager.current
    val adView by adsManager.genArtBannerAd.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        adsManager.loadAllStyleNativeAd()
    }


    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.shouldLoadBannerAds) {
        if (uiState.shouldLoadBannerAds) {
            adsManager.loadBannerAd(BannerAdsManager.BannerAdPlacement.RESULT_SCR)
            viewModel.bannerAdsLoaded()
        }
    }
    LaunchedEffect(uiState.genArtSuccess) {
        if (uiState.genArtSuccess) {
            viewModel.getResultUri()?.let {
                onNavigateToSuccess(it)
                viewModel.clearSuccessMessage()
            }
        }
    }
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

    if(uiState.showBottomPopup) {
        GenArtBottomPopup(
            onDismissRequest = {viewModel.shouldShowBottomPopup(false)},
            onActionButtonClick = {
                adsManager.showGenArtRewardedVideoAd(onComplete = {
                    viewModel.generateArt()
                    adsManager.loadAllDoneNativeAd()
                    adsManager.loadBannerAd(BannerAdsManager.BannerAdPlacement.GEN_ART)
                    viewModel.shouldShowBottomPopup(false)
                }, onRewardedAd = {})
            },
            onGoProButtonClick = {
                viewModel.shouldShowBottomPopup(false)
                onNavigateToPremium()
            },
            actionButtonTitle = stringResource(R.string.generate_now),
            actionButtonSubtitle = stringResource(R.string.watch_video_ads)
        )
    }

    ArtGenerator(
        onGenerateButtonClick = {
            if(uiState.isPremium) {
                viewModel.generateArt()
            } else {
                viewModel.shouldShowBottomPopup(true)
            }

        },
        onDeleteImageClick = viewModel::removeSelectedImage,
        onNavigateToAllStyle = {
            adsManager.loadBannerAd(BannerAdsManager.BannerAdPlacement.GEN_ART)
            onNavigateToAllStyle()
        },
        onSelectAnotherImage = {
            adsManager.loadBannerAd(BannerAdsManager.BannerAdPlacement.GEN_ART)
            onSelectAnotherImage()
        },
        onNavigateToCrop = {
            adsManager.loadBannerAd(BannerAdsManager.BannerAdPlacement.GEN_ART)
            onNavigateToCrop(it)
        },
        onNavigateToSetting = {
            adsManager.loadBannerAd(BannerAdsManager.BannerAdPlacement.GEN_ART)
            onNavigateToSetting()
        },
        modifier = modifier,
        uiState = uiState,
        adView = adView,
        onPromptTextChange = viewModel::setPrompt
    )

    if (uiState.isLoading) {
        LoadingScreen()
    }

    if (uiState.genArtFailure) {
        GenArtFailureDialog {
            viewModel.clearFailureDialog()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtGenerator(
    uiState: ArtGeneratorViewModel.UiState,
    adView: AdView?,
    onGenerateButtonClick: () -> Unit,
    onDeleteImageClick: () -> Unit,
    onNavigateToAllStyle: () -> Unit,
    onSelectAnotherImage: () -> Unit,
    onNavigateToCrop: (Uri) -> Unit,
    onNavigateToSetting: () -> Unit,
    onPromptTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isError by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(title = {
            Row(
                modifier = Modifier.fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.toolbar_logo),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )

                Text(
                    text = stringResource(id = R.string.genart_ai),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }, actions = {
            IconButton(onClick = onNavigateToSetting) {
                Icon(imageVector = Icons.Default.Settings, contentDescription = null)
            }
        }, colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Neutral900
        )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(
                            id = R.string.selected_style_title, uiState.style?.name.orEmpty()
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onNavigateToAllStyle() }) {
                        Text(text = stringResource(R.string.see_all))
                        Icon(
                            painter = painterResource(id = R.drawable.chevron_right),
                            contentDescription = null,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }

                Image(
                    painter = rememberAsyncImagePainter(model = uiState.style?.thumbnailUrl),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .size(189.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .align(Alignment.CenterHorizontally),
                    contentScale = ContentScale.Inside
                )

                Text(
                    text = stringResource(R.string.input_prompt),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 24.dp)
                )

                Box(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth()
                        .background(color = Neutral900, shape = RoundedCornerShape(8.dp))
                        .border(
                            width = 2.dp,
                            color = if (isError) Color(0xFFF44336) else Neutral900,
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    val focusManager = LocalFocusManager.current
                    OutlinedTextField(
                        value = uiState.prompt,
                        onValueChange = { text ->
                            isError = text.length > 500
                            onPromptTextChange(text)
                        },
                        placeholder = {
                            Text(
                                text = stringResource(R.string.ex_beautiful_girl_in_the_dream_world),
                                color = Color(0xFF455187)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 89.dp)
                            .padding(bottom = 16.dp),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done, keyboardType = KeyboardType.Text
                        ),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus(true) }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.Unspecified,
                            unfocusedBorderColor = Color.Unspecified,
                            focusedContainerColor = Color.Unspecified,
                            unfocusedContainerColor = Color.Unspecified
                        ),
                    )

                    Text(
                        text = ("${uiState.prompt.length}/500"),
                        color = Color(0xFF455187),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 4.dp, bottom = 4.dp)
                    )
                }

                if (isError) {
                    Text(
                        text = stringResource(id = R.string.over_limit_characters_error),
                        modifier = Modifier.padding(top = 2.dp),
                        color = Color(0xFFF44336)
                    )
                }

                Row(
                    modifier = Modifier
                        .padding(top = 20.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.demo_image_optional),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = onSelectAnotherImage) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_add_image),
                            contentDescription = null
                        )
                    }

                    IconButton(onClick = {
                        uiState.uri?.let { onNavigateToCrop(it) }
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_crop),
                            contentDescription = null
                        )
                    }

                    IconButton(onClick = onDeleteImageClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_delete),
                            contentDescription = null,
                            tint = Color.Unspecified
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (uiState.uri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(model = uiState.uri),
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .wrapContentWidth()
                            .height(189.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.FillHeight
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(89.dp)
                            .background(color = Neutral900, shape = RoundedCornerShape(8.dp))
                            .clickable { onSelectAnotherImage() },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_add_image),
                            contentDescription = null,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 16.dp)
            ) {
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable {
                        if (!isError) {
                            onGenerateButtonClick()
                        }
                    }
                    .padding(vertical = 8.dp), horizontalArrangement = Arrangement.Center) {

//                    if(!uiState.isPremium) {
//                        Image(
//                            painter = painterResource(id = R.drawable.ic_ads), contentDescription = null
//                        )
//                    }

                    Column {
                        Text(
                            text = stringResource(R.string.generate_now),
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 5.dp)
                        )

//                        if(!uiState.isPremium) {
//                            Text(
//                                text = stringResource(R.string.watch_video_ads),
//                                style = MaterialTheme.typography.bodyMedium,
//                                color = Color(0xFFFFA8C2)
//                            )
//                        }
                    }
                }
            }

            if (adView != null) {
                Box(
                    modifier = Modifier
                        .navigationBarsPaddingIfNeed()
                        .fillMaxWidth(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    BannerAdView(adView = adView)
                }
            }
        }
    }
}