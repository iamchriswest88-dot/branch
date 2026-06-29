package com.example.branch.ui.emblem

import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.branch.glyph.EmblemRenderer
import com.example.branch.theme.GymPurple
import com.example.branch.theme.FlowBlue

enum class EmblemStyle { GYM, FLOW }

@Composable
fun EmblemView(
    filledSections: Int,
    style: EmblemStyle,
    isPlannedToday: Boolean = true,
    modifier: Modifier = Modifier,
    size: Dp = 220.dp
) {
    val infinite = rememberInfiniteTransition(label = "emblem_pulse")
    val pulseAlpha by infinite.animateFloat(
        initialValue = 0.25f,
        targetValue  = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val styleStr = if (style == EmblemStyle.GYM) "gym" else "flow"
    val grid = EmblemRenderer.render(styleStr, filledSections, pulsePhase = pulseAlpha, isPlannedToday = isPlannedToday)

    GlyphMatrixView(
        grid = grid,
        activeColor = if (styleStr == "gym") GymPurple else FlowBlue,
        pulseColor = if (styleStr == "gym") GymPurple else FlowBlue,
        modifier = modifier,
        size = size
    )
}
