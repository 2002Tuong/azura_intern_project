package com.bloodpressure.app.screen.heartrate.detail

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import com.bloodpressure.app.R
import com.bloodpressure.app.data.model.HeartRateRecord
import com.bloodpressure.app.screen.heartrate.add.GenderType
import com.bloodpressure.app.screen.heartrate.add.HeartRateType
import com.bloodpressure.app.ui.component.chart.CustomBarChartRender
import com.bloodpressure.app.ui.component.chart.DateAxisFormatter
import com.bloodpressure.app.ui.component.chart.HeartRateMarkerView
import com.bloodpressure.app.ui.component.chart.MyBarDataSet
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener

val emptyHeartRateRecord = HeartRateRecord(
    heartRate = 0,
    time = "",
    date = "",
    type = HeartRateType.NORMAL,
    typeName = "",
    notes = emptySet(),
    age = 0,
    genderType = GenderType.OTHERS,
    createdAt = 0L
)

@Composable
fun TrendsBarChart(modifier: Modifier = Modifier, records: List<HeartRateRecord>, onRecordSelected: (HeartRateRecord) -> Unit) {

    val numberOfBars = 12

    val heartRateRecords = records.toMutableList()
    repeat(numberOfBars - records.size) {
        heartRateRecords.add(emptyHeartRateRecord)
    }

    AndroidView(
        modifier = modifier
            .fillMaxWidth(),
        factory = { context ->
            BarChart(context)
        },
        update = { chart ->

            chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(entry: Entry, h: Highlight) {
                    val recordIndex = entry.x.toInt()
                    val heartRateRecord = heartRateRecords[recordIndex]

                    if (heartRateRecord != emptyHeartRateRecord) {
                        onRecordSelected(heartRateRecord)
                    }
                }

                override fun onNothingSelected() {
                }
            })

            val barData: BarData

            val dateLabels = mutableListOf<String>()
            val entries = mutableListOf<BarEntry>()

            for ((index, record) in heartRateRecords.withIndex()) {
                val xValue = index.toFloat()
                val yValue = record.heartRate.toFloat()
                entries.add(BarEntry(xValue, yValue))
                dateLabels.add(record.date)
            }

            val dataSet = MyBarDataSet(entries, "").apply {
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
                chart.setMaxVisibleValueCount(60)
                chart.setPinchZoom(false)
                chart.setDrawGridBackground(false)

                val xAxisFormatter = DateAxisFormatter(dateLabels)

                val xAxis = chart.xAxis
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.setDrawGridLines(false)
                xAxis.granularity = 1f

                xAxis.labelCount = 7
                xAxis.valueFormatter = xAxisFormatter

                val leftAxis = chart.axisLeft
                leftAxis.setLabelCount(8, false)
                leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
                leftAxis.spaceTop = 15f
                leftAxis.axisMinimum = 0f

                val mv = HeartRateMarkerView(context, heartRateRecords)
                mv.chartView = chart

                chart.marker = mv

                chart.setScaleEnabled(false)
                chart.setPinchZoom(false)
                chart.isDoubleTapToZoomEnabled = false
                chart.axisRight.isEnabled = false

                chart.axisLeft.enableGridDashedLine(8f, 10f, 1f)
                chart.axisLeft.gridColor = context.resources.getColor(R.color.gray_scale_600)
                chart.axisLeft.axisLineColor = context.resources.getColor(R.color.gray_scale_600)
                chart.xAxis.axisLineColor = context.resources.getColor(R.color.gray_scale_600)
                chart.legend.isEnabled = false

                chart.isDragXEnabled = heartRateRecords[7] != emptyHeartRateRecord

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