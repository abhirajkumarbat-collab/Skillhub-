package com.example.ui.components

import kotlinx.coroutines.delay
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    borderWidth: Dp = 1.dp,
    elevation: Dp = 8.dp,
    isDark: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val bgColor = if (isDark) DarkGlass else LightGlass
    val borderColor = if (isDark) {
        Color.White.copy(alpha = 0.12f)
    } else {
        Color.White.copy(alpha = 0.4f)
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(cornerRadius),
                clip = false
            )
            .background(
                color = bgColor,
                shape = RoundedCornerShape(cornerRadius)
            )
            .border(
                width = borderWidth,
                color = borderColor,
                shape = RoundedCornerShape(cornerRadius)
            )
            .clip(RoundedCornerShape(cornerRadius))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            content = content
        )
    }
}

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    enabled: Boolean = true
) {
    val gradient = Brush.linearGradient(
        colors = listOf(ElectricBlue, RoyalPurple, NeonCyan)
    )

    Box(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(cornerRadius))
            .background(if (enabled) gradient else Brush.linearGradient(listOf(Color.Gray, Color.LightGray)))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
fun AmbientBackground(
    isDark: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    // Elegant radial backgrounds representing neon particles and glows
    val infiniteTransition = rememberInfiniteTransition(label = "ambient")
    val animOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    val baseBg = if (isDark) DarkBg else LightBg

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseBg)
            .drawBehind {
                val radialBrush = Brush.radialGradient(
                    colors = if (isDark) {
                        listOf(RoyalPurple.copy(alpha = 0.15f), Color.Transparent)
                    } else {
                        listOf(ElectricBlue.copy(alpha = 0.1f), Color.Transparent)
                    },
                    center = Offset(size.width / 2f + animOffset / 2f - 250f, size.height / 3f),
                    radius = size.width * 0.8f
                )

                val bottomRadialBrush = Brush.radialGradient(
                    colors = if (isDark) {
                        listOf(ElectricBlue.copy(alpha = 0.12f), Color.Transparent)
                    } else {
                        listOf(RoyalPurple.copy(alpha = 0.08f), Color.Transparent)
                    },
                    center = Offset(size.width / 4f, size.height * 0.8f - animOffset / 4f),
                    radius = size.width * 0.9f
                )

                drawRect(radialBrush)
                drawRect(bottomRadialBrush)
            }
    ) {
        content()
    }
}

@Composable
fun TypingAnimationText(
    modifier: Modifier = Modifier,
    texts: List<String>,
    color: Color = ElectricBlue,
    fontSize: Dp = 20.dp
) {
    var textIndex by remember { mutableIntStateOf(0) }
    var charIndex by remember { mutableIntStateOf(0) }
    var isTyping by remember { mutableStateOf(true) }

    val activeText = texts.getOrNull(textIndex) ?: ""

    LaunchedEffect(textIndex, isTyping) {
        if (isTyping) {
            for (i in 0..activeText.length) {
                charIndex = i
                delay(70L)
            }
            delay(1500L) // Wait at full word
            isTyping = false
        } else {
            for (i in activeText.length downTo 0) {
                charIndex = i
                delay(40L)
            }
            delay(400L) // Short pause before next word
            textIndex = (textIndex + 1) % texts.size
            isTyping = true
        }
    }

    val displayedText = activeText.take(charIndex)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = displayedText,
            color = color,
            fontSize = with(LocalDensity.current) { fontSize.toSp() },
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.5.sp
        )

        // Animated cursor line
        val cursorTransition = rememberInfiniteTransition(label = "cursor")
        val alpha by cursorTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(400, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "cursor_alpha"
        )

        Box(
            modifier = Modifier
                .padding(start = 2.dp)
                .width(2.dp)
                .height(with(LocalDensity.current) { fontSize })
                .background(color.copy(alpha = alpha))
        )
    }
}
