package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.ReminderLog
import com.example.data.preferences.UserPreferences
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassPill
import com.example.ui.components.LiquidGlassProgress
import com.example.ui.components.LiquidGlassSurface
import com.example.ui.viewmodel.DailyWaterStat
import com.example.ui.viewmodel.ReminderStats

@Composable
fun HistoryScreen(
    weeklyWaterStats: List<DailyWaterStat>,
    reminderStats: ReminderStats,
    todayLogs: List<ReminderLog>,
    preferences: UserPreferences,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val isCompact = preferences.compactModeEnabled

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = if (isCompact) 12.dp else 20.dp,
            bottom = 110.dp,
            start = if (isCompact) 14.dp else 20.dp,
            end = if (isCompact) 14.dp else 20.dp
        ),
        verticalArrangement = Arrangement.spacedBy(if (isCompact) 14.dp else 20.dp)
    ) {
        // Screen Title
        item {
            Column {
                Text(
                    text = "Wellness History",
                    fontSize = if (isCompact) 22.sp else 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color(0xFF0F172A)
                )
                Text(
                    text = "Weekly hydration rhythm & routine consistency",
                    fontSize = 13.sp,
                    color = if (isDark) Color.White.copy(alpha = 0.65f) else Color(0xFF64748B)
                )
            }
        }

        // WEEKLY HYDRATION BAR CHART
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                isDark = isDark,
                testTag = "weekly_water_card"
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WaterDrop,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "7-Day Water Intake",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) Color.White else Color(0xFF0F172A)
                            )
                        }

                        val weeklyAvg = if (weeklyWaterStats.isNotEmpty()) {
                            weeklyWaterStats.map { it.intakeMl }.average().toInt()
                        } else 0
                        Text(
                            text = "Avg: $weeklyAvg ml/day",
                            fontSize = 12.sp,
                            color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Minimal Liquid Glass Bar Visualizer
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        weeklyWaterStats.forEach { stat ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier.weight(1f)
                            ) {
                                // Amount label
                                if (stat.intakeMl > 0) {
                                    Text(
                                        text = "${(stat.intakeMl / 100f).toInt() / 10f}L",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF64748B),
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.height(16.dp))
                                }

                                // Pill Bar
                                Box(
                                    modifier = Modifier
                                        .width(if (isCompact) 20.dp else 26.dp)
                                        .height(80.dp)
                                        .clip(CircleShape)
                                        .background(if (isDark) Color(0xFF0C1929).copy(alpha = 0.5f) else Color(0xFFE2E8F0)),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    val barHeightPct = stat.percentage.coerceIn(0.05f, 1f)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height((80 * barHeightPct).dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color(0xFF38BDF8),
                                                        Color(0xFF0284C7)
                                                    )
                                                )
                                            )
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Day Label
                                Text(
                                    text = stat.dayLabel,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isDark) Color.White.copy(alpha = 0.8f) else Color(0xFF334155)
                                )
                            }
                        }
                    }
                }
            }
        }

        // ROUTINE COMPLETION STATS
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                isDark = isDark,
                testTag = "completion_stats_card"
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Radial Completion Ring
                    Box(
                        modifier = Modifier.size(if (isCompact) 100.dp else 115.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val animatedPct by animateFloatAsState(
                            targetValue = reminderStats.completionPercentage,
                            animationSpec = spring(dampingRatio = 0.8f, stiffness = 140f),
                            label = "ringPct"
                        )

                        Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                            val strokeWidth = 8.dp.toPx()
                            val arcRadius = (size.minDimension - strokeWidth) / 2f
                            val arcCenter = Offset(size.width / 2f, size.height / 2f)

                            // Track
                            drawCircle(
                                color = if (isDark) Color(0xFF0F223A) else Color(0xFFE2E8F0),
                                radius = arcRadius,
                                center = arcCenter,
                                style = Stroke(width = strokeWidth)
                            )

                            // Filled Arc
                            drawArc(
                                brush = Brush.sweepGradient(
                                    colors = listOf(
                                        Color(0xFF10B981),
                                        Color(0xFF38BDF8),
                                        Color(0xFF0284C7)
                                    )
                                ),
                                startAngle = -90f,
                                sweepAngle = (animatedPct * 360f).coerceIn(0f, 360f),
                                useCenter = false,
                                topLeft = Offset(arcCenter.x - arcRadius, arcCenter.y - arcRadius),
                                size = Size(arcRadius * 2, arcRadius * 2),
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${(reminderStats.completionPercentage * 100).toInt()}%",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color(0xFF0F172A)
                            )
                            Text(
                                text = "Completed",
                                fontSize = 10.sp,
                                color = if (isDark) Color.White.copy(alpha = 0.65f) else Color(0xFF64748B)
                            )
                        }
                    }

                    // Right: Breakdown Counts
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatRow(
                            icon = Icons.Default.CheckCircle,
                            iconColor = Color(0xFF10B981),
                            label = "Completed",
                            value = "${reminderStats.completedCount}",
                            isDark = isDark
                        )
                        StatRow(
                            icon = Icons.Default.RemoveCircleOutline,
                            iconColor = Color(0xFFF59E0B),
                            label = "Skipped",
                            value = "${reminderStats.skippedCount}",
                            isDark = isDark
                        )
                        StatRow(
                            icon = Icons.Default.HourglassEmpty,
                            iconColor = Color(0xFF38BDF8),
                            label = "Pending",
                            value = "${reminderStats.pendingCount}",
                            isDark = isDark
                        )
                    }
                }
            }
        }

        // RECENT LOG ENTRIES
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Activity Log",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color(0xFF0F172A)
                )
                Text(
                    text = "${todayLogs.size} logs",
                    fontSize = 12.sp,
                    color = if (isDark) Color.White.copy(alpha = 0.65f) else Color(0xFF64748B)
                )
            }
        }

        if (todayLogs.isEmpty()) {
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
                            text = "No routine actions recorded yet today",
                            fontSize = 13.sp,
                            color = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF64748B)
                        )
                    }
                }
            }
        } else {
            items(todayLogs) { log ->
                val isTaken = log.status == ReminderLog.STATUS_TAKEN
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
                            Icon(
                                imageVector = if (isTaken) Icons.Default.CheckCircle else Icons.Default.RemoveCircleOutline,
                                contentDescription = null,
                                tint = if (isTaken) Color(0xFF10B981) else Color(0xFFF59E0B),
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = log.reminderTitle,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isDark) Color.White else Color(0xFF0F172A)
                                )
                                Text(
                                    text = log.reminderType,
                                    fontSize = 11.sp,
                                    color = if (isDark) Color.White.copy(alpha = 0.55f) else Color(0xFF64748B)
                                )
                            }
                        }

                        LiquidGlassSurface(
                            shape = CircleShape,
                            isDark = isDark
                        ) {
                            Text(
                                text = if (isTaken) "Taken" else "Skipped",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isTaken) Color(0xFF10B981) else Color(0xFFF59E0B),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    label: String,
    value: String,
    isDark: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                fontSize = 13.sp,
                color = if (isDark) Color.White.copy(alpha = 0.75f) else Color(0xFF475569)
            )
        }

        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDark) Color.White else Color(0xFF0F172A)
        )
    }
}
