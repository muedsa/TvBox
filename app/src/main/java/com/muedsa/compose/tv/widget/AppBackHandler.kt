package com.muedsa.compose.tv.widget

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

@Composable
fun AppBackHandler(
    onAllowBack: () -> Unit = {}
) {

    var allowBack by remember { mutableStateOf(false) }
    var tick by remember { mutableIntStateOf(0) }

    LaunchedEffect(key1 = tick) {
        delay(250)
        if (tick > 0) {
            tick--
        } else {
            allowBack = false
        }
    }

    BackHandler(enabled = !allowBack) {
        allowBack = true
        tick = 8
        try {
            onAllowBack()
        } catch (_: Throwable) {
        }
    }
}