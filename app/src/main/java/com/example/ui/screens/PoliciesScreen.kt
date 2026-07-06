package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassmorphicCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.SkillHubViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PoliciesScreen(viewModel: SkillHubViewModel, isDark: Boolean, initialTab: Int = 0) {
    var selectedTab by remember { mutableStateOf(initialTab) }
    val tabs = listOf(
        "Privacy Policy",
        "Terms & Conditions",
        "Refund Policy",
        "Disclaimer",
        "Cookies Policy",
        "License"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Screen Title Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateTo("HOME") }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = if (isDark) Color.White else LightTextPrimary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Policy,
                contentDescription = null,
                tint = RoyalPurple,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Legal Documents",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isDark) Color.White else LightTextPrimary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Horizontal scrolling policy tabs
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(tabs) { index, tabTitle ->
                val isSelected = selectedTab == index
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isSelected) RoyalPurple else if (isDark) DarkSurface else LightSurface,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { selectedTab = index }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = tabTitle,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else if (isDark) DarkTextPrimary else LightTextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Scrollable content pane for the selected legal document
        GlassmorphicCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            isDark = isDark
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    Text(
                        text = tabs[selectedTab],
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else LightTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Last Updated: July 6, 2026 • Version 1.2",
                        fontSize = 11.sp,
                        color = if (isDark) DarkTextSecondary.copy(alpha = 0.7f) else LightTextSecondary.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f))
                }

                item {
                    when (selectedTab) {
                        0 -> PrivacyPolicyContent(isDark)
                        1 -> TermsAndConditionsContent(isDark)
                        2 -> RefundPolicyContent(isDark)
                        3 -> DisclaimerContent(isDark)
                        4 -> CookiesPolicyContent(isDark)
                        5 -> LicenseContent(isDark)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun PrivacyPolicyContent(isDark: Boolean) {
    val textColor = if (isDark) DarkTextPrimary else LightTextPrimary
    val secColor = if (isDark) DarkTextSecondary else LightTextSecondary

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "1. Information We Collect",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        Text(
            text = "At SkillHub, we collect name, email address, phone number, payment details, and project briefings when you request digital products or web/Android consultancy. Additionally, automated technical information (device specifications, approximate IP-based location, and browser information) is captured for security logs.",
            fontSize = 12.sp,
            color = secColor,
            lineHeight = 18.sp
        )

        Text(
            text = "2. How We Use Information",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        Text(
            text = "We process user data to deliver digital deliverables (e.g. Kotlin source files, premium templates, guidebooks), process payment verification requests, communicate custom development quotes, and provide context to our intelligent SkillHub Assistant chatbot. We do not sell or trade user data to third-party advertizers.",
            fontSize = 12.sp,
            color = secColor,
            lineHeight = 18.sp
        )

        Text(
            text = "3. Cookies and Tracking",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        Text(
            text = "Our platform utilizes tiny local storage files and session variables to persist user preferences like UI language, chosen theme state, shopping cart selections, and past support chat history.",
            fontSize = 12.sp,
            color = secColor,
            lineHeight = 18.sp
        )

        Text(
            text = "4. Security",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        Text(
            text = "All database operations are fully encrypted locally on SQLite via Room DB. Online synchronizations are protected via secure SSL channels. Admin actions require dual enterprise login verification to secure records.",
            fontSize = 12.sp,
            color = secColor,
            lineHeight = 18.sp
        )
    }
}

@Composable
fun TermsAndConditionsContent(isDark: Boolean) {
    val textColor = if (isDark) DarkTextPrimary else LightTextPrimary
    val secColor = if (isDark) DarkTextSecondary else LightTextSecondary

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "1. Intellectual Property Rights",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        Text(
            text = "Unless explicitly stated under standard multi-license terms, all software modules, UI designs, code blueprints, and literary guidebooks listed on SkillHub are proprietary properties of Abhiraj Kumar.",
            fontSize = 12.sp,
            color = secColor,
            lineHeight = 18.sp
        )

        Text(
            text = "2. User Obligations",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        Text(
            text = "Users agree to provide precise billing credentials (name, email, and authentic transaction hashes). Fraudulent payment submittals, screen spoofing, or attempts to abuse the chatbot system will result in instant account ban and digital access revoking.",
            fontSize = 12.sp,
            color = secColor,
            lineHeight = 18.sp
        )

        Text(
            text = "3. Digital Downloads",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        Text(
            text = "Purchased items are unlocked instantly upon payment verification. You are granted a non-exclusive, non-transferable single developer license. Distribution or reselling of source bundles is strictly prohibited.",
            fontSize = 12.sp,
            color = secColor,
            lineHeight = 18.sp
        )
    }
}

@Composable
fun RefundPolicyContent(isDark: Boolean) {
    val textColor = if (isDark) DarkTextPrimary else LightTextPrimary
    val secColor = if (isDark) DarkTextSecondary else LightTextSecondary

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "1. No Automatic Refunds on Digital Products",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        Text(
            text = "Since digital products (e.g. source boilerplates, guides, template bundles) are non-tangible, digital, and irrevocable assets that are immediately unlocked upon manual admin verification, all purchases are final. No refunds will be provided after delivery.",
            fontSize = 12.sp,
            color = secColor,
            lineHeight = 18.sp
        )

        Text(
            text = "2. Consultancy Cancellation",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        Text(
            text = "For custom Android/Web Development services, clients can request a full refund within 24 hours of booking, provided project roadmap development has not commenced. After development starts, refunds are prorated based on milestones completed.",
            fontSize = 12.sp,
            color = secColor,
            lineHeight = 18.sp
        )
    }
}

@Composable
fun DisclaimerContent(isDark: Boolean) {
    val textColor = if (isDark) DarkTextPrimary else LightTextPrimary
    val secColor = if (isDark) DarkTextSecondary else LightTextSecondary

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "1. 'As Is' Basis",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        Text(
            text = "The resources, source codes, guides, and templates provided on SkillHub are distributed on an 'AS IS' and 'AS AVAILABLE' basis, without warranties of any kind, either express or implied, including performance stability or compilation guarantees on non-standard SDKs.",
            fontSize = 12.sp,
            color = secColor,
            lineHeight = 18.sp
        )

        Text(
            text = "2. AI Chatbot Information Disclaimer",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        Text(
            text = "The interactive SkillHub Chatbot is powered by Google Gemini API. While highly optimized, it may occasionally provide outdated specifications, price approximations, or minor project timeline hallucinations. Please verify official pricing and timeline specifics with Abhiraj directly via email.",
            fontSize = 12.sp,
            color = secColor,
            lineHeight = 18.sp
        )
    }
}

@Composable
fun CookiesPolicyContent(isDark: Boolean) {
    val textColor = if (isDark) DarkTextPrimary else LightTextPrimary
    val secColor = if (isDark) DarkTextSecondary else LightTextSecondary

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "1. What are Cookies?",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        Text(
            text = "Cookies and equivalent client-side persistent variables are tiny units of data saved in your local database storage (e.g. SQLite Room schemas) to memorize your preferred setup variables across app relaunches.",
            fontSize = 12.sp,
            color = secColor,
            lineHeight = 18.sp
        )

        Text(
            text = "2. Crucial Preference Storage",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        Text(
            text = "We use local markers strictly to manage your current localized language ('en', 'hi', 'hinglish'), dark theme visual status, item wishlist, active order tokens, and support chat records so you never lose context.",
            fontSize = 12.sp,
            color = secColor,
            lineHeight = 18.sp
        )
    }
}

@Composable
fun LicenseContent(isDark: Boolean) {
    val textColor = if (isDark) DarkTextPrimary else LightTextPrimary
    val secColor = if (isDark) DarkTextSecondary else LightTextSecondary

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "1. Single Developer Commercial License",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        Text(
            text = "This license grants you a non-transferable, non-exclusive permission to run, modify, compile, and embed SkillHub products in your or your clients' final commercial products. Redistribution of standalone resource files, ZIP bundles, or template packages is prohibited.",
            fontSize = 12.sp,
            color = secColor,
            lineHeight = 18.sp
        )

        Text(
            text = "2. Open Source Licenses Used",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        Text(
            text = "SkillHub incorporates open source libraries including Jetpack Compose (Apache 2.0), Room Persistence Library (Apache 2.0), OkHttp (Apache 2.0), Moshi Json (Apache 2.0), and Google Firebase SDKs. Please respect their respective individual licensing credits.",
            fontSize = 12.sp,
            color = secColor,
            lineHeight = 18.sp
        )
    }
}
