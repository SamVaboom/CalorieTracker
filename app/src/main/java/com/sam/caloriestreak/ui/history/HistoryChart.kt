package com.sam.caloriestreak.ui.history

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sam.caloriestreak.domain.history.HistoryMetric
import com.sam.caloriestreak.domain.history.HistoryPoint
import com.sam.caloriestreak.ui.theme.AppColors
import com.sam.caloriestreak.ui.theme.AppDimensions
import java.time.LocalDate
import kotlin.math.abs
import kotlin.math.max

@Composable
fun HistoryChart(
    points: List<HistoryPoint>,
    metric: HistoryMetric,
    targetCalories: Double,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember { mutableIntStateOf(points.lastIndex.coerceAtLeast(0)) }
    LaunchedEffect(points, metric) { selectedIndex = points.lastIndex.coerceAtLeast(0) }

    val selected = points.getOrNull(selectedIndex)
    val lineColor = if (metric == HistoryMetric.SCORE) AppColors.Violet else AppColors.Coral
    val pointColor = if (metric == HistoryMetric.SCORE) AppColors.Cyan else AppColors.Warning
    val freezeColor = AppColors.Freeze
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
    val referenceColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
    val density = LocalDensity.current

    Column(modifier) {
        if (selected != null) {
            val valueText = when (metric) {
                HistoryMetric.SCORE -> "${selected.score.toInt()}%"
                HistoryMetric.CALORIES -> "${selected.calories.toInt()} kcal"
            }
            Text(
                text = "${LocalDate.ofEpochDay(selected.epochDay)} · $valueText" + if (selected.freezeProtected) " · Freeze protected" else "",
                fontWeight = FontWeight.SemiBold,
                color = lineColor,
                modifier = Modifier.padding(bottom = AppDimensions.Space8)
            )
        }

        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val spacing = when {
                points.size <= 14 -> 36.dp
                points.size <= 45 -> 24.dp
                else -> 12.dp
            }
            val desiredWidth = (spacing.value * max(points.size - 1, 1) + 48f).dp
            val canvasWidth = maxOf(maxWidth, desiredWidth)
            val scrollState = rememberScrollState()
            val leftPaddingPx = with(density) { 24.dp.toPx() }
            val rightPaddingPx = with(density) { 24.dp.toPx() }

            Column(Modifier.horizontalScroll(scrollState)) {
                Canvas(
                    modifier = Modifier
                        .width(canvasWidth)
                        .height(240.dp)
                        .semantics {
                            contentDescription = "${metric.label} history graph with ${points.size} points. Tap a point for details."
                        }
                        .pointerInput(points, metric, canvasWidth) {
                            detectTapGestures { tap ->
                                if (points.isEmpty()) return@detectTapGestures
                                val usableWidth = size.width - leftPaddingPx - rightPaddingPx
                                val step = if (points.size <= 1) 1f else usableWidth / (points.size - 1)
                                selectedIndex = points.indices.minByOrNull { index -> abs(tap.x - (leftPaddingPx + index * step)) } ?: selectedIndex
                            }
                        }
                ) {
                    val top = 12.dp.toPx()
                    val bottom = size.height - 20.dp.toPx()
                    val usableHeight = bottom - top
                    val usableWidth = size.width - leftPaddingPx - rightPaddingPx
                    val maxValue = when (metric) {
                        HistoryMetric.SCORE -> 100.0
                        HistoryMetric.CALORIES -> max(targetCalories * 1.2, points.maxOfOrNull { it.calories }?.times(1.1) ?: targetCalories)
                    }.coerceAtLeast(1.0)

                    repeat(5) { index ->
                        val y = top + usableHeight * index / 4f
                        drawLine(gridColor, Offset(leftPaddingPx, y), Offset(size.width - rightPaddingPx, y), 1.dp.toPx())
                    }

                    fun yFor(value: Double): Float = bottom - (value / maxValue).toFloat().coerceIn(0f, 1f) * usableHeight
                    val references = when (metric) {
                        HistoryMetric.SCORE -> listOf(80.0, 85.0)
                        HistoryMetric.CALORIES -> listOf(targetCalories)
                    }
                    references.forEach { value ->
                        val y = yFor(value)
                        drawLine(referenceColor, Offset(leftPaddingPx, y), Offset(size.width - rightPaddingPx, y), 1.dp.toPx())
                    }

                    val step = if (points.size <= 1) 0f else usableWidth / (points.size - 1)
                    val path = Path()
                    points.forEachIndexed { index, point ->
                        val x = leftPaddingPx + index * step
                        val y = yFor(point.value(metric))
                        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    if (points.size > 1) drawPath(path, lineColor, style = androidx.compose.ui.graphics.drawscope.Stroke(3.dp.toPx()))

                    points.forEachIndexed { index, point ->
                        val x = leftPaddingPx + index * step
                        val y = yFor(point.value(metric))
                        drawCircle(if (point.freezeProtected) freezeColor else pointColor, if (index == selectedIndex) 6.dp.toPx() else 4.dp.toPx(), Offset(x, y))
                    }
                }
                Row(
                    modifier = Modifier.width(canvasWidth).padding(horizontal = AppDimensions.Space12),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                ) {
                    Text(points.firstOrNull()?.let { LocalDate.ofEpochDay(it.epochDay).toString() }.orEmpty(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(points.lastOrNull()?.let { LocalDate.ofEpochDay(it.epochDay).toString() }.orEmpty(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Text(
            when (metric) {
                HistoryMetric.SCORE -> "Reference lines: 80% streak-safe and 85% freeze-qualifying"
                HistoryMetric.CALORIES -> "Reference line: ${targetCalories.toInt()} kcal target"
            },
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = AppDimensions.Space8)
        )
    }
}
