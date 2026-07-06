package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Service
import com.example.data.model.Review
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.GradientButton
import com.example.ui.theme.*
import com.example.ui.viewmodel.SkillHubViewModel
import com.example.util.LanguageTranslator

@Composable
fun ServicesScreen(viewModel: SkillHubViewModel, isDark: Boolean) {
    val servicesList by viewModel.services.collectAsState()
    val reviewsList by viewModel.reviews.collectAsState()
    val lang by viewModel.language.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val visibleServices = servicesList.filter { !it.isHidden }
    val approvedReviews = reviewsList.filter { it.isApproved }

    // Dynamic list of categories from live services database
    val categories = remember(visibleServices) {
        val list = mutableListOf("All")
        val uniqueCats = visibleServices.map { it.category }.distinct().sorted()
        list.addAll(uniqueCats)
        list
    }

    // Filter services based on search query AND category selection
    val filteredServices = remember(searchQuery, selectedCategory, visibleServices) {
        visibleServices.filter { service ->
            val matchesSearch = service.title.contains(searchQuery, ignoreCase = true) ||
                    service.description.contains(searchQuery, ignoreCase = true) ||
                    service.category.contains(searchQuery, ignoreCase = true)
            
            val matchesCategory = selectedCategory == "All" || service.category == selectedCategory
            matchesSearch && matchesCategory
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header Title Block
        item {
            Column(modifier = Modifier.padding(bottom = 4.dp)) {
                Text(
                    text = LanguageTranslator.translate("services", lang),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDark) Color.White else LightTextPrimary
                )
                Text(
                    text = "Explore & book high-quality freelance solutions custom-crafted by Abhiraj Kumar.",
                    fontSize = 12.sp,
                    color = if (isDark) DarkTextSecondary else LightTextSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        // 2. Real-Time Search & Filtration Input
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("What are you looking for? (e.g. App, HTML, Video)", fontSize = 13.sp, color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon", tint = RoyalPurple) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = Color.Gray)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.12f),
                    focusedContainerColor = if (isDark) Color.White.copy(alpha = 0.02f) else Color.Black.copy(alpha = 0.02f)
                )
            )
        }

        // 3. Horizontal Category Chips Scroller
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 4.dp)
            ) {
                items(categories) { category ->
                    val isSelected = category == selectedCategory
                    val chipBg = if (isSelected) RoyalPurple else (if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f))
                    val chipTextColor = if (isSelected) Color.White else (if (isDark) Color.White.copy(alpha = 0.7f) else LightTextPrimary)
                    
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(chipBg)
                            .clickable { selectedCategory = category }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = category,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = chipTextColor
                        )
                    }
                }
            }
        }

        // 4. Horizontal Scrolling Testimonials Carousel
        if (approvedReviews.isNotEmpty()) {
            item {
                TestimonialsCarousel(reviews = approvedReviews, isDark = isDark)
            }
        }

        // 5. Service Catalog Component (List of dynamic filtered services)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Catalog Offerings (${filteredServices.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else LightTextPrimary
                )
                
                if (searchQuery.isNotEmpty() || selectedCategory != "All") {
                    Text(
                        text = "Clear Filter",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricBlue,
                        modifier = Modifier.clickable {
                            searchQuery = ""
                            selectedCategory = "All"
                        }
                    )
                }
            }
        }

        if (filteredServices.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Not found",
                            tint = Color.Gray,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "No matching services found for current filters.\nTry searching for something else!",
                            color = if (isDark) DarkTextSecondary else LightTextSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        } else {
            items(filteredServices) { service ->
                ServiceCatalogItemCard(service = service, viewModel = viewModel, lang = lang, isDark = isDark)
            }
        }
    }
}

@Composable
fun TestimonialsCarousel(reviews: List<Review>, isDark: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(bottom = 10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = WarningGold,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Client Testimonials & Feedback",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else LightTextPrimary
            )
            Box(
                modifier = Modifier
                    .background(SuccessGreen.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "Verified",
                    fontSize = 8.sp,
                    color = SuccessGreen,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(reviews) { review ->
                GlassmorphicCard(
                    isDark = isDark,
                    modifier = Modifier
                        .width(260.dp)
                        .padding(bottom = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = review.authorName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else LightTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Row {
                                repeat(5) { index ->
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = if (index < review.rating) WarningGold else Color.Gray.copy(alpha = 0.3f),
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = "\"${review.comment}\"",
                            fontSize = 11.sp,
                            color = if (isDark) DarkTextSecondary else LightTextSecondary,
                            lineHeight = 16.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ServiceCatalogItemCard(
    service: Service,
    viewModel: SkillHubViewModel,
    lang: String,
    isDark: Boolean
) {
    val icon = when (service.iconName.lowercase()) {
        "web" -> Icons.Default.Web
        "android" -> Icons.Default.Android
        "brush" -> Icons.Default.Brush
        "movie" -> Icons.Default.Movie
        "image" -> Icons.Default.Image
        "terminal" -> Icons.Default.Terminal
        "share" -> Icons.Default.Share
        "email" -> Icons.Default.Email
        "star" -> Icons.Default.Star
        "settings" -> Icons.Default.Settings
        "build" -> Icons.Default.Build
        else -> Icons.Default.Category
    }

    GlassmorphicCard(
        isDark = isDark,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            // Header row: Category Badge & Delivery Time estimate
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Badge Tag
                Box(
                    modifier = Modifier
                        .background(RoyalPurple.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = service.category.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = RoyalPurple,
                        letterSpacing = 0.5.sp
                    )
                }

                // Delivery Estimate
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "${LanguageTranslator.translate("delivery", lang)} ${service.deliveryTime}",
                        fontSize = 10.sp,
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Body Row: Icon, Title, and Starting Price Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Circular Modern Icon Badge
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.04f),
                            CircleShape
                        )
                        .border(1.dp, RoyalPurple.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = service.title,
                        tint = RoyalPurple,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = service.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else LightTextPrimary,
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        )

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Starting from",
                                fontSize = 8.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "$${String.format("%.2f", service.startingPrice)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ElectricBlue
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = service.description,
                        fontSize = 12.sp,
                        color = if (isDark) DarkTextSecondary else LightTextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Expandable FAQ Accordion Block
            var showFaqs by remember { mutableStateOf(false) }
            val faqs = remember(service) { getFAQsForService(service) }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (isDark) Color.White.copy(alpha = 0.02f) else Color.Black.copy(alpha = 0.02f),
                        RoundedCornerShape(10.dp)
                    )
                    .border(
                        1.dp,
                        if (showFaqs) RoyalPurple.copy(alpha = 0.2f) else Color.Transparent,
                        RoundedCornerShape(10.dp)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showFaqs = !showFaqs }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Quiz,
                            contentDescription = null,
                            tint = RoyalPurple,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Frequently Asked Questions",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White.copy(alpha = 0.8f) else LightTextPrimary.copy(alpha = 0.8f)
                        )
                    }
                    Icon(
                        imageVector = if (showFaqs) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle FAQs",
                        tint = RoyalPurple,
                        modifier = Modifier.size(16.dp)
                    )
                }

                if (showFaqs) {
                    Column(
                        modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HorizontalDivider(color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f))
                        faqs.forEach { (question, answer) ->
                            var isQuestionExpanded by remember { mutableStateOf(false) }
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isQuestionExpanded = !isQuestionExpanded }
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = question,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isDark) Color.White else LightTextPrimary,
                                        modifier = Modifier.weight(0.9f)
                                    )
                                    Icon(
                                        imageVector = if (isQuestionExpanded) Icons.Default.RemoveCircleOutline else Icons.Default.AddCircleOutline,
                                        contentDescription = null,
                                        tint = ElectricBlue,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                                if (isQuestionExpanded) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = answer,
                                        fontSize = 10.sp,
                                        color = if (isDark) DarkTextSecondary else LightTextSecondary,
                                        lineHeight = 14.sp,
                                        modifier = Modifier.padding(start = 4.dp, end = 12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Booking & Hire buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Secondary Chat inquiry button (Pre-fills chat request)
                OutlinedButton(
                    onClick = {
                        viewModel.sendChatMessage("Hi Abhiraj! I am interested in hiring you for: '${service.title}' (${service.category}). Can we discuss the exact budget and timeline?")
                        viewModel.navigateTo("CHATBOT")
                    },
                    modifier = Modifier.weight(0.4f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isDark) Color.White else LightTextPrimary
                    ),
                    border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.15f)),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Inquire", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Primary Instant Booking Button
                GradientButton(
                    text = LanguageTranslator.translate("hire_me", lang) + " Now",
                    onClick = {
                        // Triggers pre-populated inquiry on the Contact screen
                        viewModel.navigateTo("CONTACT")
                    },
                    modifier = Modifier.weight(0.6f)
                )
            }
        }
    }
}

fun getFAQsForService(service: Service): List<Pair<String, String>> {
    return when (service.category.lowercase()) {
        "web", "website" -> listOf(
            "Will my website be responsive?" to "Absolutely! Every website is designed using fluid modern layouts that render beautifully on mobile, tablet, and desktop screens.",
            "Which technology stack do you use?" to "We primarily build with Next.js, React, Tailwind CSS, and Node.js or Firebase for fast, secure, and SEO-optimized sites.",
            "Can I update the content myself later?" to "Yes, we integrate easy-to-use Headless CMS or admin portals, allowing you to update text and media without touch-editing code."
        )
        "android", "app" -> listOf(
            "Will I receive the complete source code?" to "Yes! Once development is completed, we hand over full GitHub repository access, code documentation, and APK/AAB outputs.",
            "Do you support offline local caching?" to "Yes, we implement offline-first capabilities using SQLite Room database, ensuring high performance even in low-connectivity areas.",
            "Can you publish my app to the Play Store?" to "Definitely! We coordinate the complete submission process, from listing optimization to generating signing keys and handling Google's review."
        )
        "video", "media", "editing" -> listOf(
            "What format will the final video be?" to "We deliver high-definition 4K or 1080p MP4 files ready to upload to YouTube, Instagram Reels, TikTok, or business portfolios.",
            "How do revisions work for editing?" to "We provide up to 3 rounds of review/revisions to adjust transitions, pacing, background tracks, and visual effects to perfection.",
            "Do you provide royalty-free audio tracks?" to "Yes, we curate premium licensed tracks or custom royalty-free sound designs so you won't face any copyright issues."
        )
        "design", "ui", "ux", "art" -> listOf(
            "What design files are delivered?" to "You will receive organized Figma source files (.fig), high-resolution PNG/SVG assets, and a fully defined UI design style guide.",
            "Do you follow material design standards?" to "Yes! We strictly follow Material Design 3 (M3) specifications to ensure modern, clean, and interactive styling standards.",
            "Can we test design concepts before coding?" to "Yes, we build clickable Figma prototypes so you can experience the interactive flows and test usability before writing a single line of code."
        )
        else -> listOf(
            "What are the payment milestones?" to "We typically work on a 50% upfront deposit to initiate the planning phase, with the remaining 50% paid upon your absolute 100% approval.",
            "How will we communicate during development?" to "We'll set up a dedicated communication channel (such as WhatsApp, Telegram, or email updates) with real-time progress screenshots.",
            "Do you offer post-delivery support?" to "Yes! Every project includes 30 days of free technical support and bug fixes to ensure your software or media operates flawlessly."
        )
    }
}
