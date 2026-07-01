package com.example.branch.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

import androidx.compose.ui.text.font.Font
import com.example.branch.R

val ntype82Family = FontFamily(Font(R.font.ntype82_headline))
val ndot57Family = FontFamily(Font(R.font.ndot57_regular))
val interLightFamily = FontFamily(Font(R.font.inter_light, weight = FontWeight.ExtraLight))

val displayFontFamily: FontFamily = ntype82Family
val bodyFontFamily: FontFamily    = interLightFamily
val labelFontFamily: FontFamily   = interLightFamily

val BranchTypography = Typography(
    displayLarge = TextStyle(
        fontFamily    = ndot57Family, // Ndot is perfect for the huge timer numbers!
        fontWeight    = FontWeight.Normal,
        fontSize      = 57.sp,
        lineHeight    = 64.sp,
        letterSpacing = (-0.25).sp,
    ),
    displayMedium = TextStyle(
        fontFamily    = displayFontFamily,
        fontWeight    = FontWeight.Normal,
        fontSize      = 45.sp,
        lineHeight    = 52.sp,
        letterSpacing = 0.sp,
    ),
    displaySmall = TextStyle(
        fontFamily    = displayFontFamily,
        fontWeight    = FontWeight.Normal,
        fontSize      = 36.sp,
        lineHeight    = 44.sp,
        letterSpacing = 0.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily    = displayFontFamily,
        fontWeight    = FontWeight.Normal,
        fontSize      = 32.sp,
        lineHeight    = 40.sp,
        letterSpacing = 0.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily    = displayFontFamily,
        fontWeight    = FontWeight.Normal,
        fontSize      = 28.sp,
        lineHeight    = 36.sp,
        letterSpacing = 0.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily    = displayFontFamily,
        fontWeight    = FontWeight.Normal,
        fontSize      = 24.sp,
        lineHeight    = 32.sp,
        letterSpacing = 0.sp,
    ),
    titleLarge = TextStyle(
        fontFamily    = bodyFontFamily,
        fontWeight    = FontWeight.ExtraLight,
        fontSize      = 26.sp,
        lineHeight    = 32.sp,
        letterSpacing = 2.sp,
    ),
    titleMedium = TextStyle(
        fontFamily    = bodyFontFamily,
        fontWeight    = FontWeight.ExtraLight,
        fontSize      = 20.sp,
        lineHeight    = 28.sp,
        letterSpacing = 1.5.sp,
    ),
    titleSmall = TextStyle(
        fontFamily    = bodyFontFamily,
        fontWeight    = FontWeight.ExtraLight,
        fontSize      = 18.sp,
        lineHeight    = 24.sp,
        letterSpacing = 1.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily    = bodyFontFamily,
        fontWeight    = FontWeight.ExtraLight,
        fontSize      = 20.sp,
        lineHeight    = 28.sp,
        letterSpacing = 0.5.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily    = bodyFontFamily,
        fontWeight    = FontWeight.ExtraLight,
        fontSize      = 18.sp,
        lineHeight    = 24.sp,
        letterSpacing = 0.25.sp,
    ),
    bodySmall = TextStyle(
        fontFamily    = bodyFontFamily,
        fontWeight    = FontWeight.ExtraLight,
        fontSize      = 16.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.4.sp,
    ),
    labelLarge = TextStyle(
        fontFamily    = labelFontFamily,
        fontWeight    = FontWeight.ExtraLight,
        fontSize      = 18.sp,
        lineHeight    = 24.sp,
        letterSpacing = 1.5.sp,
    ),
    labelMedium = TextStyle(
        fontFamily    = labelFontFamily,
        fontWeight    = FontWeight.ExtraLight,
        fontSize      = 16.sp,
        lineHeight    = 20.sp,
        letterSpacing = 1.sp,
    ),
    labelSmall = TextStyle(
        fontFamily    = labelFontFamily,
        fontWeight    = FontWeight.ExtraLight,
        fontSize      = 15.sp,
        lineHeight    = 20.sp,
        letterSpacing = 1.sp,
    ),
)
