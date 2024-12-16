package com.muedsa.compose.tv

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.relocation.BringIntoViewResponder
import androidx.compose.foundation.relocation.bringIntoViewResponder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.toSize

inline fun Modifier.conditional(
    condition: Boolean,
    ifTrue: Modifier.() -> Modifier,
): Modifier = if (condition) {
    then(ifTrue(Modifier))
} else {
    this
}

inline fun Modifier.conditional(
    condition: Boolean,
    ifTrue: Modifier.() -> Modifier,
    ifFalse: Modifier.() -> Modifier = { this },
): Modifier = if (condition) {
    then(ifTrue(Modifier))
} else {
    then(ifFalse(Modifier))
}

@OptIn(ExperimentalFoundationApi::class)
// ToDo: Migrate to Modifier.Node and stop using composed function.
internal fun Modifier.bringIntoViewIfChildrenAreFocused(
    paddingValues: PaddingValues = PaddingValues()
): Modifier = composed(
    inspectorInfo = debugInspectorInfo { name = "bringIntoViewIfChildrenAreFocused" },
    factory = {
        val pxOffset = with(LocalDensity.current) {
            val y = (paddingValues.calculateBottomPadding() - paddingValues.calculateTopPadding())
                .toPx()
            Offset.Zero.copy(y = y)
        }
        var myRect: Rect = Rect.Zero
        val responder = object : BringIntoViewResponder {
            // return the current rectangle and ignoring the child rectangle received.
            @ExperimentalFoundationApi
            override fun calculateRectForParent(localRect: Rect): Rect {
                return myRect
            }

            // The container is not expected to be scrollable. Hence the child is
            // already in view with respect to the container.
            @ExperimentalFoundationApi
            override suspend fun bringChildIntoView(localRect: () -> Rect?) {
            }
        }

        this
            .onSizeChanged {
                val size = it.toSize()
                myRect = Rect(pxOffset, size)
            }
            .bringIntoViewResponder(responder)
    }
)

@Composable
fun Modifier.focusOnInitial(): Modifier {
    val focusRequester = remember { FocusRequester() }
    return focusRequester(focusRequester)
        .onPlaced {
            focusRequester.requestFocus()
        }
}

@Composable
fun Modifier.focusOnMount(
    itemKey: String,
    focusRequester: FocusRequester = remember { FocusRequester() }
): Modifier {
    val isInitialFocusTransferred = useLocalFocusTransferredOnLaunch()
    val lastFocusedItemPerDestination = useLocalLastFocusedItemPerDestination()
    val currentDestination = useLocalDestination()

    return this
        .focusRequester(focusRequester)
        .onPlaced {
            val lastFocusedKey = lastFocusedItemPerDestination[currentDestination]
            if (!isInitialFocusTransferred.value && lastFocusedKey == itemKey) {
                focusRequester.requestFocus()
                isInitialFocusTransferred.value = true
            }
        }
        .onFocusChanged {
            if (it.isFocused) {
                lastFocusedItemPerDestination[currentDestination] = itemKey
                isInitialFocusTransferred.value = true
            }
        }
}