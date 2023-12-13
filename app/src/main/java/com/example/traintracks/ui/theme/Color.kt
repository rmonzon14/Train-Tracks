package com.example.traintracks.ui.theme

import androidx.compose.ui.graphics.Color
sealed class ThemeColors(
    val background: Color,
    val surface: Color,
    val primary: Color,
    val text: Color
) {
    object Night : ThemeColors(
        background = Color(0xFF000000),
        surface = Color(0xFF000000),
        primary = Color(0xFF484848),
        text = Color(0xffffffff)
    )
    object Day : ThemeColors(
        background = Color(0xFF000000),
        surface = Color(0xFF000000),
        primary = Color(0xFF484848),
        text = Color(0xffffffff)
    )
}