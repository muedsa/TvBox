package com.muedsa.tvbox.model

import kotlinx.serialization.Serializable

@Serializable
class DanmakuEpisode(
    val provider: String,
    val mediaId: String,
    val mediaName: String,
    val episodeId: String,
    val episodeName: String,
)