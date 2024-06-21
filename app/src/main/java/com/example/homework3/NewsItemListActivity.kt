package com.example.homework3

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.homework3.adapter.NewsItemListAdapter
import com.example.homework3.data.db.AppDatabase
import com.example.homework3.data.download.NewsDownloader
import com.example.homework3.data.repository.NewsRepository
import com.example.homework3.databinding.ActivityMainBinding
import com.example.homework3.notification.NOTIFICATION_CHANNEL_ID
import com.example.homework3.viewmodel.NewsItemViewModel
import com.example.homework3.viewmodel.NewsItemViewModelProviderFactory
import com.example.homework3.worker.WorkerUtils

// Activity to display a list of news items and manage background tasks.
class NewsItemListActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var binding: ActivityMainBinding

    // Companion object for logging tag.
    companion object {
        const val LOG_TAG = "MainActivity"
    }

    // ViewModel instance for managing news items.
    private val newsItemViewModel: NewsItemViewModel by viewModels {
        NewsItemViewModelProviderFactory(application, createRepository())
    }

    // Creates and returns a repository instance.
    private fun createRepository(): NewsRepository {
        return NewsRepository(
            NewsDownloader(),
            AppDatabase.getDatabase(this).newsItemDao()
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up RecyclerView and its adapter
        val layoutManager = LinearLayoutManager(this)
        binding.rvNewsItem.layoutManager = layoutManager
        val adapter = NewsItemListAdapter(emptyList(), this)
        binding.rvNewsItem.adapter = adapter

        // Handle item click in RecyclerView
        adapter.itemClickListener = {
            val intent = Intent(this, DetailsActivity::class.java)
            intent.putExtra(DetailsActivity.ITEM_KEY, it)
            startActivity(intent)
        }

        // Observe changes in news items from ViewModel
        newsItemViewModel.newsItems.observe(this) { items ->
            adapter.items = items
            adapter.notifyDataSetChanged()
            Log.d(LOG_TAG, "Observing data change")
        }

        // Observe error state from ViewModel
        newsItemViewModel.hasError.observe(this) { hasError ->
            if (hasError) {
                Toast.makeText(this, "Error loading news items", Toast.LENGTH_SHORT).show()
            }
        }

        // Register SharedPreferences change listener
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        preferences.registerOnSharedPreferenceChangeListener(this)

        // Enqueue initial load if database is empty
        WorkerUtils.enqueueInitialLoadTask(this)

        // Schedule periodic updates
        WorkerUtils.enqueuePeriodicUpdateTask(this)

        // Create notification channel
        createNotificationChannel()

        // Request notification permissions if needed
        requestNotificationPermissions()

        // Log deep-link data from intent
        Log.d(LOG_TAG, "Deep-link data: ${intent.data}")
    }

    // Creates a notification channel for displaying news notifications.
    private fun createNotificationChannel() {
        val notificationChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "News Notifications",
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationChannel.description = "Notifications for news items"
        NotificationManagerCompat.from(this).createNotificationChannel(notificationChannel)
    }

    // Inflates the options menu.
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    // Handles menu item selection.
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.menu_reload -> {
                WorkerUtils.enqueueReloadTask(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Handles changes in SharedPreferences.
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == getString(R.string.settings_news_url_key)) {
            WorkerUtils.enqueueUrlChangeTask(this)
            Log.i(LOG_TAG, "URL LINK CHANGED: ${sharedPreferences?.getString(key, "")}")
        }
    }

    // Unregisters SharedPreferences change listener.
    override fun onDestroy() {
        super.onDestroy()
        PreferenceManager.getDefaultSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(this)
    }

    // Requests permissions for notifications.
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestNotificationPermissions() {
        val permissions = arrayOf(android.Manifest.permission.POST_NOTIFICATIONS)
        requestPermissions(permissions, 123)
    }
}
