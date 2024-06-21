package com.example.homework3.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.homework3.data.NewsItem

@Dao
interface NewsItemDao {

    // Get all news items as LiveData
    @Query("SELECT * from news_item")
    fun getAllNews(): LiveData<List<NewsItem>>

    // Find a news item by title
    @Query("SELECT * from news_item WHERE title = :xyz")
    suspend fun findNewsByTitle(xyz: String): NewsItem?

    // Find a news item by unique identifier
    @Query("SELECT * FROM news_item WHERE uniqueIdentifier = :uniqueIdentifier")
    suspend fun findNewsById(uniqueIdentifier: String): NewsItem?

    // Insert a list of news items, replacing on conflict
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCards(cards: List<NewsItem>): List<Long>

    // Delete all news items
    @Query("DELETE FROM news_item")
    suspend fun deleteAllNews()

    // Delete news items older than a certain date
    @Query("DELETE FROM news_item WHERE publicationDate < :timeInMillis")
    suspend fun deleteOldNewsItems(timeInMillis: Long)

    // Get the count of news items
    @Query("SELECT COUNT(*) FROM news_item")
    suspend fun getNewsCount(): Int

    // Get all news items synchronously
    @Query("SELECT * from news_item")
    suspend fun getAllNewsSync(): List<NewsItem>
}
