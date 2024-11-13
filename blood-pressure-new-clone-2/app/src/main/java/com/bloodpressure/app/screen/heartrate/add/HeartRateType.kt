package com.bloodpressure.app.screen.heartrate.add

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.bloodpressure.app.R

enum class HeartRateType(
    val nameRes: Int,
    val color: Color,
    val rateRange: IntRange,
) {

    SLOW(
        nameRes = R.string.slow,
        color = Color(0xFF1892FA),
        rateRange = 40..60,
    ) {
        override fun isValid(rateValue: Int): Boolean {
            return rateRange.contains(rateValue)
        }

        override fun getAnnotatedString(): AnnotatedString {
            return buildAnnotatedString {
                withStyle(style = SpanStyle(Color(0xFF1892FA))) {
                    append("<${rateRange.last}")
                }
                append(" BPM")
            }
        }
    },
    NORMAL(
        nameRes = R.string.normal,
        color = Color(0xFF62A970),
        rateRange = 61..100,
    ) {
        override fun isValid(rateValue: Int): Boolean {
            return rateRange.contains(rateValue)
        }

        override fun getAnnotatedString(): AnnotatedString {
            return buildAnnotatedString {
                withStyle(style = SpanStyle(color)) {
                    append("${rateRange.first - 1}")
                }
                append("-")
                withStyle(style = SpanStyle(color)) {
                    append("${rateRange.last}")
                }
                append(" BPM")
            }
        }
    },
    FAST(
        nameRes = R.string.fast,
        color = Color(0xFFAE2F05),
        rateRange = 101..220,
    ) {
        override fun isValid(rateValue: Int): Boolean {
            return rateRange.contains(rateValue)
        }

        override fun getAnnotatedString(): AnnotatedString {
            return buildAnnotatedString {
                withStyle(style = SpanStyle(Color(0xFF1892FA))) {
                    append(">${rateRange.first - 1}")
                }
                append(" BPM")
            }
        }
    };

    abstract fun isValid(rateValue: Int): Boolean

    abstract fun getAnnotatedString(): AnnotatedString
}
