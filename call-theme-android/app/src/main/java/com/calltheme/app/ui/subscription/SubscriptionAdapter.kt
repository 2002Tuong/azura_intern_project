package com.calltheme.app.ui.subscription

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.screentheme.app.R
import com.screentheme.app.models.PremiumPlan

class SubscriptionAdapter(private val context: Context, private val premiumPlanList: ArrayList<PremiumPlan> = ArrayList()) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var selectedItemPosition = RecyclerView.NO_POSITION

    private var onItemClickCallback: ((PremiumPlan) -> Unit)? = null
    override fun getItemCount(): Int {
        return premiumPlanList.size
    }

    fun updateItems(newPremiumPlanList: Array<PremiumPlan>) {
        val sortedPremiumPlans = newPremiumPlanList.sortedBy { it.premiumConfig?.index }

        premiumPlanList.clear()
        premiumPlanList.addAll(sortedPremiumPlans)
        notifyDataSetChanged()

    }

    fun setOnItemClick(callback: (PremiumPlan) -> Unit) {
        this.onItemClickCallback = callback
    }

    override fun getItemViewType(position: Int): Int {
        val premiumPlan = premiumPlanList[position]
        return if (premiumPlan.premiumConfig?.isBest == true) 1 else 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == 1) {
            val itemView = inflater.inflate(R.layout.item_layout_subscription_best, parent, false)
            BestSubscriptionViewHolder(itemView)
        } else {
            val itemView = inflater.inflate(R.layout.item_layout_subscription, parent, false)
            SubscriptionViewHolder(itemView)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val itemContainer: ViewGroup = holder.itemView.findViewById(R.id.itemContainer)
        val selectionRadioButton: RadioButton = holder.itemView.findViewById(R.id.radioButton)

        val premiumPlan = premiumPlanList[position]

        if (holder is BestSubscriptionViewHolder) {
            val bestViewHolder = holder as BestSubscriptionViewHolder
            bestViewHolder.bind(premiumPlan)
        } else {
            val viewHolder = holder as SubscriptionViewHolder
            viewHolder.bind(premiumPlan)
        }

        holder.itemView.setOnClickListener {
            this.onItemClickCallback?.invoke(premiumPlan)

            val previousSelectedItem = selectedItemPosition
            selectedItemPosition = position

            // Notify the adapter of the item changes
            notifyItemChanged(previousSelectedItem)
            notifyItemChanged(selectedItemPosition)
        }

        if (selectedItemPosition == position || (premiumPlan.premiumConfig?.isBest == true && selectedItemPosition == RecyclerView.NO_POSITION)) {
            itemContainer.setBackgroundResource(R.drawable.select_ringtone_background)
            selectionRadioButton.isChecked = true
        } else {
            itemContainer.setBackgroundResource(R.drawable.unselect_subscription_background)
            selectionRadioButton.isChecked = false
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.post {
            // Find the position of the item marked as 'is_best'
            val bestPosition = premiumPlanList.indexOfFirst { it.premiumConfig?.isBest == true }
            if (bestPosition != -1) {
                // Update the selected item position and notify the adapter
                selectedItemPosition = bestPosition
                notifyItemChanged(selectedItemPosition)

                if (selectedItemPosition >= 0 && selectedItemPosition < premiumPlanList.size) {
                    val premiumPlan = premiumPlanList[selectedItemPosition]
                    this.onItemClickCallback?.invoke(premiumPlan)
                }

            }
        }
    }

    inner class SubscriptionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val subscriptionNameTextView: TextView = itemView.findViewById(R.id.subscriptionNameTextView)
        private val priceTextView: TextView = itemView.findViewById(R.id.priceTextView)
        private val priceOriginTextView: TextView = itemView.findViewById(R.id.priceTextViewOrigin)

        fun bind(premiumPlan: PremiumPlan) {
            subscriptionNameTextView.text = premiumPlan.name
            if (premiumPlan.premiumConfig != null && premiumPlan.premiumConfig.discount != "null") {
                priceOriginTextView.text = premiumPlan.formattedOriginalPrice
                priceOriginTextView.paintFlags = priceOriginTextView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                priceOriginTextView.visibility = View.GONE
            }

            priceTextView.text = "${premiumPlan.sku?.price} ${premiumPlan.sku?.priceCurrencyCode}"
        }
    }

    inner class BestSubscriptionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val subscriptionNameTextView: TextView = itemView.findViewById(R.id.subscriptionNameTextView)
        private val discountTextView: TextView = itemView.findViewById(R.id.discountTextView)
        private val priceTextView: TextView = itemView.findViewById(R.id.priceTextView)
        private val priceOriginTextView: TextView = itemView.findViewById(R.id.priceTextViewOrigin)
        private val startFreeTrialTextView: TextView = itemView.findViewById(R.id.startThreeDayTextView)


        @SuppressLint("SetTextI18n")
        fun bind(premiumPlan: PremiumPlan) {
            subscriptionNameTextView.text = premiumPlan.name
            priceTextView.text = "${premiumPlan.sku?.price}${premiumPlan.sku?.priceCurrencyCode}"
            startFreeTrialTextView.isVisible = premiumPlan.hasFreeTrial()

            if (premiumPlan.premiumConfig?.discount != null) {
                discountTextView.text = context.getString(R.string.discount_off, premiumPlan.discountPercent)
                priceOriginTextView.paintFlags = priceOriginTextView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                priceOriginTextView.text = premiumPlan.formattedOriginalPrice
                discountTextView.visibility = View.VISIBLE
                priceOriginTextView.visibility = View.VISIBLE
            } else {
                discountTextView.visibility = View.GONE
                priceOriginTextView.visibility = View.GONE
            }
        }
    }
}
