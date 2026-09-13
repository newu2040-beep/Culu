package com.example.ui.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class WindowWidthClass {
    COMPACT,   // < 360dp (Small phones, narrow split-screens, flip cover displays)
    MEDIUM,    // 360dp .. 420dp (Standard Android phones, e.g. Pixel 7, Galaxy S23)
    EXPANDED   // > 420dp (Large phones, Pro/Ultra phablets, landscape, foldables)
}

enum class WindowHeightClass {
    COMPACT,   // < 700dp (Short screens, split-screen mode, landscape)
    MEDIUM,    // 700dp .. 860dp (Standard phone heights)
    EXPANDED   // > 860dp (Tall modern flagship phones, e.g. 20:9 or 21:9 displays)
}

data class ResponsiveConfig(
    val screenWidthDp: Dp,
    val screenHeightDp: Dp,
    val widthClass: WindowWidthClass,
    val heightClass: WindowHeightClass,
    val isCompactWidth: Boolean,
    val isCompactHeight: Boolean,
    val isSmallScreen: Boolean,
    val isLargeScreen: Boolean,
    val screenHorizontalPadding: Dp,
    val screenTopPadding: Dp,
    val screenBottomPadding: Dp,
    val itemSpacing: Dp,
    val cardInnerPadding: Dp,
    val cardCornerRadius: Dp,
    val visualizerHeroSize: Dp,
    val waterCompactVisualizerSize: Dp,
    val pillPaddingHorizontal: Dp,
    val pillPaddingVertical: Dp,
    val chipPaddingHorizontal: Dp,
    val chipPaddingVertical: Dp,
    val maxContentWidth: Dp = 560.dp,
    val maxNavWidth: Dp = 480.dp,
    val maxDialogWidth: Dp = 420.dp,
    val maxSheetWidth: Dp = 540.dp
) {
    companion object {
        fun default(): ResponsiveConfig = ResponsiveConfig(
            screenWidthDp = 390.dp,
            screenHeightDp = 840.dp,
            widthClass = WindowWidthClass.MEDIUM,
            heightClass = WindowHeightClass.MEDIUM,
            isCompactWidth = false,
            isCompactHeight = false,
            isSmallScreen = false,
            isLargeScreen = false,
            screenHorizontalPadding = 16.dp,
            screenTopPadding = 16.dp,
            screenBottomPadding = 120.dp,
            itemSpacing = 14.dp,
            cardInnerPadding = 16.dp,
            cardCornerRadius = 24.dp,
            visualizerHeroSize = 190.dp,
            waterCompactVisualizerSize = 115.dp,
            pillPaddingHorizontal = 14.dp,
            pillPaddingVertical = 8.dp,
            chipPaddingHorizontal = 14.dp,
            chipPaddingVertical = 8.dp
        )
    }
}

val LocalResponsiveConfig = compositionLocalOf { ResponsiveConfig.default() }

@Composable
fun rememberResponsiveConfig(userCompactMode: Boolean = false): ResponsiveConfig {
    val configuration = LocalConfiguration.current
    val widthDp = configuration.screenWidthDp.dp
    val heightDp = configuration.screenHeightDp.dp

    return remember(widthDp, heightDp, userCompactMode) {
        val widthClass = when {
            widthDp < 360.dp -> WindowWidthClass.COMPACT
            widthDp <= 420.dp -> WindowWidthClass.MEDIUM
            else -> WindowWidthClass.EXPANDED
        }

        val heightClass = when {
            heightDp < 700.dp -> WindowHeightClass.COMPACT
            heightDp <= 860.dp -> WindowHeightClass.MEDIUM
            else -> WindowHeightClass.EXPANDED
        }

        val isCompactW = widthClass == WindowWidthClass.COMPACT || userCompactMode
        val isCompactH = heightClass == WindowHeightClass.COMPACT
        val isSmall = isCompactW || isCompactH
        val isLarge = widthClass == WindowWidthClass.EXPANDED && !userCompactMode

        val screenHorizPad = when {
            isCompactW -> 12.dp
            isLarge -> 20.dp
            else -> 16.dp
        }

        val screenTopPad = when {
            isSmall -> 10.dp
            isLarge -> 20.dp
            else -> 16.dp
        }

        val spacing = when {
            isCompactW -> 10.dp
            isLarge -> 16.dp
            else -> 13.dp
        }

        val cardPadding = when {
            isCompactW -> 12.dp
            isLarge -> 18.dp
            else -> 15.dp
        }

        val cornerRadius = when {
            isCompactW -> 18.dp
            isLarge -> 26.dp
            else -> 22.dp
        }

        val heroSize = when {
            isSmall -> 160.dp
            isLarge -> 220.dp
            else -> 190.dp
        }

        val compactWaterSize = when {
            isCompactW -> 96.dp
            isLarge -> 130.dp
            else -> 115.dp
        }

        val pillPadH = when {
            isCompactW -> 10.dp
            isLarge -> 16.dp
            else -> 13.dp
        }

        val pillPadV = when {
            isCompactW -> 6.dp
            isLarge -> 9.dp
            else -> 7.dp
        }

        val chipPadH = when {
            isCompactW -> 10.dp
            isLarge -> 15.dp
            else -> 12.dp
        }

        val chipPadV = when {
            isCompactW -> 6.dp
            isLarge -> 8.dp
            else -> 7.dp
        }

        ResponsiveConfig(
            screenWidthDp = widthDp,
            screenHeightDp = heightDp,
            widthClass = widthClass,
            heightClass = heightClass,
            isCompactWidth = isCompactW,
            isCompactHeight = isCompactH,
            isSmallScreen = isSmall,
            isLargeScreen = isLarge,
            screenHorizontalPadding = screenHorizPad,
            screenTopPadding = screenTopPad,
            screenBottomPadding = if (isSmall) 115.dp else 135.dp,
            itemSpacing = spacing,
            cardInnerPadding = cardPadding,
            cardCornerRadius = cornerRadius,
            visualizerHeroSize = heroSize,
            waterCompactVisualizerSize = compactWaterSize,
            pillPaddingHorizontal = pillPadH,
            pillPaddingVertical = pillPadV,
            chipPaddingHorizontal = chipPadH,
            chipPaddingVertical = chipPadV,
            maxContentWidth = 560.dp,
            maxNavWidth = 480.dp,
            maxDialogWidth = 420.dp,
            maxSheetWidth = 540.dp
        )
    }
}

/**
 * Ensures centered, balanced presentation on large/tall screens
 * while providing full natural fluid width on compact/medium screens.
 */
@Composable
fun AdaptiveContentContainer(
    modifier: Modifier = Modifier,
    maxWidth: Dp = 560.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = maxWidth),
            content = content
        )
    }
}
