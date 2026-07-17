package com.sam.caloriestreak.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sam.caloriestreak.ui.theme.AppDimensions

@Composable
fun AppStatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    accent: Color = MaterialTheme.colorScheme.primary
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.3f))
    ) {
        Column(Modifier.fillMaxWidth().padding(AppDimensions.Space16)) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.headlineSmall, color = accent, modifier = Modifier.padding(top = AppDimensions.Space4))
            supportingText?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = AppDimensions.Space4))
            }
        }
    }
}
