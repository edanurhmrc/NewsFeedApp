package com.edanurhamurcu.newsfeedapp.models

import com.edanurhamurcu.newsfeedapp.models.Article


data class NewsResponse(
    val articles: List<Article>,
    val status: String,
    val totalResults: Int
)