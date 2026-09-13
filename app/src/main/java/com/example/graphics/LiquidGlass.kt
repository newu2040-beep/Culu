package com.example.graphics

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object LiquidGlassDefaults {
    val PillShape = CircleShape
    val CardShape = RoundedCornerShape(26.dp)
    val SheetShape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    val DialogShape = RoundedCornerShape(30.dp)
    val SmallPillShape = RoundedCornerShape(16.dp)

    // Palette tokens for Light & Dark glass with high contrast for crystal-clear readability
    val DarkGlassBody = Color(0xFF0D1C2E).copy(alpha = 0.72f)
    val DarkGlassInnerHighlight = Color(0xFF38BDF8).copy(alpha = 0.12f)
    val DarkGlassBorderTop = Color(0xFFFFFFFF).copy(alpha = 0.35f)
    val DarkGlassBorderBottom = Color(0xFF38BDF8).copy(alpha = 0.20f)
    val DarkGlassSpecular = Color(0xFFFFFFFF).copy(alpha = 0.18f)
    val DarkAmbientShadow = Color(0xFF020617).copy(alpha = 0.45f)

    val LightGlassBody = Color(0xFFFFFFFF).copy(alpha = 0.82f)
    val LightGlassInnerHighlight = Color(0xFFE0F2FE).copy(alpha = 0.45f)
    val LightGlassBorderTop = Color(0xFFFFFFFF).copy(alpha = 0.90f)
    val LightGlassBorderBottom = Color(0xFF0284C7).copy(alpha = 0.20f)
    val LightGlassSpecular = Color(0xFFFFFFFF).copy(alpha = 0.35f)
    val LightAmbientShadow = Color(0xFF0F172A).copy(alpha = 0.08f)
}

/**
 * Centralized Liquid Glass modifier that applies layered optical styling:
 * 1. Ambient soft depth shadow
 * 2. Subtle spring physics on press
 * 3. Geometry clipping
 * 4. Translucent tinted body
 * 5. Directional ambient light gradient
 * 6. Top specular reflection glint
 * 7. Razor-sharp, crystal-clear child content (no blur/distortion)
 * 8. Luminous perimeter glass edge border
 */
fun Modifier.liquidGlass(
    shape: Shape = LiquidGlassDefaults.CardShape,
    isDark: Boolean = true,
    blurRadius: Dp = 24.dp,
    borderWidth: Dp = 1.dp,
    interactivePress: Boolean = false,
    onClick: (() -> Unit)? = null
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && interactivePress) 0.965f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "glassScale"
    )

    val highlightBoost by animateFloatAsState(
        targetValue = if (isPressed && interactivePress) 1.35f else 1f,
        label = "highlightBoost"
    )

    // Palette selection based on theme
    val bodyColor = if (isDark) LiquidGlassDefaults.DarkGlassBody else LiquidGlassDefaults.LightGlassBody
    val borderTop = (if (isDark) LiquidGlassDefaults.DarkGlassBorderTop else LiquidGlassDefaults.LightGlassBorderTop)
        .copy(alpha = ((if (isDark) 0.35f else 0.85f) * highlightBoost).coerceIn(0f, 1f))
    val borderBottom = if (isDark) LiquidGlassDefaults.DarkGlassBorderBottom else LiquidGlassDefaults.LightGlassBorderBottom
    val specular = (if (isDark) LiquidGlassDefaults.DarkGlassSpecular else LiquidGlassDefaults.LightGlassSpecular)
        .copy(alpha = ((if (isDark) 0.18f else 0.40f) * highlightBoost).coerceIn(0f, 1f))
    val ambientShadow = if (isDark) LiquidGlassDefaults.DarkAmbientShadow else LiquidGlassDefaults.LightAmbientShadow

    val borderBrush = Brush.linearGradient(
        colors = listOf(
            borderTop,
            borderTop.copy(alpha = borderTop.alpha * 0.5f),
            borderBottom,
            borderBottom.copy(alpha = borderBottom.alpha * 0.3f)
        ),
        start = Offset(0f, 0f),
        end = Offset(400f, 600f)
    )

    var modifier: Modifier = this

    // 1. Interactive scale & click
    if (onClick != null) {
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    }

    // 2. Ambient soft shadow
    modifier = modifier.shadow(
        elevation = if (isDark) 10.dp else 6.dp,
        shape = shape,
        ambientColor = ambientShadow,
        spotColor = ambientShadow
    )

    // 3. Clip to geometry
    modifier = modifier.clip(shape)

    // 4. Luminous Glass Border matching exact shape
    modifier = modifier.border(
        width = borderWidth,
        brush = borderBrush,
        shape = shape
    )

    // 5. Draw layered physical glass with razor-sharp content
    modifier.drawWithContent {
        val w = size.width
        val h = size.height

        // Layer A: Translucent Body
        drawRect(color = bodyColor)

        // Layer B: Internal lighting gradient (soft light falling from top-left)
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    if (isDark) Color(0xFF38BDF8).copy(alpha = 0.10f * highlightBoost) else Color(0xFFBAE6FD).copy(alpha = 0.28f * highlightBoost),
                    Color.Transparent,
                    if (isDark) Color(0xFF0F172A).copy(alpha = 0.20f) else Color(0xFFF1F5F9).copy(alpha = 0.10f)
                ),
                start = Offset(0f, 0f),
                end = Offset(w, h)
            )
        )

        // Layer C: Specular Light Glint (upper third reflection line)
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(
                    specular,
                    Color.Transparent
                ),
                center = Offset(w * 0.35f, 0f),
                radius = w * 0.65f
            ),
            topLeft = Offset(-w * 0.1f, -h * 0.15f),
            size = Size(w * 1.2f, h * 0.45f)
        )

        // Draw Child Content (Text, Icons, controls) - Completely sharp and crystal-clear
        drawContent()
    }
}

