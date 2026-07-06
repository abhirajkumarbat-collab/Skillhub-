package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.GradientButton
import com.example.ui.theme.*
import com.example.ui.viewmodel.SkillHubViewModel
import java.util.Locale
import java.util.UUID

@Composable
fun AdminPanel(viewModel: SkillHubViewModel, isDark: Boolean) {
    val loggedIn by viewModel.isAdminLoggedIn.collectAsState()

    if (!loggedIn) {
        AdminLoginScreen(viewModel, isDark)
    } else {
        AdminDashboardScreen(viewModel, isDark)
    }
}

@Composable
fun AdminLoginScreen(viewModel: SkillHubViewModel, isDark: Boolean) {
    var email by remember { mutableStateOf("abhirajkumarbat@gmail.com") }
    var password by remember { mutableStateOf("bat@bad+9122386567") } // Pre-filled for seamless testing
    var isPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val isFbAvailable = remember { viewModel.isFirebaseAuthAvailable() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        GlassmorphicCard(
            modifier = Modifier.fillMaxWidth(0.95f),
            isDark = isDark
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(RoyalPurple.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = "Admin Security",
                        tint = RoyalPurple,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Administrator Portal",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else LightTextPrimary
                )

                Text(
                    text = "Secure Admin Panel Sign-in",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Firebase Config Status Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isFbAvailable) SuccessGreen.copy(alpha = 0.08f) else WarningGold.copy(alpha = 0.08f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isFbAvailable) SuccessGreen.copy(alpha = 0.3f) else WarningGold.copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(if (isFbAvailable) SuccessGreen else WarningGold, CircleShape)
                            )
                            Text(
                                text = if (isFbAvailable) "Firebase Auth Activated" else "Firebase Auth Config Missing",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isFbAvailable) SuccessGreen else WarningGold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isFbAvailable) {
                                "Secure connection established with cloud Firebase Auth. Input credentials will be verified securely."
                            } else {
                                "The 'google-services.json' file is missing in the /app folder. Please configure your Firebase credentials to enable cloud sign-in. Local Sandbox Dev Mode is active (use pre-filled credentials for local sandbox testing)."
                            },
                            fontSize = 10.sp,
                            lineHeight = 14.sp,
                            color = if (isDark) Color.White.copy(alpha = 0.8f) else LightTextPrimary.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Email Address
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Administrator Email", fontSize = 11.sp) },
                    placeholder = { Text("e.g. abhirajkumarbat@gmail.com", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = RoyalPurple) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    enabled = !isLoading,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.12f)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Password
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Access Key Password", fontSize = 11.sp) },
                    placeholder = { Text("Enter admin access key", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null, tint = RoyalPurple) },
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (isPasswordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    visualTransformation = if (isPasswordVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    enabled = !isLoading,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.12f)
                    )
                )

                // Error Message Box
                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ErrorRed.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .border(1.dp, ErrorRed.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(16.dp))
                        Text(
                            text = errorMessage,
                            color = ErrorRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Submission/Unlock Button
                if (isLoading) {
                    CircularProgressIndicator(color = RoyalPurple, modifier = Modifier.size(32.dp))
                } else {
                    GradientButton(
                        text = if (isFbAvailable) "Verify via Firebase Auth" else "Bypass & Enter Sandbox Panel",
                        onClick = {
                            if (email.trim().isEmpty() || password.trim().isEmpty()) {
                                errorMessage = "Email and Password fields are required."
                            } else {
                                isLoading = true
                                errorMessage = ""
                                viewModel.adminLogin(
                                    email = email,
                                    password = password,
                                    onSuccess = {
                                        isLoading = false
                                        Toast.makeText(context, "Access Granted. Welcome Administrator!", Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { err ->
                                        isLoading = false
                                        errorMessage = err
                                    }
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Authorized Personnel Only. All activities, transactions, and site parameter changes are logged to the audit tracking database.",
                    fontSize = 9.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = 12.sp
                )
            }
        }
    }
}

@Composable
fun AdminDashboardScreen(viewModel: SkillHubViewModel, isDark: Boolean) {
    val servicesList by viewModel.services.collectAsState()
    val productsList by viewModel.products.collectAsState()
    val ordersList by viewModel.orders.collectAsState()
    val messagesList by viewModel.messages.collectAsState()
    val reviewsList by viewModel.reviews.collectAsState()
    val appSettings by viewModel.appSettings.collectAsState()

    var activeTab by remember { mutableStateOf("METRICS") } // METRICS, SERVICES, PRODUCTS, ORDERS, SETTINGS

    // Modal forms states
    var serviceToEdit by remember { mutableStateOf<Service?>(null) }
    var showAddService by remember { mutableStateOf(false) }

    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var showAddProduct by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Admin Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Admin Hub",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDark) Color.White else LightTextPrimary
                )
                Text(
                    text = appSettings.ownerName,
                    fontSize = 12.sp,
                    color = RoyalPurple,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(onClick = { viewModel.adminLogout() }) {
                Icon(Icons.Default.Logout, contentDescription = "Sign Out", tint = ErrorRed)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Horizontal Category Tabs scrolling row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val tabs = listOf("METRICS", "SERVICES", "PRODUCTS", "ORDERS", "SETTINGS", "AUDIT")
            items(tabs) { tab ->
                val isSelected = activeTab == tab
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isSelected) RoyalPurple else if (isDark) DarkSurface else LightSurface,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { activeTab = tab }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = tab,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else if (isDark) DarkTextPrimary else LightTextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tab Content Routing
        Box(modifier = Modifier.weight(1f)) {
            when (activeTab) {
                "METRICS" -> MetricsPanelTab(ordersList, productsList, messagesList, reviewsList, isDark)
                "SERVICES" -> AdminServicesTab(
                    servicesList, isDark,
                    onEdit = { serviceToEdit = it },
                    onAdd = { showAddService = true },
                    onDelete = { viewModel.deleteService(it) }
                )
                "PRODUCTS" -> AdminProductsTab(
                    productsList, isDark,
                    onEdit = { productToEdit = it },
                    onAdd = { showAddProduct = true },
                    onDelete = { viewModel.deleteProduct(it) }
                )
                "ORDERS" -> AdminOrdersTab(ordersList, viewModel, isDark)
                "SETTINGS" -> AdminSettingsTab(appSettings, viewModel, isDark)
                "AUDIT" -> AdminAuditTab(viewModel, isDark)
            }
        }
    }

    // Modal dialog trigger boxes
    if (showAddService) {
        ServiceFormDialog(isDark, onDismiss = { showAddService = false }) { service ->
            viewModel.addService(service)
            showAddService = false
        }
    }

    serviceToEdit?.let { service ->
        ServiceFormDialog(isDark, serviceToEdit = service, onDismiss = { serviceToEdit = null }) { updated ->
            viewModel.editService(updated)
            serviceToEdit = null
        }
    }

    if (showAddProduct) {
        ProductFormDialog(isDark, onDismiss = { showAddProduct = false }) { product ->
            viewModel.addProduct(product)
            showAddProduct = false
        }
    }

    productToEdit?.let { product ->
        ProductFormDialog(isDark, productToEdit = product, onDismiss = { productToEdit = null }) { updated ->
            viewModel.editProduct(updated)
            productToEdit = null
        }
    }
}

// --- TAB SUB-PANELS ---

@Composable
fun MetricsPanelTab(
    orders: List<Order>,
    products: List<Product>,
    messages: List<ContactMessage>,
    reviews: List<Review>,
    isDark: Boolean
) {
    val totalRev = orders.filter { it.paymentStatus == "VERIFIED" || it.paymentStatus == "COMPLETED" }.sumOf { it.amount }
    val pendingCount = orders.count { it.paymentStatus == "PENDING" }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card 1: Revenue
                Box(
                    modifier = Modifier
                        .weight(1.3f)
                        .background(if (isDark) DarkSurface else LightSurface, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text("TOTAL REVENUE", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text("$${String.format("%.2f", totalRev)}", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = SuccessGreen)
                    }
                }

                // Card 2: Orders
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (isDark) DarkSurface else LightSurface, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text("TOTAL ORDERS", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text("${orders.size}", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = ElectricBlue)
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card 3: Pending verification
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (isDark) DarkSurface else LightSurface, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text("PENDING ORDERS", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text("$pendingCount", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = WarningGold)
                    }
                }

                // Card 4: Messages
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (isDark) DarkSurface else LightSurface, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text("MESSAGES", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text("${messages.size}", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = RoyalPurple)
                    }
                }

                // Card 5: Reviews
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (isDark) DarkSurface else LightSurface, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text("REVIEWS", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text("${reviews.size}", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = NeonCyan)
                    }
                }
            }
        }

        // Recent Client Leads / Messages logs
        item {
            Text("Recent Project Leads", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = RoyalPurple)
        }

        if (messages.isEmpty()) {
            item {
                Text("No contact requests logged.", fontSize = 12.sp, color = Color.Gray)
            }
        } else {
            items(messages.take(3)) { msg ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isDark) DarkSurface else LightSurface, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(msg.senderName, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Budget: ${msg.budget}", fontSize = 11.sp, color = ElectricBlue, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(msg.projectDescription, fontSize = 12.sp, color = Color.Gray, maxLines = 2)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminServicesTab(
    services: List<Service>,
    isDark: Boolean,
    onEdit: (Service) -> Unit,
    onAdd: () -> Unit,
    onDelete: (Service) -> Unit
) {
    Column {
        Button(
            onClick = onAdd,
            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Add Dynamic Service")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 80.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(services) { service ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isDark) DarkSurface else LightSurface, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(service.title, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("$${service.startingPrice} | Delivery: ${service.deliveryTime}", fontSize = 11.sp, color = Color.Gray)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(onClick = { onEdit(service) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = ElectricBlue)
                            }
                            IconButton(onClick = { onDelete(service) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminProductsTab(
    products: List<Product>,
    isDark: Boolean,
    onEdit: (Product) -> Unit,
    onAdd: () -> Unit,
    onDelete: (Product) -> Unit
) {
    Column {
        Button(
            onClick = onAdd,
            colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Add Store Product")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 80.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(products) { p ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isDark) DarkSurface else LightSurface, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(p.title, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("$${p.price} | Stock: ${p.stockStatus}", fontSize = 11.sp, color = Color.Gray)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(onClick = { onEdit(p) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = ElectricBlue)
                            }
                            IconButton(onClick = { onDelete(p) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrdersTab(
    orders: List<Order>,
    viewModel: SkillHubViewModel,
    isDark: Boolean
) {
    val context = LocalContext.current
    var showCreateInvoice by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Invoice/Order Management Header Toolbar
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Client Orders & Custom Invoices",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = RoyalPurple
            )

            Button(
                onClick = { showCreateInvoice = true },
                colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Create Custom Invoice", fontSize = 11.sp)
            }
        }

        if (orders.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No orders or custom invoices logged yet.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) {
                items(orders) { order ->
                    var showDetailPopup by remember { mutableStateOf(false) }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isDark) DarkSurface else LightSurface, RoundedCornerShape(12.dp))
                            .clickable { showDetailPopup = true }
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("ID: ${order.id}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = RoyalPurple)
                                Text("${order.customerName} - ${order.productTitle}", fontSize = 12.sp)
                                Text("Email: ${order.email}", fontSize = 11.sp, color = Color.Gray)
                                if (order.transactionId.isNotEmpty()) {
                                    Text("TxID: ${order.transactionId}", fontSize = 11.sp, color = Color.Gray)
                                }
                            }

                            val badgeColor = when (order.paymentStatus) {
                                "VERIFIED", "COMPLETED" -> SuccessGreen
                                "REJECTED" -> ErrorRed
                                "PENDING" -> WarningGold
                                else -> RoyalPurple // PENDING_PAYMENT
                            }
                            val badgeText = when (order.paymentStatus) {
                                "VERIFIED", "COMPLETED" -> "VERIFIED"
                                "REJECTED" -> "REJECTED"
                                "PENDING" -> "PENDING"
                                else -> "UNPAID INVOICE"
                            }

                            Text(
                                text = badgeText,
                                color = badgeColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (showDetailPopup) {
                        Dialog(onDismissRequest = { showDetailPopup = false }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (isDark) DarkSurface else LightSurface, RoundedCornerShape(16.dp))
                                    .padding(16.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text("Verify UPI Purchase", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                        IconButton(onClick = { showDetailPopup = false }) {
                                            Icon(Icons.Default.Close, contentDescription = null)
                                        }
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("Customer Name: ${order.customerName}", fontSize = 12.sp)
                                        Text("Email: ${order.email}", fontSize = 12.sp)
                                        Text("Phone: ${order.phone}", fontSize = 12.sp)
                                        Text("Product ID: ${order.productId}", fontSize = 12.sp)
                                        Text("Invoice Amount: Rs ${order.amount}", fontSize = 12.sp)
                                        Text("Date / Time: ${order.dateString} ${order.timeString}", fontSize = 12.sp)
                                        if (order.transactionId.isNotEmpty()) {
                                            Text("Transaction ID: ${order.transactionId}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ElectricBlue)
                                        }
                                    }

                                    // Screenshot placeholder/metadata frame (Only if screenshot attached)
                                    if (order.screenshotUri.isNotEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(100.dp)
                                                .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(Icons.Default.Image, contentDescription = null, tint = ElectricBlue)
                                                Text("Screenshot Metadata Verified", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                Text(order.screenshotUri, fontSize = 9.sp, color = Color.Gray)
                                            }
                                        }
                                    }

                                    // Edit Delivery Link / Admin Notes
                                    var adminNotesInput by remember { mutableStateOf(order.adminNotes) }
                                    OutlinedTextField(
                                        value = adminNotesInput,
                                        onValueChange = { adminNotesInput = it },
                                        label = { Text("Delivery Notes / Drive Link", fontSize = 11.sp) },
                                        placeholder = { Text("Enter Google Drive URL or delivery text", fontSize = 11.sp) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp)
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                viewModel.verifyOrderPayment(order.id, "VERIFIED", adminNotesInput)
                                                Toast.makeText(context, "Order payment verified & delivery updated!", Toast.LENGTH_SHORT).show()
                                                showDetailPopup = false
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Approve", fontSize = 12.sp)
                                        }

                                        Button(
                                            onClick = {
                                                viewModel.verifyOrderPayment(order.id, "REJECTED", adminNotesInput)
                                                Toast.makeText(context, "Payment Rejected.", Toast.LENGTH_SHORT).show()
                                                showDetailPopup = false
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Reject", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Create Invoice Dialog
    if (showCreateInvoice) {
        var clientName by remember { mutableStateOf("") }
        var clientEmail by remember { mutableStateOf("") }
        var clientPhone by remember { mutableStateOf("") }
        var jobTitle by remember { mutableStateOf("") }
        var amountVal by remember { mutableStateOf("") }
        var deliveryUrlVal by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showCreateInvoice = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isDark) DarkSurface else LightSurface, RoundedCornerShape(16.dp))
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Create Custom Invoice", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = RoyalPurple)
                        IconButton(onClick = { showCreateInvoice = false }) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }

                    OutlinedTextField(
                        value = clientName,
                        onValueChange = { clientName = it },
                        label = { Text("Client Name", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = clientEmail,
                        onValueChange = { clientEmail = it },
                        label = { Text("Client Email", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    OutlinedTextField(
                        value = clientPhone,
                        onValueChange = { clientPhone = it },
                        label = { Text("Client Phone (Optional)", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )

                    OutlinedTextField(
                        value = jobTitle,
                        onValueChange = { jobTitle = it },
                        label = { Text("Service Description / Job Title", fontSize = 12.sp) },
                        placeholder = { Text("e.g. YouTube Video Editing (10 Videos Package)", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = amountVal,
                        onValueChange = { amountVal = it },
                        label = { Text("Invoice Amount (Rs)", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    OutlinedTextField(
                        value = deliveryUrlVal,
                        onValueChange = { deliveryUrlVal = it },
                        label = { Text("Completed Delivery Link / URL (Optional)", fontSize = 12.sp) },
                        placeholder = { Text("Google Drive or Mega Link for client downloads", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    GradientButton(
                        text = "Generate Live Invoice",
                        onClick = {
                            val parsedAmount = amountVal.toDoubleOrNull() ?: 0.0
                            if (clientName.trim().isEmpty() || clientEmail.trim().isEmpty() || jobTitle.trim().isEmpty() || parsedAmount <= 0.0) {
                                Toast.makeText(context, "Please fill in Name, Email, Job Title and a valid Amount.", Toast.LENGTH_LONG).show()
                            } else {
                                viewModel.createCustomInvoice(
                                    customerName = clientName,
                                    email = clientEmail,
                                    phone = clientPhone,
                                    description = jobTitle,
                                    amount = parsedAmount,
                                    deliveryUrl = deliveryUrlVal
                                ) {
                                    Toast.makeText(context, "Invoice Generated Live! Client can pay now.", Toast.LENGTH_LONG).show()
                                    showCreateInvoice = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 12.dp
                    )
                }
            }
        }
    }
}

@Composable
fun AdminSettingsTab(
    settings: AppSettings,
    viewModel: SkillHubViewModel,
    isDark: Boolean
) {
    var upiId by remember { mutableStateOf(settings.upiId) }
    var instructions by remember { mutableStateOf(settings.paymentInstructions) }
    var chatbotEnabled by remember { mutableStateOf(settings.chatbotEnabled) }

    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Payment Integration Settings", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = RoyalPurple)

        OutlinedTextField(
            value = upiId,
            onValueChange = { upiId = it },
            label = { Text("Active Admin UPI ID") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = instructions,
            onValueChange = { instructions = it },
            label = { Text("Checkout instructions") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Enable Gemini AI Chatbot", fontSize = 13.sp)
            Switch(checked = chatbotEnabled, onCheckedChange = { chatbotEnabled = it })
        }

        Button(
            onClick = {
                viewModel.updateSettings(settings.copy(upiId = upiId, paymentInstructions = instructions, chatbotEnabled = chatbotEnabled))
                Toast.makeText(context, "Payment & AI parameters saved successfully!", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Payment Details")
        }

        Spacer(modifier = Modifier.height(12.dp))
        Divider(color = Color.White.copy(alpha = 0.1f))
        Spacer(modifier = Modifier.height(8.dp))

        Text("Maintenance Mode", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ErrorRed)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Enable Global Maintenance Page", fontSize = 13.sp)
            Switch(
                checked = settings.maintenanceMode,
                onCheckedChange = { viewModel.updateSettings(settings.copy(maintenanceMode = it)) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Divider(color = Color.White.copy(alpha = 0.1f))
        Spacer(modifier = Modifier.height(8.dp))

        Text("JSON Backup Framework", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NeonCyan)

        var showImportField by remember { mutableStateOf(false) }
        var importJsonText by remember { mutableStateOf("") }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    viewModel.exportSystemState { json ->
                        clipboard.setText(AnnotatedString(json))
                        Toast.makeText(context, "JSON System State exported & copied to clipboard!", Toast.LENGTH_LONG).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                modifier = Modifier.weight(1f)
            ) {
                Text("Export JSON", fontSize = 11.sp)
            }

            Button(
                onClick = {
                    showImportField = !showImportField
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                modifier = Modifier.weight(1f)
            ) {
                Text(if (showImportField) "Cancel" else "Import JSON", fontSize = 11.sp)
            }
        }

        if (showImportField) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = importJsonText,
                onValueChange = { importJsonText = it },
                label = { Text("Paste Backup JSON", fontSize = 12.sp) },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                maxLines = 8,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = Color.Gray
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    if (importJsonText.trim().isNotEmpty()) {
                        viewModel.restoreSystemState(importJsonText) { success ->
                            if (success) {
                                Toast.makeText(context, "System state successfully restored from JSON!", Toast.LENGTH_SHORT).show()
                                importJsonText = ""
                                showImportField = false
                            } else {
                                Toast.makeText(context, "Error: Invalid JSON backup schema detected.", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Apply Restore")
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

// --- FORM DIALOGS ---

@Composable
fun ServiceFormDialog(
    isDark: Boolean,
    serviceToEdit: Service? = null,
    onDismiss: () -> Unit,
    onSave: (Service) -> Unit
) {
    var title by remember { mutableStateOf(serviceToEdit?.title ?: "") }
    var price by remember { mutableStateOf(serviceToEdit?.startingPrice?.toString() ?: "") }
    var deliveryTime by remember { mutableStateOf(serviceToEdit?.deliveryTime ?: "") }
    var description by remember { mutableStateOf(serviceToEdit?.description ?: "") }
    var iconName by remember { mutableStateOf(serviceToEdit?.iconName ?: "web") }
    var category by remember { mutableStateOf(serviceToEdit?.category ?: "Development") }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (isDark) DarkSurface else LightSurface, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = if (serviceToEdit != null) "Edit Service" else "Add Service", fontSize = 16.sp, fontWeight = FontWeight.Bold)

                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Service Title") })
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Starting Price ($)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = deliveryTime, onValueChange = { deliveryTime = it }, label = { Text("Delivery Estimate (e.g., 5 Days)") })
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.height(70.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color.Gray), modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val id = serviceToEdit?.id ?: UUID.randomUUID().toString()
                            onSave(Service(id, title, description, price.toDoubleOrNull() ?: 0.0, deliveryTime, iconName, category))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
fun ProductFormDialog(
    isDark: Boolean,
    productToEdit: Product? = null,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    var title by remember { mutableStateOf(productToEdit?.title ?: "") }
    var price by remember { mutableStateOf(productToEdit?.price?.toString() ?: "") }
    var discount by remember { mutableStateOf(productToEdit?.discountPercent?.toString() ?: "0.0") }
    var description by remember { mutableStateOf(productToEdit?.description ?: "") }
    var category by remember { mutableStateOf(productToEdit?.category ?: "Web Development") }
    var stockStatus by remember { mutableStateOf(productToEdit?.stockStatus ?: "IN_STOCK") }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (isDark) DarkSurface else LightSurface, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = if (productToEdit != null) "Edit Product" else "Add Product", fontSize = 16.sp, fontWeight = FontWeight.Bold)

                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Product Name") })
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price ($)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = discount, onValueChange = { discount = it }, label = { Text("Discount (%)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.height(70.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color.Gray), modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val id = productToEdit?.id ?: UUID.randomUUID().toString()
                            onSave(
                                Product(
                                    id = id,
                                    title = title,
                                    description = description,
                                    price = price.toDoubleOrNull() ?: 0.0,
                                    discountPercent = discount.toDoubleOrNull() ?: 0.0,
                                    category = category,
                                    stockStatus = stockStatus
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
fun AdminAuditTab(viewModel: SkillHubViewModel, isDark: Boolean) {
    val auditLogs by viewModel.auditLogs.collectAsState()
    val format = remember { java.text.SimpleDateFormat("MMM d, hh:mm:ss a", java.util.Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "System Security Audit Log",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = RoyalPurple
            )
            Box(
                modifier = Modifier
                    .background(RoyalPurple.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${auditLogs.size} Events",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = RoyalPurple
                )
            }
        }

        if (auditLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No audit log entries recorded.",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(auditLogs) { log ->
                    val dateStr = format.format(java.util.Date(log.timestamp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isDark) DarkSurface else LightSurface, RoundedCornerShape(12.dp))
                            .border(1.dp, if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "SECURE_AUDIT_LOG_EVENT",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricBlue
                                )
                                Text(
                                    text = dateStr,
                                    fontSize = 9.sp,
                                    color = Color.Gray
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = log.action,
                                fontSize = 12.sp,
                                color = if (isDark) DarkTextPrimary else LightTextPrimary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
