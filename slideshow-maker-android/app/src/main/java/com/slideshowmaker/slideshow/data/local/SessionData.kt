package com.slideshowmaker.slideshow.data.local

object SessionData {
    private val usedTransitionIdList = mutableSetOf<String>()
    var appOpenAdsShown = false
    fun markTransitionUsed(transitionId: String) = usedTransitionIdList.add(transitionId)
    fun isTransitionUsed(transitionId: String) = usedTransitionIdList.contains(transitionId)
    fun clear() {
        usedTransitionIdList.clear()
    }
}