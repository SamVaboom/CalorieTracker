package com.sam.caloriestreak.ui.history

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.sam.caloriestreak.domain.protein.ProteinFormatter
import com.sam.caloriestreak.domain.protein.ProteinHistoryPoint
import com.sam.caloriestreak.ui.theme.AppColors
import com.sam.caloriestreak.ui.theme.AppDimensions
import java.time.LocalDate
import kotlin.math.abs

@Composable
fun ProteinHistoryChart(points: List<ProteinHistoryPoint>, modifier: Modifier = Modifier) {
    var selectedIndex by remember(points) { mutableIntStateOf(points.lastIndex.coerceAtLeast(0)) }
    val selected = points.getOrNull(selectedIndex)
    val maxValue = (points.maxOfOrNull { it.knownGrams } ?: 1.0).coerceAtLeast(1.0)
    val grid = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)

    Column(modifier) {
        selected?.let { point ->
            Text(
                "${LocalDate.ofEpochDay(point.epochDay)} · ${ProteinFormatter.grams(point.knownGrams)} known protein" +
                    if (point.complete) " · Complete" else " · ${point.missingEntryCount} incomplete",
                color = if (point.complete) AppColors.Cyan else AppColors.Warning,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = AppDimensions.Space8)
            )
        }
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(240.dp)
                .semantics { contentDescription = "Protein history graph with ${points.size} recorded protein days" }
                .pointerInput(points) {
                    detectTapGestures { tap ->
                        selectedIndex = points.indices.minByOrNull { index ->
                            abs(tap.x - size.width * index / (points.size - 1).coerceAtLeast(1))
                        } ?: selectedIndex
                    }
                }
        ) {
            repeat(5) { row ->
                val y = size.height * row / 4f
                drawLine(grid, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
            }
            val path = Path()
            points.forEachIndexed { index, point ->
                val x = size.width * index / (points.size - 1).coerceAtLeast(1)
                val y = size.height - (point.knownGrams / maxValue * size.height).toFloat()
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            if (points.size > 1) drawPath(path, AppColors.Cyan, style = Stroke(3.dp.toPx()))
            points.forEachIndexed { index, point ->
                val x = size.width * index / (points.size - 1).coerceAtLeast(1)
                val y = size.height - (point.knownGrams / maxValue * size.height).toFloat()
                drawCircle(
                    color = if (point.complete) AppColors.Cyan else AppColors.Warning,
                    radius = if (index == selectedIndex) 8.dp.toPx() else 5.dp.toPx(),
                    center = Offset(x, y)
                )
            }
        }
        Text(
            "Y-axis: grams of known protein · amber points have incomplete protein data",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = AppDimensions.Space8)
        )
    }
}
