package com.example.branch.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val BranchColorScheme = darkColorScheme(
    primary              = GymPurple,
    onPrimary            = NothingText,
    primaryContainer     = NothingSurface2,
    onPrimaryContainer   = NothingText,
    secondary            = FlowBlue,
    onSecondary          = NothingBg,
    secondaryContainer   = NothingSurface,
    onSecondaryContainer = FlowBlue,
    tertiary             = PhaseWork,
    background           = NothingBg,
    onBackground         = NothingText,
    surface              = NothingSurface,
    onSurface            = NothingText,
    surfaceVariant       = NothingSurface2,
    onSurfaceVariant     = NothingMuted,
    outline              = NothingLine,
    outlineVariant       = NothingLine2,
    error                = PhaseRest,
    onError              = NothingText,
)

@Composable
fun BranchTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BranchColorScheme,
        typography  = BranchTypography,
        content     = content
    )
}

fun Modifier.dotMatrixBackground(): Modifier = this.then(
    drawBehind {
        val dotColor = Color(0xFF181818)
        val spacing = 15.dp.toPx()
        val dotRadius = 0.5.dp.toPx()
        var y = 0f
        while (y < size.height) {
            var x = 0f
            while (x < size.width) {
                drawCircle(color = dotColor, radius = dotRadius, center = Offset(x, y))
                x += spacing
            }
            y += spacing
        }
    }
)
