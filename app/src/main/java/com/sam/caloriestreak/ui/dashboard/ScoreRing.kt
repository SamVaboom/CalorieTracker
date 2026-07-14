package com.sam.caloriestreak.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sam.caloriestreak.domain.calculation.ScoreDisplay

@Composable
fun ScoreRing(
    score: Double,
    calorieStatus: String,
    modifier: Modifier = Modifier
) {
    val progress = (score.coerceIn(0.0, 100.0) / 100.0).toFloat()
    val background = MaterialTheme.colorScheme.surfaceVariant
    val gradient = Brush.sweepGradient(
        listOf(
            MaterialTheme.colorScheme.error,
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.error
        )
    )

    Box(modifier.size(286.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 22.dp.toPx()
            drawArc(background, -90f, 360f, false, style = Stroke(width = stroke, cap = StrokeCap.Round))
            if (progress > 0f) {
                drawArc(gradient, -90f, 360f * progress, false, style = Stroke(width = stroke, cap = StrokeCap.Round))
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${ScoreDisplay.percent(score)}%",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = calorieStatus,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
