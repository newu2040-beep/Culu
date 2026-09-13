package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.graphics.LiquidGlassDefaults
import com.example.graphics.liquidGlass
import com.example.ui.utils.LocalResponsiveConfig

@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = LiquidGlassDefaults.CardShape,
    isDark: Boolean = isSystemInDarkTheme(),
    blurRadius: Dp = 20.dp,
    borderWidth: Dp = 1.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.liquidGlass(
            shape = shape,
            isDark = isDark,
            blurRadius = blurRadius,
            borderWidth = borderWidth
        ),
        content = content
    )
}

@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = LiquidGlassDefaults.CardShape,
    isDark: Boolean = isSystemInDarkTheme(),
    contentPadding: PaddingValues? = null,
    onClick: (() -> Unit)? = null,
    testTag: String = "liquid_glass_card",
    content: @Composable ColumnScope.() -> Unit
) {
    val responsive = LocalResponsiveConfig.current
    val padding = contentPadding ?: PaddingValues(responsive.cardInnerPadding)

    Box(
        modifier = modifier
            .testTag(testTag)
            .liquidGlass(
                shape = shape,
                isDark = isDark,
                interactivePress = onClick != null,
                onClick = onClick
            )
            .padding(padding)
    ) {
        Column(content = content)
    }
}

@Composable
fun LiquidGlassPill(
    modifier: Modifier = Modifier,
    isDark: Boolean = isSystemInDarkTheme(),
    contentPadding: PaddingValues? = null,
    onClick: (() -> Unit)? = null,
    testTag: String = "liquid_glass_pill",
    content: @Composable RowScope.() -> Unit
) {
    val responsive = LocalResponsiveConfig.current
    val padding = contentPadding ?: PaddingValues(
        horizontal = responsive.pillPaddingHorizontal,
        vertical = responsive.pillPaddingVertical
    )

    Box(
        modifier = modifier
            .testTag(testTag)
            .liquidGlass(
                shape = LiquidGlassDefaults.PillShape,
                isDark = isDark,
                blurRadius = 16.dp,
                interactivePress = onClick != null,
                onClick = onClick
            )
            .defaultMinSize(minHeight = 44.dp, minWidth = 44.dp)
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            content = content
        )
    }
}

@Composable
fun LiquidGlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = isSystemInDarkTheme(),
    enabled: Boolean = true,
    shape: Shape = CircleShape,
    isPrimary: Boolean = false,
    testTag: String = "liquid_glass_button",
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 450f),
        label = "btnScale"
    )

    val primaryBrush = if (isPrimary) {
        Brush.horizontalGradient(
            colors = listOf(
                Color(0xFF0EA5E9),
                Color(0xFF0284C7),
                Color(0xFF0369A1)
            )
        )
    } else null

    Box(
        modifier = modifier
            .testTag(testTag)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = if (enabled) 1f else 0.5f
            }
            .then(
                if (isPrimary) {
                    Modifier
                        .background(primaryBrush ?: Brush.linearGradient(listOf(Color.Transparent, Color.Transparent)), shape)
                        .clip(shape)
                } else {
                    Modifier
                }
            )
            .liquidGlass(
                shape = shape,
                isDark = isDark,
                interactivePress = false, // handled via graphicsLayer scale
                onClick = if (enabled) onClick else null
            )
            .defaultMinSize(minHeight = 48.dp, minWidth = 48.dp)
            .padding(horizontal = 20.dp, vertical = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(
            LocalContentColor provides (if (isPrimary) Color.White else (if (isDark) Color.White else Color(0xFF0F172A)))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                content = content
            )
        }
    }
}

@Composable
fun LiquidGlassChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isDark: Boolean = isSystemInDarkTheme(),
    contentPadding: PaddingValues? = null,
    icon: ImageVector? = null,
    testTag: String = "liquid_glass_chip"
) {
    val responsive = LocalResponsiveConfig.current
    val isCompact = responsive.isCompactWidth
    val activeBorderColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
    val activeBgColor = if (isDark) Color(0xFF0284C7).copy(alpha = 0.28f) else Color(0xFFBAE6FD).copy(alpha = 0.50f)
    val padding = contentPadding ?: PaddingValues(
        horizontal = responsive.chipPaddingHorizontal,
        vertical = responsive.chipPaddingVertical
    )

    Box(
        modifier = modifier
            .testTag(testTag)
            .then(
                if (selected) {
                    Modifier.background(activeBgColor, CircleShape)
                } else Modifier
            )
            .liquidGlass(
                shape = CircleShape,
                isDark = isDark,
                borderWidth = if (selected) 1.5.dp else 0.8.dp,
                interactivePress = true,
                onClick = onClick
            )
            .defaultMinSize(minHeight = if (isCompact) 36.dp else 40.dp)
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (isCompact) 4.dp else 6.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(if (isCompact) 14.dp else 16.dp),
                    tint = if (selected) activeBorderColor else (if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF475569))
                )
            }
            Text(
                text = label,
                fontSize = if (isCompact) 12.sp else 13.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (selected) {
                    if (isDark) Color.White else Color(0xFF0369A1)
                } else {
                    if (isDark) Color.White.copy(alpha = 0.75f) else Color(0xFF475569)
                }
            )
        }
    }
}

@Composable
fun LiquidGlassProgress(
    progress: Float, // 0.0 to 1.0
    modifier: Modifier = Modifier,
    isDark: Boolean = isSystemInDarkTheme(),
    height: Dp = 10.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 160f),
        label = "progress"
    )

    Box(
        modifier = modifier
            .height(height)
            .liquidGlass(
                shape = CircleShape,
                isDark = isDark,
                blurRadius = 8.dp,
                borderWidth = 0.8.dp
            )
            .padding(2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = animatedProgress
                        transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0.5f)
                    }
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF38BDF8),
                                Color(0xFF0284C7),
                                Color(0xFF06B6D4)
                            )
                        )
                    )
            )
        }
    }
}

data class NavTabItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@Composable
fun LiquidGlassNavigationBar(
    items: List<NavTabItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = isSystemInDarkTheme()
) {
    val responsive = LocalResponsiveConfig.current
    val isCompact = responsive.isCompactWidth

    Box(
        modifier = modifier
            .navigationBarsPadding()
            .padding(
                horizontal = if (isCompact) 10.dp else 20.dp,
                vertical = if (isCompact) 8.dp else 12.dp
            )
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = responsive.maxNavWidth)
                .liquidGlass(
                    shape = CircleShape,
                    isDark = isDark,
                    blurRadius = 24.dp,
                    borderWidth = 1.2.dp
                )
                .padding(
                    horizontal = if (isCompact) 6.dp else 10.dp,
                    vertical = if (isCompact) 4.dp else 6.dp
                )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                items.forEachIndexed { index, item ->
                    val isSelected = index == selectedIndex
                    val iconScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.15f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "tabIconScale"
                    )
                    val iconTint by animateColorAsState(
                        targetValue = if (isSelected) {
                            if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
                        } else {
                            if (isDark) Color.White.copy(alpha = 0.55f) else Color(0xFF64748B)
                        },
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "tabIconTint"
                    )

                    Box(
                        modifier = Modifier
                            .testTag(item.testTag)
                            .then(
                                if (isSelected) {
                                    Modifier.liquidGlass(
                                        shape = CircleShape,
                                        isDark = isDark,
                                        borderWidth = 1.2.dp,
                                        interactivePress = true,
                                        onClick = { onItemSelected(index) }
                                    )
                                } else {
                                    Modifier
                                        .clip(CircleShape)
                                        .clickable { onItemSelected(index) }
                                }
                            )
                            .defaultMinSize(
                                minHeight = if (isCompact) 44.dp else 48.dp,
                                minWidth = if (isCompact) 44.dp else 54.dp
                            )
                            .padding(
                                horizontal = if (isCompact) (if (isSelected) 10.dp else 8.dp) else (if (isSelected) 14.dp else 12.dp),
                                vertical = if (isCompact) 6.dp else 8.dp
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(if (isCompact) 4.dp else 6.dp)
                        ) {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title,
                                modifier = Modifier
                                    .size(if (isCompact) 20.dp else 22.dp)
                                    .graphicsLayer {
                                        scaleX = iconScale
                                        scaleY = iconScale
                                    },
                                tint = iconTint
                            )

                            AnimatedVisibility(
                                visible = isSelected,
                                enter = fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) + expandHorizontally(spring(stiffness = Spring.StiffnessMediumLow)),
                                exit = fadeOut(spring(stiffness = Spring.StiffnessMediumLow)) + shrinkHorizontally(spring(stiffness = Spring.StiffnessMediumLow))
                            ) {
                                Text(
                                    text = item.title,
                                    fontSize = if (isCompact) 11.sp else 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isDark) Color.White else Color(0xFF0F172A)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiquidGlassDialog(
    onDismissRequest: () -> Unit,
    isDark: Boolean = isSystemInDarkTheme(),
    content: @Composable ColumnScope.() -> Unit
) {
    val responsive = LocalResponsiveConfig.current
    val isSmall = responsive.isSmallScreen

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .widthIn(min = 280.dp, max = responsive.maxDialogWidth)
                .fillMaxWidth(if (isSmall) 0.94f else 0.88f)
                .liquidGlass(
                    shape = LiquidGlassDefaults.DialogShape,
                    isDark = isDark,
                    blurRadius = 32.dp,
                    borderWidth = 1.4.dp
                )
                .padding(if (isSmall) 16.dp else 22.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                content = content
            )
        }
    }
}
