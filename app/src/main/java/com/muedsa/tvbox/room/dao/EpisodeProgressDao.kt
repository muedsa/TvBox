package com.muedsa.tvbox.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.muedsa.tvbox.room.model.EpisodeProgressModel

@Dao
interface EpisodeProgressDao {

    @Query("SELECT * FROM episode_progress WHERE  plugin_package = :pluginPackage and media_id = :mediaId")
    suspend fun getListByPluginPackageAndMediaId(
        pluginPackage: String,
        mediaId: String
    ): List<EpisodeProgressModel>

    @Query("SELECT * FROM episode_progress WHERE  plugin_package = :pluginPackage and media_id = :mediaId and episode_id = :episodeId")
    suspend fun getOneByPluginPackageAndMediaIdAndEpisodeId(
        pluginPackage: String,
        mediaId: String,
        episodeId: String
    ): EpisodeProgressModel?

    @Upsert
    suspend fun upsert(model: EpisodeProgressModel)

    @Query("DELETE FROM episode_progress WHERE plugin_package = :pluginPackage")
    suspend fun deleteByPluginPackage(pluginPackage: String)
}