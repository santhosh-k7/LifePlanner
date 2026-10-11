package com.example.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Apple-inspired Liquid Glass Design System
 * Features translucent refractive layers, specular border highlights,
 * fluid rounded squircles, and luminous ambient chromatic orbs.
 */

// Liquid Glass Palette Accents
val LiquidGlassBlue = Color(0xFF38BDF8)
val LiquidGlassIndigo = Color(0xFF6366F1)
val LiquidGlassViolet = Color(0xFFA855F7)
val LiquidGlassRose = Color(0xFFF43F5E)
val LiquidGlassEmerald = Color(0xFF10B981)
val LiquidGlassAmber = Color(0xFFF59E0B)

/**
 * Ambient background modifier with subtle fluid refracted light orbs
 */
fun Modifier.liquidGlassBackground(isDark: Boolean): Modifier = this.then(
    Modifier.drawBehind {
        val width = size.width
        val height = size.height

        if (isDark) {
            // Pure AMOLED deep black base
            drawRect(color = Color(0xFF08090D))

            // Luminous ambient orbs beneath the refractive glass
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x334F46E5), Color(0x11312E81), Color.Transparent),
                    center = Offset(width * 0.85f, height * 0.12f),
                    radius = width * 0.75f
                ),
                radius = width * 0.75f,
                center = Offset(width * 0.85f, height * 0.12f)
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x2806B6D4), Color(0x0C0E7490), Color.Transparent),
                    center = Offset(width * 0.15f, height * 0.55f),
                    radius = width * 0.65f
                ),
                radius = width * 0.65f,
                center = Offset(width * 0.15f, height * 0.55f)
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x228B5CF6), Color(0x0A581C87), Color.Transparent),
                    center = Offset(width * 0.8f, height * 0.88f),
                    radius = width * 0.7f
                ),
                radius = width * 0.7f,
                center = Offset(width * 0.8f, height * 0.88f)
            )
        } else {
            // Pure clean light background with delicate liquid pastel orbs
            drawRect(color = Color(0xFFF8FAFC))

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x35E0E7FF), Color(0x18C7D2FE), Color.Transparent),
                    center = Offset(width * 0.85f, height * 0.1f),
                    radius = width * 0.8f
                ),
                radius = width * 0.8f,
                center = Offset(width * 0.85f, height * 0.1f)
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x30CFFAFE), Color(0x14A5F3FC), Color.Transparent),
                    center = Offset(width * 0.1f, height * 0.45f),
                    radius = width * 0.7f
                ),
                radius = width * 0.7f,
                center = Offset(width * 0.1f, height * 0.45f)
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x25FCE7F3), Color(0x10FBCFE8), Color.Transparent),
                    center = Offset(width * 0.75f, height * 0.85f),
                    radius = width * 0.65f
                ),
                radius = width * 0.65f,
                center = Offset(width * 0.75f, height * 0.85f)
            )
        }
    }
)

/**
 * Liquid Glass Card with specular highlight border and translucent frosted surface
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    accentColor: Color? = null,
    isDark: Boolean = MaterialTheme.colorScheme.background.red < 0.2f,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val surfaceColor = if (isDark) {
        if (accentColor != null) {
            accentColor.copy(alpha = 0.12f)
        } else {
            Color(0xCC111420)
        }
    } else {
        if (accentColor != null) {
            accentColor.copy(alpha = 0.08f)
        } else {
            Color(0xDDFFFFFF)
        }
    }

    val borderBrush = if (isDark) {
        if (accentColor != null) {
            Brush.linearGradient(
                colors = listOf(
                    accentColor.copy(alpha = 0.55f),
                    Color.White.copy(alpha = 0.2f),
                    accentColor.copy(alpha = 0.15f)
                ),
                start = Offset.Zero,
                end = Offset.Infinite
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.28f),
                    Color.White.copy(alpha = 0.08f),
                    Color(0x226366F1)
                ),
                start = Offset.Zero,
                end = Offset.Infinite
            )
        }
    } else {
        if (accentColor != null) {
            Brush.linearGradient(
                colors = listOf(
                    accentColor.copy(alpha = 0.6f),
                    Color.White.copy(alpha = 0.85f),
                    accentColor.copy(alpha = 0.2f)
                ),
                start = Offset.Zero,
                end = Offset.Infinite
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.95f),
                    Color.White.copy(alpha = 0.45f),
                    Color(0x254F46E5)
                ),
                start = Offset.Zero,
                end = Offset.Infinite
            )
        }
    }

    val shadowElevation = if (isDark) 4.dp else 6.dp

    Box(
        modifier = modifier
            .shadow(
                elevation = shadowElevation,
                shape = shape,
                ambientColor = if (isDark) Color(0x33000000) else Color(0x18475569),
                spotColor = if (isDark) Color(0x444F46E5) else Color(0x203B82F6)
            )
            .clip(shape)
            .background(surfaceColor)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(),
                        onClick = onClick
                    )
                } else Modifier
            )
            .drawBehind {
                // Outer refractive border
                val strokeWidth = 1.dp.toPx()
                drawRoundRect(
                    brush = borderBrush,
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(22.dp.toPx(), 22.dp.toPx()),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
                )

                // Specular top highlight shimmer line
                val shimmerHeight = 1.2f.dp.toPx()
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            if (isDark) Color.White.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.8f),
                            Color.Transparent
                        )
                    ),
                    topLeft = Offset(16.dp.toPx(), strokeWidth),
                    size = androidx.compose.ui.geometry.Size(size.width - 32.dp.toPx(), shimmerHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(shimmerHeight, shimmerHeight)
                )
            }
    ) {
        content()
    }
}

/**
 * Refractive Liquid Glass Pill Badge
 */
@Composable
fun LiquidGlassPill(
    text: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    hasGlowDot: Boolean = true,
    fontSize: androidx.compose.ui.unit.TextUnit = 11.sp
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.2f

    val bg = if (isDark) {
        accentColor.copy(alpha = 0.18f)
    } else {
        accentColor.copy(alpha = 0.12f)
    }

    val border = if (isDark) {
        accentColor.copy(alpha = 0.45f)
    } else {
        accentColor.copy(alpha = 0.35f)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = bg,
        border = BorderStroke(1.dp, border)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            if (hasGlowDot) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
            }
            Text(
                text = text,
                color = accentColor,
                fontSize = fontSize,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.2.sp
            )
        }
    }
}
