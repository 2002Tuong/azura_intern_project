package com.artgen.app.ui.screen.premium

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.Purchase
import com.artgen.app.ads.BillingClientLifecycle
import com.artgen.app.data.local.AppDataStore
import com.artgen.app.data.model.PremiumPlan
import com.artgen.app.data.remote.RemoteConfig
import com.artgen.app.utils.TextFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PremiumViewModel(
    private val remoteConfig: RemoteConfig,
    private val billingClientLifecycle: BillingClientLifecycle,
    private val dataStore: AppDataStore,
    private val textFormatter: TextFormatter
) : ViewModel(){
    private val _upgradeProState = MutableStateFlow<BillingFlowParams?>(null)
    val upgradeProState: StateFlow<BillingFlowParams?> get() = _upgradeProState

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> get() = _uiState

    init {
        viewModelScope.launch {
            billingClientLifecycle.purchasesFlow.collectLatest {purchases ->
                if(purchases == null) {
                    return@collectLatest
                }

                val isPurchased = purchases
                    .any { it.isAcknowledged && it.purchaseState == Purchase.PurchaseState.PURCHASED }
                dataStore.setPurchase(isPurchased)
                _uiState.update { it.copy(isPremium = isPurchased) }
            }
        }

        viewModelScope.launch {
            billingClientLifecycle.acknowledgingPurchases.collectLatest { isLoading ->
                _uiState.update { it.copy(isLoading = isLoading) }
            }
        }

        querySubscriptions()
    }

    private fun querySubscriptions() {
        viewModelScope.launch {
            val productIds = remoteConfig.premiumConfigs
                .filter { it.show }
                .sortedBy { it.index }
                .map { it.id }
                .filter { it.isNotEmpty() }
            if (productIds.isEmpty()) {
                return@launch
            }

            _uiState.update { it.copy(isLoading = true) }
            billingClientLifecycle.premiumPlans.filterNotNull().firstOrNull().let { premiumPlans ->
                if (premiumPlans.isNullOrEmpty()) {
                    _uiState.update { it.copy(hasError = true) }
                } else {
                    val orderedPremiumPlans =
                        premiumPlans.filter { productIds.contains(it.productId) }
                            .sortedBy { productIds.indexOf(it.productId) }

                    val promotePremiumPlan =
                        orderedPremiumPlans.firstOrNull { it.premiumConfig?.isBest == true }
                            ?: orderedPremiumPlans.firstOrNull()

                    _uiState.update {
                        it.copy(
                            premiumPlans = orderedPremiumPlans,
                            promotePremiumPlan = promotePremiumPlan
                        )
                    }
                }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun buyProduct(productId: String) {
        viewModelScope.launch {
            billingClientLifecycle.premiumPlans.value?.map { it.product }
                ?.firstOrNull{it?.productId == productId}
                ?.let {productDetails ->
                    val offerToken =
                        productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken.orEmpty()

                    BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(
                            listOf(
                                BillingFlowParams.ProductDetailsParams.newBuilder()
                                    .setProductDetails(productDetails)
                                    .setOfferToken(offerToken)
                                    .build()
                            )
                        )
                        .build()
                        .let { params ->
                            _upgradeProState.update { params }
                        }
                    return@launch
                }
            billingClientLifecycle.premiumPlans.value?.map { it.sku }
                ?.firstOrNull { it?.sku == productId }
                ?.let { skuDetails ->
                    BillingFlowParams.newBuilder()
                        .setSkuDetails(skuDetails)
                        .build()
                        .let { params ->
                            _upgradeProState.update { params }
                        }
                }
        }
    }

    fun launchBillingFlow(context: Context, params: BillingFlowParams) {
        billingClientLifecycle.launchBillingFlow(context ,params)
    }
    fun onBillingFlowLaunched() {
        _upgradeProState.update { null }
    }

    fun convertToPeriodText(isoPeriod: String): String {
        return textFormatter.convertToPeriodText(isoPeriod)
    }

    data class UiState(
        val premiumPlans: List<PremiumPlan> = emptyList(),
        val isPremium: Boolean = false,
        val isLoading: Boolean = false,
        val hasError: Boolean = false,
        val promotePremiumPlan: PremiumPlan? = null
    )
}