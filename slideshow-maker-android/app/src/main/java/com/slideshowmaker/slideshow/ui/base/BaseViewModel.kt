package com.slideshowmaker.slideshow.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.Purchase
import com.slideshowmaker.slideshow.data.SubscriptionRepository
import com.slideshowmaker.slideshow.data.local.SharedPreferUtils
import com.slideshowmaker.slideshow.data.response.BillingPlan
import com.slideshowmaker.slideshow.utils.extentions.orFalse
import com.slideshowmaker.slideshow.utils.extentions.parseDuration
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import java.util.*

open class BaseViewModel(open val subscriptionRepos: SubscriptionRepository): ViewModel() {
    fun saveSubscriptionDetail(purchased: Pair<Purchase, BillingPlan?>?) {
        viewModelScope.launch {
            SharedPreferUtils.setProUser(purchased != null)
        }
        SharedPreferUtils.isLifetime = purchased?.second?.isProduct.orFalse()
        SharedPreferUtils.subscriptionType = purchased?.second?.name.orEmpty()
        val purchaseTime = purchased?.first?.purchaseTime
        val offerBillingPeriod =
            purchased?.second?.offerBillingPeriod
        if (purchaseTime != null && offerBillingPeriod != null) {
            val datePurchased = Date(purchaseTime)
            val periodTime = parseDuration(offerBillingPeriod)
            val calendarInstance = Calendar.getInstance()
            calendarInstance.time = datePurchased
            calendarInstance.add(Calendar.YEAR, periodTime.years)
            calendarInstance.add(Calendar.MONTH, periodTime.months)
            calendarInstance.add(Calendar.DATE, periodTime.days)
            calendarInstance.add(Calendar.HOUR, periodTime.hours)
            calendarInstance.add(Calendar.MINUTE, periodTime.minutes)
            val delay = calendarInstance.timeInMillis - System.currentTimeMillis()
            if (delay > 0) {
                viewModelScope.launch {
                    launch {
                        delay(delay)
                    }
                }
            }
        }
    }

    fun checkSubscriptionStatus() {
        viewModelScope.launch {
            combine(
                subscriptionRepos.purchaseFlow.filterNotNull(),
                subscriptionRepos.billingPlanListFlow.filterNotNull(),
            ) { purchases, billingPlans ->
                purchases.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
                    .map { purchase ->
                        Pair(
                            purchase,
                            billingPlans.firstOrNull {
                                it.productId == purchase.products.firstOrNull()
                            },
                        )
                    }
            }
                .collectLatest(::onSubscriptionStatus)
        }
    }

    protected open fun onSubscriptionStatus(
        purchaseToBillingPlans: List<Pair<Purchase, BillingPlan?>>,
    ) {
        saveSubscriptionDetail(purchaseToBillingPlans.firstOrNull())
    }
}