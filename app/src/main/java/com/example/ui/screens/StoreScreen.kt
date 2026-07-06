package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import java.util.UUID
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.AppSettings
import com.example.data.model.Product
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.GradientButton
import com.example.ui.theme.*
import com.example.ui.viewmodel.SkillHubViewModel
import com.example.util.LanguageTranslator

@Composable
fun StoreScreen(viewModel: SkillHubViewModel, isDark: Boolean) {
    val productsList by viewModel.products.collectAsState()
    val wishlistSet by viewModel.wishlist.collectAsState()
    val searchVal by viewModel.storeSearchQuery.collectAsState()
    val categoryVal by viewModel.selectedStoreCategory.collectAsState()
    val sortVal by viewModel.storeSortOption.collectAsState()
    val checkoutVal by viewModel.checkoutProduct.collectAsState()
    val appSettings by viewModel.appSettings.collectAsState()
    val lang by viewModel.language.collectAsState()

    val context = LocalContext.current
    var storeTab by remember { mutableStateOf("SHOP") } // SHOP, TRACKER

    // Category list extraction
    val categories = listOf("All") + productsList.map { it.category }.distinct()

    // Filtering & Sorting logic
    val filteredProducts = productsList.filter { product ->
        val matchesSearch = product.title.contains(searchVal, ignoreCase = true) ||
                product.description.contains(searchVal, ignoreCase = true)
        val matchesCategory = categoryVal == "All" || product.category == categoryVal
        matchesSearch && matchesCategory && !product.isHidden
    }.sortedWith { a, b ->
        when (sortVal) {
            "PRICE_LOW" -> a.finalPrice.compareTo(b.finalPrice)
            "PRICE_HIGH" -> b.finalPrice.compareTo(a.finalPrice)
            else -> b.rating.compareTo(a.rating) // POPULAR default
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = LanguageTranslator.translate("digital_products", lang),
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isDark) Color.White else LightTextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Sub-tabs Row
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { storeTab = "SHOP" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (storeTab == "SHOP") RoyalPurple else if (isDark) DarkSurface else LightSurface,
                        contentColor = if (storeTab == "SHOP") Color.White else if (isDark) Color.White else LightTextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Digital Store", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { storeTab = "TRACKER" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (storeTab == "TRACKER") RoyalPurple else if (isDark) DarkSurface else LightSurface,
                        contentColor = if (storeTab == "TRACKER") Color.White else if (isDark) Color.White else LightTextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.TrackChanges, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Track & Pay", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (storeTab == "SHOP") {
                // Search Bar
                OutlinedTextField(
                    value = searchVal,
                    onValueChange = { viewModel.setStoreSearch(it) },
                    placeholder = { Text(LanguageTranslator.translate("search_products", lang), fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = RoyalPurple) },
                    trailingIcon = {
                        if (searchVal.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setStoreSearch("") }) {
                                Icon(Icons.Default.Clear, contentDescription = null)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category scrolling row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { category ->
                        val isSelected = categoryVal == category
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (isSelected) RoyalPurple else if (isDark) DarkSurface else LightSurface,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) RoyalPurple else if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { viewModel.setStoreCategory(category) }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = category,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else if (isDark) DarkTextPrimary else LightTextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Sorting Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${filteredProducts.size} items listed",
                        fontSize = 11.sp,
                        color = if (isDark) DarkTextSecondary else LightTextSecondary,
                        fontWeight = FontWeight.Bold
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Sort:",
                            fontSize = 11.sp,
                            color = if (isDark) DarkTextSecondary else LightTextSecondary,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Box(
                            modifier = Modifier
                                .background(if (isDark) DarkSurface else LightSurface, RoundedCornerShape(8.dp))
                                .clickable {
                                    val nextSort = when (sortVal) {
                                        "POPULAR" -> "PRICE_LOW"
                                        "PRICE_LOW" -> "PRICE_HIGH"
                                        else -> "POPULAR"
                                    }
                                    viewModel.setStoreSort(nextSort)
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            val text = when (sortVal) {
                                "PRICE_LOW" -> "Price: Low to High"
                                "PRICE_HIGH" -> "Price: High to Low"
                                else -> "Popularity"
                            }
                            Text(text = text, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoyalPurple)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Products Grid
                if (filteredProducts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No products match your search criteria.", color = if (isDark) DarkTextSecondary else LightTextSecondary)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(filteredProducts) { product ->
                            ProductItemCard(product, wishlistSet.contains(product.id), viewModel, lang, isDark)
                        }
                    }
                }
            } else {
                // Tracker and Custom Pay tab content
                Box(modifier = Modifier.weight(1f)) {
                    OrderTrackerContent(viewModel, appSettings, isDark, lang)
                }
            }
        }

        // Slide-up checkout sheet
        AnimatedVisibility(
            visible = checkoutVal != null,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            checkoutVal?.let { product ->
                CheckoutSheet(product, appSettings, viewModel, lang, isDark)
            }
        }
    }
}

@Composable
fun ProductItemCard(
    product: Product,
    isInWishlist: Boolean,
    viewModel: SkillHubViewModel,
    lang: String,
    isDark: Boolean
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    GlassmorphicCard(
        isDark = isDark,
        cornerRadius = 16.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Product Category tag
            Text(
                text = product.category.uppercase(),
                fontSize = 9.sp,
                color = ElectricBlue,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Title
            Text(
                text = product.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else LightTextPrimary,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Pricing structure with discount support
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$${String.format("%.2f", product.finalPrice)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NeonCyan
                )

                if (product.discountPercent > 0) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$${String.format("%.2f", product.price)}",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textDecoration = TextDecoration.LineThrough
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Rating Stars
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = WarningGold, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "${product.rating} (${product.ratingCount})",
                    fontSize = 10.sp,
                    color = if (isDark) DarkTextSecondary else LightTextSecondary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Wishlist Icon
                IconButton(
                    onClick = { viewModel.toggleWishlist(product.id) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (isInWishlist) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                        tint = if (isInWishlist) ErrorRed else if (isDark) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Share link copy
                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString("https://skillhub.abhiraj.com/store/product/${product.id}"))
                        Toast.makeText(context, "Product link copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = null,
                        tint = if (isDark) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Buy Now button
            GradientButton(
                text = LanguageTranslator.translate("buy_now", lang),
                onClick = { viewModel.startCheckout(product) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                cornerRadius = 8.dp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutSheet(
    product: Product,
    settings: AppSettings,
    viewModel: SkillHubViewModel,
    lang: String,
    isDark: Boolean
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var transactionId by remember { mutableStateOf("") }
    var screenPicked by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    val sheetBg = if (isDark) DarkSurface else LightSurface

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .background(sheetBg, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .border(1.dp, if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f), RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Pill drag handle representation
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .background(Color.Gray.copy(alpha = 0.5f), CircleShape)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = LanguageTranslator.translate("checkout", lang),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else LightTextPrimary
                )

                IconButton(onClick = { viewModel.cancelCheckout() }) {
                    Icon(Icons.Default.Close, contentDescription = null)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Product Brief Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = if (isDark) DarkBg else LightBg)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(product.title, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(product.category, fontSize = 11.sp, color = ElectricBlue)
                    }
                    Text(
                        text = "$${String.format("%.2f", product.finalPrice)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NeonCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Client Input Fields
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(LanguageTranslator.translate("name", lang), fontSize = 12.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(LanguageTranslator.translate("email", lang), fontSize = 12.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text(LanguageTranslator.translate("phone", lang), fontSize = 12.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // UPI Copy Panel
            Text(
                text = LanguageTranslator.translate("checkout_instructions", lang),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = RoyalPurple,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = settings.paymentInstructions,
                fontSize = 11.sp,
                color = if (isDark) DarkTextSecondary else LightTextSecondary,
                lineHeight = 18.sp,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Dynamic Scan & Pay QR Code
            val encodedQrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=250x250&margin=10&data=" + 
                java.net.URLEncoder.encode(
                    "upi://pay?pa=${settings.upiId}&pn=${settings.ownerName}&am=${product.finalPrice}&cu=INR",
                    "UTF-8"
                )

            Box(
                modifier = Modifier
                    .size(150.dp)
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = encodedQrUrl,
                    contentDescription = "Scan to Pay QR Code",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Scan with Google Pay, PhonePe, or Paytm",
                fontSize = 11.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // UPI Copy Widget
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isDark) DarkBg else LightBg, RoundedCornerShape(8.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("UPI ID", fontSize = 10.sp, color = Color.Gray)
                    Text(settings.upiId, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        clipboard.setText(AnnotatedString(settings.upiId))
                        Toast.makeText(context, "UPI ID copied!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("Copy", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Simulated Screenshot Picker & Verification Field
            OutlinedTextField(
                value = transactionId,
                onValueChange = { transactionId = it },
                label = { Text(LanguageTranslator.translate("transaction_id", lang), fontSize = 12.sp) },
                placeholder = { Text("e.g., 621482930491", fontSize = 12.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Payment Screenshot Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        screenPicked = true
                        Toast.makeText(context, "Simulated payment screenshot selected!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1.3f),
                    colors = ButtonDefaults.buttonColors(containerColor = if (screenPicked) SuccessGreen else RoyalPurple),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = if (screenPicked) Icons.Default.CheckCircle else Icons.Default.CloudUpload,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (screenPicked) "Screenshot Loaded" else LanguageTranslator.translate("payment_screenshot", lang),
                        fontSize = 11.sp
                    )
                }

                if (screenPicked) {
                    IconButton(onClick = { screenPicked = false }) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = ErrorRed)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Submit Button
            GradientButton(
                text = LanguageTranslator.translate("place_order", lang),
                onClick = {
                    if (name.trim().isEmpty() || email.trim().isEmpty() || transactionId.trim().isEmpty() || !screenPicked) {
                        Toast.makeText(context, "Please complete all fields and attach screenshot.", Toast.LENGTH_LONG).show()
                    } else {
                        viewModel.submitOrder(
                            customerName = name,
                            email = email,
                            phone = phone,
                            product = product,
                            transactionId = transactionId,
                            screenshotUri = "payment_screenshots/${UUID.randomUUID().toString().take(6)}.png"
                        ) {
                            Toast.makeText(context, "Order Placed! Awaiting admin verification.", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 12.dp
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTrackerContent(
    viewModel: SkillHubViewModel,
    settings: AppSettings,
    isDark: Boolean,
    lang: String
) {
    var emailInput by remember { mutableStateOf("") }
    var searchedEmail by remember { mutableStateOf("") }

    val ordersList by viewModel.orders.collectAsState()
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    val displayedOrders = remember(ordersList, searchedEmail) {
        if (searchedEmail.trim().isEmpty()) emptyList()
        else ordersList.filter { it.email.trim().lowercase(java.util.Locale.ROOT) == searchedEmail.trim().lowercase(java.util.Locale.ROOT) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Title and Info
        Text(
            text = "Track Orders & Pay Custom Invoices",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = RoyalPurple
        )

        Text(
            text = "Enter your registered email below to search for your digital product purchases, service orders, custom invoices, and download files.",
            fontSize = 11.sp,
            color = if (isDark) DarkTextSecondary else LightTextSecondary,
            lineHeight = 16.sp
        )

        // Email Search Field
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = emailInput,
                onValueChange = { emailInput = it },
                placeholder = { Text("Enter your email address...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = RoyalPurple) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f)
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Button(
                onClick = { searchedEmail = emailInput.trim() },
                colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Search", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Divider(color = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f))

        if (searchedEmail.isEmpty()) {
            // Instruction / Helper Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = if (isDark) DarkSurface else LightSurface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = ElectricBlue
                    )
                    Text(
                        text = "Awaiting Search",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Once you submit a task description via our Contact form, Abhiraj will review your request, create a custom job invoice, and set up your delivery links here. Search your email to see live updates!",
                        fontSize = 11.sp,
                        color = if (isDark) DarkTextSecondary else LightTextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }
            }
        } else if (displayedOrders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No orders or custom invoices found for '$searchedEmail'.\nMake sure this matches the email address you use.",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        } else {
            // Render Order Cards
            displayedOrders.forEach { order ->
                var transactionIdInput by remember { mutableStateOf(order.transactionId) }
                var isScreenshotAttached by remember { mutableStateOf(order.screenshotUri.isNotEmpty()) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) DarkSurface else LightSurface
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Order Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "ORDER ID: ${order.id}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ElectricBlue
                                )
                                Text(
                                    text = "${order.dateString} at ${order.timeString}",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }

                            // Payment Status Badge
                            val badgeColor = when (order.paymentStatus) {
                                "VERIFIED", "COMPLETED" -> SuccessGreen
                                "REJECTED" -> ErrorRed
                                "PENDING" -> WarningGold
                                else -> RoyalPurple // PENDING_PAYMENT
                            }
                            val badgeText = when (order.paymentStatus) {
                                "VERIFIED", "COMPLETED" -> "PAID & VERIFIED"
                                "REJECTED" -> "REJECTED"
                                "PENDING" -> "AWAITING VERIFICATION"
                                else -> "PENDING PAYMENT"
                            }

                            Text(
                                text = badgeText,
                                color = badgeColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier
                                    .background(badgeColor.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        // Order Summary
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isDark) DarkBg else LightBg, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = order.productTitle,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (order.productId == "CUSTOM_JOB") "Custom Service Order" else "Digital Asset Bundle",
                                        fontSize = 10.sp,
                                        color = RoyalPurple,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = "Rs ${String.format("%.2f", order.amount)}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = NeonCyan
                                )
                            }
                        }

                        // Status Specific Sections
                        if (order.paymentStatus == "PENDING_PAYMENT") {
                            // Needs QR payment & verification details upload
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Scan QR to Pay with Paytm / GPay / PhonePe:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RoyalPurple,
                                    modifier = Modifier.align(Alignment.Start)
                                )

                                // Dynamic QR Code
                                val encodedQrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=250x250&margin=10&data=" + 
                                    java.net.URLEncoder.encode(
                                        "upi://pay?pa=${settings.upiId}&pn=${settings.ownerName}&am=${order.amount}&cu=INR",
                                        "UTF-8"
                                    )

                                Box(
                                    modifier = Modifier
                                        .size(160.dp)
                                        .background(Color.White, RoundedCornerShape(12.dp))
                                        .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = encodedQrUrl,
                                        contentDescription = "Scan to Pay QR Code",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                Text(
                                    text = "UPI ID: ${settings.upiId}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color.White else Color.Black
                                )

                                Button(
                                    onClick = {
                                        clipboard.setText(AnnotatedString(settings.upiId))
                                        Toast.makeText(context, "UPI ID copied!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("Copy UPI ID", fontSize = 10.sp)
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Payment Submission Fields
                                OutlinedTextField(
                                    value = transactionIdInput,
                                    onValueChange = { transactionIdInput = it },
                                    label = { Text("Transaction ID (UPI Ref No.)", fontSize = 11.sp) },
                                    placeholder = { Text("e.g. 621482930491", fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    singleLine = true
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = {
                                            isScreenshotAttached = true
                                            Toast.makeText(context, "Payment screenshot loaded successfully!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isScreenshotAttached) SuccessGreen else RoyalPurple
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isScreenshotAttached) Icons.Default.CheckCircle else Icons.Default.CloudUpload,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isScreenshotAttached) "Screenshot Attached" else "Attach Screenshot",
                                            fontSize = 11.sp
                                        )
                                    }

                                    if (isScreenshotAttached) {
                                        IconButton(onClick = { isScreenshotAttached = false }) {
                                            Icon(Icons.Default.Delete, contentDescription = null, tint = ErrorRed)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Submit Button
                                GradientButton(
                                    text = "Submit Payment Verification Proof",
                                    onClick = {
                                        if (transactionIdInput.trim().isEmpty() || !isScreenshotAttached) {
                                            Toast.makeText(context, "Please enter your Transaction ID and attach payment screenshot.", Toast.LENGTH_LONG).show()
                                        } else {
                                            viewModel.submitInvoicePayment(
                                                orderId = order.id,
                                                transactionId = transactionIdInput,
                                                screenshotUri = "payment_screenshots/${UUID.randomUUID().toString().take(6)}.png"
                                            ) {
                                                Toast.makeText(context, "Payment verification proof submitted to Abhiraj!", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    cornerRadius = 8.dp
                                )
                            }
                        } else if (order.paymentStatus == "PENDING") {
                            // Submitted and awaiting verify
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(WarningGold.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Payment details submitted! Transaction ID: ${order.transactionId}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WarningGold
                                )
                                Text(
                                    text = "Abhiraj is currently verifying your GPay/Paytm transaction. Once verified, your completed files and links will be unlocked right here. Thank you for your patience!",
                                    fontSize = 10.sp,
                                    color = if (isDark) DarkTextSecondary else LightTextSecondary,
                                    lineHeight = 15.sp
                                )
                            }
                        } else if (order.paymentStatus == "REJECTED") {
                            // Rejected section
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(ErrorRed.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Verification Mismatch / Rejected",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ErrorRed
                                )
                                Text(
                                    text = "Reason: ${if (order.adminNotes.isEmpty()) "Payment proof was invalid or incorrect. Please contact Abhiraj directly via Contact tab." else order.adminNotes}",
                                    fontSize = 10.sp,
                                    color = if (isDark) DarkTextSecondary else LightTextSecondary,
                                    lineHeight = 15.sp
                                )
                            }
                        } else {
                            // VERIFIED or COMPLETED -> UNLOCKED!
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SuccessGreen.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = "Payment verified successfully!",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SuccessGreen
                                    )
                                    Text(
                                        text = "Your digital asset/custom service delivery is ready below. Enjoy!",
                                        fontSize = 10.sp,
                                        color = if (isDark) DarkTextSecondary else LightTextSecondary,
                                        lineHeight = 15.sp
                                    )
                                }

                                if (order.adminNotes.trim().isNotEmpty()) {
                                    val isLink = order.adminNotes.startsWith("http://") || order.adminNotes.startsWith("https://")

                                    if (isLink) {
                                        GradientButton(
                                            text = "Download / Access Work Link",
                                            onClick = {
                                                try {
                                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(order.adminNotes.trim()))
                                                    context.startActivity(intent)
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, "Error opening browser link", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            cornerRadius = 12.dp
                                        )

                                        Button(
                                            onClick = {
                                                clipboard.setText(AnnotatedString(order.adminNotes.trim()))
                                                Toast.makeText(context, "Link copied to clipboard!", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = if (isDark) DarkBg else LightBg),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "Copy Delivery Link",
                                                fontSize = 11.sp,
                                                color = if (isDark) Color.White else LightTextPrimary
                                            )
                                        }
                                    } else {
                                        // Plain text notes from admin
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = if (isDark) DarkBg else LightBg)
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text("Abhiraj's Delivery Note:", fontSize = 10.sp, color = RoyalPurple, fontWeight = FontWeight.Bold)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(order.adminNotes, fontSize = 12.sp, color = if (isDark) Color.White else LightTextPrimary)
                                            }
                                        }
                                    }
                                } else {
                                    // Empty notes, show a fallback message
                                    Text(
                                        text = "Contact Abhiraj at batrajbabu@gmail.com to retrieve your files, or check if he has emailed you the link directly.",
                                        fontSize = 11.sp,
                                        color = Color.Gray,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(80.dp))
    }
}
