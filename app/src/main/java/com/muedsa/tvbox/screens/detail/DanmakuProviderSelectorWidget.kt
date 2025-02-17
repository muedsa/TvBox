package com.muedsa.tvbox.screens.detail

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.RadioButton
import androidx.tv.material3.Text
import androidx.tv.material3.WideButtonDefaults
import com.muedsa.compose.tv.conditional
import com.muedsa.compose.tv.useLocalRightSideDrawerController
import com.muedsa.compose.tv.widget.NoBackground
import com.muedsa.compose.tv.widget.TwoSideWideButton

@Composable
fun DanmakuProviderSelectorWidget(
    mediaDetailScreenViewModel: MediaDetailScreenViewModel,
) {
    val drawerController = useLocalRightSideDrawerController()
    val selectedDanmakuProvider by mediaDetailScreenViewModel.selectedDanmakuProviderFlow
        .collectAsStateWithLifecycle()
    var hasFocus by remember { mutableStateOf(false) }

    Text(
        modifier = Modifier
            .conditional(hasFocus) {
                border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(6.dp),
                )
            }
            .padding(4.dp)
            .onFocusChanged {
                hasFocus = it.hasFocus
            }
            .clickable(onClick = {
                drawerController.pop {
                    DanmakuProviderSelectorSideWidget(
                        mediaDetailScreenViewModel = mediaDetailScreenViewModel,
                    )
                }
            }),
        text = selectedDanmakuProvider ?: "--",
        color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.titleMedium
    )
}

@Composable
fun DanmakuProviderSelectorSideWidget(
    mediaDetailScreenViewModel: MediaDetailScreenViewModel,
) {
    val drawerController = useLocalRightSideDrawerController()
    val danmakuProviders = remember { mediaDetailScreenViewModel.getDanmakuProviders().toList() }
    val selectedDanmakuProvider by mediaDetailScreenViewModel.selectedDanmakuProviderFlow
        .collectAsStateWithLifecycle()

    Column {
        Text(
            modifier = Modifier
                .padding(start = 8.dp, end = 15.dp),
            text = "选择弹幕提供者",
            style = MaterialTheme.typography.titleLarge
        )

        LazyColumn(
            contentPadding = PaddingValues(vertical = 20.dp)
        ) {
            items(items = danmakuProviders, key = { it }) {
                val interactionSource = remember { MutableInteractionSource() }
                TwoSideWideButton(
                    title = {
                        Text(
                            modifier = Modifier.basicMarquee(),
                            text = it
                        )
                    },
                    onClick = {
                        drawerController.close()
                        mediaDetailScreenViewModel.changeDanmakuProvider(it)
                    },
                    interactionSource = interactionSource,
                    background = {
                        WideButtonDefaults.NoBackground(
                            interactionSource = interactionSource
                        )
                    }
                ) {
                    RadioButton(
                        selected = selectedDanmakuProvider == it,
                        onClick = { },
                        interactionSource = interactionSource
                    )
                }
            }
        }
    }
}