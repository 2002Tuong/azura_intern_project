package com.slideshowmaker.slideshow.ui.premium

import android.graphics.Paint
import android.graphics.Typeface
import android.text.Spanned
import android.text.SpannedString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.text.buildSpannedString
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.data.response.BillingPlan
import com.slideshowmaker.slideshow.databinding.EpoxyViewSubscriptionItemBinding
import com.slideshowmaker.slideshow.utils.extentions.orFalse

class SubscriptionAdapter(
    private val productChangeListener: (BillingPlan, Boolean) -> Unit,
) : RecyclerView.Adapter<SubscriptionAdapter.SubscriptionViewHolder>() {

    var productSelected: BillingPlan? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var bestProductIdList: List<String>? = null
    var subscriptions: List<BillingPlan> = emptyList()
        set(value) {
            field = value
            productSelected = value.firstOrNull { bestProductIdList?.contains(it.productId).orFalse() }
            productSelected?.let { productChangeListener.invoke(it, false) }
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubscriptionViewHolder {
        val binding =
            EpoxyViewSubscriptionItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SubscriptionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SubscriptionViewHolder, position: Int) {
        holder.bind(
            subscriptions[position],
            subscriptions[position].productId == productSelected?.productId,
            bestProductIdList?.contains(subscriptions[position].productId).orFalse(),
        ) { product ->
            productSelected = product
            productChangeListener.invoke(product, true)
        }
    }

    override fun getItemCount(): Int {
        return subscriptions.size
    }

    class SubscriptionViewHolder(
        private val binding: EpoxyViewSubscriptionItemBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            product: BillingPlan,
            selected: Boolean,
            promoted: Boolean,
            onSelected: (BillingPlan) -> Unit,
        ) {
            val hasDiscount = product.hasDiscount()
            val discountPercentStr = product.discountPercentText
            binding.subscriptionLayout.isSelected = selected
            binding.icChoose.isSelected = selected
            binding.tvSubscriptionName.apply {
                text = if (product.hasFreeTrial()) {
                    val freeTrialDuration = resources.getString(
                        R.string.premium_plan_free_trial_first_subtitle,
                        product.freeTrialPeriod,
                    )
                    "${product.name} + $freeTrialDuration"
                } else {
                    product.name
                }
                isSelected = selected
            }
            binding.tvSubscriptionPrice.apply {
                isSelected = selected
                text = product.formattedBasePlanPrice
                isVisible = !hasDiscount
            }
            binding.tvDiscountPrice.apply {
                isSelected = selected
                text = product.formattedOfferPrice
                isVisible = hasDiscount
            }

            binding.tvFreeTrialDuration.apply {
                isSelected = selected
                isVisible = !hasDiscount
                text = getPricePerMonthText(product)
            }
            binding.tvOriginalPrice.apply {
                paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                isVisible = false
                text = product.formattedBasePlanPrice
            }
            binding.tvDiscountPercent.apply {
                isVisible = hasDiscount
                isSelected = selected
                text = resources.getString(R.string.premium_plan_save_title, discountPercentStr)
            }
            binding.tvDescription.apply {
                isVisible = selected && hasDiscount
                text = getDescription(product)
            }

            binding.root.setOnClickListener {
                onSelected(product)
            }
        }

        private fun getPricePerMonthText(plan: BillingPlan): SpannedString? {
            val priceOnceMonth = SubPeriodConverter.convertToPricePerMonth(
                plan.basePlanPriceAmountMicros,
                plan.basePlanPeriod,
                plan.basePlanCurrencyCode,
            ) ?: return null
            val text = binding.root.resources.getString(
                R.string.premium_plan_monthly_price_subtitle,
                priceOnceMonth,
            )
            return try {
                buildSpannedString {
                    append(text)
                    val start = text.indexOf(priceOnceMonth)
                    val end = start + priceOnceMonth.length
                    setSpan(StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            } catch (e: Exception) {
                null
            }
        }

        private fun getDescription(billingPlan: BillingPlan): String {
            if (!billingPlan.hasDiscount()) {
                return ""
            }

            val resources = binding.root.resources
            if (billingPlan.isProduct) {
                return resources.getString(R.string.premium_plan_one_time_purchase_subtitle)
            }

            return ""

        }
    }
}
