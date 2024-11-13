package com.bloodpressure.app.screen.waterreminder.history

import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.bloodpressure.app.R
import com.bloodpressure.app.data.model.WaterCupRecord
import com.bloodpressure.app.screen.waterreminder.WaterReminderViewModel
import com.bloodpressure.app.ui.component.chart.CustomBarChartRender
import com.bloodpressure.app.ui.component.chart.DateAxisFormatter
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.patrykandpatrick.vico.core.extension.round

val emptyChartDataRecord = WaterCupRecord(
    createdAt = 0L, numberOfCup = 0, bottleSize = 0, time = "", date = "", actualWater = 0
)
@Composable
fun WaterStatisticBarChar(
    chartData: List<WaterCupRecord>,
    goal: Int,
    isMl: Boolean
) {
    val numberOfBars = 12
    val valueFraction = if (isMl) 200 else 1
    val inactiveIcon = AppCompatResources.getDrawable(LocalContext.current, R.drawable.check_circle_inactive)
    val activeIcon = AppCompatResources.getDrawable(LocalContext.current, R.drawable.check_circle_active)

    val chartDataRecords = chartData.toMutableList()
    repeat(numberOfBars - chartData.size) {
        chartDataRecords.add(emptyChartDataRecord)
    }
    val waterTotal = chartData
        .groupBy { it.date }
        .map { it.key to it.value[0].actualWater }
        .map {
            it.first to if (isMl) it.second else  WaterReminderViewModel.convertMlToOz(it.second)
        }
        .sortedBy { it.first }
        .sortedByDescending {
            it.first
        }

    val maximum = waterTotal.maxOf { it.second }
    val res = if (maximum < goal) ((goal * 1.2f) / valueFraction).round * valueFraction
    else ((maximum * 1.2f) / valueFraction).round * valueFraction

    AndroidView(
        modifier = Modifier.height(200.dp)
            .fillMaxWidth(),
        factory = { context ->
            BarChart(context)
        },
        update = { chart ->

            chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(entry: Entry, h: Highlight) {
                    val recordIndex = entry.x.toInt()
                    val heartRateRecord = chartData[recordIndex]

                    if (heartRateRecord != emptyChartDataRecord) {
//                        onRecordSelected(heartRateRecord)
                    }
                }

                override fun onNothingSelected() {
                }
            })

            val barData: BarData

            val dateLabels = mutableListOf<String>()
            val entries = mutableListOf<BarEntry>()

            waterTotal.forEachIndexed { index, (dateString, y) ->
                val drawable = if (y < goal) inactiveIcon else activeIcon
                entries.add(BarEntry(index.toFloat(), y.toFloat(), drawable))
                dateLabels.add(dateString)
            }

            val dataSet = MyBarDataSet1(entries, "").apply {
                setColors(Color(0xFF1892FA).toArgb(), Color(0xFF62A970).toArgb(), Color(0xFFAE2F05).toArgb())
            }

            dataSet.setDrawValues(false)

            barData = BarData(dataSet)
            barData.setValueTextSize(10f)

            barData.barWidth = 0.4f

            chart.apply {
                chart.setDrawBarShadow(false)
                chart.setDrawValueAboveBar(true)

                chart.description.isEnabled = false
                chart.setMaxVisibleValueCount(6000)
                chart.setPinchZoom(false)
                chart.setDrawGridBackground(false)

                val xAxisFormatter = DateAxisFormatter(dateLabels)

                val xAxis = chart.xAxis
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.setDrawGridLines(false)
                xAxis.granularity = 1f

                xAxis.labelCount = xAxis.labelCount.coerceAtLeast(4)
                xAxis.valueFormatter = xAxisFormatter

                val leftAxis = chart.axisLeft
                leftAxis.setLabelCount(5, false)
                leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)

                leftAxis.spaceTop = 15f
                leftAxis.axisMinimum = 0f
                leftAxis.axisMaximum = res

//                val mv = HeartRateMarkerView(context, heartRateRecords)
//                mv.chartView = chart
//
//                chart.marker = mv

                chart.setScaleEnabled(false)
                chart.setPinchZoom(false)
                chart.isDoubleTapToZoomEnabled = false
                chart.axisRight.isEnabled = false

                chart.axisLeft.enableGridDashedLine(8f, 10f, 1f)
                chart.axisLeft.gridColor = context.resources.getColor(R.color.gray_scale_600)
                chart.axisLeft.axisLineColor = context.resources.getColor(R.color.gray_scale_600)
                chart.xAxis.axisLineColor = context.resources.getColor(R.color.gray_scale_600)
                chart.legend.isEnabled = false

//                chart.isDragXEnabled = chartData[7] != emptyChartDataRecord

                val barChartRender = CustomBarChartRender(chart, chart.animator, chart.viewPortHandler)
                barChartRender.setRadius(8)
                chart.renderer = barChartRender

                chart.data = barData
                chart.setVisibleXRangeMaximum(7f)
                chart.moveViewToX(-10f)

                chart.invalidate()
            }
        }
    )
}


class MyBarDataSet1(yVals: List<BarEntry>, label: String) : BarDataSet(yVals, label) {
    override fun getEntryIndex(e: BarEntry?): Int {
        return 0
    }

    override fun getColor(index: Int): Int {
        return if (getEntryForIndex(index).y <= 6000) mColors[0] else if (getEntryForIndex(index).y > 6000 && getEntryForIndex(index).y <= 10000) mColors[1] else mColors[2]
    }
}
