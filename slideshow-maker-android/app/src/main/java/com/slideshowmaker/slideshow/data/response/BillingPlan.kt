package com.slideshowmaker.slideshow.data.response

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetails.PricingPhase
import com.android.billingclient.api.ProductDetails.SubscriptionOfferDetails
import com.android.billingclient.api.SkuDetails
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.slideshowmaker.slideshow.ui.premium.SubPeriodConverter

data class BillingPlan(
    val premiumPlan: PremiumPlan? = null,
    val product: ProductDetails? = null,
    val sku: SkuDetails? = null,
) {

    val basePlanPeriod: String
        get() = basePlanPricingPhase?.billingPeriod ?: sku?.subscriptionPeriod.orEmpty()

    val offerBillingPeriod: String
        get() = offerPricingPhase?.billingPeriod ?: sku?.subscriptionPeriod.orEmpty()

    val freeTrialPeriod: String
        get() = if (product != null) {
            freeTrialPricingPhase?.billingPeriod.orEmpty()
        } else {
            sku?.freeTrialPeriod.orEmpty()
        }

    val formattedBasePlanPrice: String
        get() = if (isProduct) {
            val price = product?.oneTimePurchaseOfferDetails?.priceAmountMicros ?: 0L
            val discountPercent = premiumPlan?.discount?.toFloatOrNull() ?: 0f
            val originalPrice = if (discountPercent > 0) {
                price / (1 - discountPercent)
            } else {
                price
            }
            val priceCode = product?.oneTimePurchaseOfferDetails?.priceCurrencyCode.orEmpty()
            SubPeriodConverter.formatPrice(originalPrice.toLong(), priceCode).orEmpty()
        } else {
            basePlanPricingPhase?.formattedPrice ?: sku?.price.orEmpty()
        }

    val formattedOfferPrice: String
        get() = if (isProduct) {
            product?.oneTimePurchaseOfferDetails?.formattedPrice.orEmpty()
        } else {
            offerPricingPhase?.formattedPrice ?: sku?.price.orEmpty()
        }

    val basePlanPriceAmountMicros: Long
        get() = basePlanPricingPhase?.priceAmountMicros ?: sku?.priceAmountMicros ?: 0L

    val offerPriceAmountMicros: Long
        get() = offerPricingPhase?.priceAmountMicros ?: sku?.priceAmountMicros ?: 0L

    val basePlanCurrencyCode: String
        get() = basePlanPricingPhase?.priceCurrencyCode ?: sku?.priceCurrencyCode.orEmpty()

    val offerBillingCycleCount: Int
        get() = offerPricingPhase?.billingCycleCount ?: sku?.introductoryPriceCycles ?: 0

    private val basePlanPricingPhase: PricingPhase?
        get() = basePlanOfferDetails?.pricingPhases?.pricingPhaseList?.firstOrNull()

    private val offerPricingPhase: PricingPhase?
        get() = discountOfferDetails?.pricingPhases?.pricingPhaseList?.firstOrNull { it.priceAmountMicros > 0L }

    private val freeTrialPricingPhase: PricingPhase?
        get() = discountOfferDetails?.pricingPhases?.pricingPhaseList?.firstOrNull { it.priceAmountMicros == 0L }

    val discountOfferDetails: SubscriptionOfferDetails? by lazy {
        getValidOfferDetails()
    }

    val isProduct: Boolean
        get() = product?.productType == BillingClient.ProductType.INAPP

    val discountPercent: Int
        get() = ((basePlanPriceAmountMicros - offerPriceAmountMicros) / basePlanPriceAmountMicros.toFloat() * 100).toInt()

    val discountPercentText: String
        get() = if (isProduct) {
            val discount = ((premiumPlan?.discount?.toFloatOrNull() ?: 0f) * 100).toInt()
            "$discount%"
        } else {
            "$discountPercent%"
        }

    private val basePlanOfferDetails: SubscriptionOfferDetails?
        get() = product?.subscriptionOfferDetails?.lastOrNull()

    val name: String
        get() = product?.name ?: sku?.getSkuName().orEmpty()

    val productId: String
        get() = product?.productId ?: sku?.sku.orEmpty()

    val productType: String
        get() = product?.productType ?: sku?.type.orEmpty()

    fun SkuDetails.getSkuName(): String {
        return try {
            val map: Map<String, Any> = Gson().fromJson(
                originalJson,
                object : TypeToken<HashMap<String, String?>?>() {}.type,
            )
            map["name"]?.toString() ?: title
        } catch (exception: Exception) {
            title
        }
    }

    fun hasDiscount(): Boolean {
        return if (isProduct) {
            !premiumPlan?.discount.isNullOrEmpty()
        } else {
            product != null && offerPriceAmountMicros != basePlanPriceAmountMicros
        }
    }

    private fun getValidOfferDetails(): SubscriptionOfferDetails? {
        val offerDetails = getOfferDetailsByTags()
        return getLeastPricedOfferDetails(offerDetails)
    }

    private fun getLeastPricedOfferDetails(
        offerDetails: List<SubscriptionOfferDetails>,
    ): SubscriptionOfferDetails? {
        var leastPricedOffer: SubscriptionOfferDetails? = null
        var lowestPrice = Long.MAX_VALUE
        if (offerDetails.isNotEmpty()) {
            for (offer in offerDetails) {
                for (price in offer.pricingPhases.pricingPhaseList) {
                    if (price.priceAmountMicros in 1 until lowestPrice) {
                        lowestPrice = price.priceAmountMicros
                        leastPricedOffer = offer
                    }
                }
            }
        }
        return leastPricedOffer
    }

    private fun getOfferDetailsByTags(): List<SubscriptionOfferDetails> {
        return product?.subscriptionOfferDetails?.filter {
            it.offerTags.contains(NEW_USER_TAG)
        }?.takeIf { it.isNotEmpty() }
            ?: product?.subscriptionOfferDetails?.filter {
                it.offerTags.contains(UPGRADE_TAG)
            }?.takeIf { it.isNotEmpty() }
            ?: product?.subscriptionOfferDetails?.filter {
                it.offerTags.contains(DEFAULT_TAG)
            }?.takeIf { it.isNotEmpty() }
            ?: basePlanOfferDetails?.let { listOf(it) }
            ?: emptyList()
    }

    fun hasFreeTrial(): Boolean {
        return discountOfferDetails?.pricingPhases?.pricingPhaseList?.any {
            it.priceAmountMicros == 0L
        }
            ?: !sku?.freeTrialPeriod.isNullOrEmpty()
    }

    companion object {
        private const val NEW_USER_TAG = "newuser"
        private const val UPGRADE_TAG = "upgrade"
        private const val DEFAULT_TAG = "default"
    }
}
