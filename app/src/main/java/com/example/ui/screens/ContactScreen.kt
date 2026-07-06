package com.example.ui.screens

import java.util.UUID
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppSettings
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.GradientButton
import com.example.ui.theme.*
import com.example.ui.viewmodel.SkillHubViewModel
import com.example.util.LanguageTranslator

@Composable
fun ContactScreen(viewModel: SkillHubViewModel, isDark: Boolean) {
    val settings by viewModel.appSettings.collectAsState()
    val lang by viewModel.language.collectAsState()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedBudget by remember { mutableStateOf("$500 - $1,500") }
    var fileAttached by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val budgetOptions = listOf(
        "<$500", "$500 - $1,500", "$1,500 - $5,000", "$5,000+"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = LanguageTranslator.translate("contact_us", lang),
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isDark) Color.White else LightTextPrimary
            )
        }

        // Success dialog/card
        item {
            AnimatedVisibility(visible = showSuccess) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SuccessGreen.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                        .border(1.dp, SuccessGreen, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = LanguageTranslator.translate("success", lang),
                                color = SuccessGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = LanguageTranslator.translate("message_sent", lang),
                            color = if (isDark) Color.White else LightTextPrimary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Form Container Card
        item {
            GlassmorphicCard(isDark = isDark) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Tell Me About Your Project",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else LightTextPrimary
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(LanguageTranslator.translate("name", lang), fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(LanguageTranslator.translate("email", lang), fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text(LanguageTranslator.translate("phone", lang), fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text(LanguageTranslator.translate("project_desc", lang), fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Budget Row
                    Text(
                        text = LanguageTranslator.translate("budget", lang),
                        fontSize = 12.sp,
                        color = RoyalPurple,
                        fontWeight = FontWeight.Bold
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(budgetOptions) { budget ->
                            val isSelected = selectedBudget == budget
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = if (isSelected) ElectricBlue else if (isDark) DarkBg else LightBg,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedBudget = budget }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = budget,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else if (isDark) DarkTextPrimary else LightTextPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Attachment row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                fileAttached = true
                                Toast.makeText(context, "Blueprint attached!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (fileAttached) SuccessGreen else Color.Gray.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (fileAttached) "Attached" else LanguageTranslator.translate("attachment", lang),
                                fontSize = 11.sp
                            )
                        }

                        if (fileAttached) {
                            IconButton(onClick = { fileAttached = false }) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = ErrorRed)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    GradientButton(
                        text = LanguageTranslator.translate("submit", lang),
                        onClick = {
                            if (name.trim().isEmpty() || email.trim().isEmpty() || description.trim().isEmpty()) {
                                Toast.makeText(context, "Please fulfill all required fields.", Toast.LENGTH_SHORT).show()
                            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
                                Toast.makeText(context, "Please enter a valid email address.", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.sendContactMessage(
                                    name = name,
                                    email = email,
                                    phone = phone,
                                    description = description,
                                    budget = selectedBudget,
                                    attachmentUri = if (fileAttached) "attachments/brief_${UUID.randomUUID().toString().take(4)}.pdf" else ""
                                ) {
                                    showSuccess = true
                                    name = ""
                                    email = ""
                                    phone = ""
                                    description = ""
                                    fileAttached = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Direct Social Links section
        item {
            SocialNetworksWidget(settings, isDark)
        }
    }
}

@Composable
fun SocialNetworksWidget(settings: AppSettings, isDark: Boolean) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Direct Channels",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDark) Color.White else LightTextPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val activeSocials = mutableListOf<Pair<String, String>>()
        if (settings.showWhatsapp) activeSocials.add(Pair("WhatsApp", settings.whatsapp))
        if (settings.showTelegram) activeSocials.add(Pair("Telegram", settings.telegram))
        if (settings.showInstagram) activeSocials.add(Pair("Instagram", settings.instagram))
        if (settings.showGithub) activeSocials.add(Pair("GitHub", settings.github))
        if (settings.showYoutube) activeSocials.add(Pair("YouTube", settings.youtube))
        if (settings.showDiscord) activeSocials.add(Pair("Discord", settings.discord))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            activeSocials.chunked(2).forEach { rowSocials ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowSocials.forEach { social ->
                        val icon = when (social.first) {
                            "WhatsApp" -> Icons.Default.ChatBubble
                            "Telegram" -> Icons.Default.Send
                            "Instagram" -> Icons.Default.CameraAlt
                            "GitHub" -> Icons.Default.Code
                            "YouTube" -> Icons.Default.PlayCircle
                            else -> Icons.Default.Link
                        }

                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (isDark) DarkSurface else LightSurface, RoundedCornerShape(12.dp))
                                .border(1.dp, if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                .clickable {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(social.second))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Could not open URL: ${social.second}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(ElectricBlue.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = social.first,
                                    tint = ElectricBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = social.first,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else LightTextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
