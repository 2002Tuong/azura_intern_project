package com.screentheme.app.utils.helpers

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.SkuDetailsParams
import com.screentheme.app.data.remote.config.AppRemoteConfig
import com.screentheme.app.data.remote.config.RemoteConfig
import com.screentheme.app.models.PremiumPlan

class BillingClientProvider private constructor(private val activity: Activity) : BillingClientStateListener,
    PurchasesUpdatedListener {
    private lateinit var billingClient: BillingClient
    private val premiumPlans = mutableListOf<PremiumPlan>()

    private var onPremiumPlansLoadedCallback: ((Array<PremiumPlan>) -> Unit)? = null

    private var retryCount = 0
    val isPurchased: Boolean
        get() = SharePreferenceHelper.getBoolean(activity, SharePreferenceHelper.KEY_IS_PURCHASED, false)

    fun initialize() {

        billingClient = BillingClient.newBuilder(activity).setListener(purchasesUpdatedListener).enablePendingPurchases().build()

        billingClient.startConnection(this)
    }

    private fun fetchProductDetails() {

        if (RemoteConfig == null) {
            onPremiumPlansLoadedCallback?.invoke(mutableListOf<PremiumPlan>().toTypedArray())
            return
        }

        val skuListInApp = AppRemoteConfig!!.premiumConfigs.filter { !it.isSubscription }.map { it.id }
        val skuListSubs = AppRemoteConfig!!.premiumConfigs.filter { it.isSubscription }.map { it.id }

        val paramsInApp = SkuDetailsParams.newBuilder().setType(BillingClient.ProductType.INAPP).setSkusList(skuListInApp).build()

        val paramsSubs = SkuDetailsParams.newBuilder().setType(BillingClient.ProductType.SUBS).setSkusList(skuListSubs).build()

        premiumPlans.clear()
        billingClient.querySkuDetailsAsync(paramsInApp) { billingResult, skuDetailsList ->

            if (skuDetailsList != null) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && skuDetailsList.isNotEmpty()) {
                    // SkuDetailsList contains the INAPP product details
                    // Proceed with creating PremiumPlans
                    for (premiumConfig in AppRemoteConfig!!.premiumConfigs.filter { !it.isSubscription }) {
                        val skuDetails = skuDetailsList.find { it.sku == premiumConfig.id }
                        val premiumPlan = PremiumPlan(premiumConfig, null, skuDetails)
                        premiumPlans.add(premiumPlan)
                    }
                }
            }

            billingClient.querySkuDetailsAsync(paramsSubs) { billingResult, skuDetailsList ->

                if (skuDetailsList != null) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && skuDetailsList.isNotEmpty()) {
                        // SkuDetailsList contains the SUBS product details
                        // Proceed with creating PremiumPlans
                        for (premiumConfig in AppRemoteConfig!!.premiumConfigs.filter { it.isSubscription }) {
                            val skuDetails = skuDetailsList.find { it.sku == premiumConfig.id }
                            val premiumPlan = PremiumPlan(premiumConfig, null, skuDetails)
                            premiumPlans.add(premiumPlan)
                        }
                    }
                }

                // Handle the premiumPlans list as per your requirement
                onPremiumPlansLoadedCallback?.invoke(premiumPlans.toTypedArray())
            }
        }
    }

    fun endConnection() {
        if (::billingClient.isInitialized && billingClient.isReady) {
            billingClient.endConnection()
        }
    }

    fun purchasePremiumPlan(premiumPlan: PremiumPlan) {
        val skuDetails = premiumPlan.sku
        if (skuDetails != null) {
            val billingFlowParams = BillingFlowParams.newBuilder().setSkuDetails(skuDetails).build()
            billingClient.launchBillingFlow(activity, billingFlowParams)
        }
    }

    private fun refreshPurchases() {
        val queryInAppPurchases = { purchasesList: List<Purchase> ->
        }

        val querySubscriptionPurchases = { purchasesList: List<Purchase> ->
        }

        billingClient.queryPurchasesAsync(BillingClient.ProductType.INAPP) { billingResult, purchasesListInApp ->

            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                queryInAppPurchases(purchasesListInApp)
            }

            billingClient.queryPurchasesAsync(BillingClient.ProductType.SUBS) { billingResult, purchasesListSubs ->

                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    querySubscriptionPurchases(purchasesListSubs)
                }

                val allPurchases = mutableListOf<Purchase>()
                purchasesListInApp.let { allPurchases.addAll(it) }
                purchasesListSubs.let { allPurchases.addAll(it) }

                Log.d("BillingClientHelper", "purchasesListInApp: $purchasesListInApp")
                Log.d("BillingClientHelper", "purchasesListSubs: $purchasesListSubs")

                processPurchases(allPurchases)
                validatePurchases(allPurchases)
            }
        }
    }

    private fun processPurchases(purchases: List<Purchase>) {
        for (purchase in purchases) {
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        validatePurchases(purchases)
                    }
                }
            }
        }
    }

    private fun validatePurchases(purchases: List<Purchase>) {
        val hasValidPurchase = purchases.any { purchase ->
            purchase.purchaseState == Purchase.PurchaseState.PURCHASED && purchase.isAcknowledged
        }

        SharePreferenceHelper.saveBoolean(activity, SharePreferenceHelper.KEY_IS_PURCHASED, hasValidPurchase)
    }

    fun onPremiumPlansLoaded(callback: (Array<PremiumPlan>) -> Unit) {
        onPremiumPlansLoadedCallback = callback
        if (premiumPlans.isNotEmpty()) {
            onPremiumPlansLoadedCallback?.invoke(premiumPlans.toTypedArray())
        }
    }

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
    }

    override fun onBillingServiceDisconnected() {
        retryToConnect()
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {

            fetchProductDetails()

            activity.runOnUiThread {
                refreshPurchases()
            }

        }
    }

    private fun retryToConnect() {
        if (retryCount < 5) {
            billingClient.startConnection(this)
            retryCount++
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchasesList: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchasesList != null) {
            refreshPurchases()
        }
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: BillingClientProvider? = null

        fun getInstance(activity: Activity): BillingClientProvider {
            return instance ?: synchronized(this) {
                instance ?: BillingClientProvider(activity).also { instance = it }
            }
        }
    }
}