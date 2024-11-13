package com.slideshowmaker.slideshow.ui

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.lifecycle.Lifecycle
import com.slideshowmaker.slideshow.data.SubscriptionRepository
import com.slideshowmaker.slideshow.data.local.SharedPreferUtils.PRO_USER_KEY
import com.slideshowmaker.slideshow.data.local.dataStore
import com.slideshowmaker.slideshow.ui.base.BaseViewModel
import com.slideshowmaker.slideshow.utils.extentions.orFalse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HomeScreenViewModel(override val subscriptionRepos: SubscriptionRepository, context: Context) :
    BaseViewModel(subscriptionRepos) {


    val isVIpUserFlow: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[booleanPreferencesKey(PRO_USER_KEY)].orFalse() }

    init {
        checkSubscriptionStatus()
    }

    fun addObserver(lifecycle: Lifecycle) {
        lifecycle.addObserver(subscriptionRepos)
    }
}