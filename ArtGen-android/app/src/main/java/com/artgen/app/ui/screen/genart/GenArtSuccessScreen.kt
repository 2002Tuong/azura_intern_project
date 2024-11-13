package com.artgen.app.ui.screen.genart

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberImagePainter
import coil.size.Scale
import com.artgen.app.R
import com.artgen.app.ads.AdsManager
import com.artgen.app.ads.LoadState
import com.artgen.app.ads.Medium3NativeAd
import com.artgen.app.ui.theme.VampireBlack
import com.artgen.app.utils.LocalAdsManager
import com.artgen.app.utils.navigationBarsPaddingIfNeed
import org.koin.androidx.compose.koinViewModel

@Composable
fun GenArtSuccessScreen(
    uri: Uri,
    onNavigateToResult: () -> Unit,
    onNavigateUp:()->Unit,
    modifier: Modifier = Modifier,
    viewModel: GenArtSuccessViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val nativeAd by LocalAdsManager.current.allDoneNativeAd.collectAsStateWithLifecycle()
    val adsManager: AdsManager = LocalAdsManager.current

    LaunchedEffect(Unit) {
        adsManager.loadResultNativeAd()
        adsManager.loadSaveArtRewardedAds()
    }

    BackHandler(true) {
        adsManager.loadAllDoneNativeAd(true)
        onNavigateUp()
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPaddingIfNeed()
            .background(VampireBlack)
    ) {
        IconButton(
            onClick = {
                adsManager.loadAllDoneNativeAd(true)
                onNavigateToResult()
            },
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(
                imageVector = Icons.Outlined.Done,
                contentDescription = null,
                tint = Color.White
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 0.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.celebrave),
                    contentDescription = null
                )

                Image(
                    painter = painterResource(id = R.drawable.check_circle),
                    contentDescription = null
                )

                Text(
                    text = stringResource(id = R.string.gen_art_success_title),
                    modifier = Modifier.padding(top = 15.dp),
                    style = MaterialTheme.typography.headlineSmall,
                    fontSize = 20.sp,
                    color = Color.White
                )

                if (nativeAd != null && nativeAd?.loadState != LoadState.FAILED) {
                    Medium3NativeAd(
                        nativeAd = nativeAd!!,
                        modifier = Modifier
                            .navigationBarsPadding()
                            .padding(horizontal = 8.dp)
                            .padding(top = 10.dp),
                        onLoadAd = {}
                    )
                }

                Image(
                    painter = rememberImagePainter(data = uiState.style?.thumbnailUrl, builder = {
                        scale(Scale.FIT)
                    }),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 12.dp)
                        .size(150.dp)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp))
                )

                Text(
                    text = stringResource(id = R.string.view_now),
                    modifier = Modifier
                        .padding(top = 12.dp)
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
                        }
                        .clickable {
                            onNavigateToResult()
                        },
                    style = TextStyle(
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        fontFamily = FontFamily(Font(R.font.roboto)),
                        fontWeight = FontWeight(700),
                    ),
                )
            }
        }
    }
}

@Composable
@Preview
fun GenArtSuccessScreenPreview() {
    GenArtSuccessScreen(Uri.EMPTY, {},{})
}
