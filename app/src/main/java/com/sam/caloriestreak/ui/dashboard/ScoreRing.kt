package com.sam.caloriestreak.ui.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sam.caloriestreak.domain.calculation.ScoreDisplay
import com.sam.caloriestreak.ui.theme.AppColors
import com.sam.caloriestreak.ui.theme.AppMotion

@Composable
fun ScoreRing(
    score: Double,
    calories: Double,
    target: Double,
    calorieStatus: String,
    modifier: Modifier = Modifier
) {
    val targetProgress = (score.coerceIn(0.0, 100.0) / 100.0).toFloat()
    val progress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(AppMotion.Standard),
        label = "score-ring"
    )
    val background = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val gradient = remember(primary, secondary) {
        Brush.sweepGradient(listOf(AppColors.Coral, AppColors.Warning, primary, secondary, AppColors.Success))
    }
    val displayedScore = ScoreDisplay.percent(score)

    Box(
        modifier = modifier
            .size(270.dp)
            .semantics {
                contentDescription = "Today's score: $displayedScore percent. ${calories.toInt()} calories of ${target.toInt()}. $calorieStatus"
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 20.dp.toPx()
            drawArc(background, -90f, 360f, false, style = Stroke(width = stroke, cap = StrokeCap.Round))
            if (progress > 0f) {
                drawArc(gradient, -90f, 360f * progress, false, style = Stroke(width = stroke, cap = StrokeCap.Round))
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$displayedScore%",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${calories.toInt()} kcal",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "of ${target.toInt()} kcal",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = calorieStatus.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = scoreAccent(score),
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun scoreAccent(score: Double) = when {
    score < 40.0 -> AppColors.Error
    score < 80.0 -> AppColors.Warning
    score < 85.0 -> AppColors.Cyan
    score < 95.0 -> AppColors.Violet
    else -> AppColors.Success
}
