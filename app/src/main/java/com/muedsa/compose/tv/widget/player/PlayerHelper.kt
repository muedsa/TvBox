package com.muedsa.compose.tv.widget.player

import androidx.annotation.OptIn
import androidx.media3.common.Format
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi

@OptIn(UnstableApi::class)
fun buildTracksInfo(tracks: Tracks): String {
    val infoBuilder = StringBuilder()
    for (trackGroup in tracks.groups) {
        if (trackGroup.isSelected) {
            infoBuilder.appendLine("group [ type=${groupTypeToString(trackGroup)}")
            for (i in 0 until trackGroup.length) {
                val selectedText = if(trackGroup.isTrackSelected(i))
                    "[X]" else "[ ]"
                val trackFormat = trackGroup.getTrackFormat(i)
                infoBuilder.appendLine("    $selectedText Track:$i, ${Format.toLogString(trackFormat)}")
            }
            infoBuilder.appendLine("]")
        }
    }
    return infoBuilder.toString()
}