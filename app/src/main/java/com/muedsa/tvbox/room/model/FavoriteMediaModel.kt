package com.muedsa.tvbox.room.model

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "favorite_media", primaryKeys = ["plugin_package", "media_id"])
data class FavoriteMediaModel(
    @ColumnInfo(name = "plugin_package") val pluginPackage: String,
    @ColumnInfo(name = "media_id") val mediaId: String,
    @ColumnInfo(name = "media_title") val mediaTitle: String,
    @ColumnInfo(name = "media_detail_url") val mediaDetailUrl: String,
    @ColumnInfo(name = "media_sub_title", defaultValue = "NULL") val mediaSubTitle: String?,
    @ColumnInfo(name = "cover_image_url") val coverImageUrl: String,
    @ColumnInfo(name = "cover_image_url_http_headers", defaultValue = "NULL") val coverImageUrlHttpHeaders: String?,
    @ColumnInfo(name = "card_width") val cardWidth: Int,
    @ColumnInfo(name = "card_height") val cardHeight: Int,
    @ColumnInfo(
        name = "update_at",
        defaultValue = "(CURRENT_TIMESTAMP)",
        index = true
    ) var updateAt: Long = 0
)