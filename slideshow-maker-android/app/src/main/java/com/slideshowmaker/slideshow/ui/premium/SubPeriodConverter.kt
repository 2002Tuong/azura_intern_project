package com.slideshowmaker.slideshow.ui.premium

import android.content.res.Resources
import com.slideshowmaker.slideshow.R
import java.text.NumberFormat
import java.util.Currency
import org.joda.time.Period

object SubPeriodConverter {

    fun convertToPricePerMonth(price: Long, period: String, currencyCode: String): String? {
        try {
            val count = getMonthCount(period)
            if (count <= 1) {
                return null
            }
            val pricePerMonth = (price / count) / 1000000f
            val format: NumberFormat = NumberFormat.getCurrencyInstance().apply {
                maximumFractionDigits = 0
                currency = Currency.getInstance(currencyCode)
            }
            return format.format(pricePerMonth)
        } catch (e: Exception) {
            return null
        }
    }

    fun formatPrice(priceAmountMicros: Long, currencyCode: String): String? {
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

    fun convertToCycleText(resources: Resources, isoPeriod: String, cycleCount: Int): String {
        return try {
            val period = Period.parse(isoPeriod)
            val unit = when {
                period.years > 0 -> resources.getString(R.string.years)
                period.months > 0 -> resources.getString(R.string.months)
                period.weeks > 0 -> resources.getString(R.string.weeks)
                else -> resources.getString(R.string.days)
            }
            "$cycleCount $unit"
        } catch (e: Exception) {
            ""
        }
    }

    private fun getMonthCount(isoPeriod: String): Int {
        try {
            val period = Period.parse(isoPeriod)
            if (period.years > 0) {
                return period.years * 12
            }

            if (period.months > 0) {
                return period.months
            }

            return 0
        } catch (e: Exception) {
            return 0
        }
    }
}
