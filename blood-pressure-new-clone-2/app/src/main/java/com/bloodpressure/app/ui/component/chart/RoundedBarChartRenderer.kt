package com.bloodpressure.app.ui.component.chart

import android.graphics.Canvas
import android.graphics.Path
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.renderer.BarChartRenderer
import com.github.mikephil.charting.utils.ViewPortHandler
import com.github.mikephil.charting.utils.Utils

class RoundedBarChartRenderer(
    chart: BarDataProvider,
    animator: ChartAnimator,
    viewPortHandler: ViewPortHandler
) : BarChartRenderer(chart, animator, viewPortHandler) {

    private val barRoundedCornerRadius = 20f // Adjust this value as needed

    override fun drawDataSet(c: Canvas, dataSet: IBarDataSet, index: Int) {
        super.drawDataSet(c, dataSet, index)

        if (!dataSet.isVisible || dataSet.entryCount == 0) {
            return
        }

        val trans = mChart.getTransformer(dataSet.axisDependency)

        mBarBorderPaint.color = dataSet.barBorderColor
        mBarBorderPaint.strokeWidth = Utils.convertDpToPixel(dataSet.barBorderWidth)

        val drawBorder = dataSet.barBorderWidth > 0f

        val buffer = mBarBuffers[index]
        buffer.setPhases(mAnimator.phaseX, mAnimator.phaseY)
        buffer.setDataSet(index)
        buffer.setInverted(mChart.isInverted(dataSet.axisDependency))
        buffer.setBarWidth(mChart.barData.barWidth)
        buffer.feed(dataSet)

        trans.pointValuesToPixel(buffer.buffer)

        val hasStacks = dataSet.isStacked

        val roundedBarPath = Path()

        for (j in 0 until buffer.size()) {
            val x = buffer.buffer[j]

            val entryIndex = j / 2 // Get the corresponding entry index
            if (entryIndex >= 0 && entryIndex < dataSet.entryCount) {
                val e = dataSet.getEntryForIndex(entryIndex)

                val yNeg = e.y - e.negativeSum
                val yPos = e.y + e.positiveSum

                val left = x - 0.5f
                val right = x + 0.5f

                if (hasStacks && dataSet.isStacked) {
                    roundedBarPath.reset()
                    roundedBarPath.addRoundRect(
                        left,
                        yNeg,
                        right,
                        yPos,
                        barRoundedCornerRadius,
                        barRoundedCornerRadius,
                        Path.Direction.CW
                    )
                    c.drawPath(roundedBarPath, mRenderPaint)
                    if (drawBorder) {
                        c.drawPath(roundedBarPath, mBarBorderPaint)
                    }
                }
            }
        }
    }
}
