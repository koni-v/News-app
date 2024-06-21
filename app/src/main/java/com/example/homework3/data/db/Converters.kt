package com.example.homework3.data.db

import androidx.room.TypeConverter
import java.util.Date

class Converters {

    // Convert a timestamp to a Date object
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    // Convert a Date object to a timestamp
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    // Convert a Set<String> to a single String
    @TypeConverter
    fun fromStringSet(value: Set<String>?): String? {
        return value?.joinToString(",")
    }

    // Convert a single String to a Set<String>
    @TypeConverter
    fun toStringSet(value: String?): Set<String>? {
        return value?.split(",")?.toSet()
    }
}
