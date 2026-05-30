package com.billionaire.empire.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val BgDeep    = Color(0xFF06080F); val BgCard    = Color(0xFF0D1421)
val BgSurface = Color(0xFF111827); val BgSurface2= Color(0xFF1A2235)
val Border    = Color(0xFF1E2D45); val Border2   = Color(0xFF253347)
val Gold      = Color(0xFFC9A84C); val GoldLight = Color(0xFFE8C870)
val GoldDim   = Color(0xFF8B6B2A); val GoldGlow  = Color(0x40C9A84C)
val TextPrimary=Color(0xFFF0F4FF); val TextSec   = Color(0xFFB0BEC5)
val TextTert  = Color(0xFF607D8B); val Green     = Color(0xFF0DB87A)
val Red       = Color(0xFFE04444); val Blue      = Color(0xFF3D7EFF)

private val dark = darkColorScheme(
    primary=Gold, onPrimary=Color(0xFF0A0700), secondary=GoldDim,
    background=BgDeep, surface=BgCard, surfaceVariant=BgSurface,
    onBackground=TextPrimary, onSurface=TextPrimary, onSurfaceVariant=TextSec,
    outline=Border, error=Red
)

@Composable
fun BillionaireTheme(content: @Composable () -> Unit) =
    MaterialTheme(colorScheme = dark, content = content)
