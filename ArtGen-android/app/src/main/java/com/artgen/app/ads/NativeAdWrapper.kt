package com.artgen.app.ads

import com.google.android.gms.ads.nativead.NativeAd

data class NativeAdWrapper(
    val tag: Any,
    val nativeAd: NativeAd?,
    val loadState: LoadState = LoadState.LOADING
) {
    fun destroy() {
        nativeAd?.destroy()
    }

}

enum class LoadState {
    LOADING, SUCCESS, FAILED
}