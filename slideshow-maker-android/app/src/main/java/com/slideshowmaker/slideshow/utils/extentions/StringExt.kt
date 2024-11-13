package com.slideshowmaker.slideshow.utils.extentions

import org.joda.time.Period
import java.text.SimpleDateFormat
import java.util.*

fun String?.appVersionToInt(): Int {
    return this?.split(".")?.mapIndexed { index, value ->
        when (index) {
            0 -> value.toIntSafety() * 100
            1 -> value.toIntSafety() * 10
            else -> value.toIntSafety()
        }
    }.orEmpty().sumOf { it }
}

fun String.toIntSafety() = this.toIntOrNull() ?: 0

fun parseDuration(duration: String): Period {
    return try {
        Period.parse(duration)
    } catch (exception: Exception) {
        Period.days(7)
    }
}

fun Long.toDateFormat(format: String = "MMM dd, yyyy"): String =
    SimpleDateFormat(format).format(Date(this))

fun String?.isUrl() = orEmpty().startsWith("https")