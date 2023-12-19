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
        surface = Color(0xFFCCCCCC),
        primary = Color(0xFF628558),
        text = Color(0xffffffff)
    )
    object Day : ThemeColors(
        background = Color(0xffffffff),
        surface = Color(0xffffffff),
        primary = Color(0xFF41593A),
        text = Color(0xFF000000)
    )
}
//Test