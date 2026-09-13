package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.data.preferences.UserPreferences
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassChip
import com.example.ui.components.LiquidGlassPill
import com.example.ui.components.LiquidGlassSurface

@Composable
fun RemindersScreen(
    reminders: List<ReminderItem>,
    todayLogs: List<ReminderLog>,
    preferences: UserPreferences,
    onOpenAddReminder: () -> Unit,
    onToggleActive: (ReminderItem) -> Unit,
    onDeleteReminder: (ReminderItem) -> Unit,
    onMarkReminderStatus: (ReminderItem, String) -> Unit,
    onSpeakReminder: (ReminderItem) -> Unit,
    onTestRealTimeAlert: () -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val isCompact = preferences.compactModeEnabled
    var selectedFilter by remember { mutableStateOf("ALL") } // "ALL", "MEDICINE", "CUSTOM"

    val filteredReminders = remember(reminders, selectedFilter) {
        when (selectedFilter) {
            "MEDICINE" -> reminders.filter { it.type == ReminderItem.TYPE_MEDICINE }
            "CUSTOM" -> reminders.filter { it.type != ReminderItem.TYPE_MEDICINE }
            else -> reminders
        }
    }

    val logMap = remember(todayLogs) { todayLogs.associateBy { it.reminderId } }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = if (isCompact) 12.dp else 20.dp,
                bottom = 140.dp, // space for floating add button & bottom bar
                start = if (isCompact) 14.dp else 20.dp,
                end = if (isCompact) 14.dp else 20.dp
            ),
            verticalArrangement = Arrangement.spacedBy(if (isCompact) 12.dp else 16.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Routines & Medicine",
                            fontSize = if (isCompact) 22.sp else 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color(0xFF0F172A)
                        )
                        Text(
                            text = "Custom date, real-time alerts & voice playback",
                            fontSize = 13.sp,
                            color = if (isDark) Color.White.copy(alpha = 0.65f) else Color(0xFF64748B)
                        )
                    }
                }
            }

            // Real-Time Notification & Loud Voice Test Card
            item {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    isDark = isDark
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            LiquidGlassSurface(
                                modifier = Modifier.size(38.dp),
                                shape = CircleShape,
                                isDark = isDark
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier
                                        .size(20.dp)
                                        .align(Alignment.Center)
                                )
                            }

                            Column {
                                Text(
                                    text = "Real-Time Alert & Voice",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color.White else Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Hear loud alert announcement now",
                                    fontSize = 11.sp,
                                    color = if (isDark) Color.White.copy(alpha = 0.65f) else Color(0xFF64748B)
                                )
                            }
                        }

                        LiquidGlassPill(
                            isDark = isDark,
                            onClick = onTestRealTimeAlert,
                            testTag = "test_realtime_alert_button"
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Test",
                                modifier = Modifier.size(14.dp),
                                tint = Color(0xFF38BDF8)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Test Alert",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF38BDF8)
                            )
                        }
                    }
                }
            }

            // Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LiquidGlassChip(
                        selected = selectedFilter == "ALL",
                        onClick = { selectedFilter = "ALL" },
                        label = "All (${reminders.size})",
                        isDark = isDark,
                        testTag = "filter_all"
                    )
                    LiquidGlassChip(
                        selected = selectedFilter == "MEDICINE",
                        onClick = { selectedFilter = "MEDICINE" },
                        label = "Medicine (${reminders.count { it.type == ReminderItem.TYPE_MEDICINE }})",
                        icon = Icons.Default.Medication,
                        isDark = isDark,
                        testTag = "filter_medicine"
                    )
                    LiquidGlassChip(
                        selected = selectedFilter == "CUSTOM",
                        onClick = { selectedFilter = "CUSTOM" },
                        label = "Habits (${reminders.count { it.type != ReminderItem.TYPE_MEDICINE }})",
                        icon = Icons.Outlined.SelfImprovement,
                        isDark = isDark,
                        testTag = "filter_custom"
                    )
                }
            }

            // Empty state
            if (filteredReminders.isEmpty()) {
                item {
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        isDark = isDark
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = if (selectedFilter == "MEDICINE") Icons.Default.Medication else Icons.Outlined.SelfImprovement,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8).copy(alpha = 0.5f),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No reminders in this category",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) Color.White else Color(0xFF0F172A)
                            )
                            Text(
                                text = "Tap '+ Add Reminder' below to schedule custom date/time alerts",
                                fontSize = 13.sp,
                                color = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF64748B),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            } else {
                items(filteredReminders, key = { it.id }) { reminder ->
                    val log = logMap[reminder.id]
                    val status = log?.status ?: ReminderLog.STATUS_PENDING

                    FullReminderCard(
                        reminder = reminder,
                        status = status,
                        onStatusChange = { newStatus -> onMarkReminderStatus(reminder, newStatus) },
                        onToggleActive = { onToggleActive(reminder) },
                        onDelete = { onDeleteReminder(reminder) },
                        onSpeak = { onSpeakReminder(reminder) },
                        isDark = isDark,
                        isCompact = isCompact
                    )
                }
            }
        }

        // Floating Liquid Glass pill button: + Add Reminder
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 88.dp),
            contentAlignment = Alignment.Center
        ) {
            LiquidGlassButton(
                onClick = onOpenAddReminder,
                isDark = isDark,
                isPrimary = true,
                testTag = "add_reminder_fab"
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Add Reminder",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun FullReminderCard(
    reminder: ReminderItem,
    status: String,
    onStatusChange: (String) -> Unit,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit,
    onSpeak: () -> Unit,
    isDark: Boolean,
    isCompact: Boolean
) {
    val icon: ImageVector = when (reminder.iconName) {
        "pill" -> Icons.Default.Medication
        "water" -> Icons.Default.WaterDrop
        "walk" -> Icons.Outlined.FitnessCenter
        "bed" -> Icons.Outlined.Bedtime
        "book" -> Icons.Outlined.MenuBook
        "meal" -> Icons.Outlined.Restaurant
        "zen" -> Icons.Outlined.Spa
        else -> Icons.Outlined.SelfImprovement
    }

    val typeColor = when (reminder.type) {
        ReminderItem.TYPE_MEDICINE -> Color(0xFFA855F7)
        ReminderItem.TYPE_WATER -> Color(0xFF38BDF8)
        else -> Color(0xFF10B981)
    }

    val isTaken = status == ReminderLog.STATUS_TAKEN
    val isSkipped = status == ReminderLog.STATUS_SKIPPED

    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        isDark = isDark,
        testTag = "reminder_card_${reminder.id}"
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
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
                        modifier = Modifier.size(if (isCompact) 40.dp else 46.dp),
                        shape = CircleShape,
                        isDark = isDark
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.Center),
                            tint = typeColor
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = reminder.title,
                                fontSize = if (isCompact) 15.sp else 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color(0xFF0F172A)
                            )
                            if (reminder.type == ReminderItem.TYPE_MEDICINE) {
                                LiquidGlassSurface(
                                    shape = CircleShape,
                                    isDark = isDark
                                ) {
                                    Text(
                                        text = "Medicine",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFA855F7),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        if (reminder.dosage.isNotEmpty()) {
                            Text(
                                text = reminder.dosage,
                                fontSize = 12.sp,
                                color = if (isDark) Color(0xFFBAE6FD) else Color(0xFF0284C7)
                            )
                        }

                        // Schedule / Custom date indicator
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (reminder.targetDateMillis != null) Icons.Default.CalendarMonth else Icons.Default.Alarm,
                                contentDescription = null,
                                tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = reminder.formattedSchedule,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isDark) Color.White.copy(alpha = 0.75f) else Color(0xFF334155)
                            )
                        }
                    }
                }

                // Active Switch
                Switch(
                    checked = reminder.isActive,
                    onCheckedChange = { onToggleActive() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF0284C7)
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Lower Action Row: Speak Loud pill + Status pills + Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Speaker Button: Speaks loud on tap
                LiquidGlassPill(
                    isDark = isDark,
                    onClick = onSpeak,
                    testTag = "speak_reminder_${reminder.id}"
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Speak Aloud",
                        modifier = Modifier.size(13.dp),
                        tint = Color(0xFF38BDF8)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Speak",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF38BDF8)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isTaken) {
                        LiquidGlassPill(
                            isDark = isDark,
                            onClick = { onStatusChange(ReminderLog.STATUS_PENDING) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Taken",
                                modifier = Modifier.size(13.dp),
                                tint = Color(0xFF10B981)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(text = "Taken", fontSize = 11.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                        }
                    } else if (isSkipped) {
                        LiquidGlassPill(
                            isDark = isDark,
                            onClick = { onStatusChange(ReminderLog.STATUS_PENDING) }
                        ) {
                            Text(text = "Skipped", fontSize = 11.sp, color = Color(0xFFF59E0B))
                        }
                    } else {
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
                                contentDescription = "Mark Done",
                                modifier = Modifier.size(13.dp),
                                tint = Color(0xFF10B981)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Take",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                        }
                    }

                    LiquidGlassPill(
                        isDark = isDark,
                        onClick = onDelete,
                        testTag = "delete_reminder_${reminder.id}"
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFFEF4444)
                        )
                    }
                }
            }
        }
    }
}
