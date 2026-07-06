package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.ui.components.GlassmorphicCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.SkillHubViewModel
import com.example.util.LanguageTranslator
import kotlinx.coroutines.launch

@Composable
fun ChatbotScreen(viewModel: SkillHubViewModel, isDark: Boolean) {
    val chatList by viewModel.chatHistory.collectAsState()
    val isTyping by viewModel.isChatbotTyping.collectAsState()
    val lang by viewModel.language.collectAsState()

    var typedMessage by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Automatically scroll down when new message is added
    LaunchedEffect(chatList.size, isTyping) {
        if (chatList.isNotEmpty()) {
            listState.animateScrollToItem(chatList.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Chat Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = LanguageTranslator.translate("chatbot", lang),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDark) Color.White else LightTextPrimary
                )
                Text(
                    text = "Powered by Google Gemini",
                    fontSize = 11.sp,
                    color = ElectricBlue,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(onClick = { viewModel.clearChatHistory() }) {
                Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Chat History", tint = ErrorRed)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Chat Bubble Pane
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (chatList.isEmpty()) {
                // Empty Chat Greeting Card
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    GlassmorphicCard(
                        isDark = isDark,
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(RoyalPurple.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = RoyalPurple, modifier = Modifier.size(28.dp))
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Ask Abhiraj's Assistant!",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else LightTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Get answers about Abhiraj's services, digital products, pricing guides, portfolio experience, and standard order tracking instantly.",
                                fontSize = 12.sp,
                                color = if (isDark) DarkTextSecondary else LightTextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(chatList) { chat ->
                        ChatBubbleRow(chat, isDark)
                    }

                    if (isTyping) {
                        item {
                            TypingIndicatorRow(isDark)
                        }
                    }
                }
            }
        }

        // Input text bar panel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = typedMessage,
                onValueChange = { typedMessage = it },
                placeholder = { Text(LanguageTranslator.translate("chatbot_placeholder", lang), fontSize = 12.sp) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f)
                ),
                singleLine = true
            )

            FloatingActionButton(
                onClick = {
                    if (typedMessage.trim().isNotEmpty()) {
                        viewModel.sendChatMessage(typedMessage)
                        typedMessage = ""
                    }
                },
                containerColor = RoyalPurple,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send Message", modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun ChatBubbleRow(chat: ChatMessage, isDark: Boolean) {
    val isUser = chat.sender == "USER"
    val bubbleColor = if (isUser) {
        ElectricBlue.copy(alpha = 0.85f)
    } else {
        if (isDark) DarkSurface else LightSurface
    }
    val align = if (isUser) Alignment.End else Alignment.Start
    val shape = if (isUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = align
    ) {
        Box(
            modifier = Modifier
                .background(bubbleColor, shape)
                .border(1.dp, if (isUser) Color.Transparent else if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f), shape)
                .widthIn(max = 280.dp)
                .padding(12.dp)
        ) {
            Text(
                text = chat.message,
                fontSize = 13.sp,
                color = if (isUser) Color.White else if (isDark) Color.White else LightTextPrimary,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun TypingIndicatorRow(isDark: Boolean) {
    Row(
        modifier = Modifier
            .background(if (isDark) DarkSurface else LightSurface, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = RoyalPurple, modifier = Modifier.size(14.dp))
        Text(
            text = "Gemini is typing...",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = RoyalPurple
        )
    }
}
