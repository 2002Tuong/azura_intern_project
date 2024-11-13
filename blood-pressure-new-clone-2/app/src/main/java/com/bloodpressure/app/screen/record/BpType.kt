package com.bloodpressure.app.screen.record

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.bloodpressure.app.R

enum class BpType(
    val nameRes: Int,
    val color: Color,
    val systolic: IntRange,
    val diastolic: IntRange
) {

    HYPOTENSION(
        nameRes = R.string.hypotension,
        color = Color(0xFF1892FA),
        systolic = 20..89,
        diastolic = 20..59
    ) {
        override fun isValid(systolicValue: Int, diastolicValue: Int): Boolean {
            return systolic.contains(systolicValue) || diastolic.contains(diastolicValue)
        }

        override fun getAnnotatedString(): AnnotatedString {
            return buildAnnotatedString {
                append("SYS")
                append(" < ")
                withStyle(style = SpanStyle(Color(0xFF1892FA))) {
                    append("${systolic.last + 1}")
                }
                append(" or ")
                append("DIA")
                append(" < ")
                withStyle(style = SpanStyle(Color(0xFFF95721))) {
                    append("${diastolic.last + 1}")
                }
            }
        }
    },
    NORMAL(
        nameRes = R.string.normal,
        color = Color(0xFF53B69F),
        systolic = 90..119,
        diastolic = 60..79
    ) {
        override fun isValid(systolicValue: Int, diastolicValue: Int): Boolean {
            return systolic.contains(systolicValue) && diastolic.contains(diastolicValue)
        }

        override fun getAnnotatedString(): AnnotatedString {
            return buildAnnotatedString {
                append("SYS ")
                withStyle(style = SpanStyle(Color(0xFF1892FA))) {
                    append("${systolic.first}-${systolic.last}")
                }
                append(" and ")
                append("DIA ")
                withStyle(style = SpanStyle(Color(0xFFF95721))) {
                    append("${diastolic.first}-${diastolic.last}")
                }
            }
        }
    },
    ELEVATED(
        nameRes = R.string.elevated,
        color = Color(0xFFF6C504),
        systolic = 120..129,
        diastolic = 60..79
    ) {
        override fun isValid(systolicValue: Int, diastolicValue: Int): Boolean {
            return systolic.contains(systolicValue) && diastolic.contains(diastolicValue)
        }

        override fun getAnnotatedString(): AnnotatedString {
            return  buildAnnotatedString {
                append("SYS ")
                withStyle(style = SpanStyle(Color(0xFF1892FA))) {
                    append("${systolic.first}-${systolic.last}")
                }
                append(" and ")
                append("DIA ")
                withStyle(style = SpanStyle(Color(0xFFF95721))) {
                    append("${diastolic.first}-${diastolic.last}")
                }
            }
        }
    },
    HYPERTENSION_STAGE_1(
        nameRes = R.string.hypertension_stage_1,
        color = Color(0xFFEF8922),
        systolic = 130..139,
        diastolic = 80..89
    ) {
        override fun isValid(systolicValue: Int, diastolicValue: Int): Boolean {
            return systolic.contains(systolicValue) || diastolic.contains(diastolicValue)
        }

        override fun getAnnotatedString(): AnnotatedString {
            return buildAnnotatedString {
                append("SYS ")
                withStyle(style = SpanStyle(Color(0xFF1892FA))) {
                    append("${systolic.first}-${systolic.last}")
                }
                append(" or ")
                append("DIA ")
                withStyle(style = SpanStyle(Color(0xFFF95721))) {
                    append("${diastolic.first}-${diastolic.last}")
                }
            }
        }
    },
    HYPERTENSION_STAGE_2(
        nameRes = R.string.hypertension_stage_2,
        color = Color(0xFFF95721),
        systolic = 140..180,
        diastolic = 90..120
    ) {
        override fun isValid(systolicValue: Int, diastolicValue: Int): Boolean {
            return systolic.contains(systolicValue) || diastolic.contains(diastolicValue)
        }

        override fun getAnnotatedString(): AnnotatedString {
            return buildAnnotatedString {
                append("SYS ")
                withStyle(style = SpanStyle(Color(0xFF1892FA))) {
                    append("${systolic.first}-${systolic.last}")
                }
                append(" or ")
                append("DIA ")
                withStyle(style = SpanStyle(Color(0xFFF95721))) {
                    append("${diastolic.first}-${diastolic.last}")
                }
            }
        }
    },
    HYPERTENSIVE(
        nameRes = R.string.hypertensive,
        color = Color(0xFFAE2F05),
        systolic = 180..300,
        diastolic = 120..300
    ) {
        override fun isValid(systolicValue: Int, diastolicValue: Int): Boolean {
            return systolic.contains(systolicValue) || diastolic.contains(diastolicValue)
        }

        override fun getAnnotatedString(): AnnotatedString {
            return buildAnnotatedString {
                append("SYS ")
                append(">")
                withStyle(style = SpanStyle(Color(0xFF1892FA))) {
                    append("${systolic.first}")
                }
                append(" or ")
                append("DIA ")
                append(">")
                withStyle(style = SpanStyle(Color(0xFFF95721))) {
                    append("${diastolic.first}")
                }
            }
        }
    };

    abstract fun isValid(systolicValue: Int, diastolicValue: Int): Boolean

    abstract fun getAnnotatedString(): AnnotatedString
}
