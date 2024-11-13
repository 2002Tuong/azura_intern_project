package com.bloodpressure.app.screen.waterreminder.history

import android.graphics.Typeface
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import com.bloodpressure.app.R
import com.bloodpressure.app.data.model.WaterCupRecord
import com.bloodpressure.app.screen.home.tracker.BottomAxisValueFormatter
import com.bloodpressure.app.screen.home.tracker.Entry
import com.bloodpressure.app.screen.home.tracker.rememberMarker
import com.bloodpressure.app.screen.waterreminder.WaterReminderViewModel
import com.bloodpressure.app.utils.LocalTextFormatter
import com.patrykandpatrick.vico.compose.axis.axisLabelComponent
import com.patrykandpatrick.vico.compose.axis.axisTickComponent
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollSpec
import com.patrykandpatrick.vico.compose.component.lineComponent
import com.patrykandpatrick.vico.compose.component.shape.shader.fromBrush
import com.patrykandpatrick.vico.compose.component.shapeComponent
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.compose.dimensions.dimensionsOf
import com.patrykandpatrick.vico.compose.style.ChartStyle
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.DefaultAlpha
import com.patrykandpatrick.vico.core.DefaultColors
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.DecimalFormatAxisValueFormatter
import com.patrykandpatrick.vico.core.axis.horizontal.HorizontalAxis
import com.patrykandpatrick.vico.core.chart.decoration.ThresholdLine
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider
import com.patrykandpatrick.vico.core.component.shape.DashedShape
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.component.shape.ShapeComponent
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShaders
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.extension.round
import com.patrykandpatrick.vico.core.scroll.InitialScroll

@Composable
fun WaterStatisticChart(
    chartData: List<WaterCupRecord>,
    goal: Int,
    isMl: Boolean
) {
    val valueFraction = if (isMl) 200 else 1
    val textFormatter = LocalTextFormatter.current
    val chartEntryModelProducer = remember(chartData) {
        val waterTotal = chartData
            .groupBy { it.date }
            .map { it.key to it.value[0].actualWater }
            .map {
                it.first to if (isMl) it.second else  WaterReminderViewModel.convertMlToOz(it.second)
            }
            .sortedBy { it.first }
            .mapIndexed { index, (dateString, y) ->
                Entry(textFormatter.parse(dateString), index.toFloat(), y.toFloat())
            }.sortedByDescending { it.date }

        ChartEntryModelProducer().apply {
            setEntries(listOf(waterTotal))
        }
    }
    val bottomAxisValueFormatter = remember(textFormatter) {
        BottomAxisValueFormatter(textFormatter)
    }
    val startAxisValueFormatter = remember {
        DecimalFormatAxisValueFormatter<AxisPosition.Vertical.Start>("##")
    }
    val bottomTickPosition = remember(chartData) {
        if (chartData.size <= 5) {
            HorizontalAxis.TickPosition.Center(0, 1)
        } else {
            HorizontalAxis.TickPosition.Center(0, 2)
        }
    }
    val thresholdLine = rememberThresholdLine(goal = goal.toFloat())
    val marker = rememberMarker()
    val icon = ResourcesCompat.getDrawable(LocalContext.current.resources, R.drawable.check_circle_inactive, null)!!
    val background = ShapeComponent(
        shape = Shapes.drawableShape(
            drawable = icon,
            keepAspectRatio = true,
        )
    )
    val textComponent = textComponent(
        background = background
    )
    val lineComponent = axisTickComponent(
        shape = Shapes.drawableShape(
            drawable = icon,
            keepAspectRatio = true,
        )
    )
    ProvideChartStyle(
        rememberWaterChartStyle(columnChartColors = chartColors, lineChartColors = chartColors)
    ) {
        Box(modifier = Modifier.background(Color.White)) {
            Chart(
                chart = columnChart(
                    persistentMarkers = if (chartData.size == 1) {
                        remember(marker) { mapOf(0f to marker) }
                    } else {
                        null
                    },
                    decorations = remember(thresholdLine) { listOf(thresholdLine) },
                    axisValuesOverrider = object : AxisValuesOverrider<ChartEntryModel> {
                        override fun getMaxX(model: ChartEntryModel): Float {
                            return model.maxX.coerceAtLeast(4f)
                        }

                        override fun getMaxY(model: ChartEntryModel): Float {
                            return if (model.maxY < goal) ((goal * 1.2f) / valueFraction).round * valueFraction
                            else ((model.maxY * 1.2f) / valueFraction).round * valueFraction
                        }
                    }
                ),
                chartModelProducer = chartEntryModelProducer,
                startAxis = startAxis(
                    valueFormatter = startAxisValueFormatter,
                    maxLabelCount = 5
                ),
                bottomAxis = bottomAxis(
                    label = axisLabelComponent(horizontalMargin = 2.dp),
                    guideline = null,
                    valueFormatter = bottomAxisValueFormatter,
                    tickLength = 0.dp,
                    tickPosition = bottomTickPosition
                ),
                marker = rememberMarker(),
                chartScrollSpec = rememberChartScrollSpec(initialScroll = InitialScroll.End)
            )
        }
    }

}

@Composable
private fun rememberThresholdLine(
    goal: Float
): ThresholdLine {
    val line = shapeComponent(
        color = Color(0xFF62A970),
        shape = guidelineShape,
    )
    val label = textComponent(
        color = Color.Black,
        background = shapeComponent(Shapes.rectShape, Color(0xFF62A970)),
        padding = thresholdLineLabelPadding,
        margins = thresholdLineLabelMargins,
        typeface = Typeface.MONOSPACE,
    )
    return remember(line, label) {
        ThresholdLine(thresholdValue = goal, lineComponent = line, labelComponent = label)
    }
}

@Composable
internal fun rememberWaterChartStyle(
    columnChartColors: List<Color>,
    lineChartColors: List<Color>
): ChartStyle {
    return remember(columnChartColors, lineChartColors) {
        val defaultColors = DefaultColors.Light
        ChartStyle(
            ChartStyle.Axis(
                axisLabelColor = Color(defaultColors.axisLabelColor),
                axisGuidelineColor = Color(defaultColors.axisGuidelineColor),
                axisLineColor = Color(defaultColors.axisLineColor),
            ),
            ChartStyle.ColumnChart(
                columns = columnChartColors.map { columnChartColor ->
                    LineComponent(
                        columnChartColor.toArgb(),
                        COLUMN_WIDTH,
                        Shapes.roundedCornerShape(
                            topLeftPercent = COLUMN_ROUNDED_PERCENT,
                            topRightPercent = COLUMN_ROUNDED_PERCENT
                        )
                    )
                },
            ),
            ChartStyle.LineChart(
                lineChartColors.map { lineChartColor ->
                    LineChart.LineSpec(
                        lineColor = lineChartColor.toArgb(),
                        lineBackgroundShader = DynamicShaders.fromBrush(
                            Brush.verticalGradient(
                                listOf(
                                    lineChartColor.copy(DefaultAlpha.LINE_BACKGROUND_SHADER_START),
                                    lineChartColor.copy(DefaultAlpha.LINE_BACKGROUND_SHADER_END),
                                ),
                            ),
                        ),
                    )
                },
            ),
            ChartStyle.Marker(),
            Color(defaultColors.elevationOverlayColor),
        )
    }
}
val chartColors = listOf(Color(0xFF1892FA))
private val thresholdLineLabelMarginValue = 4.dp
private val thresholdLineLabelHorizontalPaddingValue = 8.dp
private val thresholdLineLabelVerticalPaddingValue = 2.dp
private val thresholdLineLabelPadding =
    dimensionsOf(thresholdLineLabelHorizontalPaddingValue, thresholdLineLabelVerticalPaddingValue)
private val thresholdLineLabelMargins = dimensionsOf(thresholdLineLabelMarginValue)
private const val COLUMN_ROUNDED_PERCENT = 30
private const val COLUMN_WIDTH = 12f
private const val GUIDELINE_DASH_LENGTH_DP = 8f
private const val GUIDELINE_GAP_LENGTH_DP = 4f
private val guidelineShape =
    DashedShape(Shapes.pillShape, GUIDELINE_DASH_LENGTH_DP, GUIDELINE_GAP_LENGTH_DP)
