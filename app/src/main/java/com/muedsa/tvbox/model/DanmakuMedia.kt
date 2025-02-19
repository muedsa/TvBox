package com.muedsa.tvbox.model

class DanmakuMedia(
    val provider: String,
    val mediaId: String,
    val mediaName: String,
    val publishDate: String? = null,
    val rating: String? = null,
    val episodes: List<DanmakuEpisode>,
    val extendData: String? = null,
)