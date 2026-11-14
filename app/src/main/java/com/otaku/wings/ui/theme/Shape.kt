package com.otaku.wings.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** 8dp small / 12dp medium / 20dp large / full pill. Mapped into MaterialTheme.shapes. */
val WingsShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

object WingsShapeTokens {
    val Cover = RoundedCornerShape(8.dp)
    val Card = RoundedCornerShape(12.dp)
    val Field = RoundedCornerShape(12.dp)
    val Button = RoundedCornerShape(12.dp)
    val Sheet = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    val Pill = RoundedCornerShape(percent = 50)
}
