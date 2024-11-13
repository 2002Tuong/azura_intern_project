package com.artgen.app.ads

import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.viewinterop.AndroidView
import com.artgen.app.R
import com.artgen.app.log.Logger
import com.artgen.app.utils.navigationBarsPaddingIfNeed
import com.artgen.app.utils.requestLayoutWithDelay
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
            modifier = modifier
        )
    }
}

@Composable
fun MediumNativeAdWrapper(
    modifier: Modifier = Modifier,
    nativeAd: NativeAdWrapper?,
    onLoadAd: () -> Unit,
    isCtaTop: Boolean = false
) {
    Box(
        modifier = modifier
            .navigationBarsPaddingIfNeed()
    ) {

        if (nativeAd?.loadState == LoadState.SUCCESS) {
            AndroidView(
                factory = { context ->
                    MediumNativeAdView(context = context, isCtaTop = isCtaTop).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        onLoadAd()
                    }
                },
                update = {
                    if (it.tag != nativeAd.tag && nativeAd.nativeAd != null) {
                        it.loadAd(nativeAd.nativeAd)
                        it.requestLayoutWithDelay(500)
                        it.tag = nativeAd.tag
                        Logger.d("MediumNativeAd update is called and updated!")
                    }
                },
                modifier = modifier
            )
        } else {
            AndroidView(factory = {
                ShimmerMediumNativeAdView(it).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                }
            })
        }
    }
}

@Composable
fun Medium2NativeAd(
    modifier: Modifier = Modifier,
    nativeAd: NativeAdWrapper,
    onLoadAd: () -> Unit,
) {
    Box(modifier = modifier) {
        if (nativeAd.loadState == LoadState.LOADING) {
            AndroidView(factory = {
                ShimmerMedium2NativeAdView(it).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                }
            })
        } else if (nativeAd.nativeAd != null) {
            AndroidView(
                factory = { context ->
                    Medium2NativeAdView(context = context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        loadAd(nativeAd.nativeAd)
                        onLoadAd()
                    }
                }, update = {
                    if (it.tag != true) {
                        it.requestLayoutWithDelay(1500L)
                        it.tag = true
                        Logger.d("MediumNativeAd update is called and updated!")
                    }
                },
                modifier = modifier
            )
        }
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
            modifier = modifier
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
                }
            }
        )
    }
}


@Composable
fun BannerAdView(modifier: Modifier = Modifier, adView: AdView) {
    var loadingState by remember { mutableStateOf(adView.isLoading) }
    Box(modifier = modifier) {
        if (loadingState) {
            AndroidView(modifier = modifier.fillMaxWidth(), factory = {
                ShimmerBannerAdView(it).apply {

                }
            }, update = {

            })
        }
        AndroidView(modifier = modifier.fillMaxWidth(), factory = {
            adView.apply {
                (parent as? ViewGroup)?.removeView(this)
                adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        super.onAdLoaded()
                        requestLayout()
                        loadingState = false
                    }
                }
            }
        }, update = {

        })
    }
}

@Composable
fun Medium3NativeAd(
    modifier: Modifier = Modifier,
    nativeAd: NativeAdWrapper,
    onLoadAd: () -> Unit,
) {
    Box(modifier = modifier) {
        if(nativeAd.loadState == LoadState.LOADING) {
            AndroidView(
                factory = {context ->
                    ShimmerMedium3NativeAdView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    }
                }
            )
        } else if (nativeAd.nativeAd != null) {
            AndroidView(
                factory = { context ->
                    Medium3NativeAdView(context = context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        loadAd(nativeAd.nativeAd)
                        onLoadAd()
                    }
                }, update = {
                    if (it.tag != true) {
                        it.requestLayoutWithDelay(1500L)
                        it.tag = true
                        Logger.d("MediumNativeAd update is called and updated!")
                    }
                },
                modifier = modifier
            )
        }
    }
}