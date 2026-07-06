package com.example.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.Update
import com.example.data.model.AppSettings
import com.example.data.model.ChatMessage
import com.example.data.model.ContactMessage
import com.example.data.model.Order
import com.example.data.model.PortfolioItem
import com.example.data.model.Product
import com.example.data.model.Review
import com.example.data.model.Service
import com.example.data.model.SkillHubTypeConverters
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceDao {
    @Query("SELECT * FROM services ORDER BY displayOrder ASC")
    fun getAllServices(): Flow<List<Service>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(services: List<Service>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(service: Service)

    @Update
    suspend fun update(service: Service)

    @Delete
    suspend fun delete(service: Service)

    @Query("DELETE FROM services WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY id DESC")
    fun getAllProducts(): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<Product>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: Product)

    @Update
    suspend fun update(product: Product)

    @Delete
    suspend fun delete(product: Product)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<Order>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(order: Order)

    @Update
    suspend fun update(order: Order)

    @Query("SELECT * FROM orders WHERE email = :email ORDER BY timestamp DESC")
    fun getOrdersByEmail(email: String): Flow<List<Order>>

    @Query("DELETE FROM orders WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<ContactMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: ContactMessage)

    @Update
    suspend fun update(message: ContactMessage)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews ORDER BY timestamp DESC")
    fun getAllReviews(): Flow<List<Review>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reviews: List<Review>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(review: Review)

    @Update
    suspend fun update(review: Review)

    @Delete
    suspend fun delete(review: Review)
}

@Dao
interface PortfolioDao {
    @Query("SELECT * FROM portfolio ORDER BY id DESC")
    fun getAllPortfolioItems(): Flow<List<PortfolioItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<PortfolioItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: PortfolioItem)

    @Update
    suspend fun update(item: PortfolioItem)

    @Delete
    suspend fun delete(item: PortfolioItem)
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings WHERE id = 'app_settings' LIMIT 1")
    fun getSettingsFlow(): Flow<AppSettings?>

    @Query("SELECT * FROM settings WHERE id = 'app_settings' LIMIT 1")
    suspend fun getSettingsDirect(): AppSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: AppSettings)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: ChatMessage)

    @Query("DELETE FROM chat_messages")
    suspend fun clearHistory()
}

@Database(
    entities = [
        Service::class,
        Product::class,
        Order::class,
        ContactMessage::class,
        Review::class,
        PortfolioItem::class,
        AppSettings::class,
        ChatMessage::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(SkillHubTypeConverters::class)
abstract class SkillHubDatabase : RoomDatabase() {
    abstract fun serviceDao(): ServiceDao
    abstract fun productDao(): ProductDao
    abstract fun orderDao(): OrderDao
    abstract fun messageDao(): MessageDao
    abstract fun reviewDao(): ReviewDao
    abstract fun portfolioDao(): PortfolioDao
    abstract fun settingsDao(): SettingsDao
    abstract fun chatDao(): ChatDao
}
