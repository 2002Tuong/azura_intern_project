package com.artgen.app.ui.screen.result

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.artgen.app.R
import com.artgen.app.ads.BannerAdView
import com.artgen.app.ads.BannerAdsManager
import com.artgen.app.ads.LoadState
import com.artgen.app.ads.Medium3NativeAd
import com.artgen.app.ads.NativeAdWrapper
import com.artgen.app.data.model.ArtStyle
import com.artgen.app.ui.screen.premium.gradientColor
import com.artgen.app.ui.theme.ArtGenTheme
import com.artgen.app.ui.theme.Neutral900
import com.artgen.app.utils.LocalAdsManager
import com.artgen.app.utils.LocalRatingManager
import com.artgen.app.utils.navigationBarsPaddingIfNeed
import com.google.android.gms.ads.AdView
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    onNavigateUp: () -> Unit,
    onNavigateToAllStyles: () -> Unit,
    onNavigateToPremium: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ResultViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val adView by LocalAdsManager.current.resultBannerAd.collectAsStateWithLifecycle()
    val resultNativeAd by LocalAdsManager.current.resultNativeAd.collectAsStateWithLifecycle()
    val adsManager = LocalAdsManager.current
    val ratingManager = LocalRatingManager.current

    val saveHandle = {
        if(uiState.isPremium) viewModel.save()
        else viewModel.shouldShowBottomSheet(true)
    }

    val backHandle = {
        if(uiState.hasSaved) {
            onNavigateUp()
        } else {
            viewModel.shouldShowSaveDialog(true)
        }
    }
    BackHandler(enabled = true) {
        backHandle()
    }

    LaunchedEffect(uiState.shouldLoadBannerAds) {
        if (uiState.shouldLoadBannerAds) {
            viewModel.bannerAdsLoaded()
        }
    }
    LaunchedEffect(uiState.shouldSaveSaveSuccessMessage) {
        if (uiState.shouldSaveSaveSuccessMessage) {
            delay(3000L)
            viewModel.hideSaveSuccessMessage()
        }
    }

    if(uiState.shouldShowSaveBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {viewModel.shouldShowBottomSheet(false)},
            containerColor =Color(0xFF343D65),
            dragHandle = { BottomSheetDefaults.DragHandle() },
            content = {
                BottomSheetButtonSave(
                    onClick = {
                        viewModel.shouldShowBottomSheet(false)
                        adsManager.showSaveArtRewardVideoAd(
                            onComplete = {
                                ratingManager.updateWaitingRateStatus(true)
                                viewModel.save()
                                adsManager.loadSaveArtRewardedAds(reload = true)
                            },
                            onRewardedAd = {}
                        )
                    }
                )

                BottomSheetButtonUnlockPro(
                    onClick = {
                        viewModel.shouldShowBottomSheet(false)
                        onNavigateToPremium()
                    }
                )
            }
        )
    }

    if(uiState.shouldShowSaveDialog) {
        SaveDialog(
            onDismissRequest = {viewModel.shouldShowSaveDialog(false)},
            onSaveClick = {
                viewModel.shouldShowSaveDialog(false)
                adsManager.showSaveArtRewardVideoAd(
                    onComplete = {
                        viewModel.save()
                        adsManager.loadSaveArtRewardedAds(reload = true)
                    },
                    onRewardedAd = {}
                )
            },
            onCancelClick = {
                viewModel.shouldShowSaveDialog(false)
                onNavigateUp()},
            isPremium = uiState.isPremium
        )
    }

    GenArtResult(
        uiState = uiState,
        resultNativeAd = resultNativeAd,
        adView = adView,
        onNavigateUp = onNavigateUp,
        onCloseClick = {
           backHandle()
        },
        onSaveClick = saveHandle,
        onNavigateToAllStyles = {
            adsManager.loadBannerAd(BannerAdsManager.BannerAdPlacement.RESULT_SCR)
            onNavigateToAllStyles()
        },
        onStyleItemClick = {
            onNavigateUp()
        },
        modifier = modifier.fillMaxSize()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenArtResult(
    uiState: ResultViewModel.UiState,
    resultNativeAd: NativeAdWrapper?,
    adView: AdView?,
    onNavigateUp: () -> Unit,
    onCloseClick: () ->Unit,
    onSaveClick: () -> Unit,
    onNavigateToAllStyles: () -> Unit,
    onStyleItemClick: (ArtStyle) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPaddingIfNeed()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = stringResource(R.string.result))
                },
                navigationIcon = {
                    IconButton(onClick = onCloseClick) {
                        Icon(imageVector = Icons.Outlined.Close, contentDescription = null)
                    }
                },
                actions = {
                    Row(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { onSaveClick() }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_action_save),
                            contentDescription = null,
                            tint = Color.White
                        )
                        Text(text = stringResource(R.string.save), color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Neutral900
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPaddingIfNeed()
            ) {

                Box(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .weight(1f)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(model = uiState.uri),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center)
                            .clip(RoundedCornerShape(8.dp)),
                    )

                    SaveImageSuccessMessage(isVisible = uiState.shouldSaveSaveSuccessMessage)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .padding(start = 16.dp, end = 16.dp, top = 8.dp)
                            .fillMaxWidth()
                            .background(
                                color = Color(0x1AFF4079),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onNavigateToAllStyles() }
                            .padding(vertical = 4.dp)
                            .weight(1f),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.create_more),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .padding(start = 16.dp, end = 16.dp, top = 8.dp)
                            .fillMaxWidth()
                            .background(
                                color = Color(0x1AFF4079),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onNavigateUp() }
                            .padding(vertical = 4.dp)
                            .weight(1f),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.re_generate),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }

                if(resultNativeAd != null && resultNativeAd.loadState != LoadState.FAILED) {
                    Medium3NativeAd(
                        nativeAd = resultNativeAd,
                        onLoadAd = {},
                        modifier = Modifier.padding(horizontal = 8.dp)
                            .padding(top = 4.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.generate_more_style),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onNavigateToAllStyles() }
                    ) {
                        Text(text = stringResource(id = R.string.see_all))
                        Icon(
                            painter = painterResource(id = R.drawable.chevron_right),
                            contentDescription = null,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.styles) {
                        StyleItem(
                            style = it,
                            onItemClick = onStyleItemClick
                        )
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

        AnimatedVisibility(visible = uiState.isSaving) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun StyleItem(style: ArtStyle, onItemClick: (ArtStyle) -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier
        .size(72.dp)
        .clickable { onItemClick(style) }
    ) {
        AsyncImage(
            model = style.thumbnailUrl,
            contentDescription = null,
            modifier = modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black)
                    ),
                    shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                )
                .padding(8.dp)
        ) {
            Text(
                text = style.name,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SaveImageSuccessMessage(isVisible: Boolean, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth()) {
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(),
            exit = slideOutVertically()
        ) {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .background(color = Color(0xFF2DC139), shape = RoundedCornerShape(6.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_check_circle),
                    contentDescription = null,
                    tint = Color.White
                )

                Text(
                    text = stringResource(R.string.save_image_success),
                    color = Color.White,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
fun BottomSheetButtonSave(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Button(
        modifier = modifier.fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding( bottom = 12.dp),
        onClick = {
                  onClick()
        },
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.save),
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight(700)
            )

            Text(
                text = stringResource(R.string.watch_video_ads),
                color = Color(0xFFFFA8C2),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun BottomSheetButtonUnlockPro(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp)
            .padding(bottom = 30.dp),
        onClick = {
            onClick()
        },
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF191D30)
        ),
        contentPadding = PaddingValues(start = 0.dp, end = 0.dp, top = 12.dp, bottom = 12.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.align(Alignment.Center)
            ) {
                Text(
                    text = stringResource(R.string.unlock_pro),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight(700)
                )

                Text(
                    text = stringResource(R.string.no_ads_unlimited_access),
                    color = Color(0xFF8C8E97),
                    style = MaterialTheme.typography.bodySmall
                )
            }


            Row(
                modifier =Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp)
                    .background(
                        brush = Brush.linearGradient(gradientColor),
                        shape = RoundedCornerShape(20.dp)
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_premium_title),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp).padding(start = 6.dp)
                )

                Text(
                    text = stringResource(R.string.pro),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = Color.White,
                    modifier = Modifier
                        .padding(horizontal = 4.dp, vertical = 5.dp)

                )
            }
        }
    }
}

@Preview
@Composable
fun UnlockProPreview() {
    ArtGenTheme {
        BottomSheetButtonUnlockPro {  }
    }
}