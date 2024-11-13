package com.bloodpressure.app.ui.component.chart

import com.bloodpressure.app.screen.bloodsugar.type.BloodSugarRateType
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry

class BloodSugarDataSet(
    yVals: List<BarEntry>,
    label: String,
    private val bloodSugarRateTypes: List<BloodSugarRateType>
) : BarDataSet(yVals, label) {
    override fun getEntryIndex(e: BarEntry?): Int {
        return 0
    }

    override fun getColor(index: Int): Int {
        return when (bloodSugarRateTypes[index]) {
             BloodSugarRateType.LOW -> mColors[0]
             BloodSugarRateType.NORMAL -> mColors[1]
             BloodSugarRateType.PRE_DIABETES -> mColors[2]
             BloodSugarRateType.DIABETES -> mColors[3]

        }
    }
}