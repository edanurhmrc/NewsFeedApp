package com.edanurhamurcu.newsfeedapp.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable


/*
To save the article to database we need to annotate the class with entity
which will tell the android studio that this article class is the table in database
Here article will be the whole table and with columns
 */
@Entity(
    tableName = "articles"
)

data class Article(
    @PrimaryKey(autoGenerate = true)
    var uid: Int? = null,
    val author: String,
    val content: String,
    val description: String,
    val publishedAt: String,
    val source: Source,
    val title: String,
    val url: String,
    val urlToImage: String
):Serializable