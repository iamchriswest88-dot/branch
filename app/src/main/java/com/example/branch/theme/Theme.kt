package com.example.branch.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

val BranchColorScheme = darkColorScheme(
    primary              = NothingRed,
    onPrimary            = NothingText,
    primaryContainer     = NothingSurface2,
    onPrimaryContainer   = NothingText,
    secondary            = NothingLeaf,
    onSecondary          = NothingBg,
    secondaryContainer   = NothingLeafDim,
    onSecondaryContainer = NothingLeaf,
    tertiary             = NothingEmblA,
    background           = NothingBg,
    onBackground         = NothingText,
    surface              = NothingSurface,
    onSurface            = NothingText,
    surfaceVariant       = NothingSurface2,
    onSurfaceVariant     = NothingMuted,
    outline              = NothingLine,
    outlineVariant       = NothingLine2,
    error                = NothingRed,
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
