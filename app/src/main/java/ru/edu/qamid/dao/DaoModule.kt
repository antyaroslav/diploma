package ru.edu.qamid.dao

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.edu.qamid.db.AppDb

@InstallIn(SingletonComponent::class)
@Module
object DaoModule {

    @Provides
    fun provideNewsDao(db: AppDb): NewsDao = db.getNewsDao()

    @Provides
    fun provideNewsCategoryDao(db: AppDb): NewsCategoryDao = db.getNewsCategoryDao()

}
