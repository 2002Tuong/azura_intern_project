package com.parallax.hdvideo.wallpapers.ads

/**
 * type = 0 is interstitial
 *  state is
 *  : 0 onAdLoaded
 *  : 1 onAdClosed
 *  : 3: onAdOpened
 *  : 4 : onAdFailedToLoad
 *
 *  type = 1 is rewarded
 *  state is
 *   0 : RewardedAdClosed
 *  : 1 User Earned Reward
 *  : 3: onAdOpened
 *  : 4 : onAdFailedToLoad
 */
data class AdWrapper(val type: Int = 0, val state: Int, val tag: String)