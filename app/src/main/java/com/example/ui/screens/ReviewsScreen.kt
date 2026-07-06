package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Review
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.GradientButton
import com.example.ui.theme.*
import com.example.ui.viewmodel.SkillHubViewModel
import com.example.util.LanguageTranslator

@Composable
fun ReviewsScreen(viewModel: SkillHubViewModel, isDark: Boolean) {
    val reviewsList by viewModel.reviews.collectAsState()
    val lang by viewModel.language.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    // Filter approved reviews for clients
    val approvedReviews = reviewsList.filter { it.isApproved }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = LanguageTranslator.translate("ratings", lang),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDark) Color.White else LightTextPrimary
                    )

                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Review", fontSize = 12.sp)
                    }
                }
            }

            // Stats Briefing Box
            item {
                ReviewStatsSummaryCard(approvedReviews, isDark)
            }

            if (approvedReviews.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No reviews submitted yet.", color = if (isDark) DarkTextSecondary else LightTextSecondary)
                    }
                }
            } else {
                items(approvedReviews) { review ->
                    ReviewRowCard(review, isDark)
                }
            }
        }

        // Add Review sliding Dialog
        AnimatedVisibility(
            visible = showAddDialog,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            AddReviewSheet(viewModel, lang, isDark) {
                showAddDialog = false
            }
        }
    }
}

@Composable
fun ReviewStatsSummaryCard(reviews: List<Review>, isDark: Boolean) {
    val average = if (reviews.isEmpty()) 5.0f else reviews.map { it.rating }.average().toFloat()

    GlassmorphicCard(isDark = isDark) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = String.format("%.1f", average),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ElectricBlue
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(5) { index ->
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (index < average.toInt()) WarningGold else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Based on ${reviews.size} verified ratings",
                    fontSize = 11.sp,
                    color = if (isDark) DarkTextSecondary else LightTextSecondary
                )
            }

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(SuccessGreen.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun ReviewRowCard(review: Review, isDark: Boolean) {
    GlassmorphicCard(isDark = isDark) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = review.authorName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else LightTextPrimary
                )

                Row {
                    repeat(5) { index ->
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (index < review.rating) WarningGold else Color.Gray.copy(alpha = 0.5f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = review.comment,
                fontSize = 13.sp,
                color = if (isDark) DarkTextSecondary else LightTextSecondary,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun AddReviewSheet(
    viewModel: SkillHubViewModel,
    lang: String,
    isDark: Boolean,
    onDismiss: () -> Unit
) {
    var author by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    var rating by remember { mutableIntStateOf(5) }

    val context = LocalContext.current
    val sheetBg = if (isDark) DarkSurface else LightSurface

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.65f)
            .background(sheetBg, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .border(1.dp, if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Submit Your Testimonial",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else LightTextPrimary
                )

                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, contentDescription = null)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = author,
                onValueChange = { author = it },
                label = { Text("Your Name", fontSize = 12.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Rating Selector Stars
            Text("Your Rating", fontSize = 12.sp, color = RoyalPurple, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Row {
                repeat(5) { index ->
                    val starIndex = index + 1
                    IconButton(
                        onClick = { rating = starIndex },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (starIndex <= rating) WarningGold else Color.Gray,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                label = { Text("Your Review / Comment", fontSize = 12.sp) },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            GradientButton(
                text = "Submit Review",
                onClick = {
                    if (author.trim().isEmpty() || comment.trim().isEmpty()) {
                        Toast.makeText(context, "Please specify your name and review details.", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.submitReview(rating, author, comment) {
                            Toast.makeText(context, "Review submitted! Awaiting administrator approval.", Toast.LENGTH_LONG).show()
                            onDismiss()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 12.dp
            )
        }
    }
}
