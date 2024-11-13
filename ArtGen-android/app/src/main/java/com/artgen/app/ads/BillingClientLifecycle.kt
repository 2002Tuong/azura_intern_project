package com.artgen.app.ads

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetailsResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.SkuDetails
import com.android.billingclient.api.SkuDetailsParams
import com.android.billingclient.api.SkuDetailsResult
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.android.billingclient.api.querySkuDetails
import com.artgen.app.data.model.PremiumPlan
import com.artgen.app.data.remote.RemoteConfig
import com.artgen.app.log.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext

class BillingClientLifecycle(
    private val context: Context,
    private val remoteConfig: RemoteConfig,
) : DefaultLifecycleObserver,
    BillingClientStateListener,
    PurchasesUpdatedListener {

    private val billingClientScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _purchasesFlow = MutableStateFlow<List<Purchase>?>(null)
    val purchasesFlow: StateFlow<List<Purchase>?> get() = _purchasesFlow

    private val _premiumPlans: MutableStateFlow<List<PremiumPlan>?> = MutableStateFlow(null)
    val premiumPlans: StateFlow<List<PremiumPlan>?> get() = _premiumPlans

    private var _acknowledgingPurchases = MutableStateFlow(false)
    val acknowledgingPurchases: StateFlow<Boolean> get() = _acknowledgingPurchases

    private lateinit var billingClient: BillingClient
    private var retryCount = 0

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        if (!billingClient.isReady) {
            billingClient.startConnection(this)
        }
    }

    override fun onBillingServiceDisconnected() {
        retryToConnect()
    }

    override fun onBillingSetupFinished(p0: BillingResult) {
        if(p0.responseCode == BillingClient.BillingResponseCode.OK) {
            billingClientScope.launch {
                remoteConfig.waitRemoteConfigLoaded()
                queryPremiumPlans()
            }

            billingClientScope.launch {
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

    override fun onDestroy(owner: LifecycleOwner) {
        if (billingClient.isReady) {
            billingClient.endConnection()
        }
    }

    fun launchBillingFlow(context: Context, params: BillingFlowParams): Int {
        return context.findActivity()?.let {
            launchBillingFlow(it, params)
        } ?: BillingClient.BillingResponseCode.ERROR
    }

    private fun launchBillingFlow(activity: Activity, params: BillingFlowParams): Int {
        if(!billingClient.isReady) {
            Logger.e(Throwable("launchBillingFlow: BillingClient is not ready"))
            billingClient.startConnection(this)
            return BillingClient.BillingResponseCode.SERVICE_DISCONNECTED
        }
        val billingResult = billingClient.launchBillingFlow(activity, params)
        return billingResult.responseCode
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            processPurchases(purchases)
        } else {
            Logger.e(Throwable("onPurchasesUpdated: ${billingResult.responseCode} -- ${billingResult.debugMessage}"))
        }
    }

    private fun processPurchases(purchasesList: List<Purchase>?) {
        if (purchasesList == null || isUnchangedPurchaseList(purchasesList)) {
            return
        }
        billingClientScope.launch {
            var shouldRefreshPurchases = false
            _acknowledgingPurchases.tryEmit(purchasesList.any { !it.isAcknowledged })
            purchasesList.filter { !it.isAcknowledged }.forEach {
                if (acknowledgePurchase(it.purchaseToken)) {
                    shouldRefreshPurchases = true
                }
            }
            if (shouldRefreshPurchases) {
                refreshPurchases()
                return@launch
            }
            _acknowledgingPurchases.tryEmit(false)
            _purchasesFlow.emit(purchasesList)
        }
    }

    private fun isUnchangedPurchaseList(purchasesList: List<Purchase>): Boolean {
        return purchasesList == purchasesFlow.value
    }

    private suspend fun acknowledgePurchase(
        purchaseToken: String
    ): Boolean = withContext(Dispatchers.IO) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()
        val billingResult = billingClient.acknowledgePurchase(params)
        billingResult.responseCode == BillingClient.BillingResponseCode.OK
    }

    private suspend fun queryPurchasesByType(type: String): List<Purchase> {
        if (!billingClient.isReady) {
            billingClient.startConnection(this)
        }
        return withContext(Dispatchers.IO) {
            val params = QueryPurchasesParams.newBuilder().setProductType(type).build()
            val purchasesResult = billingClient.queryPurchasesAsync(params)
            purchasesResult.purchasesList
        }
    }

    private suspend fun refreshPurchases() {
        return coroutineScope {
            val purchases = listOf(
                async { queryPurchasesByType(BillingClient.ProductType.SUBS) },
                async { queryPurchasesByType(BillingClient.ProductType.INAPP) }
            )
                .awaitAll()
                .flatten()
            processPurchases(purchases)
        }
    }

    private suspend fun queryPremiumPlans() {
        val premiumPlans = if (isSupportProductDetails()) {
            queryProductDetails()
        } else {
            querySkuDetails()
        }
        _premiumPlans.update { premiumPlans }
    }

    private suspend fun queryProductDetails(): List<PremiumPlan> {
        return supervisorScope {
            listOf(
                async {
                    queryProductDetailsByType(
                        BillingClient.ProductType.SUBS,
                        remoteConfig.subIds
                    )
                },
                async {
                    queryProductDetailsByType(
                        BillingClient.ProductType.INAPP,
                        remoteConfig.productIds
                    )
                }
            )
                .awaitAll()
                .map { handleProductDetailsResult(it) }
                .flatten()
                .map { product ->
                    val premiumConfig =
                        remoteConfig.premiumConfigs.firstOrNull { it.id == product.productId }
                    PremiumPlan(premiumConfig = premiumConfig, product = product)
                }
        }
    }

    private suspend fun querySkuDetails(): List<PremiumPlan> {
        return supervisorScope {
            listOf(
                async {
                    querySkuDetailsByType(
                        BillingClient.SkuType.SUBS,
                        remoteConfig.subIds
                    )
                },
                async {
                    querySkuDetailsByType(
                        BillingClient.SkuType.INAPP,
                        remoteConfig.productIds
                    )
                }
            )
                .awaitAll()
                .map { handleSkuDetailsResult(it) }
                .flatten()
                .map { sku ->
                    val premiumConfig =
                        remoteConfig.premiumConfigs.firstOrNull { it.id == sku.sku }
                    PremiumPlan(premiumConfig = premiumConfig, sku = sku)
                }
        }
    }

    private suspend fun queryProductDetailsByType(
        productType: String,
        productIds: List<String>
    ): ProductDetailsResult {
        if (productIds.isEmpty()) {
            return ProductDetailsResult(BillingResult(), emptyList())
        }
        val productList = productIds.map {
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(it)
                .setProductType(productType)
                .build()
        }
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()
        return withContext(Dispatchers.IO) {
            billingClient.queryProductDetails(params)
        }
    }

    private suspend fun querySkuDetailsByType(
        skuType: String,
        productIds: List<String>
    ): SkuDetailsResult {
        if (productIds.isEmpty()) {
            return SkuDetailsResult(BillingResult(), emptyList())
        }
        val params = SkuDetailsParams.newBuilder()
            .setSkusList(productIds)
            .setType(skuType)
            .build()
        return withContext(Dispatchers.IO) {
            billingClient.querySkuDetails(params)
        }
    }

    private fun handleProductDetailsResult(
        productDetailsResult: ProductDetailsResult
    ): List<ProductDetails> {
        return productDetailsResult.productDetailsList.orEmpty()
    }

    private fun handleSkuDetailsResult(
        skuDetailsResult: SkuDetailsResult
    ): List<SkuDetails> {
        return skuDetailsResult.skuDetailsList.orEmpty()
    }

    private fun isSupportProductDetails(): Boolean {
        return billingClient.isFeatureSupported(BillingClient.FeatureType.PRODUCT_DETAILS)
            .responseCode == BillingClient.BillingResponseCode.OK
    }

    private fun Context.findActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}