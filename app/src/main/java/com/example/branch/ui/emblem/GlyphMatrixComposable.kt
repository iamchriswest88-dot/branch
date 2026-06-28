package com.example.branch.ui.emblem

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GlyphMatrixView(
    grid: IntArray,
    activeColor: Color,
    pulseColor: Color = activeColor,
    modifier: Modifier = Modifier,
    size: Dp = 220.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val cx = this.size.width / 2f
        val cy = this.size.height / 2f
        val radius = this.size.minDimension / 2f
        
        // Background Circle
        drawCircle(color = Color.Black, radius = radius, center = Offset(cx, cy))
        
        // 13x13 Grid (137 valid LEDs)
        val cols = 13
        val rows = 13
        val padding = this.size.minDimension * 0.1f // 10% padding
        val gridWidth = this.size.minDimension - padding * 2
        val cellSize = gridWidth / cols
        val spacing = cellSize * 0.25f
        val rectSize = cellSize - spacing
        
        val startX = cx - (gridWidth / 2f) + (spacing / 2f)
        val startY = cy - (gridWidth / 2f) + (spacing / 2f)
        
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val idx = r * cols + c
                if (idx >= grid.size) continue
                
                // Nothing Phone (4a) Pro has 137 LEDs in a circular mask out of 169
                val dx = c - 6
                val dy = r - 6
                if (dx * dx + dy * dy > 42.25f) continue
                
                val brightness = grid[idx]
                val x = startX + c * cellSize
                val y = startY + r * cellSize

                val color = when {
                    brightness == 0 -> Color(0xFF1F1F1F) // NothingLedDim
                    brightness == 4095 -> activeColor
                    brightness == 400 -> activeColor.copy(alpha = 0.5f) // HUB_DIM
                    brightness == 200 -> activeColor.copy(alpha = 0.15f) // GHOST
                    else -> {
                        // Pulsing pixel (200..1500)
                        val alpha = (brightness.toFloat() / 1500f).coerceIn(0.15f, 1f)
                        pulseColor.copy(alpha = alpha)
                    }
                }
                
                drawRoundRect(
                    color = color,
                    topLeft = Offset(x, y),
                    size = Size(rectSize, rectSize),
                    cornerRadius = CornerRadius(rectSize * 0.3f, rectSize * 0.3f)
                )
            }
        }
    }
}
