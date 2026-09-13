package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.ReminderItem
import com.example.graphics.LiquidGlassDefaults
import com.example.graphics.liquidGlass
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.LiquidGlassChip
import com.example.ui.components.LiquidGlassPill
import com.example.ui.components.LiquidGlassSurface
import com.example.utils.TtsSpeaker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderSheet(
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        type: String,
        dosage: String,
        timeMinutes: Int,
        daysOfWeek: String,
        isNotificationEnabled: Boolean,
        iconName: String,
        categoryName: String,
        targetDateMillis: Long?,
        speakLoud: Boolean
    ) -> Unit,
    isDark: Boolean
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(ReminderItem.TYPE_MEDICINE) }
    var dosage by remember { mutableStateOf("") }

    // Schedule Mode: false = Repeating Days, true = Specific Custom Date & Time
    var isSpecificDateMode by remember { mutableStateOf(false) }

    // Custom Date selection: day offset from today (0 = Today, 1 = Tomorrow, 2 = Day After, etc.)
    var selectedDayOffset by remember { mutableStateOf(0) }

    var selectedHour by remember { mutableStateOf(9) } // 1..12
    var selectedMinute by remember { mutableStateOf(0) } // 0..59
    var isPm by remember { mutableStateOf(false) }

    var selectedDays by remember { mutableStateOf(setOf(1, 2, 3, 4, 5, 6, 7)) } // 1=Mon .. 7=Sun
    var notificationEnabled by remember { mutableStateOf(true) }
    var speakLoud by remember { mutableStateOf(true) } // Text-To-Speech loud voice announcement
    var selectedIcon by remember { mutableStateOf("pill") }
    var selectedCategory by remember { mutableStateOf("Health") }

    val iconOptions = listOf(
        Pair("pill", Icons.Default.Medication),
        Pair("water", Icons.Default.WaterDrop),
        Pair("walk", Icons.Outlined.FitnessCenter),
        Pair("bed", Icons.Outlined.Bedtime),
        Pair("book", Icons.Outlined.MenuBook),
        Pair("meal", Icons.Outlined.Restaurant),
        Pair("zen", Icons.Outlined.Spa),
        Pair("routine", Icons.Outlined.SelfImprovement)
    )

    val dayNames = listOf(
        Pair(1, "M"),
        Pair(2, "T"),
        Pair(3, "W"),
        Pair(4, "T"),
        Pair(5, "F"),
        Pair(6, "S"),
        Pair(7, "S")
    )

    // Calculate formatted target date string based on offset
    val targetDateCal = remember(selectedDayOffset) {
        Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, selectedDayOffset)
        }
    }
    val targetDateFormatted = remember(selectedDayOffset) {
        val sdf = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
        sdf.format(targetDateCal.time)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.Transparent,
        dragHandle = null
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlass(
                    shape = LiquidGlassDefaults.SheetShape,
                    isDark = isDark,
                    blurRadius = 32.dp,
                    borderWidth = 1.5.dp
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "New Reminder",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color(0xFF0F172A)
                        )
                        Text(
                            text = "Custom date, time & loud voice alert",
                            fontSize = 12.sp,
                            color = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF64748B)
                        )
                    }

                    LiquidGlassPill(
                        isDark = isDark,
                        onClick = onDismiss
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            modifier = Modifier.size(16.dp),
                            tint = if (isDark) Color.White else Color(0xFF0F172A)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Type Selection: Medicine vs Routine
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LiquidGlassChip(
                        selected = selectedType == ReminderItem.TYPE_MEDICINE,
                        onClick = {
                            selectedType = ReminderItem.TYPE_MEDICINE
                            selectedIcon = "pill"
                            selectedCategory = "Medicine"
                        },
                        label = "Medicine / Pill",
                        icon = Icons.Default.Medication,
                        isDark = isDark,
                        modifier = Modifier.weight(1f)
                    )

                    LiquidGlassChip(
                        selected = selectedType == ReminderItem.TYPE_CUSTOM,
                        onClick = {
                            selectedType = ReminderItem.TYPE_CUSTOM
                            selectedIcon = "routine"
                            selectedCategory = "Routine"
                        },
                        label = "Habit / Routine",
                        icon = Icons.Outlined.SelfImprovement,
                        isDark = isDark,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Reminder Title
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Reminder Title") },
                    placeholder = {
                        Text(if (selectedType == ReminderItem.TYPE_MEDICINE) "e.g., Vitamin D, Omega 3" else "e.g., Evening stretch, Read 10 min")
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.2f) else Color(0xFFCBD5E1),
                        focusedLabelColor = Color(0xFF38BDF8),
                        unfocusedLabelColor = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF64748B),
                        focusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
                        unfocusedTextColor = if (isDark) Color.White else Color(0xFF0F172A)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reminder_title_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Dosage / Description Field
                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text(if (selectedType == ReminderItem.TYPE_MEDICINE) "Dosage / Instructions" else "Notes / Target") },
                    placeholder = {
                        Text(if (selectedType == ReminderItem.TYPE_MEDICINE) "e.g., 1 tablet after breakfast" else "e.g., 20 pushups, hydrate after")
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.2f) else Color(0xFFCBD5E1),
                        focusedLabelColor = Color(0xFF38BDF8),
                        unfocusedLabelColor = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF64748B),
                        focusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
                        unfocusedTextColor = if (isDark) Color.White else Color(0xFF0F172A)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reminder_dosage_input")
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Schedule Mode Selector: Daily Recurring vs Specific Custom Date
                Text(
                    text = "Schedule Type",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Color.White else Color(0xFF0F172A),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LiquidGlassChip(
                        selected = !isSpecificDateMode,
                        onClick = { isSpecificDateMode = false },
                        label = "Repeat Days",
                        icon = Icons.Default.Repeat,
                        isDark = isDark,
                        modifier = Modifier.weight(1f),
                        testTag = "schedule_recurring_chip"
                    )

                    LiquidGlassChip(
                        selected = isSpecificDateMode,
                        onClick = { isSpecificDateMode = true },
                        label = "Custom Date",
                        icon = Icons.Default.CalendarMonth,
                        isDark = isDark,
                        modifier = Modifier.weight(1f),
                        testTag = "schedule_custom_date_chip"
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (isSpecificDateMode) {
                    // Custom Date Selection section
                    Text(
                        text = "Select Target Date",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) Color.White.copy(alpha = 0.85f) else Color(0xFF334155),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(listOf(0 to "Today", 1 to "Tomorrow", 2 to "+2 Days", 3 to "+3 Days", 7 to "Next Week")) { (offset, label) ->
                            LiquidGlassChip(
                                selected = selectedDayOffset == offset,
                                onClick = { selectedDayOffset = offset },
                                label = label,
                                isDark = isDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Date banner preview
                    LiquidGlassSurface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        isDark = isDark
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Scheduled for: $targetDateFormatted",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isDark) Color.White else Color(0xFF0F172A)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                } else {
                    // Repeat Days (Monday..Sunday)
                    Text(
                        text = "Repeat on Days",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) Color.White.copy(alpha = 0.85f) else Color(0xFF334155),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        dayNames.forEach { (dayInt, label) ->
                            val isDaySelected = selectedDays.contains(dayInt)
                            LiquidGlassChip(
                                selected = isDaySelected,
                                onClick = {
                                    selectedDays = if (isDaySelected) {
                                        if (selectedDays.size > 1) selectedDays - dayInt else selectedDays
                                    } else {
                                        selectedDays + dayInt
                                    }
                                },
                                label = label,
                                isDark = isDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Scheduled Time Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Custom Time",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) Color.White else Color(0xFF0F172A)
                    )

                    // Formatted display
                    val displayTime = "%d:%02d %s".format(selectedHour, selectedMinute, if (isPm) "PM" else "AM")
                    Text(
                        text = displayTime,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Hour selector pills + AM/PM
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items((1..12).toList()) { hour ->
                            LiquidGlassChip(
                                selected = selectedHour == hour,
                                onClick = { selectedHour = hour },
                                label = hour.toString(),
                                isDark = isDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // AM / PM toggle
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        LiquidGlassChip(
                            selected = !isPm,
                            onClick = { isPm = false },
                            label = "AM",
                            isDark = isDark
                        )
                        LiquidGlassChip(
                            selected = isPm,
                            onClick = { isPm = true },
                            label = "PM",
                            isDark = isDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Custom Minute Selection pills (00, 10, 15, 20, 30, 45, 50) + Stepper
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Quick minute chips
                    listOf(0, 10, 15, 20, 30, 45).forEach { min ->
                        LiquidGlassChip(
                            selected = selectedMinute == min,
                            onClick = { selectedMinute = min },
                            label = ":%02d".format(min),
                            isDark = isDark,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Stepper +5m
                    LiquidGlassPill(
                        isDark = isDark,
                        onClick = { selectedMinute = (selectedMinute + 5) % 60 }
                    ) {
                        Text("+5m", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Icon Symbol Picker
                Text(
                    text = "Icon Symbol",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Color.White else Color(0xFF0F172A),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(iconOptions) { (key, vector) ->
                        val isIconSelected = selectedIcon == key
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .liquidGlass(
                                    shape = CircleShape,
                                    isDark = isDark,
                                    borderWidth = if (isIconSelected) 2.dp else 0.8.dp,
                                    interactivePress = true,
                                    onClick = { selectedIcon = key }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = vector,
                                contentDescription = key,
                                modifier = Modifier.size(20.dp),
                                tint = if (isIconSelected) Color(0xFF38BDF8) else (if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF64748B))
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Notification Alert Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Notification Alert",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Color.White else Color(0xFF0F172A)
                        )
                        Text(
                            text = "Real-time heads-up notification with sound & vibration",
                            fontSize = 12.sp,
                            color = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF64748B)
                        )
                    }

                    Switch(
                        checked = notificationEnabled,
                        onCheckedChange = { notificationEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF0284C7)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Speak Loud Feature (TTS)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Speak Loudly (Voice Alert)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) Color.White else Color(0xFF0F172A)
                            )
                        }
                        Text(
                            text = "Speaks reminder clearly so you can hear across the room",
                            fontSize = 12.sp,
                            color = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF64748B)
                        )
                    }

                    Switch(
                        checked = speakLoud,
                        onCheckedChange = { speakLoud = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF0284C7)
                        )
                    )
                }

                // Test voice preview button
                if (speakLoud) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        LiquidGlassPill(
                            isDark = isDark,
                            onClick = {
                                val testTitle = if (name.isNotBlank()) name.trim() else "Medicine Routine"
                                val testDosage = if (dosage.isNotBlank()) dosage.trim() else "Take your scheduled dose"
                                TtsSpeaker.speakReminder(context, testTitle, testDosage)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Test Voice",
                                modifier = Modifier.size(14.dp),
                                tint = Color(0xFF38BDF8)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Test Loud Voice Now",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF38BDF8)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Save Pill
                LiquidGlassButton(
                    onClick = {
                        if (name.isNotBlank()) {
                            val computedHour24 = when {
                                selectedHour == 12 && !isPm -> 0
                                selectedHour == 12 && isPm -> 12
                                isPm -> selectedHour + 12
                                else -> selectedHour
                            }
                            val timeMinutes = computedHour24 * 60 + selectedMinute
                            val daysOfWeekStr = selectedDays.sorted().joinToString(",")

                            val targetMillis = if (isSpecificDateMode) {
                                targetDateCal.apply {
                                    set(Calendar.HOUR_OF_DAY, computedHour24)
                                    set(Calendar.MINUTE, selectedMinute)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }.timeInMillis
                            } else null

                            onSave(
                                name.trim(),
                                selectedType,
                                dosage.trim(),
                                timeMinutes,
                                daysOfWeekStr,
                                notificationEnabled,
                                selectedIcon,
                                selectedCategory,
                                targetMillis,
                                speakLoud
                            )
                        }
                    },
                    isDark = isDark,
                    isPrimary = true,
                    enabled = name.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_reminder_button")
                ) {
                    Text(
                        text = "Save Reminder",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
