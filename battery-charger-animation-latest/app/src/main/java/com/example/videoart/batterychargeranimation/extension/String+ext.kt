package com.example.videoart.batterychargeranimation.extension

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