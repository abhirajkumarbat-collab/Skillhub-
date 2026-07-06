package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AppSettings
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.GradientButton
import com.example.ui.components.TypingAnimationText
import com.example.ui.theme.*
import com.example.ui.viewmodel.SkillHubViewModel
import com.example.util.LanguageTranslator

@Composable
fun HomeScreen(viewModel: SkillHubViewModel, isDark: Boolean) {
    val settings by viewModel.appSettings.collectAsState()
    val lang by viewModel.language.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Hero & Profile Intro
        item {
            Spacer(modifier = Modifier.height(16.dp))
            // Quick Navigation Toolbar Row
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Welcome to SkillHub",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) DarkTextSecondary else LightTextSecondary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = { viewModel.navigateTo("SEARCH") }) {
                        Icon(Icons.Default.Search, contentDescription = "Universal Search", tint = RoyalPurple)
                    }
                    IconButton(onClick = { viewModel.navigateTo("NOTIFICATIONS") }) {
                        val notifications by viewModel.notifications.collectAsState()
                        val unreadCount = notifications.filter { !it.isRead }.size
                        BadgedBox(
                            badge = {
                                if (unreadCount > 0) {
                                    Badge(containerColor = ErrorRed) {
                                        Text(unreadCount.toString(), color = Color.White, fontSize = 9.sp)
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications Center", tint = RoyalPurple)
                        }
                    }
                    IconButton(onClick = { viewModel.navigateTo("ACTIVITY_HISTORY") }) {
                        Icon(Icons.Default.History, contentDescription = "Activity History", tint = RoyalPurple)
                    }
                }
            }
            HeroSection(viewModel, settings, lang, isDark)
        }

        // Stats Section
        item {
            StatsSection(viewModel, lang, isDark)
        }

        // Skills Grid Section
        item {
            SkillsSection(lang, isDark)
        }

        // FAQ Section
        item {
            FaqSection(lang, isDark)
        }

        // Professional Footer Section
        item {
            FooterSection(viewModel, lang, settings, isDark)
        }
    }
}

@Composable
fun HeroSection(
    viewModel: SkillHubViewModel,
    settings: AppSettings,
    lang: String,
    isDark: Boolean
) {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        isDark = isDark,
        cornerRadius = 24.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            // Profile Image Frame
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.sweepGradient(
                            listOf(ElectricBlue, RoyalPurple, NeonCyan, ElectricBlue)
                        )
                    )
                    .padding(4.dp)
            ) {
                // Secure lookup for profile photo
                val context = LocalContext.current
                val resId = context.resources.getIdentifier(
                    "profile_abhiraj_kumar", "drawable", context.packageName
                )
                if (resId != 0) {
                    Image(
                        painter = painterResource(id = resId),
                        contentDescription = "Abhiraj Kumar Profile",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Fallback visual avatar
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(DarkSurface, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "AK",
                            color = ElectricBlue,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Author Badge
            Box(
                modifier = Modifier
                    .background(ElectricBlue.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = settings.ownerName,
                    color = ElectricBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Multilingual Hero Title
            Text(
                text = LanguageTranslator.translate("hero_title", lang),
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isDark) Color.White else LightTextPrimary,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Custom Dynamic Typing Text representing key services
            TypingAnimationText(
                texts = listOf(
                    "Software Architect",
                    "Native Android Developer",
                    "AI Automation Architect",
                    "Full Stack Engineer"
                ),
                color = RoyalPurple,
                fontSize = 18.dp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Hero Description
            Text(
                text = LanguageTranslator.translate("hero_desc", lang),
                fontSize = 14.sp,
                color = if (isDark) DarkTextSecondary else LightTextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GradientButton(
                    text = LanguageTranslator.translate("hire_me", lang),
                    onClick = { viewModel.navigateTo("CONTACT") },
                    modifier = Modifier.weight(1f)
                )

                OutlinedButton(
                    onClick = { viewModel.navigateTo("SERVICES") },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(24.dp),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                        brush = Brush.linearGradient(listOf(ElectricBlue, RoyalPurple))
                    )
                ) {
                    Text(
                        text = LanguageTranslator.translate("services", lang),
                        color = if (isDark) Color.White else LightTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun StatsSection(viewModel: SkillHubViewModel, lang: String, isDark: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val textColor = if (isDark) Color.White else LightTextPrimary
        val subColor = if (isDark) DarkTextSecondary else LightTextSecondary

        // Stat Card 1
        Box(
            modifier = Modifier
                .weight(1f)
                .background(if (isDark) DarkSurface else LightSurface, RoundedCornerShape(16.dp))
                .border(1.dp, if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("250+", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = ElectricBlue)
                Spacer(modifier = Modifier.height(4.dp))
                Text(LanguageTranslator.translate("active_orders", lang), fontSize = 11.sp, color = subColor, textAlign = TextAlign.Center)
            }
        }

        // Stat Card 2
        Box(
            modifier = Modifier
                .weight(1f)
                .background(if (isDark) DarkSurface else LightSurface, RoundedCornerShape(16.dp))
                .border(1.dp, if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("5.0 ★", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = RoyalPurple)
                Spacer(modifier = Modifier.height(4.dp))
                Text(LanguageTranslator.translate("rating", lang), fontSize = 11.sp, color = subColor, textAlign = TextAlign.Center)
            }
        }

        // Stat Card 3
        Box(
            modifier = Modifier
                .weight(1f)
                .background(if (isDark) DarkSurface else LightSurface, RoundedCornerShape(16.dp))
                .border(1.dp, if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("8+ Yrs", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Architect", fontSize = 11.sp, color = subColor, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun SkillsSection(lang: String, isDark: Boolean) {
    Column {
        Text(
            text = LanguageTranslator.translate("skills", lang),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDark) Color.White else LightTextPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        val skills = listOf(
            Pair("Kotlin & Compose", Icons.Default.Android),
            Pair("Firebase Firestore", Icons.Default.Storage),
            Pair("LLM Integration", Icons.Default.Memory),
            Pair("Next.js & React", Icons.Default.Web),
            Pair("Node.js Backend", Icons.Default.Terminal),
            Pair("UI/UX Glassmorphism", Icons.Default.Palette)
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            skills.chunked(2).forEach { rowSkills ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowSkills.forEach { skill ->
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (isDark) DarkSurface else LightSurface, RoundedCornerShape(12.dp))
                                .border(1.dp, if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = skill.second,
                                contentDescription = null,
                                tint = RoyalPurple,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = skill.first,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) Color.White else LightTextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FaqSection(lang: String, isDark: Boolean) {
    val faqItems = listOf(
        Pair("How does digital delivery work?", "As soon as your payment is submitted and manually approved, the download button will automatically unlock on your dashboard order details. You can download multiple ZIP or PDF templates directly inside the app!"),
        Pair("What payment models do you accept?", "We accept instant, zero-commission payments via standard UPI, GPay, Paytm, and direct bank transfers. Simply scan the generated custom QR codes and upload your transaction ID."),
        Pair("Can I request custom revisions?", "Absolutely! Every service card clearly details the delivery time and starting budget. Custom requests can be fully tailored and mapped through the contact screen form.")
    )

    Column {
        Text(
            text = LanguageTranslator.translate("faq", lang),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDark) Color.White else LightTextPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        faqItems.forEach { faq ->
            var expanded by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .background(if (isDark) DarkSurface else LightSurface, RoundedCornerShape(12.dp))
                    .clickable { expanded = !expanded }
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = faq.first,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else LightTextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = ElectricBlue
                        )
                    }

                    AnimatedVisibility(visible = expanded) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = faq.second,
                            fontSize = 13.sp,
                            color = if (isDark) DarkTextSecondary else LightTextSecondary,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FooterSection(
    viewModel: SkillHubViewModel,
    lang: String,
    settings: AppSettings,
    isDark: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Divider(color = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f))
        Spacer(modifier = Modifier.height(16.dp))

        // Quick Admin Login action
        Text(
            text = LanguageTranslator.translate("admin", lang),
            color = RoyalPurple,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clickable { viewModel.navigateTo("ADMIN_LOGIN") }
                .padding(8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Terms of Service | Privacy Policy | Refund Policy",
            color = if (isDark) DarkTextSecondary else LightTextSecondary,
            fontSize = 11.sp,
            modifier = Modifier
                .clickable { viewModel.navigateTo("POLICIES") }
                .padding(bottom = 8.dp)
        )

        Text(
            text = settings.footerText,
            color = if (isDark) DarkTextSecondary.copy(alpha = 0.7f) else LightTextSecondary.copy(alpha = 0.7f),
            fontSize = 10.sp,
            textAlign = TextAlign.Center
        )
    }
}
