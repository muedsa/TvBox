package com.muedsa.tvbox.room.model

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "episode_progress", primaryKeys = ["plugin_package", "media_id", "episode_id"])
data class EpisodeProgressModel(
    @ColumnInfo(name = "plugin_package") val pluginPackage: String,
    @ColumnInfo(name = "media_id") val mediaId: String,
    @ColumnInfo(name = "episode_id") val episodeId: String,
    @ColumnInfo(name = "progress", defaultValue = "0") var progress: Long,
    @ColumnInfo(name = "duration", defaultValue = "0") var duration: Long,
    @ColumnInfo(
        name = "update_at",
        defaultValue = "(CURRENT_TIMESTAMP)",
        index = true
    ) var updateAt: Long = 0
)