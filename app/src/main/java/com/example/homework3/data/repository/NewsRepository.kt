package com.example.homework3.data.repository

import android.util.Log
import com.example.homework3.data.NewsItem
import com.example.homework3.data.db.NewsItemDao
import com.example.homework3.data.download.NewsDownloader
import java.util.concurrent.TimeUnit

class NewsRepository(
    private val newsDownloader: NewsDownloader,
    private val newsItemDao: NewsItemDao
) {
    // LiveData list of all news items from the database
    val allNews = newsItemDao.getAllNews()

    // Fetch news items from a URL, filter new items, and save them to the database
    suspend fun fetchNews(url: String): List<NewsItem> {
        // Download news from the given URL
        val fetchedNews = newsDownloader.load(url) ?: emptyList()

        // Retrieve existing news items from the database
        val existingNews = newsItemDao.getAllNewsSync()

        // Filter the newly fetched news to include only new items
        val newNews = fetchedNews.filter { fetchedItem ->
            existingNews.none { existingItem -> existingItem.uniqueIdentifier == fetchedItem.uniqueIdentifier }
        }

        // Insert new news items into the database if there are any
        if (newNews.isNotEmpty()) {
            newsItemDao.insertCards(newNews)
        }

        // Log the unique identifiers of the new news items
        newNews.forEach { newsItem ->
            Log.d("FetchNews", "New news item ID being returned: ${newsItem.uniqueIdentifier}")
        }

        return newNews
    }

    // Clear all news items from the database
    suspend fun clearNews() {
        newsItemDao.deleteAllNews()
        Log.i("NewsRepository", "Previous data deleted from the database")
    }

    // Find a news item by its title
    suspend fun findNewsByTitle(title: String): NewsItem? {
        return newsItemDao.findNewsByTitle(title)
    }

    // Clear news items older than the specified number of days
    suspend fun clearOldNews(days: Int) {
        newsItemDao.deleteOldNewsItems(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(days.toLong()))
    }

    // Check if the database is empty
    suspend fun isDatabaseEmpty(): Boolean {
        return newsItemDao.getNewsCount() == 0
    }
}
