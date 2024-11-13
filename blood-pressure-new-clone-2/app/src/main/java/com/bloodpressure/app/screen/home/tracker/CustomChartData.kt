package com.bloodpressure.app.screen.home.tracker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.bloodpressure.app.utils.TextFormatter
import com.patrykandpatrick.vico.compose.component.shape.shader.fromBrush
import com.patrykandpatrick.vico.compose.style.ChartStyle
import com.patrykandpatrick.vico.core.DefaultAlpha
import com.patrykandpatrick.vico.core.DefaultColors
import com.patrykandpatrick.vico.core.DefaultDimens
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShaders
import com.patrykandpatrick.vico.core.entry.ChartEntry
import java.util.Date

class Entry(
    val date: Date,
    override val x: Float,
    override val y: Float
) : ChartEntry {
    override fun withY(y: Float): ChartEntry {
        return Entry(date, x, y)
    }
}

private const val COLOR_1_CODE = 0xffa485e0
private const val PERSISTENT_MARKER_X = 10f

private val color1 = Color(COLOR_1_CODE)
val chartColors = listOf(Color(0xFFF95721), Color(0xFF1892FA))

class BottomAxisValueFormatter(private val textFormatter: TextFormatter) :
    AxisValueFormatter<AxisPosition.Horizontal.Bottom> {
    override fun formatValue(value: Float, chartValues: ChartValues): CharSequence {
        val date =
            (chartValues.chartEntryModel.entries.firstOrNull()
                ?.getOrNull(value.toInt()) as? Entry)?.date
        return date?.let { textFormatter.formatLineChartDate(it) }.orEmpty()
    }
}

@Composable
internal fun rememberChartStyle(chartColors: List<Color>) =
    rememberChartStyle(columnChartColors = chartColors, lineChartColors = chartColors)

@Composable
internal fun rememberChartStyle(
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
                columnChartColors.map { columnChartColor ->
                    LineComponent(
                        columnChartColor.toArgb(),
                        DefaultDimens.COLUMN_WIDTH,
                        Shapes.roundedCornerShape(DefaultDimens.COLUMN_ROUNDNESS_PERCENT),
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
