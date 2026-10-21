package ru.edu.qamid.api

import retrofit2.Response
import retrofit2.http.*
import ru.edu.qamid.dto.News
import ru.edu.qamid.dto.NewsResponse

interface NewsApi {
    @GET("news")
    suspend fun getAllNews(
        @Query("pages") pages: Int = 1,
        @Query("publishDate") publishDate: Boolean = false
    ): Response<NewsResponse>

    @PUT("news")
    suspend fun editNewsItem(@Body newsItem: News): Response<News>

    @POST("news")
    suspend fun saveNewsItem(@Body newsItem: News): Response<News>

    @DELETE("news/{id}")
    suspend fun removeNewsItemById(@Path("id") id: Int): Response<Unit>
}
