package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.graphics.liquidGlass

@Composable
fun ProfileEditDialog(
    currentName: String,
    currentAge: Int,
    currentGender: String,
    currentPhotoUri: String,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onSaveProfile: (name: String, age: Int, gender: String, photoUri: String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var ageText by remember { mutableStateOf(currentAge.toString()) }
    var gender by remember { mutableStateOf(if (currentGender.equals("Female", ignoreCase = true)) "Female" else "Male") }
    var photoUriString by remember { mutableStateOf(currentPhotoUri) }

    // Android Photo Picker Launcher (Zero-permission & Play Store compliant)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            photoUriString = it.toString()
        }
    }

    // Fallback Gallery Launcher
    val fallbackGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            photoUriString = it.toString()
        }
    }

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
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = "User Profile",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF0F172A)
                    )
                    Text(
                        text = "Customize name, age, gender & profile photo",
                        fontSize = 11.sp,
                        color = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Profile Photo Avatar with Edit Badge
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .liquidGlass(
                        shape = CircleShape,
                        isDark = isDark,
                        interactivePress = true,
                        onClick = {
                            try {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            } catch (e: Exception) {
                                fallbackGalleryLauncher.launch("image/*")
                            }
                        }
                    )
                    .testTag("profile_avatar_picker"),
                contentAlignment = Alignment.Center
            ) {
                if (photoUriString.isNotBlank()) {
                    AsyncImage(
                        model = photoUriString,
                        contentDescription = "Profile Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                } else {
                    Icon(
                        imageVector = if (gender == "Female") Icons.Default.Female else Icons.Default.Male,
                        contentDescription = "Avatar Placeholder",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(44.dp)
                    )
                }

                // Camera Badge overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0284C7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Change Photo",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Add/Change Photo Button label
            LiquidGlassPill(
                isDark = isDark,
                onClick = {
                    try {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    } catch (e: Exception) {
                        fallbackGalleryLauncher.launch("image/*")
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.AddAPhoto,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (photoUriString.isBlank()) "Choose Photo from Gallery" else "Change Gallery Photo",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF38BDF8)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Full Name Input
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Profile Name") },
                placeholder = { Text("e.g., Alex Morgan") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF38BDF8),
                    unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.2f) else Color(0xFFCBD5E1),
                    focusedLabelColor = Color(0xFF38BDF8),
                    unfocusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
                    focusedTextColor = if (isDark) Color.White else Color(0xFF0F172A)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_name_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Age Input & Stepper
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = ageText,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.all { it.isDigit() }) {
                            ageText = input
                        }
                    },
                    label = { Text("Age (Years)") },
                    placeholder = { Text("25") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.2f) else Color(0xFFCBD5E1),
                        focusedLabelColor = Color(0xFF38BDF8),
                        unfocusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
                        focusedTextColor = if (isDark) Color.White else Color(0xFF0F172A)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("profile_age_input")
                )

                // Quick age stepper buttons (-1, +1)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    LiquidGlassPill(
                        isDark = isDark,
                        onClick = {
                            val current = ageText.toIntOrNull() ?: 25
                            if (current > 1) {
                                ageText = (current - 1).toString()
                            }
                        }
                    ) {
                        Text("-1", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    LiquidGlassPill(
                        isDark = isDark,
                        onClick = {
                            val current = ageText.toIntOrNull() ?: 25
                            if (current < 120) {
                                ageText = (current + 1).toString()
                            }
                        }
                    ) {
                        Text("+1", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Gender Selection (Male / Female ONLY as requested)
            Text(
                text = "Gender",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) Color.White.copy(alpha = 0.85f) else Color(0xFF334155),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Male Chip
                LiquidGlassChip(
                    selected = gender == "Male",
                    onClick = { gender = "Male" },
                    label = "Male 👨",
                    icon = Icons.Default.Male,
                    isDark = isDark,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("gender_male_chip")
                )

                // Female Chip
                LiquidGlassChip(
                    selected = gender == "Female",
                    onClick = { gender = "Female" },
                    label = "Female 👩",
                    icon = Icons.Default.Female,
                    isDark = isDark,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("gender_female_chip")
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

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
                        val parsedAge = ageText.toIntOrNull()?.coerceIn(1, 120) ?: 25
                        val finalName = if (name.isNotBlank()) name.trim() else "User"
                        onSaveProfile(finalName, parsedAge, gender, photoUriString)
                        onDismiss()
                    },
                    isDark = isDark,
                    isPrimary = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("save_profile_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Profile", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
