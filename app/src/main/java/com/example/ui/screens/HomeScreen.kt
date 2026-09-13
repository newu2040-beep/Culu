package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Hotel
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.ReminderItem
import com.example.data.database.ReminderLog
import com.example.data.database.WaterEntry
import com.example.data.preferences.UserPreferences
import com.example.graphics.LiquidGlassDefaults
import com.example.graphics.WaterGlassVisualizer
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassDialog
import com.example.ui.components.LiquidGlassPill
import com.example.ui.components.LiquidGlassProgress
import com.example.ui.components.LiquidGlassSurface
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    waterTotalMl: Int,
    preferences: UserPreferences,
    todayEntries: List<WaterEntry>,
    reminders: List<ReminderItem>,
    todayLogs: List<ReminderLog>,
    waterFlareActive: Boolean,
    onAddWater: (Int) -> Unit,
    onRemoveWaterEntry: (WaterEntry) -> Unit,
    onMarkReminderStatus: (ReminderItem, String) -> Unit,
    onOpenSettings: () -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val isCompact = preferences.compactModeEnabled
    val targetGoal = preferences.dailyWaterGoalMl
    val progressFraction = if (targetGoal > 0) waterTotalMl.toFloat() / targetGoal.toFloat() else 0f
    val remainingMl = (targetGoal - waterTotalMl).coerceAtLeast(0)
    val percentage = (progressFraction * 100).toInt()

    var showCustomWaterDialog by remember { mutableStateOf(false) }
    var customWaterInput by remember { mutableStateOf("300") }

    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..21 -> "Good evening"
            else -> "Peaceful night"
        }
    }

    val todayFormattedDate = remember {
        val sdf = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        sdf.format(Date())
    }

    val logMap = remember(todayLogs) { todayLogs.associateBy { it.reminderId } }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = if (isCompact) 12.dp else 20.dp,
            bottom = 110.dp,
            start = if (isCompact) 14.dp else 20.dp,
            end = if (isCompact) 14.dp else 20.dp
        ),
        verticalArrangement = Arrangement.spacedBy(if (isCompact) 14.dp else 22.dp)
    ) {
        // TOP HEADER: Greeting + Date + Settings
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = greeting,
                        fontSize = if (isCompact) 20.sp else 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF0F172A)
                    )
                    Text(
                        text = todayFormattedDate,
                        fontSize = if (isCompact) 12.sp else 14.sp,
                        color = if (isDark) Color.White.copy(alpha = 0.65f) else Color(0xFF64748B)
                    )
                }

                LiquidGlassPill(
                    isDark = isDark,
                    onClick = onOpenSettings,
                    testTag = "settings_button"
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        modifier = Modifier.size(20.dp),
                        tint = if (isDark) Color.White else Color(0xFF0F172A)
                    )
                }
            }
        }

        // HERO SECTION: Large Liquid Glass Water Visualizer & Progress
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                isDark = isDark,
                testTag = "water_hero_card"
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Title badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Daily Hydration",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
                        )
                    }

                    Spacer(modifier = Modifier.height(if (isCompact) 12.dp else 16.dp))

                    // Liquid Glass Vessel with Animated Waves
                    Box(
                        modifier = Modifier
                            .size(if (isCompact) 190.dp else 230.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        WaterGlassVisualizer(
                            progressFraction = progressFraction,
                            modifier = Modifier.fillMaxSize(),
                            isDark = isDark,
                            accentGlow = waterFlareActive
                        )

                        // Center Stats readout
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "%.1f L".format(waterTotalMl / 1000f),
                                fontSize = if (isCompact) 32.sp else 38.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "of %.1f L target".format(targetGoal / 1000f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$percentage%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFBAE6FD)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(if (isCompact) 12.dp else 18.dp))

                    // Subtext info
                    Text(
                        text = if (remainingMl > 0) {
                            "${(remainingMl / 100f).toInt() / 10f} L remaining to reach goal"
                        } else {
                            "Daily goal accomplished! 🎉"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isDark) Color.White.copy(alpha = 0.75f) else Color(0xFF475569)
                    )

                    Spacer(modifier = Modifier.height(if (isCompact) 14.dp else 20.dp))

                    // QUICK ADD PILLS: +150ml, +250ml, +500ml, Custom
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        QuickAddPill(label = "+150 ml", amount = 150, isDark = isDark, onAdd = onAddWater)
                        QuickAddPill(label = "+250 ml", amount = 250, isDark = isDark, onAdd = onAddWater)
                        QuickAddPill(label = "+500 ml", amount = 500, isDark = isDark, onAdd = onAddWater)
                        LiquidGlassPill(
                            isDark = isDark,
                            onClick = { showCustomWaterDialog = true },
                            testTag = "quick_add_custom"
                        ) {
                            Text(
                                text = "Custom",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
                            )
                        }
                    }
                }
            }
        }

        // RECENT WATER ENTRY UNDO (if any logged today)
        if (todayEntries.isNotEmpty()) {
            item {
                val latest = todayEntries.first()
                LiquidGlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    isDark = isDark
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Opacity,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Last entry: +${latest.amountMl} ml",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isDark) Color.White.copy(alpha = 0.85f) else Color(0xFF334155)
                            )
                        }

                        LiquidGlassPill(
                            isDark = isDark,
                            onClick = { onRemoveWaterEntry(latest) },
                            testTag = "undo_water_button"
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Remove entry",
                                    modifier = Modifier.size(14.dp),
                                    tint = Color(0xFFEF4444)
                                )
                                Text(
                                    text = "Undo",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFEF4444)
                                )
                            }
                        }
                    }
                }
            }
        }

        // TODAY'S REMINDERS & MEDICINE PREVIEW
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Routine",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color(0xFF0F172A)
                )

                val activeCount = reminders.count { it.isActive }
                val doneCount = todayLogs.count { it.status == ReminderLog.STATUS_TAKEN }
                Text(
                    text = "$doneCount of $activeCount done",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
                )
            }
        }

        val activeReminders = reminders.filter { it.isActive }
        if (activeReminders.isEmpty()) {
            item {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    isDark = isDark
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No routines scheduled for today",
                            fontSize = 14.sp,
                            color = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF64748B)
                        )
                        Text(
                            text = "Add medicine or daily habits in the Reminders tab",
                            fontSize = 12.sp,
                            color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        } else {
            items(activeReminders) { reminder ->
                val log = logMap[reminder.id]
                val status = log?.status ?: ReminderLog.STATUS_PENDING

                TodayReminderItemCard(
                    reminder = reminder,
                    status = status,
                    onStatusChange = { newStatus -> onMarkReminderStatus(reminder, newStatus) },
                    isDark = isDark,
                    isCompact = isCompact
                )
            }
        }
    }

    // CUSTOM WATER DIALOG
    if (showCustomWaterDialog) {
        LiquidGlassDialog(
            onDismissRequest = { showCustomWaterDialog = false },
            isDark = isDark
        ) {
            Text(
                text = "Log Custom Water",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else Color(0xFF0F172A)
            )

            Spacer(modifier = Modifier.height(14.dp))

            val quickAmounts = listOf(100, 200, 300, 400, 600, 800)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                quickAmounts.take(3).forEach { amt ->
                    LiquidGlassPill(
                        isDark = isDark,
                        onClick = {
                            onAddWater(amt)
                            showCustomWaterDialog = false
                        }
                    ) {
                        Text(text = "$amt ml", fontSize = 12.sp, color = if (isDark) Color.White else Color(0xFF0F172A))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                quickAmounts.takeLast(3).forEach { amt ->
                    LiquidGlassPill(
                        isDark = isDark,
                        onClick = {
                            onAddWater(amt)
                            showCustomWaterDialog = false
                        }
                    ) {
                        Text(text = "$amt ml", fontSize = 12.sp, color = if (isDark) Color.White else Color(0xFF0F172A))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            LiquidGlassButton(
                onClick = { showCustomWaterDialog = false },
                isDark = isDark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Close", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun QuickAddPill(
    label: String,
    amount: Int,
    isDark: Boolean,
    onAdd: (Int) -> Unit
) {
    LiquidGlassPill(
        isDark = isDark,
        onClick = { onAdd(amount) },
        testTag = "quick_add_$amount"
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isDark) Color.White else Color(0xFF0F172A)
        )
    }
}

@Composable
private fun TodayReminderItemCard(
    reminder: ReminderItem,
    status: String,
    onStatusChange: (String) -> Unit,
    isDark: Boolean,
    isCompact: Boolean
) {
    val icon = when (reminder.iconName) {
        "pill" -> Icons.Default.Medication
        "water" -> Icons.Default.WaterDrop
        "walk" -> Icons.Outlined.FitnessCenter
        "bed" -> Icons.Outlined.Hotel
        else -> Icons.Outlined.SelfImprovement
    }

    val isTaken = status == ReminderLog.STATUS_TAKEN
    val isSkipped = status == ReminderLog.STATUS_SKIPPED

    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        isDark = isDark,
        testTag = "today_reminder_${reminder.id}"
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LiquidGlassSurface(
                    modifier = Modifier.size(if (isCompact) 38.dp else 44.dp),
                    shape = CircleShape,
                    isDark = isDark
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier
                            .size(22.dp)
                            .align(Alignment.Center),
                        tint = when (reminder.type) {
                            ReminderItem.TYPE_MEDICINE -> Color(0xFFA855F7)
                            ReminderItem.TYPE_WATER -> Color(0xFF38BDF8)
                            else -> Color(0xFF10B981)
                        }
                    )
                }

                Column {
                    Text(
                        text = reminder.title,
                        fontSize = if (isCompact) 14.sp else 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) Color.White else Color(0xFF0F172A)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = reminder.formattedTime,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isDark) Color.White.copy(alpha = 0.65f) else Color(0xFF64748B)
                        )
                        if (reminder.dosage.isNotEmpty()) {
                            Text(
                                text = "• ${reminder.dosage}",
                                fontSize = 11.sp,
                                color = if (isDark) Color(0xFFBAE6FD) else Color(0xFF0284C7)
                            )
                        }
                    }
                }
            }

            // Quick Actions: Taken / Skip status
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isTaken) {
                    LiquidGlassPill(
                        isDark = isDark,
                        onClick = { onStatusChange(ReminderLog.STATUS_PENDING) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Taken",
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFF10B981)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Done",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                    }
                } else if (isSkipped) {
                    LiquidGlassPill(
                        isDark = isDark,
                        onClick = { onStatusChange(ReminderLog.STATUS_PENDING) }
                    ) {
                        Text(
                            text = "Skipped",
                            fontSize = 11.sp,
                            color = Color(0xFFF59E0B)
                        )
                    }
                } else {
                    // Pending Actions: Skip or Taken
                    LiquidGlassPill(
                        isDark = isDark,
                        onClick = { onStatusChange(ReminderLog.STATUS_SKIPPED) }
                    ) {
                        Text(
                            text = "Skip",
                            fontSize = 11.sp,
                            color = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF64748B)
                        )
                    }

                    LiquidGlassPill(
                        isDark = isDark,
                        onClick = { onStatusChange(ReminderLog.STATUS_TAKEN) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Mark Taken",
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFF10B981)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Done",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                    }
                }
            }
        }
    }
}
