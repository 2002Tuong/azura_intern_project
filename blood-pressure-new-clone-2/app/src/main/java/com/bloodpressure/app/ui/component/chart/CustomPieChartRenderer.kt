package com.bloodpressure.app.ui.component.chart

import android.graphics.Canvas
import android.graphics.Color
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.renderer.PieChartRenderer
import com.github.mikephil.charting.utils.ViewPortHandler

class CustomPieChartRenderer(
    chart: PieChart,
    animator: ChartAnimator,
    viewPortHandler: ViewPortHandler
) : PieChartRenderer(chart, animator, viewPortHandler) {

    override fun drawHighlighted(c: Canvas, indices: Array<out Highlight>?) {
        // Calculate highlight radius based on selected slice
        val highlightRadius = (mChart.radius + mChart.holeRadius) * 0.5f

        // Calculate the center position of the pie chart
        val centerX = mChart.width / 2f
        val centerY = mChart.height / 2f

        // Customize highlight paint
        mHighlightPaint.color = Color.BLUE // Set your desired highlight color
        mHighlightPaint.alpha = 100 // Set alpha level

        // Draw highlight circle
        c.drawCircle(centerX, centerY, highlightRadius, mHighlightPaint)
    }

}
