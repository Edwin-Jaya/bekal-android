package com.edwin.bekal.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Core Electric Violet System Colors
val PrimaryLight = Color(0xFF000000)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFF1B1B1F)
val OnPrimaryContainerLight = Color(0xFF848387)

val SecondaryLight = Color(0xFF8127CF)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFF9C48EA)
val OnSecondaryContainerLight = Color(0xFFFFFBF0)

val TertiaryLight = Color(0xFF000000)
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFF201A17)
val OnTertiaryContainerLight = Color(0xFF8B827D)

val ErrorLight = Color(0xFFBA1A1A)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFFDAD6)
val OnErrorContainerLight = Color(0xFF93000A)

val BackgroundLight = Color(0xFFFCF8F8)
val OnBackgroundLight = Color(0xFF1C1B1C)
val SurfaceLight = Color(0xFFFCF8F8)
val OnSurfaceLight = Color(0xFF1C1B1C)
val SurfaceVariantLight = Color(0xFFE5E2E1)
val OnSurfaceVariantLight = Color(0xFF46464B)
val OutlineLight = Color(0xFF76777B)
val OutlineVariantLight = Color(0xFFC7C6CB)

val SurfaceDimLight = Color(0xFFDDD9D9)
val SurfaceBrightLight = Color(0xFFFCF8F8)
val SurfaceContainerLowestLight = Color(0xFFFFFFFF)
val SurfaceContainerLowLight = Color(0xFFF7F3F2)
val SurfaceContainerLight = Color(0xFFF1EDED)
val SurfaceContainerHighLight = Color(0xFFEBE7E7)
val SurfaceContainerHighestLight = Color(0xFFE5E2E1)

val PrimaryFixedLight = Color(0xFFE3E2E6)
val PrimaryFixedDimLight = Color(0xFFC7C6CA)
val OnPrimaryFixedLight = Color(0xFF1B1B1F)
val OnPrimaryFixedVariantLight = Color(0xFF46464A)

val SecondaryFixedLight = Color(0xFFF0DBFF)
val SecondaryFixedDimLight = Color(0xFFDDB7FF)
val OnSecondaryFixedLight = Color(0xFF2C0051)
val OnSecondaryFixedVariantLight = Color(0xFF6900B3)

val TertiaryFixedLight = Color(0xFFECE0DB)
val TertiaryFixedDimLight = Color(0xFFCFC4BF)
val OnTertiaryFixedLight = Color(0xFF201A17)
val OnTertiaryFixedVariantLight = Color(0xFF4C4541)

// Extended Brand & Micro-Brutalist Visual Colors
val CanvasBackground = Color(0xFFF3F0F7)
val SurfaceCard = Color(0xFFFFFFFF)
val AccentSoft = Color(0xFFF3E8FF)
val ElectricViolet = Color(0xFFA855F7)
val DeepCharcoal = Color(0xFF121316)
val TextMuted = Color(0xFF9CA3AF)

@Immutable
data class ExtendedColors(
    val canvasBackground: Color = CanvasBackground,
    val surfaceCard: Color = SurfaceCard,
    val accentSoft: Color = AccentSoft,
    val electricViolet: Color = ElectricViolet,
    val deepCharcoal: Color = DeepCharcoal,
    val textMuted: Color = TextMuted
)

val LocalExtendedColors = staticCompositionLocalOf { ExtendedColors() }