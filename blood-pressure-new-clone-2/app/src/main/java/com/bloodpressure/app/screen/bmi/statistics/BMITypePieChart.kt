package com.bloodpressure.app.screen.bmi.statistics

import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.bloodpressure.app.data.model.BMIRecord
import com.bloodpressure.app.screen.bmi.add.BMIType
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.MPPointF

@Composable
fun BMITypePieChart(
    modifier: Modifier = Modifier,
    data: Map<BMIType, List<BMIRecord>>
) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            PieChart(context).apply {
                val marker = PieChartMaker(this.context)
                marker.chartView = this
                this.marker = marker
            }
        },
        update = { pieChart ->
            val entries = data.map { PieEntry(it.value.size.toFloat(), it.key.name) }
            val dataSet = MyPieDataSet(entries, "")

            dataSet.sliceSpace = 3f
            dataSet.iconsOffset = MPPointF(0f, 40f)
            dataSet.selectionShift = 5f

            dataSet.setValueTextColors(listOf(Color.White.toArgb()))
            dataSet.valueTextSize = 14f

            val pieData = PieData(dataSet)
            pieChart.data = pieData

            pieChart.setUsePercentValues(false)
            pieChart.description.isEnabled = false
            pieChart.setExtraOffsets(5f, 10f, 5f, 5f)

            pieChart.dragDecelerationFrictionCoef = 0.95f

            pieChart.isDrawHoleEnabled = true
            pieChart.setHoleColor(Color.White.toArgb())

            pieChart.setTransparentCircleColor(Color.White.toArgb())
            pieChart.setTransparentCircleAlpha(110)

            pieChart.holeRadius = 54f
            pieChart.transparentCircleRadius = 61f
            pieChart.setHoleColor(Color.Transparent.toArgb())

            pieChart.setDrawCenterText(true)

            pieChart.rotationAngle = 0f
            pieChart.isRotationEnabled = true
            pieChart.isHighlightPerTapEnabled = true
            pieChart.setTouchEnabled(true)

            val marker = PieChartMaker(pieChart.context)
            marker.chartView = pieChart
            pieChart.marker = marker


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
    label: String,
) : PieDataSet(yVals, label) {
    override fun getEntryIndex(e: PieEntry?): Int {
        return 0
    }


    override fun getColor(index: Int): Int {
        return BMIType.values().firstOrNull {
            it.name == getEntryForIndex(index).label
        }?.color?.toArgb() ?: BMIType.NORMAL.color.toArgb()
    }
}