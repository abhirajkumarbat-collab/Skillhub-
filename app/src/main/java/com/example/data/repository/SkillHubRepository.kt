package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.data.local.*
import com.example.data.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class SkillHubRepository(
    private val context: Context,
    private val serviceDao: ServiceDao,
    private val productDao: ProductDao,
    private val orderDao: OrderDao,
    private val messageDao: MessageDao,
    private val reviewDao: ReviewDao,
    private val portfolioDao: PortfolioDao,
    private val settingsDao: SettingsDao,
    private val chatDao: ChatDao
) {
    private val tag = "SkillHubRepository"

    // Firebase References (Safe Access)
    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w(tag, "Firebase Firestore is not initialized or configured: ${e.message}")
            null
        }
    }

    private val firebaseAuth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.w(tag, "Firebase Auth is not initialized or configured: ${e.message}")
            null
        }
    }

    fun isFirebaseAuthAvailable(): Boolean {
        return firebaseAuth != null
    }

    fun signInWithFirebase(email: String, password: String, onComplete: (Boolean, String?) -> Unit) {
        val auth = firebaseAuth
        if (auth == null) {
            onComplete(false, "Firebase Auth is not configured (missing google-services.json)")
            return
        }
        auth.signInWithEmailAndPassword(email.trim(), password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        onComplete(true, user.email)
                    } else {
                        onComplete(false, "Authentication succeeded but current user is null.")
                    }
                } else {
                    onComplete(false, task.exception?.localizedMessage ?: "Unknown Firebase Authentication Error")
                }
            }
    }

    fun signOutFirebase() {
        firebaseAuth?.signOut()
    }

    // Live Data Flows from Room
    val allServices: Flow<List<Service>> = serviceDao.getAllServices()
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val allOrders: Flow<List<Order>> = orderDao.getAllOrders()
    val allMessages: Flow<List<ContactMessage>> = messageDao.getAllMessages()
    val allReviews: Flow<List<Review>> = reviewDao.getAllReviews()
    val allPortfolioItems: Flow<List<PortfolioItem>> = portfolioDao.getAllPortfolioItems()
    val appSettings: Flow<AppSettings?> = settingsDao.getSettingsFlow()
    val chatHistory: Flow<List<ChatMessage>> = chatDao.getAllMessages()

    init {
        // Initialize Seed Data on first launch
        CoroutineScope(Dispatchers.IO).launch {
            seedDatabaseIfNeeded()
        }
    }

    // Seed Initial Data to make the app gorgeous and complete immediately
    private suspend fun seedDatabaseIfNeeded() {
        val existingSettings = settingsDao.getSettingsDirect()
        if (existingSettings == null) {
            Log.d(tag, "Seeding database with premium SkillHub initial data...")

            // Seed Settings
            val defaultSettings = AppSettings(
                id = "app_settings",
                websiteName = "SkillHub",
                ownerName = "Abhiraj Kumar",
                tagline = "Digital Expertise Hub",
                profilePhotoUrl = "profile_abhiraj_kumar", // Points to local resource
                paymentInstructions = "Choose your QR code or scan the UPI ID to pay. Upload a screenshot and enter transaction ID to verify your payment. Abhiraj will manually review and unlock your download instantly!"
            )
            settingsDao.insertOrUpdate(defaultSettings)

            // Seed Services
            val services = listOf(
                Service("s1", "Logo Making (Social Media)", "Professional, bespoke high-impact branding logo customized for all major social media platforms (Instagram, YouTube, Discord, Twitter).", 15.00, "1 Day", "brush", "Design", 1),
                Service("s2", "Thumbnail Making", "Eye-catching, high click-through-rate thumbnails optimized for YouTube, Twitch, and digital campaigns to maximize viral reach.", 25.00, "1 Day", "image", "Design", 2),
                Service("s3", "Social Media Channel Management", "Full-stack management of Instagram, YouTube, and Discord. Content strategy, upload scheduling, caption scripting, and active user engagement.", 199.00, "14 Days", "share", "Marketing", 3),
                Service("s4", "Influencer Services & Collabs", "Influencer promotion, brand alignment, viral marketing campaigns, and outreach strategies to multiply brand value.", 250.00, "7 Days", "star", "Marketing", 4),
                Service("s5", "Typing & Email/DM Management", "Fast, high-accuracy professional copy typing, automated spreadsheet data entry, and business client email/DM communication management.", 10.00, "1 Day", "email", "Admin Support", 5),
                Service("s6", "Professional Video Editing", "High-impact video edits for YouTube, TikTok Reels, or promo courses. Complete with modern transitions, clean color grading, and sound design.", 89.00, "2 Days", "movie", "Media", 6),
                Service("s7", "Photo & Art Editing", "Expert Photoshop retouching, background removal, visual effects, and color enhancements for e-commerce, portfolios, or social media.", 20.00, "1 Day", "face", "Design", 7),
                Service("s8", "Script Writing - HTML, CSS, JS", "Bespoke front-end scripting, custom interactive templates, utility scripts, and landing pages coded directly from scratch.", 150.00, "3 Days", "terminal", "Development", 8),
                Service("s9", "Watermark-Free AI Art Visuals", "Hyper-realistic artistic illustrations, visual concept assets, or custom graphics generated via advanced generative prompt diffusion models with absolute zero watermarks.", 45.00, "1 Day", "brush", "AI Generation", 9),
                Service("s10", "Automation & APIs", "Set up background automated web scrapers, automated messaging funnels, data syndication, and API integration flows to streamline your business.", 120.00, "3 Days", "settings", "Development", 10),
                Service("s11", "App, Games, & Web Development", "Production-grade custom native Android apps, premium cross-platform Jetpack Compose solutions, indie games, or premium web app design.", 499.00, "7 Days", "android", "Development", 11),
                Service("s12", "Mod APK Development", "Custom functional Android application modification, feature enhancements, and specialized APK customization where legally permitted.", 40.00, "2 Days", "build", "Development", 12),
                Service("s13", "Game Control Panels", "Setting up or selling game admin control panels, custom dashboard integrations, server managers, and multiplayer configuration controls.", 60.00, "2 Days", "gamepad", "Development", 13),
                Service("s14", "AI Movie Making & Editing", "Full cinematic AI movies generated and post-produced via premium synthesis models, combining professional voice synthesis and customized visual frames.", 250.00, "5 Days", "movie", "AI Generation", 14),
                Service("s15", "Song & Poem Writing", "Creative custom lyrics, heartfelt poetry, script narratives, or copy writing tailored around your designated themes or melodies.", 35.00, "2 Days", "edit", "Creative", 15),
                Service("s16", "AI Video Bundle Pack", "Premium downloadable bundle featuring curated watermark-free aesthetic stock loops, AI background compilations, and social media starter packs.", 29.99, "1 Day", "folder", "AI Generation", 16)
            )
            serviceDao.insertAll(services)

            // Seed Products
            val products = listOf(
                Product("p1", "SaaS Bootstrap Boilerplate", "Build your next micro-SaaS with this ultimate web development boilerplate. Clean architecture, auth ready, billing integrated.", 49.99, 10.0, 4.9f, 24, "Web Development", listOf("Next.js", "React", "SaaS"), true, "boilerplate_cover", "IN_STOCK", listOf("saas_boilerplate.zip")),
                Product("p2", "Midjourney Ultimate Prompt Guide", "Unlock over 1,500 hyper-realistic, stylistic, and graphic design prompts with our comprehensive PDF guide.", 9.99, 0.0, 4.8f, 112, "AI Art", listOf("Midjourney", "Prompt Engineering", "PDF"), true, "prompt_guide_cover", "IN_STOCK", listOf("midjourney_prompts.pdf")),
                Product("p3", "Cinematic LUTS & Transition Presets", "Upgrade your video editing workflow with 40 premium Color LUTS and seamless Premiere/CapCut transitions.", 19.90, 20.0, 5.0f, 15, "Video Production", listOf("LUTS", "Premiere", "Editing"), false, "luts_cover", "IN_STOCK", listOf("cinematic_luts.zip")),
                Product("p4", "Gemini AI Chatbot Kotlin Template", "A native Android Kotlin Jetpack Compose template featuring real-time streaming chatbot responses via Direct REST APIs.", 29.99, 15.0, 4.9f, 8, "Android Development", listOf("Android", "Compose", "Gemini"), true, "kotlin_gemini_cover", "IN_STOCK", listOf("android_gemini_chat.zip"))
            )
            productDao.insertAll(products)

            // Seed Portfolio Items
            val portfolioItems = listOf(
                PortfolioItem("port1", "E-Commerce Android App", "Android", "", "", "A full-featured shopping application built with Jetpack Compose, Room Database, and Stripe payment gateway."),
                PortfolioItem("port2", "AI Portrait Generator", "AI", "", "", "A customized Stable Diffusion workflow generating elite portraits with face-swapping capabilities."),
                PortfolioItem("port3", "Fintech Dashboard", "Web", "", "", "Premium glassmorphism dashboard styling for real-time financial indicators and crypto tracking."),
                PortfolioItem("port4", "Tech Channel Video Edits", "Video", "", "", "A compilation of highly cinematic editing work for popular creators, achieving 1M+ views.")
            )
            portfolioDao.insertAll(portfolioItems)

            // Seed Reviews
            val reviews = listOf(
                Review("r1", 5, "Nikhil Sharma", "", "Abhiraj is an absolute genius! The Android app he developed for my business is flawless, responsive, and beautifully styled.", true, true),
                Review("r2", 5, "Sophia Carter", "", "Purchased the Midjourney prompt guidebook, and my graphics look entirely premium now. Absolutely worth every dollar!", true, true),
                Review("r3", 5, "Rohit Verma", "", "Outstanding web design! He utilized an elite blue-violet gradient theme with gorgeous animations. Extremely responsive.", true, true)
            )
            reviewDao.insertAll(reviews)

            // Try syncing with Firestore (only if Firestore is available)
            syncLocalToFirestore()
        }
    }

    // Sync Local Seed Data to Firestore safely
    private fun syncLocalToFirestore() {
        val firestoreDb = firestore ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val services = serviceDao.getAllServices().firstOrNull() ?: emptyList()
                val products = productDao.getAllProducts().firstOrNull() ?: emptyList()
                val settings = settingsDao.getSettingsDirect() ?: AppSettings()

                // Save to Firestore collections
                for (s in services) {
                    firestoreDb.collection("services").document(s.id).set(s, SetOptions.merge())
                }
                for (p in products) {
                    firestoreDb.collection("products").document(p.id).set(p, SetOptions.merge())
                }
                firestoreDb.collection("settings").document("app_settings").set(settings, SetOptions.merge())
                Log.d(tag, "Successfully synchronized seeded data to Firebase Firestore.")
            } catch (e: Exception) {
                Log.w(tag, "Failed to push seeds to Firestore: ${e.message}")
            }
        }
    }

    // --- SERVICES MANAGEMENT ---
    suspend fun saveService(service: Service) {
        serviceDao.insert(service)
        firestore?.collection("services")?.document(service.id)?.set(service, SetOptions.merge())
    }

    suspend fun deleteService(service: Service) {
        serviceDao.delete(service)
        firestore?.collection("services")?.document(service.id)?.delete()
    }

    suspend fun deleteServiceById(id: String) {
        serviceDao.deleteById(id)
        firestore?.collection("services")?.document(id)?.delete()
    }

    // --- PRODUCTS MANAGEMENT ---
    suspend fun saveProduct(product: Product) {
        productDao.insert(product)
        firestore?.collection("products")?.document(product.id)?.set(product, SetOptions.merge())
    }

    suspend fun deleteProduct(product: Product) {
        productDao.delete(product)
        firestore?.collection("products")?.document(product.id)?.delete()
    }

    suspend fun deleteProductById(id: String) {
        productDao.deleteById(id)
        firestore?.collection("products")?.document(id)?.delete()
    }

    // --- PORTFOLIO MANAGEMENT ---
    suspend fun savePortfolioItem(item: PortfolioItem) {
        portfolioDao.insert(item)
        firestore?.collection("portfolio")?.document(item.id)?.set(item, SetOptions.merge())
    }

    suspend fun deletePortfolioItem(item: PortfolioItem) {
        portfolioDao.delete(item)
        firestore?.collection("portfolio")?.document(item.id)?.delete()
    }

    // --- ORDER MANAGEMENT & DIGITAL DELIVERY ---
    suspend fun placeOrder(
        customerName: String,
        email: String,
        phone: String,
        product: Product,
        transactionId: String,
        screenshotUri: String = ""
    ): Order {
        val dateFmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val timeFmt = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val curDate = Date()

        val order = Order(
            id = "SH-${UUID.randomUUID().toString().take(6).uppercase(Locale.ROOT)}",
            customerName = customerName,
            email = email,
            phone = phone,
            productId = product.id,
            productTitle = product.title,
            amount = product.finalPrice,
            paymentStatus = "PENDING",
            transactionId = transactionId,
            screenshotUri = screenshotUri,
            dateString = dateFmt.format(curDate),
            timeString = timeFmt.format(curDate),
            adminNotes = "Awaiting screenshot verification.",
            isDownloadEnabled = false
        )

        orderDao.insert(order)
        firestore?.collection("orders")?.document(order.id)?.set(order, SetOptions.merge())
        return order
    }

    suspend fun updateOrder(order: Order) {
        orderDao.insert(order) // Room replace
        firestore?.collection("orders")?.document(order.id)?.set(order, SetOptions.merge())
    }

    suspend fun deleteOrderById(id: String) {
        orderDao.deleteById(id)
        firestore?.collection("orders")?.document(id)?.delete()
    }

    // --- CONTACT MESSAGES ---
    suspend fun sendMessage(
        name: String,
        email: String,
        phone: String,
        description: String,
        budget: String,
        attachmentUri: String = ""
    ): ContactMessage {
        val msg = ContactMessage(
            id = "MSG-${UUID.randomUUID().toString().take(6).uppercase(Locale.ROOT)}",
            senderName = name,
            email = email,
            phone = phone,
            projectDescription = description,
            budget = budget,
            attachmentUri = attachmentUri
        )
        // Store locally and in Firebase Firestore
        messageDao.insert(msg)
        firestore?.collection("messages")?.document(msg.id)?.set(msg, SetOptions.merge())

        // Securely Send via EmailJS REST API matching Abhiraj's request
        withContext(Dispatchers.IO) {
            try {
                val emailjsPublicKey = BuildConfig.EMAILJS_PUBLIC_KEY
                val client = OkHttpClient.Builder()
                    .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    .build()

                val url = "https://api.emailjs.com/api/v1.0/email/send"

                val templateParams = JSONObject().apply {
                    put("name", name)
                    put("from_name", name)
                    put("email", email)
                    put("from_email", email)
                    put("reply_to", email)
                    put("to_email", "batrajbabu@gmail.com")
                    put("admin_email", "batrajbabu@gmail.com")
                    put("phone", phone)
                    put("budget", budget)
                    put("description", description)
                    put("project_description", description)
                    put("message", description)
                    put("attachment_url", attachmentUri.ifEmpty { "None" })
                }

                val jsonBody = JSONObject().apply {
                    put("service_id", "service_6440qmq")
                    put("template_id", "template_e2lalcu")
                    put("user_id", emailjsPublicKey.ifEmpty { "qF4vX7PtlLyTyIcir" })
                    put("template_params", templateParams)
                }

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val requestBody = jsonBody.toString().toRequestBody(mediaType)
                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    Log.d("SkillHubRepository", "EmailJS successfully sent project lead email to batrajbabu@gmail.com / abhirajkumarbat@gmail.com")
                } else {
                    Log.e("SkillHubRepository", "EmailJS sending failed with status code ${response.code}: ${response.body?.string()}")
                }

                // 2. Automatically dispatch Client Booking Confirmation email using EmailJS
                try {
                    val clientTemplateParams = JSONObject().apply {
                        put("name", name)
                        put("from_name", "Abhiraj Kumar")
                        put("to_name", name)
                        put("email", email)
                        put("reply_to", "batrajbabu@gmail.com")
                        put("to_email", email) // Send confirmation directly to client email!
                        put("admin_email", "batrajbabu@gmail.com")
                        put("phone", phone)
                        put("budget", budget)
                        val welcomeMsg = "Thank you for booking/inquiring about our services on SkillHub! We have received your project details and scheduled your initial virtual design review.\n\nYour Service Request Details:\n\"$description\"\n\nOur average response rate is within 2 hours. We look forward to building something incredible together!"
                        put("description", welcomeMsg)
                        put("project_description", welcomeMsg)
                        put("message", welcomeMsg)
                        put("attachment_url", attachmentUri.ifEmpty { "None" })
                    }

                    val clientJsonBody = JSONObject().apply {
                        put("service_id", "service_6440qmq")
                        put("template_id", "template_e2lalcu")
                        put("user_id", emailjsPublicKey.ifEmpty { "qF4vX7PtlLyTyIcir" })
                        put("template_params", clientTemplateParams)
                    }

                    val clientRequest = Request.Builder()
                        .url(url)
                        .post(clientJsonBody.toString().toRequestBody(mediaType))
                        .build()

                    val clientResponse = client.newCall(clientRequest).execute()
                    if (clientResponse.isSuccessful) {
                        Log.d("SkillHubRepository", "EmailJS successfully sent confirmation email to client: $email")
                    } else {
                        Log.e("SkillHubRepository", "EmailJS client confirmation failed with status code ${clientResponse.code}: ${clientResponse.body?.string()}")
                    }
                } catch (clientEx: Exception) {
                    Log.e("SkillHubRepository", "Error sending client confirmation EmailJS: ${clientEx.message}", clientEx)
                }
            } catch (e: Exception) {
                Log.e("SkillHubRepository", "Error sending EmailJS: ${e.message}", e)
            }
        }

        return msg
    }

    suspend fun updateMessage(message: ContactMessage) {
        messageDao.insert(message)
        firestore?.collection("messages")?.document(message.id)?.set(message, SetOptions.merge())
    }

    suspend fun deleteMessageById(id: String) {
        messageDao.deleteById(id)
        firestore?.collection("messages")?.document(id)?.delete()
    }

    // --- REVIEWS MANAGEMENT ---
    suspend fun submitReview(rating: Int, author: String, comment: String) {
        val review = Review(
            id = "REV-${UUID.randomUUID().toString().take(6).uppercase(Locale.ROOT)}",
            rating = rating,
            authorName = author,
            comment = comment,
            isApproved = false // Awaiting admin moderation
        )
        reviewDao.insert(review)
        firestore?.collection("reviews")?.document(review.id)?.set(review, SetOptions.merge())
    }

    suspend fun saveReview(review: Review) {
        reviewDao.insert(review)
        firestore?.collection("reviews")?.document(review.id)?.set(review, SetOptions.merge())
    }

    suspend fun deleteReview(review: Review) {
        reviewDao.delete(review)
        firestore?.collection("reviews")?.document(review.id)?.delete()
    }

    // --- SETTINGS MANAGEMENT ---
    suspend fun updateSettings(settings: AppSettings) {
        settingsDao.insertOrUpdate(settings)
        firestore?.collection("settings")?.document("app_settings")?.set(settings, SetOptions.merge())
    }

    // --- BACKUP & RESTORE ARCHITECTURE (JSON Export / Import) ---
    suspend fun exportBackupJson(): String = withContext(Dispatchers.IO) {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

        val services = serviceDao.getAllServices().firstOrNull() ?: emptyList()
        val products = productDao.getAllProducts().firstOrNull() ?: emptyList()
        val orders = orderDao.getAllOrders().firstOrNull() ?: emptyList()
        val messages = messageDao.getAllMessages().firstOrNull() ?: emptyList()
        val reviews = reviewDao.getAllReviews().firstOrNull() ?: emptyList()
        val settings = settingsDao.getSettingsDirect() ?: AppSettings()

        val backupObj = JSONObject().apply {
            put("website", settings.websiteName)
            put("exportTimestamp", System.currentTimeMillis())

            val servicesArray = JSONArray()
            services.forEach { s ->
                servicesArray.put(JSONObject().apply {
                    put("id", s.id)
                    put("title", s.title)
                    put("description", s.description)
                    put("startingPrice", s.startingPrice)
                    put("deliveryTime", s.deliveryTime)
                    put("iconName", s.iconName)
                    put("category", s.category)
                    put("displayOrder", s.displayOrder)
                    put("isHidden", s.isHidden)
                })
            }
            put("services", servicesArray)

            val productsArray = JSONArray()
            products.forEach { p ->
                productsArray.put(JSONObject().apply {
                    put("id", p.id)
                    put("title", p.title)
                    put("description", p.description)
                    put("price", p.price)
                    put("discountPercent", p.discountPercent)
                    put("rating", p.rating.toDouble())
                    put("category", p.category)
                    put("tags", JSONArray(p.tags))
                    put("isFeatured", p.isFeatured)
                    put("imageUrl", p.imageUrl)
                    put("stockStatus", p.stockStatus)
                    put("downloadableFiles", JSONArray(p.downloadableFiles))
                    put("isHidden", p.isHidden)
                })
            }
            put("products", productsArray)
        }

        backupObj.toString(4)
    }

    suspend fun importBackupJson(jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            if (root.has("services")) {
                val servicesArray = root.getJSONArray("services")
                val servicesList = mutableListOf<Service>()
                for (i in 0 until servicesArray.length()) {
                    val sObj = servicesArray.getJSONObject(i)
                    servicesList.add(Service(
                        id = sObj.getString("id"),
                        title = sObj.getString("title"),
                        description = sObj.getString("description"),
                        startingPrice = sObj.getDouble("startingPrice"),
                        deliveryTime = sObj.getString("deliveryTime"),
                        iconName = sObj.getString("iconName"),
                        category = sObj.getString("category"),
                        displayOrder = sObj.optInt("displayOrder", 0),
                        isHidden = sObj.optBoolean("isHidden", false)
                    ))
                }
                serviceDao.insertAll(servicesList)
            }

            if (root.has("products")) {
                val productsArray = root.getJSONArray("products")
                val productsList = mutableListOf<Product>()
                for (i in 0 until productsArray.length()) {
                    val pObj = productsArray.getJSONObject(i)
                    val tagsArray = pObj.getJSONArray("tags")
                    val tags = List(tagsArray.length()) { tagsArray.getString(it) }

                    val filesArray = pObj.optJSONArray("downloadableFiles") ?: JSONArray()
                    val files = List(filesArray.length()) { filesArray.getString(it) }

                    productsList.add(Product(
                        id = pObj.getString("id"),
                        title = pObj.getString("title"),
                        description = pObj.getString("description"),
                        price = pObj.getDouble("price"),
                        discountPercent = pObj.optDouble("discountPercent", 0.0),
                        rating = pObj.optDouble("rating", 5.0).toFloat(),
                        category = pObj.getString("category"),
                        tags = tags,
                        isFeatured = pObj.optBoolean("isFeatured", false),
                        imageUrl = pObj.optString("imageUrl", ""),
                        stockStatus = pObj.optString("stockStatus", "IN_STOCK"),
                        downloadableFiles = files,
                        isHidden = pObj.optBoolean("isHidden", false)
                    ))
                }
                productDao.insertAll(productsList)
            }
            true
        } catch (e: Exception) {
            Log.e(tag, "Failed to import JSON backup: ${e.message}")
            false
        }
    }


    // --- INTELLIGENT AI CHATBOT ENGINE ---
    suspend fun saveChatMessage(sender: String, text: String) {
        chatDao.insert(ChatMessage(sender = sender, message = text))
    }

    suspend fun clearChatHistory() {
        chatDao.clearHistory()
    }

    // Direct REST Gemini API call matching our gemini-api SKILL guidelines
    suspend fun getAiResponse(userMessage: String, systemPrompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val liveServices = serviceDao.getAllServices().firstOrNull() ?: emptyList()
        val liveProducts = productDao.getAllProducts().firstOrNull() ?: emptyList()
        val liveOrders = orderDao.getAllOrders().firstOrNull() ?: emptyList()
        val livePortfolio = portfolioDao.getAllPortfolioItems().firstOrNull() ?: emptyList()
        val liveReviews = reviewDao.getAllReviews().firstOrNull() ?: emptyList()
        val settings = settingsDao.getSettingsDirect() ?: AppSettings()

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            // Offline/No Key Fallback: Dynamic smart business answer based on real live data
            return@withContext getOfflineSmartResponse(userMessage, liveServices, liveProducts, liveOrders, livePortfolio, liveReviews, settings)
        }

        // Format dynamic context based on live database pricing
        val servicesStr = if (liveServices.isEmpty()) {
            "No custom services are loaded."
        } else {
            liveServices.joinToString("\n") { s ->
                "- **${s.title}** (${s.category}): Starts at $${s.startingPrice}. Delivery Estimate: ${s.deliveryTime}. Description: ${s.description}"
            }
        }

        val productsStr = if (liveProducts.isEmpty()) {
            "No store products are loaded."
        } else {
            liveProducts.joinToString("\n") { p ->
                "- **${p.title}** (${p.category}): $${p.price} with ${p.discountPercent}% off. Status: ${p.stockStatus}. Description: ${p.description}"
            }
        }

        val ordersStr = if (liveOrders.isEmpty()) {
            "No customer orders registered yet."
        } else {
            liveOrders.take(15).joinToString("\n") { o ->
                "- ID: ${o.id} | Name: ${o.customerName} | Email: ${o.email} | Product: ${o.productTitle} | Status: ${o.paymentStatus} | Delivery URL/Notes: ${o.adminNotes}"
            }
        }

        val portfolioStr = if (livePortfolio.isEmpty()) {
            "No portfolio items loaded."
        } else {
            livePortfolio.joinToString("\n") { item ->
                "- **${item.title}** [Category: ${item.category}]: ${item.description}"
            }
        }

        val reviewsStr = if (liveReviews.isEmpty()) {
            "No reviews loaded yet."
        } else {
            liveReviews.take(5).joinToString("\n") { r ->
                "- ${r.authorName} (${r.rating} Stars): \"${r.comment}\""
            }
        }

        val enrichedSystemPrompt = """
            $systemPrompt
            
            You have real-time live access to the SkillHub Database. Use this live data to answer all rate, price, cost, buy, portfolio, and order status tracking inquiries:
            
            APP BRANDING & CUSTOM SETTINGS:
            - Website Name: ${settings.websiteName}
            - Tagline: ${settings.tagline}
            - Owner/Administrator Name: ${settings.ownerName}
            - Contact Email: ${settings.contactEmail}
            - UPI Payment ID: ${settings.upiId}
            - Bank Details: ${settings.bankDetails}
            
            LIVE PREMIUM SERVICES:
            $servicesStr
            
            LIVE DIGITAL PRODUCTS:
            $productsStr
            
            LIVE PORTFOLIO PROJECTS:
            $portfolioStr
            
            LIVE CLIENT REVIEWS & SATISFACTION:
            $reviewsStr
            
            LIVE RECENT ORDER REGISTRY FOR TRACKING:
            $ordersStr
            
            IMPORTANT INSTRUCTIONS:
            1. If the client asks about rates or prices of any service or product, ALWAYS mention the exact live numbers listed above. 
            2. Aap client ke language (English, Hindi, ya Hinglish) me helpful aur professional reply dein.
            3. Order Status Tracking: If a client asks to check, track, or verify their order status (e.g., using keywords like 'order status', 'track my order', 'mera product', 'receipt', 'payment verified', or specifying an Order ID like o1, o2 or their email), match it against the LIVE RECENT ORDER REGISTRY listed above. If found, tell them the exact paymentStatus (e.g. VERIFIED, PENDING, REJECTED) and read them any Delivery notes/Drive links if paymentStatus is VERIFIED! If not found, politely ask them for their Order ID or Email so you can look it up in the secure registry.
            4. Make sure to represent Abhiraj Kumar's skills and products accurately.
        """.trimIndent()

        // Robust Model Failover Strategy to completely bypass 503 errors
        val modelsToTry = listOf("gemini-3.5-flash", "gemini-3.1-pro-preview", "gemini-3.1-flash-lite-preview")
        
        // Fetch last 12 chat history messages to establish multi-turn dialogue memory
        val history = chatDao.getAllMessages().firstOrNull() ?: emptyList()
        val formattedContents = JSONArray()
        
        var foundCurrent = false
        val lastMessages = history.takeLast(12)
        for (msg in lastMessages) {
            val role = if (msg.sender == "USER") "user" else "model"
            if (msg.sender == "USER" && msg.message == userMessage) {
                foundCurrent = true
            }
            formattedContents.put(JSONObject().apply {
                put("role", role)
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", msg.message) })
                })
            })
        }
        
        // If current message was not found in the retrieved database history, append manually
        if (!foundCurrent) {
            formattedContents.put(JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", userMessage) })
                })
            })
        }

        for (modelName in modelsToTry) {
            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    .build()

                val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"

                val jsonBody = JSONObject().apply {
                    put("contents", formattedContents)
                    put("systemInstruction", JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", enrichedSystemPrompt) })
                        })
                    })
                    put("generationConfig", JSONObject().apply {
                        put("temperature", 0.7)
                    })
                }

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val requestBody = jsonBody.toString().toRequestBody(mediaType)
                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                val responseString = response.body?.string() ?: ""

                if (response.isSuccessful && responseString.isNotEmpty()) {
                    val responseJson = JSONObject(responseString)
                    val textResponse = responseJson
                        .getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text")

                    return@withContext textResponse
                } else {
                    Log.w(tag, "Model $modelName returned status code ${response.code}. Trying next...")
                }
            } catch (e: Exception) {
                Log.e(tag, "Error with model $modelName: ${e.message}. Trying next...")
            }
        }

        // Ultimate safe fallback: Return smart response formatted with live pricing and lookup
        return@withContext getOfflineSmartResponse(userMessage, liveServices, liveProducts, liveOrders, livePortfolio, liveReviews, settings)
    }

    // Smart Local Expert Fallback System representing conversational competence with dynamic database awareness
    private fun getOfflineSmartResponse(
        message: String,
        liveServices: List<Service>,
        liveProducts: List<Product>,
        liveOrders: List<Order>,
        livePortfolio: List<PortfolioItem>,
        liveReviews: List<Review>,
        settings: AppSettings
    ): String {
        val lower = message.lowercase()

        // Formatting services
        val servicesListStr = if (liveServices.isEmpty()) {
            "*   **Website Development**: Starts from **$499.00**\n" +
            "*   **Android App Development**: Starts from **$799.00**\n" +
            "*   **Video Editing**: Starts from **$149.00**\n" +
            "*   **Thumbnail Design**: Starts from **$29.00**"
        } else {
            liveServices.joinToString("\n") { s ->
                "*   **${s.title}**: Starts from **$${s.startingPrice}** (Estimate: ${s.deliveryTime} | ${s.category})"
            }
        }

        // Formatting products
        val productsListStr = if (liveProducts.isEmpty()) {
            "*   **SaaS Bootstrap Boilerplate**: **$49.99**\n" +
            "*   **Midjourney Ultimate Prompt Guide**: **$9.99**\n" +
            "*   **Cinematic LUTS & Transition Presets**: **$19.90**\n" +
            "*   **Gemini AI Chatbot Kotlin Template**: **$29.99**"
        } else {
            liveProducts.joinToString("\n") { p ->
                "*   **${p.title}**: **$${p.price}** (${if (p.discountPercent > 0) "${p.discountPercent}% Off | " else ""}${p.stockStatus})"
            }
        }

        // Formatting portfolio items
        val portfolioListStr = if (livePortfolio.isEmpty()) {
            "*   **E-Commerce Android App**: Built using Jetpack Compose and Room DB.\n" +
            "*   **SaaS Dashboard Landing Page**: High converting Next.js portal.\n" +
            "*   **AI Image Generator Integration**: Built using Gemini Vision API."
        } else {
            livePortfolio.joinToString("\n") { item ->
                "*   **${item.title}** (${item.category}): ${item.description}"
            }
        }

        // Formatting reviews
        val reviewsListStr = if (liveReviews.isEmpty()) {
            "*   **Rohan Sharma**: \"Excellent delivery! Abhiraj made a spectacular app for my business.\"\n" +
            "*   **Sanjana Verma**: \"The Video editing is top class, very engaging transitions.\""
        } else {
            liveReviews.take(4).joinToString("\n") { r ->
                "*   **${r.authorName}** (${r.rating}⭐): \"${r.comment}\""
            }
        }

        // Order Status Lookup (Search by ID, Name or Email)
        val matchedOrder = liveOrders.find { order ->
            lower.contains(order.id.lowercase()) ||
            (order.transactionId.isNotEmpty() && lower.contains(order.transactionId.lowercase())) ||
            (order.email.isNotEmpty() && lower.contains(order.email.lowercase())) ||
            (order.customerName.isNotEmpty() && lower.contains(order.customerName.lowercase()))
        }

        return when {
            // Order Lookup triggers
            matchedOrder != null -> {
                "**Order Status Found (Aapka Order Status mil gaya hai) 🔍**\n\n" +
                "E-Registry checked successfully:\n" +
                "*   **Order ID**: `${matchedOrder.id}`\n" +
                "*   **Client Name**: `${matchedOrder.customerName}`\n" +
                "*   **Product/Service**: `${matchedOrder.productTitle}`\n" +
                "*   **Amount**: `Rs ${matchedOrder.amount}`\n" +
                "*   **Payment Status**: **${matchedOrder.paymentStatus}**\n\n" +
                when (matchedOrder.paymentStatus) {
                    "VERIFIED" -> "🎉 **Aapka payment verify ho chuka hai!** Aapka order approved hai. \n👉 **Delivery Notes/Download Link**: ${matchedOrder.adminNotes.ifEmpty { "No notes provided yet." }}"
                    "REJECTED" -> "❌ **Payment status: REJECTED.** Details mismatch. Please contact Abhiraj with valid UPI transaction screenshot."
                    else -> "⏳ **Aapka payment verification abhi PENDING hai.** Admin (Abhiraj) aapke upload kiye transaction details ko check kar rahe hain. Jald hi aapka product unlock ho jayega!"
                }
            }

            // Explicit order checking intent but no order matched
            lower.contains("order") || lower.contains("track") || lower.contains("status") || lower.contains("receipt") || lower.contains("invoice") || lower.contains("verify") || lower.contains("mera status") || lower.contains("mera download") -> {
                "**Order & Invoice Status Tracking 🔍**\n\n" +
                "Aap is chatbot me directly apna **Order ID** (jaise `o1`, `o2`), registered **Email Address**, ya **Transaction ID** type karke send karein.\n\n" +
                "Mera smart offline system secure database se match karke instantly aapka live payment status aur completion notes retrieve kar lega!"
            }

            // Greetings (English, Hindi, Hinglish)
            lower.contains("hello") || lower.contains("hi ") || lower.contains("hey") || lower.contains("namaste") || lower.contains("pranam") || lower.contains("kya haal") || lower.contains("kaise ho") || lower.trim() == "hi" -> {
                "**Namaste! Welcome to ${settings.websiteName}!** 🙏\n\n" +
                "I am **${settings.ownerName}'s** smart AI assistant. Main ${settings.websiteName} ke premium services, store products, portfolio projects, aur order status tracking me aapki instant help kar sakta hoon.\n\n" +
                "**Aap mujhse ye details pooch sakte hain:**\n" +
                "1. 🛠️ **Services**: Hire rates & categories\n" +
                "2. 📦 **Products**: Digital store downloads in stock\n" +
                "3. 💰 **Price**: Exact rate card for items\n" +
                "4. 💳 **Payment/UPI**: QR code, Bank details & payment flow\n" +
                "5. 🔍 **Track Order**: Enter order ID or Email to view verification status!\n" +
                "6. 📞 **Contact**: WhatsApp, Email, or Social connections\n\n" +
                "Aap Hindi, Hinglish ya English me inquiry bhej sakte hain!"
            }

            // Pricing & Buying options: "kitna", "price", "daam", "buy", "cost", "khareed", "purchase"
            lower.contains("price") || lower.contains("daam") || lower.contains("kitna") || lower.contains("kitne") || lower.contains("cost") || lower.contains("charge") || lower.contains("rate") || lower.contains("money") || lower.contains("rupay") || lower.contains("rupee") || lower.contains("usd") || lower.contains("dollar") || lower.contains("how much") || lower.contains("buy") || lower.contains("khareed") || lower.contains("purchase") || lower.contains("khared") -> {
                "**SkillHub Pricing & Purchase Catalog (Hiring & Store Rates) 💰**\n\n" +
                "Hum premium development services and ready-to-use digital templates offer karte hain:\n\n" +
                "### 📦 1. Digital Store Products (Instant Download):\n$productsListStr\n\n" +
                "### 🛠️ 2. Premium Services & Hiring Rates:\n$servicesListStr\n\n" +
                "**Kharidne ka tareeka (How to Buy):**\n" +
                "*   **Digital Products**: **Store** page par jaakar product ke 'Buy Now' par click karein. Pay with GPay/PhonePe and copy the transaction ID, and upload payment screenshot. Instant approval ke baad download active ho jayega!\n" +
                "*   **Custom Services**: **Services** screen se direct query form fill karke request bheinjein ya fir **Contact** form fill karein."
            }

            // Services details
            lower.contains("service") || lower.contains("work") || lower.contains("kaam") || lower.contains("website") || lower.contains("app") || lower.contains("video") || lower.contains("editing") || lower.contains("thumbnail") || lower.contains("design") || lower.contains("automation") -> {
                "**Custom Professional Services 🛠️**\n\n" +
                "Abhiraj Kumar and team provide industry-standard development and digital solutions:\n\n" +
                "$servicesListStr\n\n" +
                "👉 *Aap directly **Services** screen par custom service card ke neeche 'Hire Now' button use karke request bheinjein. Hum customized quote coordinate karenge!*"
            }

            // Store products details
            lower.contains("product") || lower.contains("store") || lower.contains("zip") || lower.contains("pdf") || lower.contains("boilerplate") || lower.contains("prompt") || lower.contains("midjourney") || lower.contains("template") || lower.contains("download") || lower.contains("code") -> {
                "**Digital Download Store 📦**\n\n" +
                "Instant production-ready digital tools designed for developers and creators:\n\n" +
                "$productsListStr\n\n" +
                "👉 *Store purchase verify hone par file aapke dashboard (My Activity) aur Store me automatically download ke liye available ho jati hai.*"
            }

            // Payment / manual check/ UPI flow details
            lower.contains("payment") || lower.contains("pay") || lower.contains("gpay") || lower.contains("phonepe") || lower.contains("paytm") || lower.contains("upi") || lower.contains("qr") || lower.contains("screenshot") || lower.contains("transfer") || lower.contains("paisa") || lower.contains("bank") || lower.contains("account") -> {
                "**Secure Payment Methods & Setup 💳**\n\n" +
                "### 🔗 1. UPI Payment ID:\n" +
                "**`${settings.upiId}`**\n\n" +
                "### 🏦 2. Bank Wire Transfer:\n" +
                "**${settings.bankDetails}**\n\n" +
                "### 📜 Instructions:\n" +
                "${settings.paymentInstructions}\n\n" +
                "Aap pay karne ke baad transaction id feed karein aur payment receipt upload karein. Admin panel me iski verification manually aur automated process ke through jaldi approve ki jati hai."
            }

            // Contacts / handles queries
            lower.contains("contact") || lower.contains("email") || lower.contains("phone") || lower.contains("whatsapp") || lower.contains("connect") || lower.contains("social") || lower.contains("chat") || lower.contains("message") || lower.contains("number") -> {
                "**How to Connect with Abhiraj Kumar 📞**\n\n" +
                "Aap directly creator and site administrator se connect kar sakte hain:\n\n" +
                "1. 📩 **Direct Email**: `${settings.contactEmail}`\n" +
                "2. 💬 **WhatsApp**: Direct reach out via screen header WhatsApp icons.\n" +
                "3. 🤝 **Contact Inquiry Form**: Navigate to **Contact** tab on main dashboard. Enter specifications, budget (Rs), and attachments to post query directly to secure cloud storage."
            }

            // Abhiraj Kumar info
            lower.contains("abhiraj") || lower.contains("who is") || lower.contains("owner") || lower.contains("malik") || lower.contains("kumar") || lower.contains("skills") || lower.contains("expert") -> {
                "**About Abhiraj Kumar (Founder) 👑**\n\n" +
                "Abhiraj Kumar is an expert Full Stack Engineer, Native Android Architect, UI/UX Consultant, and digital content creator.\n\n" +
                "*   **Specialties**: Kotlin/Jetpack Compose Native Android development, Next.js, Firebase Backend Systems, secure server infrastructure, high impact video transition edits, and AI product integration.\n" +
                "*   **Contact Email**: `${settings.contactEmail}`\n\n" +
                "**Reviews & Client Testimonials:**\n" +
                "$reviewsListStr"
            }

            // Portfolio & projects
            lower.contains("portfolio") || lower.contains("project") || lower.contains("experience") || lower.contains("work") || lower.contains("kaam") || lower.contains("past project") -> {
                "**SkillHub Creative Portfolio Projects 🎨**\n\n" +
                "Abhiraj Kumar has built several high impact digital projects:\n\n" +
                "$portfolioListStr\n\n" +
                "Aap portfolio tab me screenshots and links live explore kar sakte hain!"
            }

            // Reviews / Testimonials
            lower.contains("review") || lower.contains("rating") || lower.contains("feedback") || lower.contains("testimonial") || lower.contains("log kya") || lower.contains("customer say") -> {
                "**What Clients Say About SkillHub (Testimonials) ⭐**\n\n" +
                "$reviewsListStr\n\n" +
                "👉 *Aap directly **Reviews** screen par jaakar apna stars and authentic feedback add kar sakte hain!*"
            }

            // Catch-all general query handler
            else -> {
                "Thank you for reaching out! Main aapki help karne ke liye taiyar hoon.\n\n" +
                "Aap mujhse pooch sakte hain:\n" +
                "*   **Pricing Card & Price List** of services/products\n" +
                "*   **Track Order Status** with Order ID/Email\n" +
                "*   **Payment UPI & Bank credentials**\n" +
                "*   **Portfolio items** built by Abhiraj\n" +
                "*   **How to Buy** templates and checkouts\n\n" +
                "Please type your query and I will fetch live results from our local repository database!"
            }
        }
    }
}
