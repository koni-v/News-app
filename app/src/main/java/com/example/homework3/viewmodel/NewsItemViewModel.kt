package com.example.homework3.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import com.example.homework3.R
import com.example.homework3.data.NewsItem
import com.example.homework3.data.repository.NewsRepository
import kotlinx.coroutines.launch

// ViewModel for managing NewsItem data.
class NewsItemViewModel(
    application: Application,
    private val newsRepository: NewsRepository
) : AndroidViewModel(application) {

    private val TAG = "NewsItemViewModel"

    // LiveData for fetching all news items from repository
    val newsItems: LiveData<List<NewsItem>> = newsRepository.allNews

    // LiveData to indicate if an error occurred during data fetching
    private val _hasError = MutableLiveData<Boolean>()
    val hasError: LiveData<Boolean> = _hasError

    // Load news items on initialization
    init {
        reload()
    }

    // Downloads news items from the provided URL and logs success or failure.
    private fun downloadNewsItems(newsFeedUrl: String) {
        Log.d(TAG, "Starting download for URL: $newsFeedUrl")
        _hasError.value = false

        viewModelScope.launch {
            val newItems = newsRepository.fetchNews(newsFeedUrl)
            val fetchSuccessful = newItems.isNotEmpty()
            if (fetchSuccessful) {
                Log.d(TAG, "News items fetched successfully")
            } else {
                _hasError.postValue(true)
                Log.d(TAG, "Error fetching news items")
            }
        }
    }

    // Reloads news items using the URL fetched from SharedPreferences.
    private fun reload() {
        val url = getUrl()
        Log.d(TAG, "Reloading news items with URL: $url")
        downloadNewsItems(url)
    }

    // Retrieves news feed URL from SharedPreferences or defaults to a predefined URL.
    private fun getUrl(): String {
        val context = getApplication<Application>().applicationContext
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val url = sharedPreferences.getString(
            context.getString(R.string.settings_news_url_key),
            context.getString(R.string.settings_news_url_default)
        ) ?: context.getString(R.string.settings_news_url_default)
        Log.d(TAG, "Current URL from preferences: $url")
        return url
    }

    // Clears all news items from the repository.
    fun clearNews() {
        viewModelScope.launch {
            newsRepository.clearNews()
        }
    }
}

// ViewModelProviderFactory for creating instances of NewsItemViewModel.
class NewsItemViewModelProviderFactory(
    private val application: Application,
    private val newsRepository: NewsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NewsItemViewModel::class.java)) {
            return NewsItemViewModel(application, newsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
