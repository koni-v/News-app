package com.example.homework3.notification

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.StrictMode
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.homework3.R
import com.example.homework3.data.NewsItem
import com.example.homework3.DetailsActivity
import java.net.URL

const val NOTIFICATION_CHANNEL_ID = "MY_UPDATE_CHANNEL"

object NotificationUtil {

    var notificationId = 1

    fun postNotification(context: Context, newsItem: NewsItem) {
        val manager = NotificationManagerCompat.from(context)

        // Create intent to open DetailsActivity when the notification is clicked
        val detailsIntent = Intent(context, DetailsActivity::class.java).apply {
            putExtra(DetailsActivity.ITEM_KEY, newsItem)
        }

        // Create a unique request code using the notificationId
        val requestCode = notificationId

        // Create a PendingIntent with detailsIntent
        val detailsPendingIntent: PendingIntent? = PendingIntent.getActivity(
            context,
            requestCode,
            detailsIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Build notification
        val notificationBuilder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(newsItem.title)
            .setContentText(newsItem.description)
            .setSmallIcon(R.drawable.baseline_newspaper_24)
            .setAutoCancel(true)
            .setContentIntent(detailsPendingIntent) // Set the PendingIntent here

        // Check if the news item has an image
        if (newsItem.imageUrl != null) {
            try {
                // Allow network operations in main thread for simplicity
                val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
                StrictMode.setThreadPolicy(policy)

                val url = URL(newsItem.imageUrl)
                val bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())

                // Create BigPictureStyle
                val bigPictureStyle = NotificationCompat.BigPictureStyle()
                    .bigPicture(bitmap)
                    .bigLargeIcon(bitmap) // Pass the Bitmap as the large icon

                notificationBuilder.setStyle(bigPictureStyle)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Build the notification
        val notification = notificationBuilder.build()

        // Notify
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        manager.notify(notificationId++, notification)
    }
}
