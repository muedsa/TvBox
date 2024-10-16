package com.muedsa.tvbox.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.muedsa.tvbox.room.model.FavoriteMediaModel
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteMediaDao {

    @Query("SELECT * FROM favorite_media WHERE plugin_package = :pluginPackage ORDER BY update_at DESC")
    fun flowByPluginPackage(pluginPackage: String): Flow<List<FavoriteMediaModel>>

    @Query("SELECT * FROM favorite_media WHERE plugin_package = :pluginPackage and media_id = :mediaId")
    suspend fun getOneByPluginPackageAndMediaId(pluginPackage: String, mediaId: String): FavoriteMediaModel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg models: FavoriteMediaModel)

    @Query("DELETE FROM favorite_media WHERE plugin_package = :pluginPackage and media_id = :mediaId")
    suspend fun deleteByPluginPackageAndMediaId(pluginPackage: String, mediaId: String)
}