package ru.edu.qamid.dto

data class NewsResponse(
    val pages: Int,
    val elements: List<News>
)
