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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.WaterEntry
import com.example.data.preferences.UserPreferences
import com.example.graphics.WaterGlassVisualizer
import com.example.ui.components.CustomWaterDialog
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassDialog
import com.example.ui.components.LiquidGlassPill
import com.example.ui.components.LiquidGlassProgress
import com.example.ui.components.LiquidGlassSurface
import com.example.ui.utils.LocalResponsiveConfig
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WaterScreen(
    waterTotalMl: Int,
    preferences: UserPreferences,
    todayEntries: List<WaterEntry>,
    waterFlareActive: Boolean,
    onAddWater: (Int) -> Unit,
    onRemoveWaterEntry: (WaterEntry) -> Unit,
    onUpdateDailyGoal: (Int) -> Unit,
    onUpdateCustomCup: (Int) -> Unit,
    onUpdateReminderInterval: (Int) -> Unit,
    onToggleWaterReminders: (Boolean) -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val responsive = LocalResponsiveConfig.current
    val isCompact = responsive.isCompactWidth || preferences.compactModeEnabled
    val targetGoal = preferences.dailyWaterGoalMl
    val progressFraction = if (targetGoal > 0) waterTotalMl.toFloat() / targetGoal.toFloat() else 0f
    val remainingMl = (targetGoal - waterTotalMl).coerceAtLeast(0)

    var showEditGoalDialog by remember { mutableStateOf(false) }
    var goalInputText by remember { mutableStateOf(targetGoal.toString()) }

    var showCustomCupDialog by remember { mutableStateOf(false) }
    var customCupInputText by remember { mutableStateOf(preferences.customQuickCupMl.toString()) }

    var showCustomWaterDialog by remember { mutableStateOf(false) }

    val timeFormatter = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = responsive.screenTopPadding,
            bottom = responsive.screenBottomPadding,
            start = responsive.screenHorizontalPadding,
            end = responsive.screenHorizontalPadding
        ),
        verticalArrangement = Arrangement.spacedBy(responsive.itemSpacing)
    ) {
        // Screen Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hydration Tracker",
                        fontSize = if (isCompact) 22.sp else 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF0F172A)
                    )
                    Text(
                        text = "Balance your hydration with liquid clarity",
                        fontSize = 13.sp,
                        color = if (isDark) Color.White.copy(alpha = 0.65f) else Color(0xFF64748B)
                    )
                }

                LiquidGlassPill(
                    isDark = isDark,
                    onClick = {
                        goalInputText = targetGoal.toString()
                        showEditGoalDialog = true
                    },
                    testTag = "edit_goal_pill"
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Goal",
                        modifier = Modifier.size(15.dp),
                        tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Goal",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) Color.White else Color(0xFF0F172A)
                    )
                }
            }
        }

        // Dedicated Visualizer Card
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
                    // Left: Compact round glass visualizer
                    Box(
                        modifier = Modifier.size(if (isCompact) 105.dp else 135.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        WaterGlassVisualizer(
                            progressFraction = progressFraction,
                            modifier = Modifier.fillMaxSize(),
                            isDark = isDark,
                            accentGlow = waterFlareActive
                        )
                        Text(
                            text = "${(progressFraction * 100).toInt()}%",
                            fontSize = if (isCompact) 16.sp else 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Right: Detailed breakdown
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = if (isCompact) 12.dp else 18.dp)
                    ) {
                        Text(
                            text = "Current Intake",
                            fontSize = 12.sp,
                            color = if (isDark) Color.White.copy(alpha = 0.65f) else Color(0xFF64748B)
                        )
                        Text(
                            text = "$waterTotalMl ml",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color(0xFF0F172A)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Daily Target: $targetGoal ml",
                            fontSize = 12.sp,
                            color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = if (remainingMl > 0) "$remainingMl ml to go" else "Goal met!",
                            fontSize = 12.sp,
                            color = if (isDark) Color.White.copy(alpha = 0.75f) else Color(0xFF475569)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        LiquidGlassProgress(
                            progress = progressFraction,
                            isDark = isDark,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // QUICK ADD CONTAINER SIZES
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick Hydrate",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color(0xFF0F172A)
                )

                LiquidGlassPill(
                    isDark = isDark,
                    onClick = { showCustomWaterDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Custom ml",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Custom ml",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val quickVessels = listOf(
                Pair("Small Glass", 150),
                Pair("Standard Cup", 250),
                Pair("Sports Bottle", 500),
                Pair("Large Flask", 750)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                quickVessels.take(2).forEach { (label, amount) ->
                    VesselQuickPill(
                        label = label,
                        amount = amount,
                        isDark = isDark,
                        modifier = Modifier.weight(1f),
                        onClick = { onAddWater(amount) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                quickVessels.takeLast(2).forEach { (label, amount) ->
                    VesselQuickPill(
                        label = label,
                        amount = amount,
                        isDark = isDark,
                        modifier = Modifier.weight(1f),
                        onClick = { onAddWater(amount) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Custom user bottle quick action
            LiquidGlassSurface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                isDark = isDark
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalDrink,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = "My Custom Container",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) Color.White else Color(0xFF0F172A)
                            )
                            Text(
                                text = "${preferences.customQuickCupMl} ml capacity",
                                fontSize = 12.sp,
                                color = if (isDark) Color.White.copy(alpha = 0.65f) else Color(0xFF64748B)
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        LiquidGlassPill(
                            isDark = isDark,
                            onClick = {
                                customCupInputText = preferences.customQuickCupMl.toString()
                                showCustomCupDialog = true
                            }
                        ) {
                            Text(text = "Edit", fontSize = 11.sp, color = if (isDark) Color.White else Color(0xFF0F172A))
                        }

                        LiquidGlassPill(
                            isDark = isDark,
                            onClick = { onAddWater(preferences.customQuickCupMl) },
                            testTag = "add_custom_bottle"
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add custom",
                                modifier = Modifier.size(14.dp),
                                tint = Color(0xFF38BDF8)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "+${preferences.customQuickCupMl} ml",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
                            )
                        }
                    }
                }
            }
        }

        // HYDRATION REMINDER INTERVAL SETTINGS
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                isDark = isDark
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = if (preferences.waterRemindersEnabled) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                            contentDescription = null,
                            tint = if (preferences.waterRemindersEnabled) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = "Periodic Hydration Alerts",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) Color.White else Color(0xFF0F172A)
                            )
                            Text(
                                text = if (preferences.waterRemindersEnabled) {
                                    "Alerts every ${preferences.waterReminderIntervalMinutes} min"
                                } else "Reminders muted",
                                fontSize = 12.sp,
                                color = if (isDark) Color.White.copy(alpha = 0.65f) else Color(0xFF64748B)
                            )
                        }
                    }

                    Switch(
                        checked = preferences.waterRemindersEnabled,
                        onCheckedChange = { onToggleWaterReminders(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF0284C7)
                        )
                    )
                }

                if (preferences.waterRemindersEnabled) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Reminder Interval",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isDark) Color.White.copy(alpha = 0.75f) else Color(0xFF475569)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(if (isCompact) 6.dp else 8.dp)
                    ) {
                        listOf(45, 60, 90, 120).forEach { mins ->
                            val isSelected = preferences.waterReminderIntervalMinutes == mins
                            LiquidGlassPill(
                                modifier = Modifier.weight(1f),
                                isDark = isDark,
                                onClick = { onUpdateReminderInterval(mins) }
                            ) {
                                Text(
                                    text = if (mins >= 60) "${mins / 60}h ${if (mins % 60 != 0) "${mins % 60}m" else ""}" else "${mins}m",
                                    fontSize = if (isCompact) 11.sp else 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) {
                                        if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
                                    } else {
                                        if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF64748B)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // TODAY'S WATER LOG HISTORY
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Intake Log",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color(0xFF0F172A)
                )
                Text(
                    text = "${todayEntries.size} entries",
                    fontSize = 12.sp,
                    color = if (isDark) Color.White.copy(alpha = 0.65f) else Color(0xFF64748B)
                )
            }
        }

        if (todayEntries.isEmpty()) {
            item {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    isDark = isDark
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8).copy(alpha = 0.5f),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "No water logged yet today",
                            fontSize = 14.sp,
                            color = if (isDark) Color.White.copy(alpha = 0.65f) else Color(0xFF64748B)
                        )
                        Text(
                            text = "Tap any quick action above to start your day",
                            fontSize = 12.sp,
                            color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
                        )
                    }
                }
            }
        } else {
            items(todayEntries) { entry ->
                LiquidGlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    isDark = isDark
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            LiquidGlassSurface(
                                modifier = Modifier.size(34.dp),
                                shape = CircleShape,
                                isDark = isDark
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Opacity,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier
                                        .size(16.dp)
                                        .align(Alignment.Center)
                                )
                            }
                            Column {
                                Text(
                                    text = "+${entry.amountMl} ml",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isDark) Color.White else Color(0xFF0F172A)
                                )
                                Text(
                                    text = timeFormatter.format(Date(entry.timestamp)),
                                    fontSize = 11.sp,
                                    color = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF64748B)
                                )
                            }
                        }

                        LiquidGlassPill(
                            isDark = isDark,
                            onClick = { onRemoveWaterEntry(entry) },
                            testTag = "delete_entry_${entry.id}"
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete entry",
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFFEF4444)
                            )
                        }
                    }
                }
            }
        }
    }

    // EDIT DAILY GOAL DIALOG
    if (showEditGoalDialog) {
        LiquidGlassDialog(
            onDismissRequest = { showEditGoalDialog = false },
            isDark = isDark
        ) {
            Text(
                text = "Set Daily Target",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else Color(0xFF0F172A)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Recommended daily water intake is 2000 - 3000 ml",
                fontSize = 12.sp,
                color = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF64748B)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = goalInputText,
                onValueChange = { goalInputText = it },
                label = { Text("Daily Target (ml)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF38BDF8),
                    unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.3f) else Color(0xFFCBD5E1),
                    focusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
                    unfocusedTextColor = if (isDark) Color.White else Color(0xFF0F172A)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Quick presets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf(2000, 2500, 3000, 3500).forEach { preset ->
                    LiquidGlassPill(
                        isDark = isDark,
                        onClick = { goalInputText = preset.toString() }
                    ) {
                        Text(text = "$preset ml", fontSize = 11.sp, color = if (isDark) Color.White else Color(0xFF0F172A))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LiquidGlassButton(
                    onClick = { showEditGoalDialog = false },
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }

                LiquidGlassButton(
                    onClick = {
                        val parsed = goalInputText.toIntOrNull()
                        if (parsed != null && parsed > 0) {
                            onUpdateDailyGoal(parsed)
                        }
                        showEditGoalDialog = false
                    },
                    isDark = isDark,
                    isPrimary = true,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save")
                }
            }
        }
    }

    // EDIT CUSTOM CONTAINER DIALOG
    if (showCustomCupDialog) {
        LiquidGlassDialog(
            onDismissRequest = { showCustomCupDialog = false },
            isDark = isDark
        ) {
            Text(
                text = "Custom Container Size",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else Color(0xFF0F172A)
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = customCupInputText,
                onValueChange = { customCupInputText = it },
                label = { Text("Capacity (ml)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF38BDF8),
                    unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.3f) else Color(0xFFCBD5E1),
                    focusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
                    unfocusedTextColor = if (isDark) Color.White else Color(0xFF0F172A)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LiquidGlassButton(
                    onClick = { showCustomCupDialog = false },
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }

                LiquidGlassButton(
                    onClick = {
                        val parsed = customCupInputText.toIntOrNull()
                        if (parsed != null && parsed > 0) {
                            onUpdateCustomCup(parsed)
                        }
                        showCustomCupDialog = false
                    },
                    isDark = isDark,
                    isPrimary = true,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save")
                }
            }
        }
    }

    // CUSTOM WATER INTAKE DIALOG
    if (showCustomWaterDialog) {
        CustomWaterDialog(
            initialAmountMl = 250,
            dailyGoalMl = targetGoal,
            isDark = isDark,
            onDismiss = { showCustomWaterDialog = false },
            onLogWater = { amt, _ ->
                onAddWater(amt)
            }
        )
    }
}

@Composable
private fun VesselQuickPill(
    label: String,
    amount: Int,
    isDark: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    LiquidGlassSurface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        isDark = isDark
    ) {
        LiquidGlassPill(
            isDark = isDark,
            onClick = onClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "+$amount ml",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
                )
            }
        }
    }
}
