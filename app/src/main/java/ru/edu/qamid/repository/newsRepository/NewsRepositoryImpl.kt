package ru.edu.qamid.repository.newsRepository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import ru.edu.qamid.api.NewsApi
import ru.edu.qamid.dao.NewsCategoryDao
import ru.edu.qamid.dao.NewsDao
import ru.edu.qamid.dto.News
import ru.edu.qamid.dto.NewsWithCategory
import ru.edu.qamid.dto.User
import ru.edu.qamid.entity.toEntity
import ru.edu.qamid.entity.toNewsCategoryDto
import ru.edu.qamid.entity.toNewsCategoryEntity
import ru.edu.qamid.utils.Utils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewsRepositoryImpl @Inject constructor(
    private val newsDao: NewsDao,
    private val newsCategoryDao: NewsCategoryDao,
    private val newsApi: NewsApi
) : NewsRepository {

    override var newsList: List<News> = emptyList()
        private set
    
    override fun getAllNews(
        coroutineScope: CoroutineScope,
        publishEnabled: Boolean?,
        publishDateBefore: Long?,
        newsCategoryId: Int?,
        dateStart: Long?,
        dateEnd: Long?,
        status: Boolean?
    ): Flow<List<NewsWithCategory>> = newsDao.getAllNews(
        publishEnabled = publishEnabled,
        publishDateBefore = publishDateBefore,
        newsCategoryId = newsCategoryId,
        dateStart = dateStart,
        dateEnd = dateEnd,
        status = status
    ).flowOn(Dispatchers.Default)

    override suspend fun changeIsOpen(newsItem: News) {
        newsDao.insert(newsItem.toEntity())
    }

    override suspend fun refreshNews() = Utils.makeRequest(
        request = { newsApi.getAllNews() },
        onSuccess = { body ->
            val apiId = body.elements
                .map { it.id }
            val databaseId = newsDao.getAllNewsList()
                .map { it.newsItem.id }
                .toMutableList()
            databaseId.removeAll(apiId)
            newsDao.removeNewsItemsByIdList(databaseId)
            newsDao.insert(body.elements.toEntity())
        }
    )

    override suspend fun modificationOfExistingNews(newsItem: News): News =
        Utils.makeRequest(
            request = { newsApi.editNewsItem(newsItem) },
            onSuccess = { body ->
                newsDao.insert(body.toEntity())
                body
            }
        )

    override suspend fun createNews(newsItem: News): News =
        Utils.makeRequest(
            request = { newsApi.saveNewsItem(newsItem) },
            onSuccess = { body ->
                newsDao.insert(body.toEntity())
                body
            }
        )

    override suspend fun removeNewsItemById(id: Int) =
        Utils.makeRequest(
            request = { newsApi.removeNewsItemById(id) },
            onSuccess = {
                newsDao.removeNewsItemById(id)
            }
        )

    override fun getAllNewsCategories(): Flow<List<News.Category>> =
        newsCategoryDao.getAllNewsCategories().map { it.toNewsCategoryDto() }

    override suspend fun saveNewsCategories(listNewsCategories: List<News.Category>) =
        newsCategoryDao.insert(listNewsCategories.toNewsCategoryEntity())
}