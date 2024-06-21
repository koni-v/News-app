package com.example.homework3

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import com.example.homework3.data.NewsItem
import com.example.homework3.data.db.AppDatabase
import com.example.homework3.databinding.ActivityDetailsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Activity to display details of a news item.
class DetailsActivity : AppCompatActivity() {

    companion object {
        const val ITEM_KEY = "item"
        const val LOG_TAG = "DetailsActivity"
    }

    private lateinit var binding: ActivityDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val item = intent?.extras?.getSerializable(ITEM_KEY) as? NewsItem

        if (item != null) {
            displayNewsItem(item)
        } else {
            Log.e(LOG_TAG, "No news item found in intent")
        }
    }

    // Fetches a news item from the database by ID using Coroutine.
    private fun fetchNewsItemById(itemId: String) {
        val database = AppDatabase.getDatabase(this)
        val newsItemDao = database.newsItemDao()

        CoroutineScope(Dispatchers.IO).launch {
            val newsItem = newsItemDao.findNewsById(itemId)
            withContext(Dispatchers.Main) {
                newsItem?.let { displayNewsItem(it) }
            }
        }
    }

    // Displays the details of the news item on the UI.
    private fun displayNewsItem(item: NewsItem) {
        binding.tvCardTitle.text = item.title
        binding.tvAuthor.text = item.author

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val showImages = preferences.getBoolean("showImages", false)

        if (showImages) {
            val imageUrl = item.imageUrl
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .into(binding.ivCard)
        } else {
            binding.ivCard.visibility = View.GONE
        }

        val htmlDescription = item.description.toString()
        val webView: WebView = findViewById(R.id.webView)
        webView.loadDataWithBaseURL(null, htmlDescription, "text/html", "utf-8", null)

        binding.tvPublicationDate.text = item.publicationDate.toString()
        binding.tvKeywords.text = item.keywords.joinToString("\n")

        binding.btnfullstory.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.fullArticleLink))
            startActivity(intent)
        }
    }
}
