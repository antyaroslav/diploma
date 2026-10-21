package ru.edu.qamid.db

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.edu.qamid.dao.*
import ru.edu.qamid.entity.NewsCategoryEntity
import ru.edu.qamid.entity.NewsEntity

@Database(
    entities = [
        NewsEntity::class,
        NewsCategoryEntity::class,
    ], version = 1, exportSchema = false
)


abstract class AppDb : RoomDatabase() {
    abstract fun getNewsDao(): NewsDao
    abstract fun getNewsCategoryDao(): NewsCategoryDao
}
