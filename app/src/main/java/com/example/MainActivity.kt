package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.data.local.SkillHubDatabase
import com.example.data.repository.SkillHubRepository
import com.example.ui.components.AmbientBackground
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.GradientButton
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.SkillHubViewModel
import com.example.util.LanguageTranslator

class MainActivity : ComponentActivity() {
    private lateinit var database: SkillHubDatabase
    private lateinit var repository: SkillHubRepository
    private lateinit var viewModel: SkillHubViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Initialize local SQLite Room database
        database = Room.databaseBuilder(
            applicationContext,
            SkillHubDatabase::class.java,
            "skillhub_db"
        ).fallbackToDestructiveMigration().build()

        // 2. Initialize unified repository manager
        repository = SkillHubRepository(
            context = applicationContext,
            serviceDao = database.serviceDao(),
            productDao = database.productDao(),
            orderDao = database.orderDao(),
            messageDao = database.messageDao(),
            reviewDao = database.reviewDao(),
            portfolioDao = database.portfolioDao(),
            settingsDao = database.settingsDao(),
            chatDao = database.chatDao()
        )

        // 3. Setup standard architecture ViewModel Factory
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(SkillHubViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return SkillHubViewModel(repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
        viewModel = ViewModelProvider(this, factory)[SkillHubViewModel::class.java]

        // 4. Render main application views
        setContent {
            val themeOverride by viewModel.themeOverride.collectAsState()
            val isDark = themeOverride ?: isSystemInDarkTheme()

            MyApplicationTheme(darkTheme = isDark) {
                MainAppShell(viewModel, isDark)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppShell(viewModel: SkillHubViewModel, isDark: Boolean) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val appSettings by viewModel.appSettings.collectAsState()
    val lang by viewModel.language.collectAsState()

    var showLanguageMenu by remember { mutableStateOf(false) }

    // Maintenance Mode Switch
    if (appSettings.maintenanceMode && currentScreen != "ADMIN_LOGIN" && currentScreen != "ADMIN_DASHBOARD") {
        MaintenanceScreen(viewModel, isDark)
    } else {
        AmbientBackground(isDark = isDark) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent,
                topBar = {
                    // Transparent Blurred Header
                    TopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.clickable { viewModel.navigateTo("HOME") }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(
                                            Brush.linearGradient(listOf(ElectricBlue, RoyalPurple)),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AllInclusive,
                                        contentDescription = "SkillHub Logo",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    text = appSettings.websiteName,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isDark) Color.White else LightTextPrimary
                                )
                            }
                        },
                        actions = {
                            // Language Selector Dialog Trigger
                            IconButton(onClick = { showLanguageMenu = !showLanguageMenu }) {
                                Icon(
                                    imageVector = Icons.Default.Translate,
                                    contentDescription = "Language",
                                    tint = if (isDark) Color.White else LightTextPrimary
                                )
                            }

                            // Theme Toggle Button
                            IconButton(onClick = { viewModel.toggleTheme() }) {
                                Icon(
                                    imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = "Toggle Theme",
                                    tint = if (isDark) Color.White else LightTextPrimary
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                },
                bottomBar = {
                    // Sticky Rounded Navigation bar (Omit if inside Admin panels)
                    if (currentScreen != "ADMIN_LOGIN" && currentScreen != "ADMIN_DASHBOARD") {
                        SkillHubBottomNav(currentScreen, viewModel, isDark)
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (currentScreen) {
                        "HOME" -> HomeScreen(viewModel, isDark)
                        "SERVICES" -> ServicesScreen(viewModel, isDark)
                        "PORTFOLIO" -> PortfolioScreen(viewModel, isDark)
                        "STORE" -> StoreScreen(viewModel, isDark)
                        "REVIEWS" -> ReviewsScreen(viewModel, isDark)
                        "CONTACT" -> ContactScreen(viewModel, isDark)
                        "CHATBOT" -> ChatbotScreen(viewModel, isDark)
                        "ADMIN_LOGIN" -> AdminPanel(viewModel, isDark)
                        "ADMIN_DASHBOARD" -> AdminPanel(viewModel, isDark)
                        "POLICIES" -> PoliciesScreen(viewModel, isDark)
                        "SEARCH" -> SearchScreen(viewModel, isDark)
                        "NOTIFICATIONS" -> NotificationCenterScreen(viewModel, isDark)
                        "ACTIVITY_HISTORY" -> ActivityHistoryScreen(viewModel, isDark)
                        "ERROR" -> ErrorScreen(viewModel, isDark)
                    }
                }
            }

            // Simple language switcher overlay
            if (showLanguageMenu) {
                LanguagePickerDialog(viewModel, lang, isDark) {
                    showLanguageMenu = false
                }
            }
        }
    }
}

@Composable
fun SkillHubBottomNav(
    currentScreen: String,
    viewModel: SkillHubViewModel,
    isDark: Boolean
) {
    val navColor = if (isDark) DarkSurface else LightSurface
    val borderColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .background(navColor, RoundedCornerShape(24.dp))
            .border(1.dp, borderColor, RoundedCornerShape(24.dp))
            .padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val navItems = listOf(
                Pair("HOME", Icons.Default.Home),
                Pair("SERVICES", Icons.Default.Category),
                Pair("PORTFOLIO", Icons.Default.Dashboard),
                Pair("STORE", Icons.Default.ShoppingBag),
                Pair("CHATBOT", Icons.Default.AutoAwesome),
                Pair("CONTACT", Icons.Default.AlternateEmail)
            )

            navItems.forEach { item ->
                val isSelected = currentScreen == item.first
                val tint = if (isSelected) RoyalPurple else if (isDark) Color.Gray else Color.LightGray

                IconButton(
                    onClick = { viewModel.navigateTo(item.first) },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = item.second,
                        contentDescription = item.first,
                        tint = tint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun LanguagePickerDialog(
    viewModel: SkillHubViewModel,
    currentLang: String,
    isDark: Boolean,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (isDark) DarkSurface else LightSurface, RoundedCornerShape(16.dp))
                .border(1.dp, if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Change System Language",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                val languages = listOf(
                    Pair("en", "English"),
                    Pair("hi", "हिंदी (Hindi)"),
                    Pair("hinglish", "Hinglish (Mix)")
                )

                languages.forEach { langPair ->
                    val isSelected = currentLang == langPair.first
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isSelected) RoyalPurple.copy(alpha = 0.15f) else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                viewModel.setLanguage(langPair.first)
                                onDismiss()
                            }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = langPair.second,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) RoyalPurple else if (isDark) Color.White else LightTextPrimary
                        )

                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = RoyalPurple)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MaintenanceScreen(viewModel: SkillHubViewModel, isDark: Boolean) {
    AmbientBackground(isDark = isDark) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            GlassmorphicCard(isDark = isDark) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(WarningGold.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Construction,
                            contentDescription = null,
                            tint = WarningGold,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Under Maintenance",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else LightTextPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "We are currently implementing fresh dynamic upgrades to Abhiraj's digital portfolio. Back shortly!",
                        fontSize = 12.sp,
                        color = if (isDark) DarkTextSecondary else LightTextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    GradientButton(
                        text = "Admin Bypass",
                        onClick = { viewModel.navigateTo("ADMIN_LOGIN") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
