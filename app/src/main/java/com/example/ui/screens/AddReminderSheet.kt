package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditCalendar
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import com.example.ui.utils.LocalResponsiveConfig
import com.example.utils.TtsSpeaker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddReminderSheet(
    initialReminder: ReminderItem? = null,
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

    val isEditing = initialReminder != null

    var name by remember { mutableStateOf(initialReminder?.title ?: "") }
    var selectedType by remember { mutableStateOf(initialReminder?.type ?: ReminderItem.TYPE_MEDICINE) }
    var dosage by remember { mutableStateOf(initialReminder?.dosage ?: "") }

    // Schedule Mode: false = Repeating Days, true = Specific Custom Date & Time
    var isSpecificDateMode by remember {
        mutableStateOf(initialReminder?.targetDateMillis != null)
    }

    // Custom Date selection: target calendar
    var targetDateMillis by remember {
        mutableStateOf(initialReminder?.targetDateMillis ?: System.currentTimeMillis())
    }

    val initialTime = initialReminder?.timeMinutes ?: 540 // 9:00 AM
    val initHour24 = initialTime / 60
    val initMin = initialTime % 60
    val initHour12 = when {
        initHour24 == 0 -> 12
        initHour24 > 12 -> initHour24 - 12
        else -> initHour24
    }
    val initPm = initHour24 >= 12

    var selectedHour by remember { mutableIntStateOf(initHour12) } // 1..12
    var selectedMinute by remember { mutableIntStateOf(initMin) } // 0..59
    var minuteSliderValue by remember { mutableFloatStateOf(initMin.toFloat()) }
    var isPm by remember { mutableStateOf(initPm) }

    val initialDays = remember {
        initialReminder?.daysOfWeek?.split(",")?.mapNotNull { it.trim().toIntOrNull() }?.toSet()
            ?: setOf(1, 2, 3, 4, 5, 6, 7)
    }
    var selectedDays by remember { mutableStateOf(initialDays) }

    var notificationEnabled by remember { mutableStateOf(initialReminder?.isNotificationEnabled ?: true) }
    var speakLoud by remember { mutableStateOf(initialReminder?.speakLoud ?: true) }
    var selectedIcon by remember { mutableStateOf(initialReminder?.iconName ?: "pill") }
    var selectedCategory by remember { mutableStateOf(initialReminder?.categoryName ?: "Health") }

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

    // Medicine quick suggestions
    val medicineSuggestions = listOf(
        "Paracetamol", "Amoxicillin", "Vitamin D3", "Omega-3",
        "Multivitamin", "Magnesium", "Zinc", "Iron", "Allergy Pill", "Blood Pressure"
    )

    // Habit/Routine suggestions
    val habitSuggestions = listOf(
        "Morning Stretch", "Drink 500ml Water", "Deep Breathing",
        "Read Book", "Evening Walk", "Posture Check", "Eye Rest", "Take Vitamins"
    )

    // Dosage unit chips
    val dosageUnits = listOf(
        "1 Tablet", "2 Tablets", "1 Capsule", "2 Capsules",
        "5 ml", "10 ml", "10 Drops", "1 Puff", "1 Sachet", "500 mg"
    )

    // Instruction notes
    val instructions = listOf(
        "Before Meal", "After Meal", "With Water", "Empty Stomach", "At Bedtime", "With Breakfast"
    )

    val targetDateFormatted = remember(targetDateMillis) {
        val sdf = SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault())
        sdf.format(Date(targetDateMillis))
    }

    val responsive = LocalResponsiveConfig.current
    val isCompact = responsive.isCompactWidth

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.Transparent,
        dragHandle = null
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = responsive.maxContentWidth)
                    .liquidGlass(
                        shape = LiquidGlassDefaults.SheetShape,
                        isDark = isDark,
                        blurRadius = 32.dp,
                        borderWidth = 1.5.dp
                    )
                    .padding(if (isCompact) 16.dp else 24.dp)
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
                                text = if (isEditing) "Edit Custom Reminder" else "Custom Reminder & Schedule",
                                fontSize = if (isCompact) 18.sp else 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color(0xFF0F172A)
                            )
                            Text(
                                text = "Custom medicine name, dosage, date, time & voice alert",
                                fontSize = 11.sp,
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

                    Spacer(modifier = Modifier.height(14.dp))

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

                    // Reminder Title (Custom Medicine Name / Habit)
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(if (selectedType == ReminderItem.TYPE_MEDICINE) "Custom Medicine Name" else "Custom Reminder Title") },
                        placeholder = {
                            Text(if (selectedType == ReminderItem.TYPE_MEDICINE) "e.g., Paracetamol 500mg, Vitamin D3" else "e.g., Evening Stretch, Take Supplements")
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

                    Spacer(modifier = Modifier.height(6.dp))

                    // Quick suggestions for medicine or habit
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val suggestions = if (selectedType == ReminderItem.TYPE_MEDICINE) medicineSuggestions else habitSuggestions
                        suggestions.forEach { suggestion ->
                            val isSelected = name.equals(suggestion, ignoreCase = true)
                            LiquidGlassChip(
                                selected = isSelected,
                                onClick = { name = suggestion },
                                label = suggestion,
                                isDark = isDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Dosage / Instructions Field
                    OutlinedTextField(
                        value = dosage,
                        onValueChange = { dosage = it },
                        label = { Text(if (selectedType == ReminderItem.TYPE_MEDICINE) "Custom Dosage / Instructions" else "Notes / Target") },
                        placeholder = {
                            Text(if (selectedType == ReminderItem.TYPE_MEDICINE) "e.g., 1 tablet with water after lunch" else "e.g., 15 minutes, drink water after")
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

                    Spacer(modifier = Modifier.height(6.dp))

                    // Dosage Unit & Instruction Quick Chips
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (selectedType == ReminderItem.TYPE_MEDICINE) {
                            dosageUnits.forEach { unit ->
                                LiquidGlassPill(
                                    isDark = isDark,
                                    onClick = {
                                        dosage = if (dosage.isBlank()) unit else "$dosage, $unit"
                                    }
                                ) {
                                    Text(unit, fontSize = 11.sp, color = if (isDark) Color(0xFFBAE6FD) else Color(0xFF0284C7))
                                }
                            }
                        }
                        instructions.forEach { inst ->
                            LiquidGlassPill(
                                isDark = isDark,
                                onClick = {
                                    dosage = if (dosage.isBlank()) inst else "$dosage ($inst)"
                                }
                            ) {
                                Text(inst, fontSize = 11.sp, color = if (isDark) Color.White.copy(alpha = 0.75f) else Color(0xFF475569))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Schedule Mode Selector: Repeating Days vs Specific Custom Calendar Date
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Schedule Mode",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Color.White else Color(0xFF0F172A)
                        )
                    }

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
                            label = "Specific Date",
                            icon = Icons.Default.CalendarMonth,
                            isDark = isDark,
                            modifier = Modifier.weight(1f),
                            testTag = "schedule_custom_date_chip"
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isSpecificDateMode) {
                        // Custom Calendar Date Selection
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Target Date",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) Color.White.copy(alpha = 0.85f) else Color(0xFF334155)
                            )

                            // Pick from System Calendar button
                            LiquidGlassPill(
                                isDark = isDark,
                                onClick = {
                                    val currentCal = Calendar.getInstance().apply { timeInMillis = targetDateMillis }
                                    DatePickerDialog(
                                        context,
                                        { _, y, m, d ->
                                            val newCal = Calendar.getInstance().apply {
                                                set(Calendar.YEAR, y)
                                                set(Calendar.MONTH, m)
                                                set(Calendar.DAY_OF_MONTH, d)
                                            }
                                            targetDateMillis = newCal.timeInMillis
                                        },
                                        currentCal.get(Calendar.YEAR),
                                        currentCal.get(Calendar.MONTH),
                                        currentCal.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EditCalendar,
                                    contentDescription = "Pick Date",
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Calendar Picker 📅", fontSize = 11.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick date offset chips
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                0 to "Today",
                                1 to "Tomorrow",
                                2 to "+2 Days",
                                3 to "+3 Days",
                                5 to "+5 Days",
                                7 to "Next Week"
                            ).forEach { (offset, label) ->
                                val offsetCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, offset) }
                                val isSelected = run {
                                    val tCal = Calendar.getInstance().apply { timeInMillis = targetDateMillis }
                                    tCal.get(Calendar.YEAR) == offsetCal.get(Calendar.YEAR) &&
                                            tCal.get(Calendar.DAY_OF_YEAR) == offsetCal.get(Calendar.DAY_OF_YEAR)
                                }
                                LiquidGlassChip(
                                    selected = isSelected,
                                    onClick = {
                                        targetDateMillis = offsetCal.timeInMillis
                                    },
                                    label = label,
                                    isDark = isDark
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Date banner preview
                        LiquidGlassSurface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            isDark = isDark
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Scheduled for: $targetDateFormatted",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
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

                    // Scheduled Custom Time Section
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
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF38BDF8)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Hour selector chips + AM/PM
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

                    Spacer(modifier = Modifier.height(10.dp))

                    // Fluid Interactive Minute Slider (0 to 59)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Exact Minute: :%02d".format(selectedMinute),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isDark) Color(0xFFBAE6FD) else Color(0xFF0284C7)
                        )
                    }

                    Slider(
                        value = minuteSliderValue,
                        onValueChange = {
                            minuteSliderValue = it
                            selectedMinute = it.roundToInt().coerceIn(0, 59)
                        },
                        valueRange = 0f..59f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF38BDF8),
                            activeTrackColor = Color(0xFF0284C7),
                            inactiveTrackColor = if (isDark) Color.White.copy(alpha = 0.15f) else Color(0xFFCBD5E1)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Minute quick presets & steppers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(0, 15, 30, 45).forEach { min ->
                            LiquidGlassChip(
                                selected = selectedMinute == min,
                                onClick = {
                                    selectedMinute = min
                                    minuteSliderValue = min.toFloat()
                                },
                                label = ":%02d".format(min),
                                isDark = isDark,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        LiquidGlassPill(
                            isDark = isDark,
                            onClick = {
                                val nextMin = (selectedMinute + 5) % 60
                                selectedMinute = nextMin
                                minuteSliderValue = nextMin.toFloat()
                            }
                        ) {
                            Text("+5m", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Common time-of-day shortcuts
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            Triple("Morning (8:00 AM)", 8, false),
                            Triple("Noon (12:00 PM)", 12, true),
                            Triple("Afternoon (2:30 PM)", 2, true),
                            Triple("Evening (6:30 PM)", 6, true),
                            Triple("Bedtime (10:00 PM)", 10, true)
                        ).forEach { (label, hr, pm) ->
                            LiquidGlassPill(
                                isDark = isDark,
                                onClick = {
                                    selectedHour = hr
                                    isPm = pm
                                    selectedMinute = if (label.contains("30")) 30 else 0
                                    minuteSliderValue = selectedMinute.toFloat()
                                }
                            ) {
                                Text(label, fontSize = 11.sp, color = if (isDark) Color.White.copy(alpha = 0.75f) else Color(0xFF475569))
                            }
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
                                fontSize = 11.sp,
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
                                fontSize = 11.sp,
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
                                    text = "Preview Voice Alert 🔊",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF38BDF8)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // Save / Update Button
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

                                val finalTargetMillis = if (isSpecificDateMode) {
                                    Calendar.getInstance().apply {
                                        timeInMillis = targetDateMillis
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
                                    finalTargetMillis,
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
                            text = if (isEditing) "Update Reminder" else "Save Reminder",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
