package com.artgen.app.data.model

import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.SkuDetails
import com.artgen.app.data.remote.JsonProvider
import kotlinx.serialization.decodeFromString
import java.text.NumberFormat
import java.util.Currency
import kotlin.time.Duration

data class PremiumPlan(
    val premiumConfig: PremiumConfig? = null,
    val product: ProductDetails? = null,
    val sku: SkuDetails? = null,
) {
    val name: String
        get() = product?.name ?: sku?.getSkuName().orEmpty()

    val hasDiscount: Boolean
        get() = discountPercent > 0

    val productId: String
        get() = product?.productId ?: sku?.sku.orEmpty()

    val isProduct: Boolean
        get() = premiumConfig?.isSubscription == false

    val formattedBasePlanPrice: String
        get() = if(isProduct) {
            product?.oneTimePurchaseOfferDetails?.formattedPrice.orEmpty()
        }else {
            basePlanPricingPhase?.formattedPrice ?: sku?.price.orEmpty()
        }


    val discountPercent: Int = ((premiumConfig?.discount?.toFloatOrNull() ?: 0f) * 100f).toInt()

    val formattedOriginalPrice: String
        get() {
            val originalPrice = if (discountPercent > 0) {
                ((basePlanPriceAmountMicros / (100 - discountPercent)) * 100f).toLong()
            } else {
                basePlanPriceAmountMicros / 50 * 100
            }
            val priceCode = basePlanCurrencyCode
            return formatPrice(originalPrice, priceCode).orEmpty()
        }

    fun hasFreeTrial(): Boolean = product?.subscriptionOfferDetails?.any { it ->
        it.pricingPhases.pricingPhaseList.any { it.priceAmountMicros == 0L }
    } ?: !sku?.freeTrialPeriod.isNullOrEmpty()

    val freeTrialPeriod: String
        get() = if (product != null) {
            Duration.parse(
                product.subscriptionOfferDetails.orEmpty()
                    .firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.billingPeriod.orEmpty()
            ).inWholeDays.toString()
        } else {
            Duration.parse(sku?.freeTrialPeriod.orEmpty()).inWholeDays.toString()
        }

    private val basePlanPriceAmountMicros: Long
        get() = if (isProduct) {
            product?.oneTimePurchaseOfferDetails?.priceAmountMicros ?: 0L
        } else {
            basePlanPricingPhase?.priceAmountMicros ?: sku?.priceAmountMicros ?: 0L
        }

    val basePlanPeriod: String
        get() = basePlanPricingPhase?.billingPeriod ?: sku?.subscriptionPeriod.orEmpty()

    val basePlanCurrencyCode: String
        get() = if(isProduct) {
            product?.oneTimePurchaseOfferDetails?.priceCurrencyCode.orEmpty()
        } else {
            basePlanPricingPhase?.priceCurrencyCode ?: sku?.priceCurrencyCode.orEmpty()
        }

    private val basePlanPricingPhase: ProductDetails.PricingPhase?
        get() = basePlanOfferDetails?.pricingPhases?.pricingPhaseList?.firstOrNull()

    private val basePlanOfferDetails: ProductDetails.SubscriptionOfferDetails?
        get() = product?.subscriptionOfferDetails?.lastOrNull()

    private fun SkuDetails.getSkuName(): String {
        return try {
            val map: Map<String, Any> = JsonProvider.json.decodeFromString(originalJson)
            map["name"]?.toString() ?: title
        } catch (exception: Exception) {
            title
        }
    }

    private fun formatPrice(priceAmountMicros: Long, currencyCode: String): String? {
        return try {
            val pricePerMonth = priceAmountMicros / 1000000f
            val format: NumberFormat = NumberFormat.getCurrencyInstance().apply {
                maximumFractionDigits = 0
                currency = Currency.getInstance(currencyCode)
            }
            format.format(pricePerMonth)
        } catch (e: Exception) {
            null
        }
    }
}
