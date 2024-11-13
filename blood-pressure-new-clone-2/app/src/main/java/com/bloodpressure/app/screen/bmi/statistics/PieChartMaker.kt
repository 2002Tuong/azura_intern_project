package com.bloodpressure.app.screen.bmi.statistics

import android.content.Context
import android.widget.TextView
import com.bloodpressure.app.R
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF

class PieChartMaker(context: Context) : MarkerView(context, R.layout.pie_chart_marker) {
    private val type: TextView = findViewById(R.id.tvContent)
    private val size: TextView = findViewById(R.id.tvSize)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        if (e is PieEntry) {
            type.text = e.label
            size.text =
                if (e.value.toInt() > 1) "${e.value.toInt()} ${context.getString(R.string.records)}"
                else "${e.value.toInt()} ${context.getString(R.string.record)}"
        }
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-(width / 2).toFloat(), -(height).toFloat())
    }
}