package com.example.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.SkillHubRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class SkillHubViewModel(private val repository: SkillHubRepository) : ViewModel() {
    private val tag = "SkillHubViewModel"

    // Multi-Language state ("en", "hi", "hinglish")
    private val _language = MutableStateFlow("en")
    val language: StateFlow<String> = _language.asStateFlow()

    // Dark/Light Theme manual toggle state (null means follow system)
    private val _themeOverride = MutableStateFlow<Boolean?>(null)
    val themeOverride: StateFlow<Boolean?> = _themeOverride.asStateFlow()

    // Screen-level navigation: HOME, SERVICES, PORTFOLIO, STORE, REVIEWS, CONTACT, CHATBOT, ADMIN_LOGIN, ADMIN_DASHBOARD
    private val _currentScreen = MutableStateFlow("HOME")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // Observables from Repository (Room & Firestore hybrid)
    val services: StateFlow<List<Service>> = repository.allServices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val products: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val orders: StateFlow<List<Order>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val messages: StateFlow<List<ContactMessage>> = repository.allMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reviews: StateFlow<List<Review>> = repository.allReviews
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val portfolioItems: StateFlow<List<PortfolioItem>> = repository.allPortfolioItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appSettings: StateFlow<AppSettings> = repository.appSettings
        .map { it ?: AppSettings() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    val chatHistory: StateFlow<List<ChatMessage>> = repository.chatHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- INTERACTIVE UI STATE ---
    // Shopping Cart & Wishlist (local state)
    private val _wishlist = MutableStateFlow<Set<String>>(emptySet())
    val wishlist: StateFlow<Set<String>> = _wishlist.asStateFlow()

    // Filter & Sort Settings for Digital Store
    private val _storeSearchQuery = MutableStateFlow("")
    val storeSearchQuery: StateFlow<String> = _storeSearchQuery.asStateFlow()

    private val _selectedStoreCategory = MutableStateFlow("All")
    val selectedStoreCategory: StateFlow<String> = _selectedStoreCategory.asStateFlow()

    private val _storeSortOption = MutableStateFlow("POPULAR") // POPULAR, PRICE_LOW, PRICE_HIGH
    val storeSortOption: StateFlow<String> = _storeSortOption.asStateFlow()

    // Filter for Portfolio
    private val _selectedPortfolioCategory = MutableStateFlow("All")
    val selectedPortfolioCategory: StateFlow<String> = _selectedPortfolioCategory.asStateFlow()

    // Checkout Sheet state
    private val _checkoutProduct = MutableStateFlow<Product?>(null)
    val checkoutProduct: StateFlow<Product?> = _checkoutProduct.asStateFlow()

    // Client email state for personalized dashboard lookup
    private val _clientEmail = MutableStateFlow("singerruparani1560@gmail.com")
    val clientEmail: StateFlow<String> = _clientEmail.asStateFlow()

    fun setClientEmail(email: String) {
        _clientEmail.value = email.trim().lowercase(Locale.ROOT)
    }

    // AI Chatbot State
    private val _isChatbotTyping = MutableStateFlow(false)
    val isChatbotTyping: StateFlow<Boolean> = _isChatbotTyping.asStateFlow()

    // Admin Authenticated State
    private val _isAdminLoggedIn = MutableStateFlow(false)
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

    // Notification logs Center (In-Memory database representation)
    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    data class NotificationItem(
        val id: String = UUID.randomUUID().toString(),
        val title: String,
        val text: String,
        val type: String, // ORDER, MESSAGE, REVIEW, ALERT
        val timestamp: Long = System.currentTimeMillis(),
        var isRead: Boolean = false
    )

    data class UserActivity(
        val id: String = UUID.randomUUID().toString(),
        val description: String,
        val type: String, // SYSTEM, ORDER, LEAD, SEARCH, WISHLIST, CHAT
        val timestamp: Long = System.currentTimeMillis()
    )

    data class AuditLog(
        val id: String = UUID.randomUUID().toString(),
        val action: String,
        val timestamp: Long = System.currentTimeMillis()
    )

    private val _activityHistory = MutableStateFlow<List<UserActivity>>(emptyList())
    val activityHistory: StateFlow<List<UserActivity>> = _activityHistory.asStateFlow()

    private val _auditLogs = MutableStateFlow<List<AuditLog>>(emptyList())
    val auditLogs: StateFlow<List<AuditLog>> = _auditLogs.asStateFlow()

    fun addActivity(description: String, type: String) {
        val newAct = UserActivity(description = description, type = type)
        _activityHistory.value = listOf(newAct) + _activityHistory.value
    }

    fun addAuditLog(action: String) {
        val newLog = AuditLog(action = action)
        _auditLogs.value = listOf(newLog) + _auditLogs.value
    }

    init {
        // Seed some initial activity and audit logs for fully populated feel
        _activityHistory.value = listOf(
            UserActivity(description = "Visited SkillHub digital portfolio", type = "SYSTEM"),
            UserActivity(description = "Inquired about Android development solutions", type = "CHAT"),
            UserActivity(description = "Searched for 'Next.js Bootstrap Boilerplate'", type = "SEARCH")
        )

        _auditLogs.value = listOf(
            AuditLog(action = "System database initialized with premium seed assets."),
            AuditLog(action = "Configured direct integration tokens for Google Gemini AI models.")
        )

        // Automatically set initial language and settings on launch
        viewModelScope.launch {
            appSettings.collect { settings ->
                _language.value = settings.defaultLanguage
            }
        }
    }

    // --- PUBLIC ACTIONS ---

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    fun setLanguage(lang: String) {
        _language.value = lang
        addActivity("Updated interface language preference to $lang", "SYSTEM")
        viewModelScope.launch {
            val updated = appSettings.value.copy(defaultLanguage = lang)
            repository.updateSettings(updated)
        }
    }

    fun toggleTheme() {
        val current = _themeOverride.value
        val next = if (current == true) false else true
        _themeOverride.value = next
        addActivity("Toggled dark theme override to: $next", "SYSTEM")
    }

    fun setSystemTheme() {
        _themeOverride.value = null
    }

    // Wishlist Toggles
    fun toggleWishlist(productId: String) {
        val currentSet = _wishlist.value.toMutableSet()
        val added = if (currentSet.contains(productId)) {
            currentSet.remove(productId)
            false
        } else {
            currentSet.add(productId)
            true
        }
        _wishlist.value = currentSet
        addActivity(
            if (added) "Bookmarked product ID $productId to wishlist"
            else "Removed product ID $productId from wishlist", "WISHLIST"
        )
    }

    // Store Filtering and Searching
    fun setStoreSearch(query: String) {
        _storeSearchQuery.value = query
        if (query.trim().isNotEmpty()) {
            addActivity("Searched the digital store for \"$query\"", "SEARCH")
        }
    }

    fun setStoreCategory(category: String) {
        _selectedStoreCategory.value = category
    }

    fun setStoreSort(option: String) {
        _storeSortOption.value = option
    }

    fun setPortfolioCategory(category: String) {
        _selectedPortfolioCategory.value = category
    }

    // Checkout Operations
    fun startCheckout(product: Product) {
        _checkoutProduct.value = product
    }

    fun cancelCheckout() {
        _checkoutProduct.value = null
    }

    // Place an Order & Upload screenshot metadata
    fun submitOrder(
        customerName: String,
        email: String,
        phone: String,
        product: Product,
        transactionId: String,
        screenshotUri: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val order = repository.placeOrder(
                customerName, email, phone, product, transactionId, screenshotUri
            )
            setClientEmail(email)
            // Add notification for admin
            addNotification(
                title = "New Product Order!",
                text = "${customerName} ordered ${product.title} for $${order.amount}. Needs verification.",
                type = "ORDER"
            )
            addActivity("Submitted order for product: ${product.title} ($${order.amount})", "ORDER")
            _checkoutProduct.value = null
            onSuccess()
        }
    }

    // Create a Custom Invoice for a client's work request
    fun createCustomInvoice(
        customerName: String,
        email: String,
        phone: String,
        description: String,
        amount: Double,
        deliveryUrl: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val dateFmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val timeFmt = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val curDate = Date()
            val orderId = "INV-${UUID.randomUUID().toString().take(6).uppercase(Locale.ROOT)}"
            
            val order = Order(
                id = orderId,
                customerName = customerName,
                email = email.trim().lowercase(Locale.ROOT),
                phone = phone,
                productId = "CUSTOM_JOB",
                productTitle = description,
                amount = amount,
                paymentStatus = "PENDING_PAYMENT", // Client needs to pay this via QR
                transactionId = "",
                screenshotUri = "",
                dateString = dateFmt.format(curDate),
                timeString = timeFmt.format(curDate),
                adminNotes = deliveryUrl, // Delivery URL
                isDownloadEnabled = false
            )
            repository.updateOrder(order)
            addNotification(
                title = "Custom Invoice Created",
                text = "Invoice $orderId created for $customerName ($$amount).",
                type = "ORDER"
            )
            addActivity("Created custom invoice $orderId for $customerName", "ORDER")
            onSuccess()
        }
    }

    // Submit payment verification details for a custom invoice / order
    fun submitInvoicePayment(
        orderId: String,
        transactionId: String,
        screenshotUri: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val existingOrders = orders.value
            val order = existingOrders.find { it.id == orderId } ?: return@launch
            val updated = order.copy(
                paymentStatus = "PENDING", // Changes status to PENDING (Awaiting Admin Verification)
                transactionId = transactionId,
                screenshotUri = screenshotUri
            )
            repository.updateOrder(updated)
            addNotification(
                title = "Invoice Payment Submitted",
                text = "Client submitted transaction ID $transactionId for invoice $orderId.",
                type = "ORDER"
            )
            addActivity("Submitted payment details for invoice $orderId", "ORDER")
            onSuccess()
        }
    }

    // --- CLIENT SUBMISSIONS ---

    fun sendContactMessage(
        name: String,
        email: String,
        phone: String,
        description: String,
        budget: String,
        attachmentUri: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val msg = repository.sendMessage(name, email, phone, description, budget, attachmentUri)
            setClientEmail(email)
            addNotification(
                title = "New Project Lead!",
                text = "$name submitted a request with a budget of $budget.",
                type = "MESSAGE"
            )
            addActivity("Dispatched service request lead ($budget budget) to Abhiraj", "LEAD")
            onSuccess()
        }
    }

    fun submitReview(rating: Int, author: String, comment: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.submitReview(rating, author, comment)
            addNotification(
                title = "New Review Submitted",
                text = "$author rated the platform $rating stars. Review requires approval.",
                type = "REVIEW"
            )
            addActivity("Authored $rating-star feedback review", "WISHLIST")
            onSuccess()
        }
    }

    // --- CHATBOT LOGIC ---

    fun sendChatMessage(userText: String) {
        if (userText.trim().isEmpty()) return

        viewModelScope.launch {
            // Save User message
            repository.saveChatMessage("USER", userText)
            addActivity("Consulted Gemini AI Assistant: \"${userText.take(30)}...\"", "CHAT")

            _isChatbotTyping.value = true
            val systemPrompt = appSettings.value.chatbotSystemPrompt

            // Fetch response from Gemini / Local simulator fallback
            val aiResponse = repository.getAiResponse(userText, systemPrompt)

            // Dynamic delays for premium typing visual experience
            delay(1000)

            repository.saveChatMessage("AI", aiResponse)
            _isChatbotTyping.value = false
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            repository.clearChatHistory()
        }
    }

    // --- ADMIN PANEL OPERATIONS (CRUD & Moderate) ---

    fun isFirebaseAuthAvailable(): Boolean = repository.isFirebaseAuthAvailable()

    fun adminLogin(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val normalizedEmail = email.trim().lowercase(Locale.ROOT)
            if (repository.isFirebaseAuthAvailable()) {
                repository.signInWithFirebase(normalizedEmail, password) { success, resultEmail ->
                    viewModelScope.launch {
                        if (success && resultEmail != null) {
                            val authenticatedEmail = resultEmail.trim().lowercase(Locale.ROOT)
                            if (authenticatedEmail == "abhirajkumarbat@gmail.com" || authenticatedEmail == "batrajbabu@gmail.com") {
                                _isAdminLoggedIn.value = true
                                addAuditLog("Admin session unlocked successfully via Firebase Auth for $authenticatedEmail.")
                                onSuccess()
                            } else {
                                repository.signOutFirebase()
                                addAuditLog("Access Denied: $authenticatedEmail is not configured as an administrator.")
                                onError("Access Denied: This email ($authenticatedEmail) is not a configured administrator.")
                            }
                        } else {
                            addAuditLog("Failed Firebase Auth login attempt with email: $email. Error: $resultEmail")
                            onError(resultEmail ?: "Invalid Credentials or Firebase Auth Error.")
                        }
                    }
                }
            } else {
                if ((normalizedEmail == "abhirajkumarbat@gmail.com" || normalizedEmail == "batrajbabu@gmail.com") && password == "bat@bad+9122386567") {
                    _isAdminLoggedIn.value = true
                    addAuditLog("Admin session unlocked successfully (Local Dev Sandbox Mode) for $normalizedEmail.")
                    onSuccess()
                } else {
                    addAuditLog("Failed local login attempt with email: $email.")
                    onError("Invalid local email or password. (Local Dev Sandbox Mode active due to missing Firebase configuration)")
                }
            }
        }
    }

    fun adminLogout() {
        addAuditLog("Admin session terminated manually.")
        repository.signOutFirebase()
        _isAdminLoggedIn.value = false
        _currentScreen.value = "HOME"
    }

    // Admin Services CRUD
    fun addService(service: Service) {
        viewModelScope.launch {
            repository.saveService(service)
            addAuditLog("Created new service: ${service.title}")
        }
    }

    fun editService(service: Service) {
        viewModelScope.launch {
            repository.saveService(service)
            addAuditLog("Modified service parameters: ${service.title}")
        }
    }

    fun deleteService(service: Service) {
        viewModelScope.launch {
            repository.deleteService(service)
            addAuditLog("Archived service: ${service.title}")
        }
    }

    // Admin Products CRUD
    fun addProduct(product: Product) {
        viewModelScope.launch {
            repository.saveProduct(product)
            addAuditLog("Stocked new digital product: ${product.title}")
        }
    }

    fun editProduct(product: Product) {
        viewModelScope.launch {
            repository.saveProduct(product)
            addAuditLog("Modified digital product: ${product.title}")
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            addAuditLog("Archived digital product: ${product.title}")
        }
    }

    // Order state transitions & Screenshot manual verifications
    fun verifyOrderPayment(orderId: String, status: String, adminNotes: String = "") {
        viewModelScope.launch {
            val existingOrders = orders.value
            val order = existingOrders.find { it.id == orderId } ?: return@launch
            val updated = order.copy(
                paymentStatus = status,
                isDownloadEnabled = (status == "VERIFIED" || status == "COMPLETED"),
                adminNotes = adminNotes
            )
            repository.updateOrder(updated)
            addNotification(
                title = "Order Status Updated",
                text = "Order $orderId status changed to $status.",
                type = "ORDER"
            )
            addAuditLog("Set payment status for Order $orderId to $status.")
        }
    }

    // Message triage/Archiving
    fun updateMessageStatus(msgId: String, read: Boolean, archived: Boolean) {
        viewModelScope.launch {
            val existing = messages.value
            val msg = existing.find { it.id == msgId } ?: return@launch
            val updated = msg.copy(isRead = read, isArchived = archived)
            repository.updateMessage(updated)
        }
    }

    // Review Moderation
    fun approveReview(reviewId: String) {
        viewModelScope.launch {
            val review = reviews.value.find { it.id == reviewId } ?: return@launch
            repository.saveReview(review.copy(isApproved = true))
            addAuditLog("Approved review ID $reviewId for public site.")
        }
    }

    fun featureReview(reviewId: String, featured: Boolean) {
        viewModelScope.launch {
            val review = reviews.value.find { it.id == reviewId } ?: return@launch
            repository.saveReview(review.copy(isFeatured = featured))
            addAuditLog("Featured review ID $reviewId on home widgets.")
        }
    }

    fun rejectReview(review: Review) {
        viewModelScope.launch {
            repository.deleteReview(review)
            addAuditLog("Rejected and deleted review ID ${review.id}.")
        }
    }

    // Settings panel updating
    fun updateSettings(settings: AppSettings) {
        viewModelScope.launch {
            repository.updateSettings(settings)
            addAuditLog("Updated core system settings.")
        }
    }

    // Backup Systems (JSON string outputs)
    fun exportSystemState(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val state = repository.exportBackupJson()
            addAuditLog("Exported full local database backup JSON.")
            onResult(state)
        }
    }

    fun restoreSystemState(json: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.importBackupJson(json)
            if (success) {
                addAuditLog("Restored database state from backup JSON.")
            } else {
                addAuditLog("Failed database restore attempt (Malformed JSON).")
            }
            onResult(success)
        }
    }

    // In-memory notifications log management
    private fun addNotification(title: String, text: String, type: String) {
        val newItem = NotificationItem(title = title, text = text, type = type)
        val currentList = _notifications.value.toMutableList()
        currentList.add(0, newItem)
        _notifications.value = currentList
    }

    fun markNotificationsRead() {
        val current = _notifications.value.map { it.copy(isRead = true) }
        _notifications.value = current
    }

    fun clearNotifications() {
        _notifications.value = emptyList()
    }
}
