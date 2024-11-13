package com.bloodpressure.app.ui.component.chart

import android.content.Context
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.bloodpressure.app.R
import com.bloodpressure.app.data.model.HeartRateRecord
import com.bloodpressure.app.screen.heartrate.detail.emptyHeartRateRecord
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF

class HeartRateMarkerView(context: Context, private val heartRateRecords: List<HeartRateRecord>) : MarkerView(context, R.layout.custom_marker_view) {
    private val tvContent: TextView = findViewById(R.id.tvContent)
    private val markerView: RelativeLayout = findViewById(R.id.markerView)

    override fun refreshContent(entry: Entry, highlight: Highlight) {

        entry.let {
            if (it is BarEntry) {
                val heartRateRecord = getHeartRateRecord(it)

                if (heartRateRecord != null && heartRateRecord != emptyHeartRateRecord) {
                    tvContent.text = context.getString(heartRateRecord.type.nameRes)
                    markerView.visibility = View.VISIBLE
                } else {
                    markerView.visibility = View.GONE
                }
            }
        }
        super.refreshContent(entry, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-(width / 2).toFloat(), -height.toFloat())
    }

    private fun getHeartRateRecord(entry: BarEntry): HeartRateRecord? {

        return try {
            val recordIndex = entry.x.toInt()
            val heartRateRecord = heartRateRecords[recordIndex]
            heartRateRecord
        } catch (e: Exception) {
            null
        }

    }
}