package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

@Entity(tableName = "services")
data class Service(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val startingPrice: Double,
    val deliveryTime: String,
    val iconName: String,
    val category: String,
    val displayOrder: Int = 0,
    val isHidden: Boolean = false
)

@Entity(tableName = "products")
data class Product(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val price: Double,
    val discountPercent: Double = 0.0,
    val rating: Float = 5.0f,
    val ratingCount: Int = 1,
    val category: String,
    val tags: List<String> = emptyList(),
    val isFeatured: Boolean = false,
    val imageUrl: String = "",
    val stockStatus: String = "IN_STOCK", // IN_STOCK, OUT_OF_STOCK
    val downloadableFiles: List<String> = emptyList(), // Zip, PDF filenames or paths
    val isHidden: Boolean = false
) {
    val finalPrice: Double
        get() = if (discountPercent > 0) price * (1.0 - (discountPercent / 100.0)) else price
}

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey val id: String,
    val customerName: String,
    val email: String,
    val phone: String,
    val productId: String,
    val productTitle: String,
    val amount: Double,
    val paymentStatus: String = "PENDING", // PENDING, VERIFIED, REJECTED, COMPLETED
    val transactionId: String,
    val screenshotUri: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String = "",
    val timeString: String = "",
    val adminNotes: String = "",
    val downloadCount: Int = 0,
    val isDownloadEnabled: Boolean = true
)

@Entity(tableName = "messages")
data class ContactMessage(
    @PrimaryKey val id: String,
    val senderName: String,
    val email: String,
    val phone: String,
    val projectDescription: String,
    val budget: String,
    val attachmentUri: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val isArchived: Boolean = false,
    val replyStatus: String = "PENDING" // PENDING, REPLIED, ARCHIVED
)

@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey val id: String,
    val rating: Int,
    val authorName: String,
    val authorAvatar: String = "",
    val comment: String,
    val isApproved: Boolean = false,
    val isFeatured: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "portfolio")
data class PortfolioItem(
    @PrimaryKey val id: String,
    val title: String,
    val category: String, // Logo, Video, Web, Android, AI
    val imageUrl: String = "",
    val videoUrl: String = "",
    val description: String = ""
)

@Entity(tableName = "settings")
data class AppSettings(
    @PrimaryKey val id: String = "app_settings",
    val websiteName: String = "SkillHub",
    val ownerName: String = "Abhiraj Kumar",
    val tagline: String = "Digital Expertise Hub",
    val profilePhotoUrl: String = "",
    val logoUrl: String = "",
    val defaultTheme: String = "SYSTEM", // LIGHT, DARK, SYSTEM
    val defaultLanguage: String = "en", // en, hi, hinglish
    val maintenanceMode: Boolean = false,
    val contactEmail: String = "batrajbabu@gmail.com",
    val whatsapp: String = "https://wa.me/919999999999",
    val telegram: String = "https://t.me/abhiraj_kumar",
    val instagram: String = "https://instagram.com/abhiraj_kumar",
    val facebook: String = "https://facebook.com/abhiraj_kumar",
    val youtube: String = "https://youtube.com/c/abhiraj_kumar",
    val discord: String = "https://discord.gg/skillhub",
    val github: String = "https://github.com/abhiraj_kumar",
    val showWhatsapp: Boolean = true,
    val showTelegram: Boolean = true,
    val showInstagram: Boolean = true,
    val showFacebook: Boolean = false,
    val showYoutube: Boolean = true,
    val showDiscord: Boolean = true,
    val showGithub: Boolean = true,
    val footerText: String = "© 2026 SkillHub. Developed by Abhiraj Kumar. All rights reserved.",
    val upiId: String = "abhirajkumar@upi",
    val bankDetails: String = "Bank: SBI\nAcc: 1234567890\nIFSC: SBIN0001234",
    val gpayQrUrl: String = "",
    val paytmQrUrl: String = "",
    val phonepeQrUrl: String = "",
    val paymentInstructions: String = "Scan the QR code or pay using UPI ID. Enter the transaction ID and upload payment screenshot below for manual verification.",
    val currentAiProvider: String = "GEMINI", // GEMINI, OPENAI, CLAUDE, DEEPSEEK
    val geminiApiKey: String = "",
    val openAiApiKey: String = "",
    val chatbotEnabled: Boolean = true,
    val chatbotSystemPrompt: String = "You are SkillHub Assistant, a smart AI representative of Abhiraj Kumar, a premium software architect, developer, and digital content creator. Help users navigate services, store products, view portfolio, and submit project requests."
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String, // USER, AI
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

class SkillHubTypeConverters {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val listStringAdapter = moshi.adapter<List<String>>(
        Types.newParameterizedType(List::class.java, String::class.java)
    )

    @TypeConverter
    fun stringToList(value: String?): List<String> {
        if (value == null) return emptyList()
        return listStringAdapter.fromJson(value) ?: emptyList()
    }

    @TypeConverter
    fun listToString(list: List<String>?): String {
        return listStringAdapter.toJson(list ?: emptyList())
    }
}
