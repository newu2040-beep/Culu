package com.example.graphics

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.sin

@Composable
fun WaterGlassVisualizer(
    progressFraction: Float, // 0.0f to 1.0f+
    modifier: Modifier = Modifier,
    isDark: Boolean = true,
    accentGlow: Boolean = false
) {
    // Smooth animated water level
    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction.coerceIn(0f, 1.2f),
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 120f),
        label = "waterLevel"
    )

    // Continuous liquid oscillation wave
    val infiniteTransition = rememberInfiniteTransition(label = "waveOscillation")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    val secondaryPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "secondaryPhase"
    )

    val glassHighlightAlpha by animateFloatAsState(
        targetValue = if (accentGlow) 0.85f else 0.45f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 200f),
        label = "highlightGlow"
    )

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val radius = minOf(width, height) / 2f
            val center = Offset(width / 2f, height / 2f)

            // 1. Clip path to circular glass vessel
            val vesselPath = Path().apply {
                addOval(androidx.compose.ui.geometry.Rect(center = center, radius = radius - 4.dp.toPx()))
            }

            // Draw Vessel Glass Background
            val vesselBgColor = if (isDark) Color(0xFF0C1929).copy(alpha = 0.50f) else Color(0xFFE2F0FD).copy(alpha = 0.45f)
            drawCircle(
                color = vesselBgColor,
                radius = radius - 4.dp.toPx(),
                center = center
            )

            // Inner glass depth ambient shadow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Transparent,
                        if (isDark) Color(0xFF020617).copy(alpha = 0.4f) else Color(0xFF0369A1).copy(alpha = 0.12f)
                    ),
                    center = center,
                    radius = radius
                ),
                radius = radius - 4.dp.toPx(),
                center = center
            )

            // 2. Liquid Layer (clipped to vessel)
            drawContext.canvas.save()
            drawContext.canvas.clipPath(vesselPath)

            val waterTopY = (height - 4.dp.toPx()) - ((height - 8.dp.toPx()) * animatedProgress.coerceIn(0f, 1f))
            val waveAmplitude = (height * 0.035f) * (1f - (animatedProgress - 0.5f).let { it * it * 4f }.coerceIn(0f, 0.6f))

            // Primary Back Wave (lighter cyan-deep azure)
            val backWavePath = Path().apply {
                moveTo(0f, height)
                lineTo(0f, waterTopY)
                val step = 10f
                var x = 0f
                while (x <= width) {
                    val y = waterTopY + waveAmplitude * sin((x / width * 2 * Math.PI + wavePhase).toFloat())
                    lineTo(x, y)
                    x += step
                }
                lineTo(width, height)
                close()
            }

            drawPath(
                path = backWavePath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF38BDF8).copy(alpha = if (isDark) 0.45f else 0.55f),
                        Color(0xFF0284C7).copy(alpha = if (isDark) 0.65f else 0.75f),
                        Color(0xFF0369A1).copy(alpha = 0.85f)
                    ),
                    startY = waterTopY - waveAmplitude,
                    endY = height
                )
            )

            // Secondary Front Wave (rich turquoise / ocean gradient)
            val frontWavePath = Path().apply {
                moveTo(0f, height)
                lineTo(0f, waterTopY)
                val step = 10f
                var x = 0f
                while (x <= width) {
                    val y = waterTopY + (waveAmplitude * 0.8f) * sin((x / width * 2.5 * Math.PI + secondaryPhase + Math.PI / 2).toFloat())
                    lineTo(x, y)
                    x += step
                }
                lineTo(width, height)
                close()
            }

            drawPath(
                path = frontWavePath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF06B6D4).copy(alpha = 0.70f),
                        Color(0xFF0284C7).copy(alpha = 0.85f),
                        Color(0xFF0369A1).copy(alpha = 0.95f)
                    ),
                    startY = waterTopY - waveAmplitude,
                    endY = height
                )
            )

            // Liquid Surface Glow Line (luminous water crest)
            drawPath(
                path = frontWavePath,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFE0F2FE).copy(alpha = 0.2f),
                        Color(0xFFFFFFFF).copy(alpha = 0.85f),
                        Color(0xFF38BDF8).copy(alpha = 0.95f),
                        Color(0xFFE0F2FE).copy(alpha = 0.3f)
                    )
                ),
                style = Stroke(width = 2.5.dp.toPx())
            )

            // Ambient Bubbles / Caustic light
            if (animatedProgress > 0.15f) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.45f),
                    radius = 3.dp.toPx(),
                    center = Offset(width * 0.35f, height * 0.75f - (wavePhase * 5f) % (height * 0.4f))
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.35f),
                    radius = 2.2.dp.toPx(),
                    center = Offset(width * 0.62f, height * 0.85f - (secondaryPhase * 6f) % (height * 0.5f))
                )
                drawCircle(
                    color = Color(0xFFBAE6FD).copy(alpha = 0.4f),
                    radius = 4.dp.toPx(),
                    center = Offset(width * 0.48f, height * 0.65f - (wavePhase * 7f) % (height * 0.45f))
                )
            }

            // Restore canvas clip
            drawContext.canvas.restore()

            // 3. Physical Glass Specular Reflections (on top of water)
            // Crescent Glint (upper left curve reflection)
            drawArc(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = glassHighlightAlpha),
                        Color.White.copy(alpha = glassHighlightAlpha * 0.3f),
                        Color.Transparent
                    ),
                    start = Offset(center.x - radius, center.y - radius),
                    end = Offset(center.x, center.y)
                ),
                startAngle = 190f,
                sweepAngle = 100f,
                useCenter = false,
                topLeft = Offset(center.x - radius + 5.dp.toPx(), center.y - radius + 5.dp.toPx()),
                size = Size((radius - 5.dp.toPx()) * 2, (radius - 5.dp.toPx()) * 2),
                style = Stroke(width = 3.5.dp.toPx())
            )

            // Subtle lower reflection arc
            drawArc(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.25f),
                        Color.Transparent
                    )
                ),
                startAngle = 30f,
                sweepAngle = 70f,
                useCenter = false,
                topLeft = Offset(center.x - radius + 6.dp.toPx(), center.y - radius + 6.dp.toPx()),
                size = Size((radius - 6.dp.toPx()) * 2, (radius - 6.dp.toPx()) * 2),
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Outer Crystal Glass Rim
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color.White.copy(alpha = if (isDark) 0.75f else 0.90f),
                        Color(0xFF38BDF8).copy(alpha = 0.45f),
                        Color(0xFF0284C7).copy(alpha = 0.20f),
                        Color.White.copy(alpha = 0.30f),
                        Color.White.copy(alpha = if (isDark) 0.75f else 0.90f)
                    ),
                    center = center
                ),
                radius = radius - 2.dp.toPx(),
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}
