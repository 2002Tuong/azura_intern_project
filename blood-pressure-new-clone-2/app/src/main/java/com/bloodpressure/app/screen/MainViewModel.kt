package com.bloodpressure.app.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.Purchase
import com.bloodpressure.app.ads.BillingClientLifecycle
import com.bloodpressure.app.data.local.AppDataStore
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainViewModel(
    private val billingClientLifecycle: BillingClientLifecycle,
    private val dataStore: AppDataStore,
) : ViewModel() {

    fun observePurchases() {
        viewModelScope.launch {
            billingClientLifecycle.purchasesFlow.collectLatest { purchases ->
                if (purchases == null) {
                    return@collectLatest
                }
                val isPurchased = purchases.any {
                    it.isAcknowledged && it.purchaseState == Purchase.PurchaseState.PURCHASED
                }
                dataStore.setPurchase(isPurchased)
            }
        }
    }
}
