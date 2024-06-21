package com.example.homework3.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.homework3.data.NewsItem

@Database(entities = [NewsItem::class], version = 11)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    // Abstract method to access the NewsItemDao
    abstract fun newsItemDao(): NewsItemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Get the database instance, ensuring it's a singleton
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val tempInstance = INSTANCE
                if (tempInstance != null) {
                    return tempInstance
                }

                // Create a new instance of the database
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}
