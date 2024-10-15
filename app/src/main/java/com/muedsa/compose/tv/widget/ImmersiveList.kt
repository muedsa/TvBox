package com.muedsa.compose.tv.widget

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.muedsa.compose.tv.bringIntoViewIfChildrenAreFocused

/**
 * A replacement for ImmersiveList.
 */
@Composable
fun ImmersiveList(
    modifier: Modifier = Modifier,
    background: @Composable BoxScope.() -> Unit,
    list: @Composable BoxScope.() -> Unit
) {
    Box(modifier.bringIntoViewIfChildrenAreFocused()) {
        background()
        Box(
            contentAlignment = Alignment.BottomEnd,
            content = list
        )
    }
}