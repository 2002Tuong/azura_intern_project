package com.slideshowmaker.slideshow.data

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Build
import android.provider.Settings
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
import com.slideshowmaker.slideshow.BuildConfig
import com.slideshowmaker.slideshow.data.local.SharedPreferUtils
import com.slideshowmaker.slideshow.data.remote.VideoMakerServerInterface
import com.slideshowmaker.slideshow.data.response.BillingPlan
import com.slideshowmaker.slideshow.data.response.PremiumPlan
import com.slideshowmaker.slideshow.utils.extentions.orFalse
import com.slideshowmaker.slideshow.utils.extentions.safeApiCall
import java.math.BigInteger
import java.security.MessageDigest
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import timber.log.Timber


class SubscriptionRepository(
    private val context: Context,
    private val remoteConfig: RemoteConfigRepository,
    private val call: VideoMakerServerInterface,
) : DefaultLifecycleObserver, PurchasesUpdatedListener, BillingClientStateListener {

    private var discountPercentCache: Int = DISCOUNT_PERCENT_NOT_LOADED

    private val billingClientScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _purchaseFlow = MutableStateFlow<List<Purchase>?>(null)
    val purchaseFlow: StateFlow<List<Purchase>?> get() = _purchaseFlow

    private val _billingPlanListFlow: MutableStateFlow<List<BillingPlan>?> = MutableStateFlow(null)
    val billingPlanListFlow: StateFlow<List<BillingPlan>?> get() = _billingPlanListFlow

    private var _acknowledgingPurchase = MutableStateFlow(false)
    val acknowledgingPurchase: StateFlow<Boolean> get() = _acknowledgingPurchase

    private var billingClient: BillingClient? = null

    private var retryCount = 0

    override fun onCreate(owner: LifecycleOwner) {
        initBillingClientAndStartConnection()
    }

    private fun initBillingClientAndStartConnection() {
        if (billingClient == null ||
            billingClient?.connectionState == BillingClient.ConnectionState.CLOSED
        ) {
            billingClient =
                BillingClient.newBuilder(context).setListener(this).enablePendingPurchases().build()
        }
        if (!billingClient?.isReady.orFalse()) {
            billingClient?.startConnection(this)
        }
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            Timber.d("onBillingSetupFinished with status code ${billingResult.responseCode} ${billingResult.debugMessage}")
            billingClientScope.launch {
                queryBillingPlans()
            }
            billingClientScope.launch {
                refreshPurchases()
            }
        } else {
            Timber.d("onBillingSetupFinished with status code ${billingResult.responseCode} ${billingResult.debugMessage}")
        }
    }

    override fun onBillingServiceDisconnected() {
        retryToConnect()
    }

    private fun retryToConnect() {
        if (retryCount < 5) {
            billingClient?.startConnection(this)
            retryCount++
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        if (isBillingClientReady()) {
            billingClient?.endConnection()
            billingClient = null
        }
    }

    fun launchBillingFlow(activity: Activity, params: BillingFlowParams): BillingResult {
        if (!isBillingClientReady()) {
            initBillingClientAndStartConnection()
            return BillingResult.newBuilder()
                .setResponseCode(BillingClient.BillingResponseCode.SERVICE_DISCONNECTED)
                .setDebugMessage("BillingClient is not ready").build()
        }
        return billingClient!!.launchBillingFlow(activity, params)
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?,
    ) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            processPurchases(purchases)
        } else {

        }
    }

    private fun processPurchases(purchasesList: List<Purchase>?) {
        if (purchasesList == null || isUnchangedPurchaseList(purchasesList)) {
            billingClientScope.launch {
                _purchaseFlow.emit(purchasesList.orEmpty())
            }
            return
        }
        billingClientScope.launch {
            var shouldRefreshPurchases = false
            _acknowledgingPurchase.tryEmit(purchasesList.any { !it.isAcknowledged })
            purchasesList.filter { !it.isAcknowledged }.forEach {
                if (acknowledgePurchase(it.purchaseToken)) {
                    shouldRefreshPurchases = true
                    //sendTransactionRecord(it.products.firstOrNull().orEmpty())
                }
            }
            if (shouldRefreshPurchases) {
                refreshPurchases()
            }
            _acknowledgingPurchase.tryEmit(false)
            _purchaseFlow.emit(purchasesList)
        }
    }

    private fun isUnchangedPurchaseList(purchasesList: List<Purchase>): Boolean {
        return purchasesList == purchaseFlow.value
    }

    private suspend fun acknowledgePurchase(
        purchaseToken: String,
    ): Boolean = withContext(Dispatchers.IO) {
        val params = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchaseToken).build()
        val billingResult = billingClient?.acknowledgePurchase(params)
        billingResult?.responseCode == BillingClient.BillingResponseCode.OK
    }

    private suspend fun queryPurchasesByType(type: String): List<Purchase>? {
        if (!isBillingClientReady()) {
            initBillingClientAndStartConnection()
            return null
        }
        return withContext(Dispatchers.IO) {
            val params = QueryPurchasesParams.newBuilder().setProductType(type).build()
            val purchasesResult = billingClient?.queryPurchasesAsync(params)
            purchasesResult?.purchasesList?.takeIf {
                purchasesResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK
            }
        }
    }

    private suspend fun refreshPurchases() {
        return coroutineScope {
            listOf(
                async { queryPurchasesByType(BillingClient.ProductType.SUBS) },
                async { queryPurchasesByType(BillingClient.ProductType.INAPP) },
            ).awaitAll()
                .filterNotNull()
                .takeIf { it.isNotEmpty() }
                ?.flatten()
                ?.let { purchases ->
                    processPurchases(purchases)
                }
        }
    }

    fun isBillingClientReady(): Boolean = billingClient != null && billingClient?.isReady.orFalse()

    suspend fun queryBillingPlans() {
        if (!isBillingClientReady()) {
            initBillingClientAndStartConnection()
            return
        }
        val billingPlans = if (isSupportProductDetails()) {
            queryProductDetails()
        } else {
            querySkuDetails()
        }
        _billingPlanListFlow.update { billingPlans }
    }

    private suspend fun queryProductDetails(): List<BillingPlan> {
        return supervisorScope {
            listOf(
                async {
                    queryProductDetailsByType(
                        BillingClient.ProductType.SUBS,
                        remoteConfig.premiumPlans.orEmpty().filter { it.isSubscription }
                            .map { it.productId.orEmpty() },
                    )
                },
                async {
                    queryProductDetailsByType(
                        BillingClient.ProductType.INAPP,
                        remoteConfig.premiumPlans.orEmpty().filter { it.isInApp }
                            .map { it.productId.orEmpty() },
                    )
                },
            ).awaitAll()
                .filterNotNull()
                .map {
                    handleProductDetailsResult(
                        it,
                        RemoteConfigRepository.premiumPlans.orEmpty(),
                    )
                }.flatten().map { product ->
                    val premiumPlan = remoteConfig.premiumPlans?.firstOrNull {
                        it.productId == product.productId
                    }
                    BillingPlan(premiumPlan = premiumPlan, product = product)
                }
        }
    }

    private suspend fun querySkuDetails(): List<BillingPlan> {
        return supervisorScope {
            listOf(
                async {
                    querySkuDetailsByType(
                        BillingClient.SkuType.SUBS,
                        remoteConfig.premiumPlans.orEmpty().filter { it.isSubscription }
                            .map { it.productId.orEmpty() },
                    )
                },
                async {
                    querySkuDetailsByType(
                        BillingClient.SkuType.INAPP,
                        remoteConfig.premiumPlans.orEmpty().filter { it.isInApp }
                            .map { it.productId.orEmpty() },
                    )
                },
            ).awaitAll()
                .filterNotNull()
                .map {
                    handleSkuDetailsResult(it, remoteConfig.premiumPlans.orEmpty())
                }
                .flatten()
                .map { sku ->
                    val premiumPlan = remoteConfig.premiumPlans?.firstOrNull {
                        it.productId == sku.sku
                    }
                    BillingPlan(premiumPlan = premiumPlan, sku = sku)
                }
        }
    }

    private suspend fun queryProductDetailsByType(
        productType: String,
        productIds: List<String>,
    ): ProductDetailsResult? {
        if (productIds.isEmpty()) {
            return ProductDetailsResult(BillingResult(), emptyList())
        }
        val productList = productIds.map {
            QueryProductDetailsParams.Product.newBuilder().setProductId(it)
                .setProductType(productType).build()
        }
        val params = QueryProductDetailsParams.newBuilder().setProductList(productList).build()
        return withContext(Dispatchers.IO) {
            billingClient?.queryProductDetails(params)
        }
    }

    private suspend fun querySkuDetailsByType(
        skuType: String,
        productIds: List<String>,
    ): SkuDetailsResult? {
        if (productIds.isEmpty()) {
            return SkuDetailsResult(BillingResult(), emptyList())
        }
        val params = SkuDetailsParams.newBuilder().setSkusList(productIds).setType(skuType).build()
        return withContext(Dispatchers.Main) {
            billingClient?.querySkuDetails(params)
        }
    }

    private fun handleProductDetailsResult(
        productDetailsResult: ProductDetailsResult,
        products: List<PremiumPlan>,
    ): List<ProductDetails> {
        return productDetailsResult.productDetailsList.orEmpty().sortedBy { product ->
            products.indexOfFirst { it.productId == product.productId }
        }
    }

    private fun handleSkuDetailsResult(
        skuDetailsResult: SkuDetailsResult,
        products: List<PremiumPlan>,
    ): List<SkuDetails> {
        return skuDetailsResult.skuDetailsList.orEmpty().sortedBy { sku ->
            products.indexOfFirst { it.productId == sku.sku }
        }
    }

    private fun isSupportProductDetails(): Boolean {
        return billingClient?.isFeatureSupported(BillingClient.FeatureType.PRODUCT_DETAILS)?.responseCode == BillingClient.BillingResponseCode.OK
    }

    fun getSubscriptionManagementUrl(packageName: String, productId: String) = String.format(
        "https://play.google.com/store/account/subscriptions?sku=%s&package=%s",
        productId,
        packageName,
    )

    suspend fun getDiscountPercent(): Int {
        if (discountPercentCache != DISCOUNT_PERCENT_NOT_LOADED) {
            return discountPercentCache
        }

        val productIds =
            RemoteConfigRepository.premiumPlans.orEmpty().map { it.productId.orEmpty() }
                .filter { it.isNotEmpty() }

        if (productIds.isEmpty()) {
            discountPercentCache = 0
        } else {
            val billingPlans = _billingPlanListFlow.filterNotNull().firstOrNull()
            if (billingPlans != null) {
                var maxDiscountPercent = 0
                billingPlans.filter { it.hasDiscount() }.forEach { billingPlan ->
                    val discountPercent = billingPlan.discountPercent
                    if (discountPercent > maxDiscountPercent) {
                        maxDiscountPercent = discountPercent
                    }
                }
                discountPercentCache = maxDiscountPercent
            } else {
                discountPercentCache = 0
            }
        }

        return discountPercentCache
    }

    @SuppressLint("HardwareIds")
    fun sendTransactionRecord(id: String) {
        if (BuildConfig.FLAVOR != "PROD") {
            return
        }
        billingClientScope.launch(Dispatchers.IO) {
            try {
                val deviceId = Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.ANDROID_ID,
                )
                val os = "android"
                val country = SharedPreferUtils.countryCode ?: Locale.getDefault().country
                val currentTime = System.currentTimeMillis() / 1000L
                val phoneName = Build.MODEL
                val osVersion = Build.VERSION.RELEASE
                val version = BuildConfig.VERSION_NAME
                val isExtend = SUBSCRIPTION_TYPE_NEW
                val signature = md5(
                    listOf(
                        deviceId,
                        os,
                        id,
                        country,
                        currentTime,
                        phoneName,
                        osVersion,
                        version,
                    ).joinToString("|"),
                )
                safeApiCall {
                    call.acknowledgeTransactionAsync(
                        deviceId = deviceId,
                        subId = id,
                        country = country,
                        currentTime = currentTime,
                        phoneName = phoneName,
                        osVersion = osVersion,
                        version = version,
                        extend = isExtend,
                        signature = signature,
                    )
                }
            } catch (exception: Exception) {
            }
        }
    }

    private fun md5(input: String): String {
        val messageDigest = MessageDigest.getInstance("MD5")
        return BigInteger(1, messageDigest.digest(input.toByteArray())).toString(16).padStart(32, '0')
    }

    companion object {
        private const val DISCOUNT_PERCENT_NOT_LOADED = -1
        private const val SUBSCRIPTION_TYPE_NEW = 0
    }
}
