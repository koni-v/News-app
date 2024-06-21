package com.example.homework3.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.Date

@Entity(tableName = "news_item")
data class NewsItem(

    @PrimaryKey
    val uniqueIdentifier: String,
    val title: String,
    val description: String?,
    val imageUrl: String?,
    val author: String?,
    val fullArticleLink: String?,
    val publicationDate: Date,
    val keywords: Set<String>

) : Serializable

