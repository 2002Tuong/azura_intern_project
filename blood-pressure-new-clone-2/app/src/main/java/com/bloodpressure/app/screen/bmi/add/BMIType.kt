package com.bloodpressure.app.screen.bmi.add

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.bloodpressure.app.R

enum class BMIType(
    @StringRes val nameRes: Int,
    val color: Color,
    val bmiRangeStart: Float,
    val bmiRangeEnd: Float
) {
    VERY_SEVERELY(
        nameRes = R.string.very_severely,
        color = Color(0xFF4906C5),
        bmiRangeStart = 0f,
        bmiRangeEnd = 16f,
    ) {
        override fun isValid(bmiValue: Float): Boolean {
            return bmiValue <= bmiRangeEnd
        }

        override fun getAnnotatedString(): AnnotatedString {
            return buildAnnotatedString {
                append("BMI")
                append(" < ")
                withStyle(style = SpanStyle(color)) {
                    append("$bmiRangeEnd")
                }
            }
        }
      },

    SEVERELY_UNDERWEIGHT(
        nameRes = R.string.severely_underweight,
        color = Color(0xFF2D63E5),
        bmiRangeStart = 16.0f,
        bmiRangeEnd = 16.9f
    ) {
        override fun isValid(bmiValue: Float): Boolean {
            return bmiValue in bmiRangeStart..bmiRangeEnd
        }

        override fun getAnnotatedString(): AnnotatedString {
            return buildAnnotatedString {
                append("BMI ")
                withStyle(style = SpanStyle(color)) {
                    append("$bmiRangeStart - $bmiRangeEnd")
                }
            }
        }
      },

    UNDERWEIGHT(
        nameRes = R.string.underweight,
        color = Color(0xFF1892FA),
        bmiRangeStart = 17.0f,
        bmiRangeEnd = 18.4f,
    ){
        override fun isValid(bmiValue: Float): Boolean {
            return bmiValue in bmiRangeStart..bmiRangeEnd
        }

        override fun getAnnotatedString(): AnnotatedString {
            return buildAnnotatedString {
                append("BMI ")
                withStyle(style = SpanStyle(color)) {
                    append("$bmiRangeStart - $bmiRangeEnd")
                }
            }
        }
    },

    NORMAL(
        nameRes = R.string.normal,
        color = Color(0xFF62A970),
        bmiRangeStart = 18.5f,
        bmiRangeEnd = 24.9f
    ){
        override fun isValid(bmiValue: Float): Boolean {
            return bmiValue in bmiRangeStart..bmiRangeEnd
        }

        override fun getAnnotatedString(): AnnotatedString {
            return buildAnnotatedString {
                append("BMI ")
                withStyle(style = SpanStyle(color)) {
                    append("$bmiRangeStart - $bmiRangeEnd")
                }
            }
        }
    },

    OVERWEIGHT(
        nameRes = R.string.overweight,
        color = Color(0xFFF6C504),
        bmiRangeStart = 25.0f,
        bmiRangeEnd = 29.9f,
    ){
        override fun isValid(bmiValue: Float): Boolean {
            return bmiValue in bmiRangeStart..bmiRangeEnd
        }

        override fun getAnnotatedString(): AnnotatedString {
            return buildAnnotatedString {
                append("BMI ")
                withStyle(style = SpanStyle(color)) {
                    append("$bmiRangeStart - $bmiRangeEnd")
                }
            }
        }
    },

    OBESE_CLASS_1(
        nameRes = R.string.obese_class_1,
        color = Color(0xFFF4763C),
        bmiRangeStart = 30.0f,
        bmiRangeEnd = 34.9f,
    ){
        override fun isValid(bmiValue: Float): Boolean {
            return bmiValue in bmiRangeStart..bmiRangeEnd
        }

        override fun getAnnotatedString(): AnnotatedString {
            return buildAnnotatedString {
                append("BMI ")
                withStyle(style = SpanStyle(color)) {
                    append("$bmiRangeStart - $bmiRangeEnd")
                }
            }
        }
    },

    OBESE_CLASS_2(
        nameRes = R.string.obese_class_2,
        color = Color(0xFFF95721),
        bmiRangeStart = 35.0f,
        bmiRangeEnd = 39.9f,
    ){
        override fun isValid(bmiValue: Float): Boolean {
            return bmiValue in bmiRangeStart..bmiRangeEnd
        }

        override fun getAnnotatedString(): AnnotatedString {
            return buildAnnotatedString {
                append("BMI ")
                withStyle(style = SpanStyle(color)) {
                    append("$bmiRangeStart - $bmiRangeEnd")
                }
            }
        }
    },

    OBESE_CLASS_3(
        nameRes = R.string.obese_class_3,
        color = Color(0xFFAD2E04),
        bmiRangeStart = 40.0f,
        bmiRangeEnd = 100.0f,
    ){
        override fun isValid(bmiValue: Float): Boolean {
            return bmiValue >= bmiRangeStart
        }

        override fun getAnnotatedString(): AnnotatedString {
            return buildAnnotatedString {
                append("BMI")
                append(" > ")
                withStyle(style = SpanStyle(color)) {
                    append("$bmiRangeStart")
                }
            }
        }
    };

    abstract fun isValid(bmiValue: Float): Boolean

    abstract fun getAnnotatedString(): AnnotatedString
}