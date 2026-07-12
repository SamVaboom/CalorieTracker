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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sam.caloriestreak.domain.history.HistoryMetric
import com.sam.caloriestreak.domain.history.HistoryPoint
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
    LaunchedEffect(points, metric) {
        selectedIndex = points.lastIndex.coerceAtLeast(0)
    }

    val selected = points.getOrNull(selectedIndex)
    val lineColor = MaterialTheme.colorScheme.primary
    val pointColor = MaterialTheme.colorScheme.secondary
    val freezeColor = MaterialTheme.colorScheme.tertiary
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val referenceColor = MaterialTheme.colorScheme.outline
    val density = LocalDensity.current

    Column(modifier) {
        if (selected != null) {
            val valueText = when (metric) {
                HistoryMetric.SCORE -> "${selected.score.toInt()}%"
                HistoryMetric.CALORIES -> "${selected.calories.toInt()} kcal"
            }
            Text(
                text = "${LocalDate.ofEpochDay(selected.epochDay)} · $valueText" +
                    if (selected.freezeProtected) " · Freeze protected" else "",
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
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
                        .pointerInput(points, metric, canvasWidth) {
                            detectTapGestures { tap ->
                                if (points.isEmpty()) return@detectTapGestures
                                val usableWidth = size.width - leftPaddingPx - rightPaddingPx
                                val step = if (points.size <= 1) 1f else usableWidth / (points.size - 1)
                                val nearest = points.indices.minByOrNull { index ->
                                    abs(tap.x - (leftPaddingPx + index * step))
                                }
                                if (nearest != null) selectedIndex = nearest
                            }
                        }
                ) {
                    val top = 12.dp.toPx()
                    val bottom = size.height - 20.dp.toPx()
                    val usableHeight = bottom - top
                    val usableWidth = size.width - leftPaddingPx - rightPaddingPx
                    val maxValue = when (metric) {
                        HistoryMetric.SCORE -> 100.0
                        HistoryMetric.CALORIES -> max(
                            targetCalories * 1.2,
                            points.maxOfOrNull { it.calories }?.times(1.1) ?: targetCalories
                        )
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
                        drawLine(
                            color = referenceColor,
                            start = Offset(leftPaddingPx, y),
                            end = Offset(size.width - rightPaddingPx, y),
                            strokeWidth = 1.dp.toPx()
                        )
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
                        val radius = if (index == selectedIndex) 6.dp.toPx() else 4.dp.toPx()
                        drawCircle(
                            color = if (point.freezeProtected) freezeColor else pointColor,
                            radius = radius,
                            center = Offset(x, y)
                        )
                    }
                }
                Row(
                    modifier = Modifier.width(canvasWidth).padding(horizontal = 12.dp),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                ) {
                    Text(points.firstOrNull()?.let { LocalDate.ofEpochDay(it.epochDay).toString() }.orEmpty())
                    Text(points.lastOrNull()?.let { LocalDate.ofEpochDay(it.epochDay).toString() }.orEmpty())
                }
            }
        }
    }
}