package com.bloodpressure.app.screen.bloodsugar.statistics

import android.content.Context
import android.graphics.Color
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import com.bloodpressure.app.R
import com.bloodpressure.app.screen.bloodsugar.type.BloodSugarRateType
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.MPPointF

@Composable
fun BloodSugarStatisticsPieChart(
    modifier: Modifier = Modifier,
    lowRangeMin: Int = 40,
    normalRangeMax: Int = 60,
    preDiabetesValue: Int = 126,
    diabetesValue: Int = 126,
) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            PieChart(context)
        },
        update = { pieChart ->
            val entries = listOf(
                PieEntry(lowRangeMin.toFloat(), MyPieDataSet.LOW),
                PieEntry(normalRangeMax.toFloat(), MyPieDataSet.NORMAL),
                PieEntry(preDiabetesValue.toFloat(), MyPieDataSet.PRE_DIABETES),
                PieEntry(diabetesValue.toFloat(), MyPieDataSet.DIABETES)
            ).filter { it.value > 0f }

            val dataSet = MyPieDataSet(entries, "")

            dataSet.setDrawIcons(false)

            dataSet.sliceSpace = 3f
            dataSet.iconsOffset = MPPointF(0f, 40f)
            dataSet.selectionShift = 5f

            dataSet.colors = BloodSugarRateType.values().map {
                it.color.toArgb()
            }
            dataSet.setValueTextColors(listOf(Color.WHITE))
            dataSet.valueTextSize = 14f

            val data = PieData(dataSet)
            pieChart.data = data

            pieChart.setUsePercentValues(false)
            pieChart.description.isEnabled = false
            pieChart.setExtraOffsets(5f, 10f, 5f, 5f)

            pieChart.dragDecelerationFrictionCoef = 0.95f

            pieChart.isDrawHoleEnabled = true
            pieChart.setHoleColor(Color.WHITE)

            pieChart.setTransparentCircleColor(Color.WHITE)
            pieChart.setTransparentCircleAlpha(110)

            pieChart.holeRadius = 54f
            pieChart.transparentCircleRadius = 61f

            pieChart.setDrawCenterText(true)

            pieChart.rotationAngle = 0f
            pieChart.isRotationEnabled = true
            pieChart.isHighlightPerTapEnabled = true

            pieChart.animateY(1400, Easing.EaseInOutQuad)
            pieChart.setDrawEntryLabels(false)
            pieChart.legend.isEnabled = false
            dataSet.setDrawValues(true)

            pieChart.invalidate()
        }
    )
}

private class MyPieDataSet(
    yVals: List<PieEntry>,
    label: String
) : PieDataSet(yVals, label) {
    override fun getEntryIndex(e: PieEntry?): Int {
        return 0
    }


    override fun getColor(index: Int): Int {
        return when (getEntryForIndex(index).label) {
            LOW -> mColors[0]
            NORMAL -> mColors[1]
            PRE_DIABETES -> mColors[2]
            else -> mColors[3]
        }

    }
    companion object {
        const val LOW = "low"
        const val NORMAL = "normal"
        const val PRE_DIABETES = "pre-diabetes"
        const val DIABETES = "diabetes"
    }
}