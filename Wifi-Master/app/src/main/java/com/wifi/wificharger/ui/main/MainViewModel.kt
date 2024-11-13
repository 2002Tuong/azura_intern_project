package com.wifi.wificharger.ui.main

import androidx.lifecycle.ViewModel
import com.wifi.wificharger.data.local.AppDataStore

class MainViewModel(
//    private val billingClientLifecycle: BillingClientLifecycle,
    private val dataStore: AppDataStore,
) : ViewModel() {

    fun observePurchases() {
//        viewModelScope.launch {
//            billingClientLifecycle.purchasesFlow.collectLatest { purchases ->
//                if (purchases == null) {
//                    return@collectLatest
//                }
//                val isPurchased = purchases.any {
//                    it.isAcknowledged && it.purchaseState == Purchase.PurchaseState.PURCHASED
//                }
//                dataStore.setPurchase(isPurchased)
//            }
//        }
    }
}
