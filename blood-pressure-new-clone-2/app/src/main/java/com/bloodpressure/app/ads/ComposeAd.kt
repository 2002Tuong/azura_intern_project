package com.bloodpressure.app.ads

import android.view.ViewGroup
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.viewinterop.AndroidView
import com.bloodpressure.app.R
import com.bloodpressure.app.utils.requestLayoutWithDelay
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.nativead.NativeAd

@Composable
fun LargeNativeAd(
    modifier: Modifier = Modifier,
    nativeAd: NativeAd,
    onLoadAd: () -> Unit = {}
) {
    Box(modifier = modifier) {
        AndroidView(
            factory = { context ->
                LargeNativeAdView(context = context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    loadAd(nativeAd)
                    onLoadAd()
                }
            },
            modifier = modifier,
            update = {
                if (it.tag != true) {
                    it.requestLayoutWithDelay()
                    it.tag = true
                }
            }
        )
    }
}

@Composable
fun LargeNativeCTATopAd(
    modifier: Modifier = Modifier,
    nativeAd: NativeAd,
    onLoadAd: () -> Unit = {}
) {
    Box(modifier = modifier) {
        AndroidView(
            factory = { context ->
                LargeNativeCtaTopAdView(context = context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    loadAd(nativeAd)
                    onLoadAd()
                }
            },
            modifier = modifier,
            update = {
                if (it.tag != true) {
                    it.requestLayoutWithDelay()
                    it.tag = true
                }
            }
        )
    }
}

@Composable
fun MediumNativeAd(
    modifier: Modifier = Modifier,
    nativeAd: NativeAd,
    onLoadAd: () -> Unit,
) {
    Box(modifier = modifier) {
        AndroidView(
            factory = { context ->
                MediumNativeAdView(context = context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    loadAd(nativeAd)
                    onLoadAd()
                }
            },
            modifier = modifier,
            update = {
                if (it.tag != true) {
                    it.requestLayoutWithDelay()
                    it.tag = true
                }
            }
        )
    }
}

@Composable
fun MediumNativeCtaTopAd(
    modifier: Modifier = Modifier,
    nativeAd: NativeAd,
    onLoadAd: () -> Unit,
) {
    Box(modifier = modifier) {
        AndroidView(
            factory = { context ->
                MediumNativeCtaTopAdView(context = context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    loadAd(nativeAd)
                    onLoadAd()
                }
            },
            modifier = modifier,
            update = {
                if (it.tag != true) {
                    it.requestLayoutWithDelay()
                    it.tag = true
                }
            }
        )
    }
}
@Composable
fun SmallNativeAd(
    modifier: Modifier = Modifier,
    nativeAd: NativeAd
) {
    Box(modifier = modifier) {
        AndroidView(
            factory = { context ->
                SmallNativeAdView(context = context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    loadAd(nativeAd)
                }
            },
            modifier = modifier,
            update = {
                if (it.tag != true) {
                    it.requestLayoutWithDelay()
                    it.tag = true
                }
            }
        )
    }
}

@Composable
fun CardItemNativeAd(
    modifier: Modifier = Modifier,
    nativeAd: NativeAd,
) {
    Box(modifier = modifier) {
        AndroidView(
            factory = { context ->
                CardItemNativeAdView(context = context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    loadAd(nativeAd)
                }
            },
            modifier = modifier,
            update = {
                if (it.tag != true) {
                    it.requestLayoutWithDelay()
                    it.tag = true
                }
            }
        )
    }
}

@Composable
fun BannerAd(modifier: Modifier = Modifier) {
    val configuration = LocalConfiguration.current
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = {
                AdView(it).apply {
                    val adSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                        it,
                        configuration.screenWidthDp
                    )
                    setAdSize(adSize)
                    adUnitId = it.resources.getString(R.string.banner)
                    loadAd(AdRequest.Builder().build())
                    adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            super.onAdLoaded()
                            requestLayout()
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun shimmerBrush(showShimmer: Boolean = true, targetValue: Float = 1000f): Brush {
    return if (showShimmer) {
        val shimmerColors = listOf(
            Color.LightGray.copy(alpha = 0.6f),
            Color.LightGray.copy(alpha = 0.2f),
            Color.LightGray.copy(alpha = 0.6f),
        )

        val transition = rememberInfiniteTransition(label = "infinite")
        val translateAnimation = transition.animateFloat(
            initialValue = 0f,
            targetValue = targetValue,
            animationSpec = infiniteRepeatable(
                animation = tween(800), repeatMode = RepeatMode.Reverse
            ),
            label = "infinite"
        )
        Brush.linearGradient(
            colors = shimmerColors,
            start = Offset.Zero,
            end = Offset(x = translateAnimation.value, y = translateAnimation.value)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color.Transparent, Color.Transparent),
            start = Offset.Zero,
            end = Offset.Zero
        )
    }
}
