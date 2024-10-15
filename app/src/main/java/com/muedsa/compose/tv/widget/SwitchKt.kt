package com.muedsa.compose.tv.widget

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.tv.material3.Switch
import androidx.tv.material3.SwitchColors
import androidx.tv.material3.SwitchDefaults


@Composable
fun FocusScaleSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    thumbContent: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    colors: SwitchColors = SwitchDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    scaleFactor: Float = 1.3f
) {
    var hasFocus by remember { mutableStateOf(false) }
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier
            .onFocusChanged {
                hasFocus = it.hasFocus
            }
            .scale(if (hasFocus) scaleFactor else 1f),
        thumbContent = thumbContent,
        enabled = enabled,
        colors = colors,
        interactionSource = interactionSource
    )
}