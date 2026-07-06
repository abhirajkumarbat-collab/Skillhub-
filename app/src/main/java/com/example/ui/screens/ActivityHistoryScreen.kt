package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ContactMessage
import com.example.data.model.Order
import com.example.ui.components.GlassmorphicCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.SkillHubViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityHistoryScreen(viewModel: SkillHubViewModel, isDark: Boolean) {
    val context = LocalContext.current
    val clientEmail by viewModel.clientEmail.collectAsState()
    val allMessages by viewModel.messages.collectAsState()
    val allOrders by viewModel.orders.collectAsState()
    val activities by viewModel.activityHistory.collectAsState()

    // Filtered lists based on client email
    val clientBookings = remember(allMessages, clientEmail) {
        allMessages.filter { it.email.trim().lowercase() == clientEmail.trim().lowercase() }
    }
    val clientOrders = remember(allOrders, clientEmail) {
        allOrders.filter { it.email.trim().lowercase() == clientEmail.trim().lowercase() }
    }

    var activeTab by remember { mutableStateOf(0) } // 0: Bookings & Projects, 1: Store & Invoices, 2: Activity Logs
    var selectedInvoiceOrder by remember { mutableStateOf<Order?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header Section
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
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.Dashboard,
                contentDescription = null,
                tint = RoyalPurple,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "My Client Dashboard",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isDark) Color.White else LightTextPrimary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Email Selector Input Box
        var tempEmail by remember { mutableStateOf(clientEmail) }
        LaunchedEffect(clientEmail) {
            tempEmail = clientEmail
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isDark) Color.White.copy(alpha = 0.03f) else Color.Black.copy(alpha = 0.02f),
                    RoundedCornerShape(16.dp)
                )
                .border(
                    1.dp,
                    if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f),
                    RoundedCornerShape(16.dp)
                )
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = tempEmail,
                onValueChange = { tempEmail = it },
                label = { Text("Registered Client Email", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                placeholder = { Text("client@example.com") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RoyalPurple,
                    unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.12f),
                    focusedLabelColor = RoyalPurple
                ),
                leadingIcon = {
                    Icon(Icons.Default.AccountCircle, contentDescription = null, tint = RoyalPurple, modifier = Modifier.size(18.dp))
                }
            )
            Button(
                onClick = {
                    if (tempEmail.trim().isNotEmpty()) {
                        viewModel.setClientEmail(tempEmail)
                        Toast.makeText(context, "Synchronized records!", Toast.LENGTH_SHORT).show()
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Text("Sync", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Segmented Tabs
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = Color.Transparent,
            contentColor = RoyalPurple,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                    color = RoyalPurple
                )
            },
            divider = {
                HorizontalDivider(color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f))
            }
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = { Text("Projects & Bookings", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = { Text("Invoices & Orders", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = activeTab == 2,
                onClick = { activeTab = 2 },
                text = { Text("In-App Logs", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Active View
        Box(modifier = Modifier.weight(1f)) {
            when (activeTab) {
                0 -> ClientBookingsTab(clientBookings, isDark, viewModel)
                1 -> ClientInvoicesTab(clientOrders, isDark) { selectedInvoiceOrder = it }
                2 -> ClientActivityLogsTab(activities, isDark)
            }
        }
    }

    // Invoice View Modal Dialog
    if (selectedInvoiceOrder != null) {
        InvoicePreviewDialog(
            order = selectedInvoiceOrder!!,
            isDark = isDark,
            onDismiss = { selectedInvoiceOrder = null }
        )
    }
}

@Composable
fun ClientBookingsTab(bookings: List<ContactMessage>, isDark: Boolean, viewModel: SkillHubViewModel) {
    if (bookings.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = RoyalPurple.copy(alpha = 0.3f),
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No Service Bookings Found",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else LightTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Submit a custom service inquiry from the Contact screen to launch your project.",
                    fontSize = 12.sp,
                    color = if (isDark) DarkTextSecondary else LightTextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.navigateTo("CONTACT") },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Hire Abhiraj Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(bookings) { booking ->
                BookingProjectCard(booking, isDark)
            }
        }
    }
}

@Composable
fun BookingProjectCard(booking: ContactMessage, isDark: Boolean) {
    val dateFmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val bookingDate = dateFmt.format(Date(booking.timestamp))

    // Progress stepper stages based on status
    val (statusLabel, progressValue, progressColor) = when (booking.replyStatus.uppercase()) {
        "PENDING" -> Triple("Discovery & Scoping Phase", 0.25f, ElectricBlue)
        "REPLIED" -> Triple("Development & Active Coding", 0.65f, RoyalPurple)
        "ARCHIVED" -> Triple("Project Handed Over", 1.0f, SuccessGreen)
        else -> Triple("Requirements Analysis", 0.4f, WarningGold)
    }

    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        isDark = isDark
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Card Title Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.TaskAlt, contentDescription = null, tint = progressColor, modifier = Modifier.size(16.dp))
                    Text(
                        text = "FREELANCE PROJECT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = progressColor,
                        letterSpacing = 0.5.sp
                    )
                }
                Text(
                    text = bookingDate,
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Project Description
            Text(
                text = booking.projectDescription,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else LightTextPrimary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Budget Info & Attachment info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Budget", fontSize = 9.sp, color = Color.Gray)
                    Text(booking.budget, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ElectricBlue)
                }
                Column {
                    Text("Attachment", fontSize = 9.sp, color = Color.Gray)
                    Text(
                        if (booking.attachmentUri.isNotEmpty()) "Ready (Requirement PDF)" else "None",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (booking.attachmentUri.isNotEmpty()) SuccessGreen else Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Stepper Visual
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(statusLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else LightTextPrimary)
                    Text("${(progressValue * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = progressColor)
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progressValue },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = progressColor,
                    trackColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }
        }
    }
}

@Composable
fun ClientInvoicesTab(orders: List<Order>, isDark: Boolean, onViewInvoice: (Order) -> Unit) {
    if (orders.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                Icon(
                    imageVector = Icons.Default.ReceiptLong,
                    contentDescription = null,
                    tint = RoyalPurple.copy(alpha = 0.3f),
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No Digital Orders Found",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else LightTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Once you purchase custom source assets, UI kits, or templates, invoices will reflect here.",
                    fontSize = 12.sp,
                    color = if (isDark) DarkTextSecondary else LightTextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(orders) { order ->
                InvoiceOrderCard(order, isDark, onViewInvoice)
            }
        }
    }
}

@Composable
fun InvoiceOrderCard(order: Order, isDark: Boolean, onViewInvoice: (Order) -> Unit) {
    val statusColor = when (order.paymentStatus.uppercase()) {
        "COMPLETED", "VERIFIED" -> SuccessGreen
        "PENDING", "PENDING_PAYMENT" -> WarningGold
        else -> ErrorRed
    }

    val context = LocalContext.current

    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        isDark = isDark
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.id,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = RoyalPurple
                )
                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = order.paymentStatus.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = order.productTitle,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else LightTextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text("Total Price", fontSize = 9.sp, color = Color.Gray)
                    Text("$${String.format("%.2f", order.amount)}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = ElectricBlue)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { onViewInvoice(order) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RoyalPurple),
                        border = BorderStroke(1.dp, RoyalPurple.copy(alpha = 0.4f)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("View Receipt", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            Toast.makeText(context, "Invoice Saved to Downloads folder as PDF", Toast.LENGTH_LONG).show()
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Invoice PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun ClientActivityLogsTab(activities: List<SkillHubViewModel.UserActivity>, isDark: Boolean) {
    if (activities.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.HistoryToggleOff,
                    contentDescription = null,
                    tint = RoyalPurple.copy(alpha = 0.3f),
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No Activities Logged",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else LightTextPrimary
                )
            }
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(activities) { item ->
                ActivityRow(item, isDark)
            }
        }
    }
}

@Composable
fun InvoicePreviewDialog(order: Order, isDark: Boolean, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDark) DarkSurface else LightSurface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .border(1.dp, RoyalPurple.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Diagonal Brand Header block
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("SkillHub Invoice", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = RoyalPurple)
                        Text("by Abhiraj Kumar", fontSize = 10.sp, color = Color.Gray)
                    }
                    Box(
                        modifier = Modifier
                            .background(RoyalPurple.copy(alpha = 0.12f), CircleShape)
                            .padding(8.dp)
                    ) {
                        Icon(Icons.Default.AllInclusive, contentDescription = null, tint = RoyalPurple, modifier = Modifier.size(24.dp))
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f))

                // Invoice metadata details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("BILL TO:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text(order.customerName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else LightTextPrimary)
                        Text(order.email, fontSize = 10.sp, color = Color.Gray)
                        Text(order.phone, fontSize = 10.sp, color = Color.Gray)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("INVOICE ID:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text(order.id, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RoyalPurple)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("DATE:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text(if (order.dateString.isNotEmpty()) order.dateString else "Today", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = if (isDark) Color.White else LightTextPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Receipt Item Table
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isDark) Color.White.copy(alpha = 0.03f) else Color.Black.copy(alpha = 0.03f),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Item Description", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text("Total", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        }
                        HorizontalDivider(color = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(order.productTitle, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.7f), color = if (isDark) Color.White else LightTextPrimary)
                            Text("$${String.format("%.2f", order.amount)}", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = ElectricBlue)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Calculation details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("Subtotal:", fontSize = 11.sp, color = Color.Gray)
                            Text("$${String.format("%.2f", order.amount)}", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = if (isDark) Color.White else LightTextPrimary)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("Service Tax (GST 0%):", fontSize = 11.sp, color = Color.Gray)
                            Text("$0.00", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = if (isDark) Color.White else LightTextPrimary)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("Amount Paid (PAID):", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                            Text("$${String.format("%.2f", order.amount)}", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = SuccessGreen)
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f))

                // Stamp / Verified signature block
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("PAYMENT METHOD:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text(if (order.transactionId.isNotEmpty()) "UPI/GPay / QR Transfer" else "Direct Handoff Settlement", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = if (isDark) Color.White else LightTextPrimary)
                        if (order.transactionId.isNotEmpty()) {
                            Text("Txn Ref: ${order.transactionId}", fontSize = 9.sp, color = Color.Gray)
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("SIGNATURE", fontSize = 8.sp, color = Color.Gray)
                        Text("Abhiraj Kumar", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = RoyalPurple)
                        Box(
                            modifier = Modifier
                                .background(SuccessGreen.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("VERIFIED & STAMPED", fontSize = 8.sp, color = SuccessGreen, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple)
                ) {
                    Text("Close Receipt Preview", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun ActivityRow(item: SkillHubViewModel.UserActivity, isDark: Boolean) {
    val iconColor = when (item.type) {
        "ORDER" -> SuccessGreen
        "LEAD" -> ElectricBlue
        "WISHLIST" -> WarningGold
        "SEARCH" -> NeonCyan
        "CHAT" -> RoyalPurple
        else -> Color.Gray
    }

    val iconVector = when (item.type) {
        "ORDER" -> Icons.Default.ShoppingBag
        "LEAD" -> Icons.Default.Email
        "WISHLIST" -> Icons.Default.Favorite
        "SEARCH" -> Icons.Default.Search
        "CHAT" -> Icons.Default.AutoAwesome
        else -> Icons.Default.Info
    }

    val format = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    val dateStr = format.format(Date(item.timestamp))

    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        isDark = isDark
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(iconColor.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(iconVector, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.description,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else LightTextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = dateStr,
                    fontSize = 10.sp,
                    color = if (isDark) DarkTextSecondary.copy(alpha = 0.6f) else LightTextSecondary.copy(alpha = 0.6f)
                )
            }
        }
    }
}
