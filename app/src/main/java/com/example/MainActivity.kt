package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import com.example.utils.TtsSpeaker
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.ui.components.LiquidGlassNavigationBar
import com.example.ui.components.NavTabItem
import com.example.ui.screens.AddReminderSheet
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.RemindersScreen
import com.example.ui.screens.SettingsDialog
import com.example.ui.screens.WaterScreen
import com.example.ui.theme.CuluTheme
import com.example.ui.viewmodel.CuluViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: CuluViewModel by viewModels {
        CuluViewModel.provideFactory(application as CuluApplication)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val preferences by viewModel.preferences.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val isDark = when (preferences.themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> systemDark
            }

            // Notification permission request for Android 13+
            val context = LocalContext.current
            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                viewModel.updateAllNotificationsEnabled(isGranted)
            }

            LaunchedEffect(Unit) {
                TtsSpeaker.initialize(context)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }

            CuluTheme(darkTheme = isDark) {
                CuluAppScaffold(
                    viewModel = viewModel,
                    isDark = isDark
                )
            }
        }
    }
}

@Composable
fun CuluAppScaffold(
    viewModel: CuluViewModel,
    isDark: Boolean
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val preferences by viewModel.preferences.collectAsState()
    val todayWaterTotal by viewModel.todayWaterTotal.collectAsState()
    val todayWaterEntries by viewModel.todayWaterEntries.collectAsState()
    val allReminders by viewModel.allReminders.collectAsState()
    val todayLogs by viewModel.todayLogs.collectAsState()
    val past7DaysStats by viewModel.past7DaysStats.collectAsState()
    val reminderStats by viewModel.reminderStats.collectAsState()
    val isSettingsOpen by viewModel.isSettingsOpen.collectAsState()
    val isAddReminderOpen by viewModel.isAddReminderOpen.collectAsState()
    val waterFlareActive by viewModel.waterFlareActive.collectAsState()

    val navItems = remember {
        listOf(
            NavTabItem("Home", Icons.Default.Home, Icons.Outlined.Home, "nav_home"),
            NavTabItem("Water", Icons.Default.WaterDrop, Icons.Outlined.WaterDrop, "nav_water"),
            NavTabItem("Routines", Icons.Default.Alarm, Icons.Outlined.Alarm, "nav_routines"),
            NavTabItem("History", Icons.Default.Assessment, Icons.Outlined.Assessment, "nav_history")
        )
    }

    // Ambient Chromatic Liquid Glass Backdrop
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = if (isDark) {
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF0E223D), // Deep sapphire ambient light center
                            Color(0xFF091424), // Midnight Navy
                            Color(0xFF030712)  // Obsidian Edge
                        ),
                        center = Offset(400f, 300f),
                        radius = 1200f
                    )
                } else {
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFE0F2FE), // Soft azure glow
                            Color(0xFFF0F9FF), // Pale sky
                            Color(0xFFF8FAFC)  // Clean pearl
                        ),
                        center = Offset(400f, 300f),
                        radius = 1200f
                    )
                }
            )
    ) {
        // Decorative ambient gradient bubbles under glass
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            if (isDark) Color(0xFF0369A1).copy(alpha = 0.15f) else Color(0xFF38BDF8).copy(alpha = 0.12f),
                            Color.Transparent,
                            if (isDark) Color(0xFF0284C7).copy(alpha = 0.10f) else Color(0xFFBAE6FD).copy(alpha = 0.10f)
                        )
                    )
                )
        )

        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                LiquidGlassNavigationBar(
                    items = navItems,
                    selectedIndex = currentTab,
                    onItemSelected = { viewModel.setTab(it) },
                    isDark = isDark
                )
            },
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding())
                    .statusBarsPadding()
            ) {
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = {
                        val isForward = targetState > initialState
                        val slideOffset = { size: Int -> if (isForward) size / 4 else -size / 4 }
                        (slideInHorizontally(
                            animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
                            initialOffsetX = slideOffset
                        ) + fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + scaleIn(
                            initialScale = 0.97f,
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                        )).togetherWith(
                            slideOutHorizontally(
                                animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
                                targetOffsetX = { -slideOffset(it) }
                            ) + fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + scaleOut(
                                targetScale = 0.97f,
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                            )
                        )
                    },
                    label = "tabTransition"
                ) { tab ->
                    when (tab) {
                        0 -> HomeScreen(
                            waterTotalMl = todayWaterTotal,
                            preferences = preferences,
                            todayEntries = todayWaterEntries,
                            reminders = allReminders,
                            todayLogs = todayLogs,
                            waterFlareActive = waterFlareActive,
                            onAddWater = { amt -> viewModel.addWater(amt) },
                            onRemoveWaterEntry = { entry -> viewModel.removeWaterEntry(entry) },
                            onMarkReminderStatus = { rem, status -> viewModel.markReminderStatus(rem, status) },
                            onOpenSettings = { viewModel.openSettings(true) },
                            isDark = isDark
                        )

                        1 -> WaterScreen(
                            waterTotalMl = todayWaterTotal,
                            preferences = preferences,
                            todayEntries = todayWaterEntries,
                            waterFlareActive = waterFlareActive,
                            onAddWater = { amt -> viewModel.addWater(amt) },
                            onRemoveWaterEntry = { entry -> viewModel.removeWaterEntry(entry) },
                            onUpdateDailyGoal = { goal -> viewModel.updateDailyWaterGoal(goal) },
                            onUpdateCustomCup = { cup -> viewModel.updateCustomQuickCup(cup) },
                            onUpdateReminderInterval = { min -> viewModel.updateWaterReminderInterval(min) },
                            onToggleWaterReminders = { enabled -> viewModel.updateWaterRemindersEnabled(enabled) },
                            isDark = isDark
                        )

                        2 -> RemindersScreen(
                            reminders = allReminders,
                            todayLogs = todayLogs,
                            preferences = preferences,
                            onOpenAddReminder = { viewModel.openAddReminder(true) },
                            onToggleActive = { rem -> viewModel.toggleReminderActive(rem) },
                            onDeleteReminder = { rem -> viewModel.deleteReminder(rem) },
                            onMarkReminderStatus = { rem, status -> viewModel.markReminderStatus(rem, status) },
                            onSpeakReminder = { rem -> viewModel.speakReminderAloud(rem) },
                            onTestRealTimeAlert = { viewModel.testRealTimeAlert() },
                            isDark = isDark
                        )

                        3 -> HistoryScreen(
                            weeklyWaterStats = past7DaysStats,
                            reminderStats = reminderStats,
                            todayLogs = todayLogs,
                            preferences = preferences,
                            isDark = isDark
                        )
                    }
                }
            }
        }

        // Add Reminder Bottom Sheet
        if (isAddReminderOpen) {
            AddReminderSheet(
                onDismiss = { viewModel.openAddReminder(false) },
                onSave = { title, type, dosage, time, days, notify, icon, cat, targetMillis, speakLoud ->
                    viewModel.addReminder(title, type, dosage, time, days, notify, icon, cat, targetMillis, speakLoud)
                },
                isDark = isDark
            )
        }

        // Settings Dialog
        if (isSettingsOpen) {
            SettingsDialog(
                preferences = preferences,
                onClose = { viewModel.openSettings(false) },
                onUpdateTheme = { mode -> viewModel.updateThemeMode(mode) },
                onToggleCompactMode = { compact -> viewModel.updateCompactMode(compact) },
                onToggleHaptics = { haptics -> viewModel.updateHapticsEnabled(haptics) },
                onToggleAllNotifications = { notif -> viewModel.updateAllNotificationsEnabled(notif) },
                isDark = isDark
            )
        }
    }
}
