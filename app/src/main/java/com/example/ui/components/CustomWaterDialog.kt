package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.graphics.liquidGlass
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomWaterDialog(
    initialAmountMl: Int = 250,
    dailyGoalMl: Int = 2500,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onLogWater: (amountMl: Int, note: String) -> Unit
) {
    var amountMl by remember { mutableIntStateOf(initialAmountMl.coerceIn(20, 3000)) }
    var textInput by remember { mutableStateOf(amountMl.toString()) }
    var customNote by remember { mutableStateOf("") }
    var sliderValue by remember { mutableFloatStateOf(amountMl.toFloat().coerceIn(50f, 1500f)) }

    val presetAmounts = listOf(
        Pair("Sip", 60),
        Pair("Small Glass", 150),
        Pair("Cup", 250),
        Pair("Mug", 350),
        Pair("Bottle", 500),
        Pair("Flask", 750),
        Pair("Carafe", 1000)
    )

    val notePresets = listOf("Water", "Warm Water", "Electrolytes", "Tea", "Post-Workout", "Hydrate")

    val progressFraction = (amountMl.toFloat() / dailyGoalMl.toFloat()).coerceIn(0f, 1f)
    val animatedFill by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "miniWaterFill"
    )

    LiquidGlassDialog(
        onDismissRequest = onDismiss,
        isDark = isDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .liquidGlass(shape = CircleShape, isDark = isDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalDrink,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Custom Water Intake",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color(0xFF0F172A)
                        )
                        Text(
                            text = "Log exact quantity & beverage note",
                            fontSize = 11.sp,
                            color = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF64748B)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Fluid Animated Mini Visualizer & Amount Display
            LiquidGlassSurface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                isDark = isDark
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Big fluid amount readout
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "$amountMl",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF38BDF8)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "ml",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF64748B),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    val pct = ((amountMl.toFloat() / dailyGoalMl.toFloat()) * 100).roundToInt()
                    Text(
                        text = "$pct% of daily $dailyGoalMl ml goal",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isDark) Color(0xFFBAE6FD) else Color(0xFF0284C7)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Liquid level bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color.White.copy(alpha = 0.1f) else Color(0xFFE2E8F0))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .graphicsLayer {
                                    scaleX = animatedFill
                                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0.5f)
                                }
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF38BDF8), Color(0xFF0284C7), Color(0xFF06B6D4))
                                    )
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Smooth Fluid Slider
            Slider(
                value = sliderValue,
                onValueChange = {
                    sliderValue = it
                    val rounded = (it / 10).roundToInt() * 10
                    amountMl = rounded
                    textInput = rounded.toString()
                },
                valueRange = 50f..1500f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF38BDF8),
                    activeTrackColor = Color(0xFF0284C7),
                    inactiveTrackColor = if (isDark) Color.White.copy(alpha = 0.15f) else Color(0xFFCBD5E1)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("water_slider")
            )

            // Direct Steppers (-100, -50, +50, +100, +250)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LiquidGlassPill(
                    isDark = isDark,
                    onClick = {
                        val newAmt = (amountMl - 100).coerceAtLeast(20)
                        amountMl = newAmt
                        sliderValue = newAmt.toFloat().coerceIn(50f, 1500f)
                        textInput = newAmt.toString()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("-100", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                LiquidGlassPill(
                    isDark = isDark,
                    onClick = {
                        val newAmt = (amountMl - 50).coerceAtLeast(20)
                        amountMl = newAmt
                        sliderValue = newAmt.toFloat().coerceIn(50f, 1500f)
                        textInput = newAmt.toString()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("-50", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                LiquidGlassPill(
                    isDark = isDark,
                    onClick = {
                        val newAmt = (amountMl + 50).coerceAtMost(3000)
                        amountMl = newAmt
                        sliderValue = newAmt.toFloat().coerceIn(50f, 1500f)
                        textInput = newAmt.toString()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("+50", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                LiquidGlassPill(
                    isDark = isDark,
                    onClick = {
                        val newAmt = (amountMl + 100).coerceAtMost(3000)
                        amountMl = newAmt
                        sliderValue = newAmt.toFloat().coerceIn(50f, 1500f)
                        textInput = newAmt.toString()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("+100", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                LiquidGlassPill(
                    isDark = isDark,
                    onClick = {
                        val newAmt = (amountMl + 250).coerceAtMost(3000)
                        amountMl = newAmt
                        sliderValue = newAmt.toFloat().coerceIn(50f, 1500f)
                        textInput = newAmt.toString()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("+250", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Preset Beverage Chips
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                presetAmounts.forEach { (label, presetMl) ->
                    val isSelected = amountMl == presetMl
                    LiquidGlassChip(
                        selected = isSelected,
                        onClick = {
                            amountMl = presetMl
                            sliderValue = presetMl.toFloat().coerceIn(50f, 1500f)
                            textInput = presetMl.toString()
                        },
                        label = "$label ($presetMl ml)",
                        isDark = isDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Exact Numeric Input TextField + Note TextField
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = {
                        textInput = it
                        val parsed = it.toIntOrNull()
                        if (parsed != null && parsed > 0) {
                            amountMl = parsed.coerceIn(10, 5000)
                            sliderValue = amountMl.toFloat().coerceIn(50f, 1500f)
                        }
                    },
                    label = { Text("Exact ml") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.25f) else Color(0xFFCBD5E1),
                        focusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
                        unfocusedTextColor = if (isDark) Color.White else Color(0xFF0F172A)
                    ),
                    modifier = Modifier
                        .weight(0.4f)
                        .testTag("exact_water_input")
                )

                OutlinedTextField(
                    value = customNote,
                    onValueChange = { customNote = it },
                    label = { Text("Note (optional)") },
                    placeholder = { Text("e.g., Post-run hydration") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.25f) else Color(0xFFCBD5E1),
                        focusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
                        unfocusedTextColor = if (isDark) Color.White else Color(0xFF0F172A)
                    ),
                    modifier = Modifier
                        .weight(0.6f)
                        .testTag("water_note_input")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Note Presets
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                notePresets.forEach { note ->
                    val isNoteSelected = customNote.equals(note, ignoreCase = true)
                    LiquidGlassPill(
                        isDark = isDark,
                        onClick = { customNote = if (isNoteSelected) "" else note }
                    ) {
                        Text(
                            text = note,
                            fontSize = 11.sp,
                            color = if (isNoteSelected) Color(0xFF38BDF8) else (if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF475569)),
                            fontWeight = if (isNoteSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LiquidGlassButton(
                    onClick = onDismiss,
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel", fontSize = 14.sp)
                }

                LiquidGlassButton(
                    onClick = {
                        if (amountMl > 0) {
                            onLogWater(amountMl, customNote.trim())
                            onDismiss()
                        }
                    },
                    isDark = isDark,
                    isPrimary = true,
                    enabled = amountMl > 0,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("log_custom_water_submit")
                ) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Log +$amountMl ml", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
