package com.bloodpressure.app.screen.bloodpressure.statistics

import android.content.Context
import android.graphics.Color
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import com.bloodpressure.app.data.model.Record
import com.bloodpressure.app.screen.bmi.add.BMIType
import com.bloodpressure.app.screen.record.BpType
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.MPPointF

@Composable
fun BloodPressureStatisticsPieChart(
    modifier: Modifier = Modifier,
    records: List<Record>
) {
    val hypo = records.count { it.type == BpType.HYPOTENSION }
    val normal = records.count { it.type == BpType.NORMAL }
    val elevated = records.count { it.type == BpType.ELEVATED }
    val hypoStage1 = records.count { it.type == BpType.HYPERTENSION_STAGE_1 }
    val hypoStage2 = records.count { it.type == BpType.HYPERTENSION_STAGE_2 }
    val hypotensive = records.count { it.type == BpType.HYPERTENSIVE }
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            PieChart(context)
        },
        update = { pieChart ->
            val entries = listOf(
                PieEntry(hypo.toFloat(), BpType.HYPOTENSION.name),
                PieEntry(normal.toFloat(), BpType.NORMAL.name),
                PieEntry(elevated.toFloat(), BpType.ELEVATED.name),
                PieEntry(hypoStage1.toFloat(), BpType.HYPERTENSION_STAGE_1.name),
                PieEntry(hypoStage2.toFloat(), BpType.HYPERTENSION_STAGE_2.name),
                PieEntry(hypotensive.toFloat(), BpType.HYPERTENSIVE.name)
            ).filter {
                it.value > 0f
            }

            val dataSet = MyPieDataSet(entries, "")

            dataSet.setDrawIcons(false)

            dataSet.sliceSpace = 3f
            dataSet.iconsOffset = MPPointF(0f, 40f)
            dataSet.selectionShift = 5f

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
    label: String,
) : PieDataSet(yVals, label) {
    override fun getEntryIndex(e: PieEntry?): Int {
        return 0
    }


    override fun getColor(index: Int): Int {
        return BpType.values().firstOrNull {
            it.name == getEntryForIndex(index).label
        }?.color?.toArgb() ?: BMIType.NORMAL.color.toArgb()
    }
}