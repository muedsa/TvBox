package com.muedsa.tvbox.model.dandanplay

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class BangumiInfo(
    @SerialName("type") val type: String,
    @SerialName("typeDescription") val typeDescription: String,
    @SerialName("titles") val titles: List<BangumiTitle>,
    @SerialName("episodes") val episodes: List<BangumiEpisode>,
    @SerialName("summary") val summary: String,
    @SerialName("metadata") val metadata: List<String>,
    @SerialName("bangumiUrl") val bangumiUrl: String,
//    @SerialName("userRating") val userRating: Int = 0,
//    @SerialName("favoriteStatus") val favoriteStatus: Boolean? = false,
//    @SerialName("comment") val comment: List<DanComment>? = null,
    @SerialName("ratingDetails") val ratingDetails: Map<String, Float>,
    // relateds
    // similars
    // tags
    // onlineDatabases
    @SerialName("animeId") val animeId: Int,
    @SerialName("animeTitle") val animeTitle: String,
    @SerialName("imageUrl") val imageUrl: String,
    @SerialName("searchKeyword") val searchKeyword: String,
    @SerialName("isOnAir") val isOnAir: Boolean,
    @SerialName("airDay") val airDay: Int,
    @SerialName("isFavorited") val isFavorited: Boolean = false,
    @SerialName("isRestricted") val isRestricted: Boolean = false,
    @SerialName("rating") val rating: Float,
)
