package com.slideshowmaker.slideshow.ui.premium

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.Purchase
import com.slideshowmaker.slideshow.data.RemoteConfigRepository
import com.slideshowmaker.slideshow.data.SubscriptionRepository
import com.slideshowmaker.slideshow.data.local.SharedPreferUtils
import com.slideshowmaker.slideshow.data.models.Result
import com.slideshowmaker.slideshow.data.remote.VideoMakerServerInterface
import com.slideshowmaker.slideshow.data.response.BillingPlan
import com.slideshowmaker.slideshow.data.response.IpInfoModelSetting
import com.slideshowmaker.slideshow.ui.base.BaseViewModel
import com.slideshowmaker.slideshow.utils.extentions.orFalse
import com.slideshowmaker.slideshow.utils.extentions.safeApiCall
import java.util.Calendar
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class PremiumPlanViewModel(
    override val subscriptionRepos: SubscriptionRepository,
    private val call: VideoMakerServerInterface,
) : BaseViewModel(subscriptionRepos) {

    private val _subscriptionsState = MutableStateFlow<List<BillingPlan>>(emptyList())
    val subscriptionsState: StateFlow<List<BillingPlan>> get() = _subscriptionsState

    private val _planStateLiveData = MutableLiveData<Subscription>()
    val planStateLiveData: LiveData<Subscription> get() = _planStateLiveData

    private val _purchasesState = Channel<List<Purchase>>()
    val purchasesState: Flow<List<Purchase>> get() = _purchasesState.receiveAsFlow()

    private val _screenState = MutableStateFlow(ScreenState.HideLoading)
    val screenState: StateFlow<ScreenState> get() = _screenState

    init {
        viewModelScope.launch(Dispatchers.IO) {
            if (SharedPreferUtils.countryCode.isNullOrEmpty()) {
                val result = safeApiCall {
                    call.getIpInfoAsync()
                }
                (result as? Result.Success<IpInfoModelSetting>)?.data?.country?.takeIf { it.isNotEmpty() }
                    ?.let {
                        SharedPreferUtils.countryCode = it
                    }
            }
        }

        viewModelScope.launch {
            subscriptionRepos.acknowledgingPurchase.collectLatest {
                if (it) {
                    _screenState.emit(ScreenState.ShowLoading)
                } else {
                    _screenState.emit(ScreenState.HideLoading)
                }
            }
        }
    }

    fun queryPurchased() {
        viewModelScope.launch {
            _screenState.emit(ScreenState.ShowLoading)
            subscriptionRepos.purchaseFlow.filterNotNull().firstOrNull().let { purchases ->
                if (purchases == null) {
                    return@let
                }
                val isPurchasedSuccess =
                    purchases.any { it.purchaseState == Purchase.PurchaseState.PURCHASED }
                _screenState.emit(ScreenState.HideLoading)
                if (isPurchasedSuccess) {
                    _purchasesState.send(purchases)
                }
            }
        }
    }

    fun querySubscriptions() {
        viewModelScope.launch {
            val productIds = RemoteConfigRepository.premiumPlans.orEmpty()
                .filter { it.show }
                .sortedBy { it.index }
                .map { it.productId.orEmpty() }
                .filter { it.isNotEmpty() }
            if (productIds.isEmpty()) {
                Timber.d("empty product")
                _screenState.emit(ScreenState.APIError)
                return@launch
            }

            if (!subscriptionRepos.isBillingClientReady()) {
                Timber.d("billing is not available")
                _screenState.emit(ScreenState.APIError)
                subscriptionRepos.queryBillingPlans()
                return@launch
            }

            subscriptionRepos.billingPlanListFlow.filterNotNull().firstOrNull()
                .let { billingPlans ->
                    if (billingPlans != null && billingPlans.isEmpty()) {
                        Timber.d("billing plan is not available")
                        _screenState.emit(ScreenState.APIError)
                        subscriptionRepos.queryBillingPlans()
                    } else {
                        val subscriptions =
                            billingPlans?.filter { productIds.contains(it.productId) }
                                ?.sortedBy { productIds.indexOf(it.productId) }
                        _subscriptionsState.emit(subscriptions.orEmpty())
                    }
                }
        }
    }

    fun checkProUserAndLoadPackage() {
        _planStateLiveData.value =
            if (SharedPreferUtils.proUser) {
                Subscription(
                    Plan.PRO,
                    SharedPreferUtils.subscriptionType,
                    SharedPreferUtils.isLifetime,
                )
            } else {
                Subscription(Plan.STANDARD, SharedPreferUtils.subscriptionType)
            }
    }

    override fun onSubscriptionStatus(purchaseToBillingPlans: List<Pair<Purchase, BillingPlan?>>) {
        saveSubscriptionDetail(purchaseToBillingPlans.firstOrNull())
        viewModelScope.launch {
            if (purchaseToBillingPlans.isNotEmpty()) {
                _purchasesState.send(purchaseToBillingPlans.map { it.first })
                _planStateLiveData.value = Subscription(
                    Plan.PRO,
                    purchaseToBillingPlans.first().second?.name.orEmpty(),
                    purchaseToBillingPlans.first().second?.isProduct.orFalse(),
                )
            }
            saveSubscriptionDetail(purchaseToBillingPlans.firstOrNull())
            _screenState.emit(ScreenState.HideLoading)
        }
    }

    fun buyProduct(activity: Activity, billingPlan: BillingPlan) {
        val flowParams = when {
            billingPlan.product != null -> {
                BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(
                        listOf(
                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(billingPlan.product)
                                .setOfferToken(
                                    billingPlan.discountOfferDetails?.offerToken.orEmpty(),
                                )
                                .build(),
                        ),
                    ).build()
            }

            billingPlan.sku != null -> {
                BillingFlowParams.newBuilder()
                    .setSkuDetails(billingPlan.sku)
                    .build()
            }

            else -> return
        }
        val result = subscriptionRepos.launchBillingFlow(activity, flowParams)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {

//            TrackingFactory.PremiumPlan.purchasedFailed(billingPlan.productId, result.responseCode)
//                .track()
        }
    }

    data class Subscription(
        val plan: Plan,
        val type: String = "",
        val isLifetime: Boolean = false,
        val nextBillingDate: Date = Calendar.getInstance().time,
    )

    enum class Plan {
        STANDARD, PRO
    }
}
