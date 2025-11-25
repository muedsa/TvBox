package com.muedsa.tvbox.model

import androidx.datastore.preferences.core.Preferences
import com.muedsa.tvbox.store.KEY_DANMAKU_ALPHA
import com.muedsa.tvbox.store.KEY_DANMAKU_ENABLE
import com.muedsa.tvbox.store.KEY_DANMAKU_MERGE_ENABLE
import com.muedsa.tvbox.store.KEY_DANMAKU_SCREEN_PART
import com.muedsa.tvbox.store.KEY_DANMAKU_SIZE_SCALE
import com.muedsa.tvbox.store.KEY_FSR_ENABLE
import com.muedsa.tvbox.store.KEY_MEDIA_SNIFFING_TIMEOUT

data class AppSettingModel(
    val danmakuEnable: Boolean = true,
    val danmakuMergeEnable: Boolean = false,
    val danmakuSizeScale: Int = 140,
    val danmakuAlpha: Int = 100,
    val danmakuScreenPart: Int = 100,
    val mediaSniffingTimeout: Long = 30, // 秒
    val fsrEnable: Boolean = false,
) {

    companion object {

        fun fromPreferences(prefs: Preferences): AppSettingModel =
            AppSettingModel(
                danmakuEnable = prefs[KEY_DANMAKU_ENABLE] != false,
                danmakuMergeEnable = prefs[KEY_DANMAKU_MERGE_ENABLE] == true,
                danmakuSizeScale = prefs[KEY_DANMAKU_SIZE_SCALE] ?: 140,
                danmakuAlpha = prefs[KEY_DANMAKU_ALPHA] ?: 100,
                danmakuScreenPart = prefs[KEY_DANMAKU_SCREEN_PART] ?: 100,
                mediaSniffingTimeout = prefs[KEY_MEDIA_SNIFFING_TIMEOUT] ?: 30,
                fsrEnable = prefs[KEY_FSR_ENABLE] ?: false,
            )
    }
}
