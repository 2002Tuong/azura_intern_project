package com.artgen.app.ui.screen.main

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberImagePainter
import coil.size.Scale
import com.artgen.app.R
import com.artgen.app.ads.LoadState
import com.artgen.app.ads.MediumNativeAdWrapper
import com.artgen.app.ads.NativeAdWrapper
import com.artgen.app.data.model.ArtStyle
import com.artgen.app.ui.screen.rating.RatingDialog
import com.artgen.app.ui.screen.updateapp.AppUpdateChecker
import com.artgen.app.ui.screen.updateapp.AppUpdateDialog
import com.artgen.app.ui.theme.Neutral900
import com.artgen.app.utils.LocalAdsManager
import com.artgen.app.utils.LocalRatingManager
import com.artgen.app.utils.LocalShareController
import com.artgen.app.utils.navigationBarsPaddingIfNeed
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StylePickerScreen(
    modifier: Modifier = Modifier,
    onNavigateToImagePicker: (ArtStyle) -> Unit,
    onNavigateToSetting: () -> Unit,
    onNavigateUp: () -> Unit,
    onNavigateToPremium: () -> Unit,
    viewModel: StylePickerViewModel = koinViewModel()
) {
    val ratingManager = LocalRatingManager.current
    BackHandler(true) {
        if (ratingManager.canShowRate(isExitApp = true)) {
            viewModel.showRating()
        } else {
            onNavigateUp.invoke()
        }
    }
    val adsManager = LocalAdsManager.current
    val nativeAd by adsManager.stylePickerNativeAd.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        adsManager.loadImagePickerNativeAd()
    }

    val shareController = LocalShareController.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isShowingRatingDialog) {
        RatingDialog(
            onDismissRequest = {
                viewModel.hideRating()
                onNavigateUp.invoke()
            },
            onRateClick = { rateStar, feedback ->
                viewModel.hideRating()
                if (rateStar >= 4) {
                    ratingManager.requestReviewStore(onNavigateUp)
                } else {
                    shareController.sendFeedback(feedback)
                    onNavigateUp.invoke()
                }
            }
        )
    }
    RequestNotificationPermissionIfNeeded() {
        viewModel.showMenuNotification()
    }

    val updateType by viewModel.updateType.collectAsStateWithLifecycle(
        minActiveState = Lifecycle.State.RESUMED
    )

    if (updateType is AppUpdateChecker.UpdateType.RequireUpdate) {
        AppUpdateDialog(
            title = (updateType as AppUpdateChecker.UpdateType.RequireUpdate).title,
            message = (updateType as AppUpdateChecker.UpdateType.RequireUpdate).message,
            isCancellable = !(updateType as AppUpdateChecker.UpdateType.RequireUpdate).forceUpdate,
            onDismissRequest = {
                viewModel.clearUpdateType()
            },
            onConfirmClick = {
                shareController.openAppStore((updateType as AppUpdateChecker.UpdateType.RequireUpdate).url)
            }
        )
    }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        TopAppBar(title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                }) {
                    Icon(
                        modifier = Modifier.size(32.dp),
                        painter = painterResource(id = R.drawable.ic_logo),
                        contentDescription = null,
                        tint = Color.Unspecified
                    )
                }

                Text(text = stringResource(R.string.genart_ai), style = TextStyle(
                    fontSize = 20.sp,
                    lineHeight = 30.sp,
                    fontFamily = FontFamily(Font(R.font.roboto)),
                    fontWeight = FontWeight(800),
                ), modifier = Modifier
                    .graphicsLayer(alpha = 0.99f)
                    .drawWithCache {
                        val brush = Brush.horizontalGradient(
                            listOf(
                                Color(0xFFDF4746), Color(0xFFA02727)
                            )
                        )
                        onDrawWithContent {
                            drawContent()
                            drawRect(brush, blendMode = BlendMode.SrcAtop)
                        }
                    })
            }

        }, colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Neutral900
        ), actions = {
            IconButton(onClick = onNavigateToSetting) {
                Image(
                    painter = painterResource(id = R.drawable.ic_setting),
                    contentDescription = null,
                    modifier = Modifier.size(21.dp),
                    contentScale = ContentScale.Crop
                )
            }

            IconButton(onClick = {
               onNavigateToPremium()
            }) {
                Image(painter = painterResource(id = R.drawable.ic_premium_title),
                    contentDescription = null)
            }

        })

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            StylePickerContent(
                uiState = uiState,
                nativeAd = nativeAd,
                onSelectStyle = { artStyle ->
                    viewModel.setSelectedArtStyle(artStyle)
                },
                onNavigateToImagePicker = {
                    if (uiState.selectedArtStyle != null) {
                        adsManager.loadStylePickerNativeAd(true)
                        onNavigateToImagePicker(uiState.selectedArtStyle!!)
                    }
                }
            )
        }
    }
}

@Composable
fun StylePickerContent(
    modifier: Modifier = Modifier,
    uiState: StylePickerViewModel.UiState,
    nativeAd: NativeAdWrapper?,
    onSelectStyle: (ArtStyle) -> Unit,
    onNavigateToImagePicker: () -> Unit
) {

    var selectedArtStyle by remember { mutableStateOf<ArtStyle?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.pick_a_style), style = TextStyle(
                    fontSize = 20.sp,
                    lineHeight = 30.sp,
                    fontFamily = FontFamily(Font(R.font.roboto)),
                    fontWeight = FontWeight(800),
                    color = Color.White,
                )
            )

            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = {
                if (uiState.selectedArtStyle != null)
                    onNavigateToImagePicker()
            }) {
                Image(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(id = R.drawable.ic_arrow_right),
                    contentDescription = "",
                    colorFilter = ColorFilter.tint(Color.White),
                    alpha = if (uiState.selectedArtStyle != null) 1f else 0.4f
                )
            }

        }

        SelectStyle(
            uiState = uiState,
            nativeAd = nativeAd,
            selectedArtStyle = uiState.selectedArtStyle,
            onSelectStyle = { style ->
                selectedArtStyle = style
                onSelectStyle(style)
            }
        )
    }
}

@Composable
fun SelectStyle(
    uiState: StylePickerViewModel.UiState,
    nativeAd: NativeAdWrapper?,
    selectedArtStyle: ArtStyle?,
    onSelectStyle: (ArtStyle) -> Unit,
    modifier: Modifier = Modifier,
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPaddingIfNeed()
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = modifier
                .fillMaxSize()
                .padding(vertical = 8.dp)
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            items(uiState.artStyleList) { style ->

                val isSelected = style == selectedArtStyle

                Box(modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .border(
                        width = if (isSelected) 2.dp else 0.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable {
                        onSelectStyle(style)
                    }) {
                    Image(
                        painter = rememberImagePainter(data = style.thumbnailUrl, builder = {
                            scale(Scale.FIT)
                        }), contentDescription = null, // Set proper content description if needed
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Transparent,
                                        Color.Black
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxSize()
                    ) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Spacer(modifier = Modifier.weight(1f))
                            if (isSelected) {
                                Image(
                                    modifier = Modifier.size(21.dp),
                                    painter = painterResource(id = R.drawable.ic_check_circle),
                                    contentDescription = ""
                                )
                            }
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = style.name, style = TextStyle(
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                fontFamily = FontFamily(Font(R.font.roboto)),
                                fontWeight = FontWeight(700),
                                color = Color.White,
                            )
                        )
                    }


                }


            }
        }

        if (nativeAd != null && nativeAd.loadState != LoadState.FAILED) {
            MediumNativeAdWrapper(
                nativeAd = nativeAd,
                modifier = Modifier
                    .navigationBarsPaddingIfNeed()
                    .fillMaxWidth()
                    .wrapContentHeight(),
                onLoadAd = {}
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestNotificationPermissionIfNeeded(onPermissionGranted: () -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionState = rememberMultiplePermissionsState(
            listOf(
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.SCHEDULE_EXACT_ALARM
            )
        )
        LaunchedEffect(permissionState.allPermissionsGranted) {
            if (!permissionState.allPermissionsGranted) {
                permissionState.launchMultiplePermissionRequest()
            } else {
                onPermissionGranted()
            }
        }
    } else {
        onPermissionGranted()
    }
}

@Composable
@Preview
fun PreviewMainScreen() {
    StylePickerScreen(
        onNavigateToImagePicker = {},
        onNavigateToSetting = {},
        onNavigateToPremium = {},
        onNavigateUp = {})
}
