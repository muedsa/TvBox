package com.muedsa.tvbox.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.muedsa.tvbox.room.dao.EpisodeProgressDao
import com.muedsa.tvbox.room.dao.FavoriteMediaDao
import com.muedsa.tvbox.room.model.EpisodeProgressModel
import com.muedsa.tvbox.room.model.FavoriteMediaModel

@Database(entities = [FavoriteMediaModel::class, EpisodeProgressModel::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun favoriteMediaDao(): FavoriteMediaDao

    abstract fun episodeProgressDao(): EpisodeProgressDao
}