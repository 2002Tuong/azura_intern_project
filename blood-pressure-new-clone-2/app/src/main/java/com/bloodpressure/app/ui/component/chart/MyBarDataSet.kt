package com.bloodpressure.app.ui.component.chart

import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry


class MyBarDataSet(yVals: List<BarEntry>, label: String) : BarDataSet(yVals, label) {
    override fun getEntryIndex(e: BarEntry?): Int {
        return 0
    }

    override fun getColor(index: Int): Int {
        return if (getEntryForIndex(index).y <= 60) mColors[0] else if (getEntryForIndex(index).y > 60 && getEntryForIndex(index).y <= 100) mColors[1] else mColors[2]
    }
}
