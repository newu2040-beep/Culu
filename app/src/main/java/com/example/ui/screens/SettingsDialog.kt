package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.ViewCompact
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.UserPreferences
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassChip
import com.example.ui.components.LiquidGlassDialog
import com.example.ui.components.LiquidGlassPill
import com.example.ui.components.LiquidGlassSurface

@Composable
fun SettingsDialog(
    preferences: UserPreferences,
    onClose: () -> Unit,
    onUpdateTheme: (String) -> Unit,
    onToggleCompactMode: (Boolean) -> Unit,
    onToggleHaptics: (Boolean) -> Unit,
    onToggleAllNotifications: (Boolean) -> Unit,
    isDark: Boolean
) {
    LiquidGlassDialog(
        onDismissRequest = onClose,
        isDark = isDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title & Close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Preferences",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color(0xFF0F172A)
                )

                LiquidGlassPill(
                    isDark = isDark,
                    onClick = onClose,
                    testTag = "close_settings_button"
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        modifier = Modifier.size(16.dp),
                        tint = if (isDark) Color.White else Color(0xFF0F172A)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // THEME SELECTION
            Text(
                text = "Liquid Glass Appearance",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LiquidGlassChip(
                    selected = preferences.themeMode == "SYSTEM",
                    onClick = { onUpdateTheme("SYSTEM") },
                    label = "System",
                    icon = Icons.Default.SettingsBrightness,
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
                LiquidGlassChip(
                    selected = preferences.themeMode == "DARK",
                    onClick = { onUpdateTheme("DARK") },
                    label = "Dark",
                    icon = Icons.Default.DarkMode,
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
                LiquidGlassChip(
                    selected = preferences.themeMode == "LIGHT",
                    onClick = { onUpdateTheme("LIGHT") },
                    label = "Light",
                    icon = Icons.Default.LightMode,
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // TOGGLES
            Text(
                text = "Interface & Tactile",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Compact Mode Toggle
            SettingToggleRow(
                icon = Icons.Default.ViewCompact,
                title = "Compact Mode",
                subtitle = "Optimized layout density and tighter padding",
                checked = preferences.compactModeEnabled,
                onCheckedChange = onToggleCompactMode,
                isDark = isDark
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Haptics Toggle
            SettingToggleRow(
                icon = Icons.Default.Vibration,
                title = "Tactile Haptics",
                subtitle = "Subtle physical clicks and fluid water drop feedback",
                checked = preferences.hapticsEnabled,
                onCheckedChange = onToggleHaptics,
                isDark = isDark
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Notifications Master Toggle
            SettingToggleRow(
                icon = Icons.Default.Notifications,
                title = "Push Notifications",
                subtitle = "Receive local on-time reminder and hydration notifications",
                checked = preferences.allNotificationsEnabled,
                onCheckedChange = onToggleAllNotifications,
                isDark = isDark
            )

            Spacer(modifier = Modifier.height(20.dp))

            // PRIVACY & OFFLINE PROMISE
            LiquidGlassSurface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                isDark = isDark
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "100% Offline & Private",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color(0xFF0F172A)
                        )
                        Text(
                            text = "CULU stores all your wellness logs locally on this device. No tracking, no cloud databases, no account needed.",
                            fontSize = 11.sp,
                            color = if (isDark) Color.White.copy(alpha = 0.65f) else Color(0xFF64748B)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Dismiss button
            LiquidGlassButton(
                onClick = onClose,
                isDark = isDark,
                isPrimary = true,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Done", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SettingToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isDark: Boolean
) {
    LiquidGlassSurface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        isDark = isDark
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (checked) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) Color.White else Color(0xFF0F172A)
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF64748B)
                    )
                }
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF0284C7)
                )
            )
        }
    }
}
