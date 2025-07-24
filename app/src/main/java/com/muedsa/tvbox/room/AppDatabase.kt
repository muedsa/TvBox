package com.muedsa.tvbox.room

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.muedsa.tvbox.room.dao.EpisodeProgressDao
import com.muedsa.tvbox.room.dao.FavoriteMediaDao
import com.muedsa.tvbox.room.model.EpisodeProgressModel
import com.muedsa.tvbox.room.model.FavoriteMediaModel

@Database(
    entities = [FavoriteMediaModel::class, EpisodeProgressModel::class],
    version = 3,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3)
    ]
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun favoriteMediaDao(): FavoriteMediaDao

    abstract fun episodeProgressDao(): EpisodeProgressDao
}