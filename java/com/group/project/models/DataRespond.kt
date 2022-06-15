package com.group.project.models

data class DataRespond(
    val approved: Boolean,
    val article: String,
    val article_image: String,
    val author: Int,
    val categories: String,
    val date_published: String,
    val id: Int,
    val title: String
)