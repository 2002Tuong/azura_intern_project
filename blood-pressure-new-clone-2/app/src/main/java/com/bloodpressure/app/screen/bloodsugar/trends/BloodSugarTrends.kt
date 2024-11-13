package com.bloodpressure.app.screen.bloodsugar.trends

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import com.bloodpressure.app.R
import com.bloodpressure.app.data.model.BloodSugarRecord
import com.bloodpressure.app.screen.bloodsugar.convertToMg
import com.bloodpressure.app.screen.bloodsugar.convertToMole
import com.bloodpressure.app.screen.bloodsugar.type.BloodSugarRateType
import com.bloodpressure.app.screen.bloodsugar.type.BloodSugarStateType
import com.bloodpressure.app.ui.component.chart.BloodSugarDataSet
import com.bloodpressure.app.ui.component.chart.BloodSugarMarkerView
import com.bloodpressure.app.ui.component.chart.CustomBarChartRender
import com.bloodpressure.app.ui.component.chart.DateAxisFormatter
import com.bloodpressure.app.ui.component.chart.MyBarDataSet
import com.bloodpressure.app.utils.BloodSugarUnit
import com.bloodpressure.app.utils.Logger
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener

val emptyBloodSugarRecord = BloodSugarRecord(
    bloodSugar = 0f,
    time = "",
    date = "",
    bloodSugarStateType = BloodSugarStateType.DEFAULT,
    targetRanges = listOf(),
    bloodSugarRateType = BloodSugarRateType.NORMAL,
    notes = setOf(),
    rowId = 0L,
    bloodSugarUnit = BloodSugarUnit.MILLIMOLES_PER_LITRE
)

@Composable
fun BloodSugarTrendsBarChart(
    modifier: Modifier = Modifier,
    records: List<BloodSugarRecord>,
    onRecordSelected: (BloodSugarRecord) -> Unit,
    bloodSugarUnit: BloodSugarUnit
) {
    val numberOfBars = 12

    val bloodSugarRecordsAllUnit = records.toMutableList()
    repeat(numberOfBars - records.size) {
        bloodSugarRecordsAllUnit.add(emptyBloodSugarRecord)
    }

    val bloodSugarRecords = bloodSugarRecordsAllUnit.map {
        if (it.bloodSugarUnit == bloodSugarUnit) it
        else if (bloodSugarUnit == BloodSugarUnit.MILLIMOLES_PER_LITRE){
            it.copy(
                bloodSugar = it.bloodSugar.convertToMole()
            )
        } else {
            it.copy(
                bloodSugar = it.bloodSugar.convertToMg()
            )
        }
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
                    val bloodSugarRecord = bloodSugarRecords[recordIndex]

                    if (bloodSugarRecord != emptyBloodSugarRecord) {
                        onRecordSelected(bloodSugarRecord)
                    }
                }

                override fun onNothingSelected() {
                }
            })

            val barData: BarData

            val dateLabels = mutableListOf<String>()
            val entries = mutableListOf<BarEntry>()
            val bloodSugarRateTypes = mutableListOf<BloodSugarRateType>()

            for ((index, record) in bloodSugarRecords.withIndex()) {
                val xValue = index.toFloat()
                val yValue = record.bloodSugar
                entries.add(BarEntry(xValue, yValue))
                bloodSugarRateTypes.add(record.bloodSugarRateType)
                dateLabels.add(record.date)
            }

            val dataSet = BloodSugarDataSet(entries, "", bloodSugarRateTypes).apply {
                setColors(
                    Color(0xFF1892FA).toArgb(),
                    Color(0xFF62A970).toArgb(),
                    Color(0xFFF4763C).toArgb(),
                    Color(0xFFAE2F05).toArgb(),
                )
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

                val mv = BloodSugarMarkerView(context, bloodSugarRecords)
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

                chart.isDragXEnabled = bloodSugarRecords[7] != emptyBloodSugarRecord

                val barChartRender =
                    CustomBarChartRender(chart, chart.animator, chart.viewPortHandler)
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
