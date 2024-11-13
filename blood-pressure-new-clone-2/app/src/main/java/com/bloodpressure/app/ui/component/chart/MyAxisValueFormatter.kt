package com.bloodpressure.app.ui.component.chart

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter

class MyAxisValueFormatter : ValueFormatter() {

    override fun getFormattedValue(value: Float, axis: AxisBase): String {
        return "${value.toInt()} Hihi"
    }
}
