package com.muedsa.tvbox.screens.plugin.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.OutlinedIconButton
import com.muedsa.compose.tv.model.ContentModel
import com.muedsa.compose.tv.theme.ImageCardRowCardPadding
import com.muedsa.compose.tv.theme.ScreenPaddingLeft
import com.muedsa.compose.tv.useLocalNavHostController
import com.muedsa.compose.tv.widget.ContentBlock
import com.muedsa.compose.tv.widget.FillTextScreen
import com.muedsa.compose.tv.widget.ImmersiveList
import com.muedsa.compose.tv.widget.ScreenBackgroundType
import com.muedsa.tvbox.api.data.MediaCardRow
import com.muedsa.tvbox.plugin.PluginInfo
import com.muedsa.tvbox.screens.NavigationItems
import com.muedsa.tvbox.screens.nav
import com.muedsa.tvbox.screens.plugin.useLocalHomeScreenBackgroundState

@Composable
fun MediaCardRows(
    pluginInfo: PluginInfo,
    rows: List<MediaCardRow>,
    onRefresh: () -> Unit,
) {
    if (rows.isNotEmpty()) {
        val configuration = LocalConfiguration.current
        val backgroundState = useLocalHomeScreenBackgroundState()
        val navController = useLocalNavHostController()
        val titleHeight= (MaterialTheme.typography.titleLarge.fontSize.value * configuration.fontScale + 0.5f).dp
        val labelHeight= (MaterialTheme.typography.labelLarge.fontSize.value * configuration.fontScale + 0.5f).dp
        var firstRow = remember { rows.first() }
        val firstRowHeight = titleHeight + ImageCardRowCardPadding * 3 + firstRow.cardHeight.dp
        val tabHeight = remember { labelHeight + 24.dp * 2 + 6.dp * 2 }
        val screenHeight = configuration.screenHeightDp.dp
        val screenWidth = configuration.screenWidthDp.dp
        var title by remember { mutableStateOf("") }
        var subTitle by remember { mutableStateOf<String?>(null) }
        LaunchedEffect(key1 = rows) {
            firstRow = rows.first()
            firstRow.list.firstOrNull()?.let {
                title = it.title
                subTitle = it.subTitle
                backgroundState.url = it.coverImageUrl
                backgroundState.type = ScreenBackgroundType.SCRIM
            }
        }
        LazyColumn(
            modifier = Modifier
                .padding(start = ScreenPaddingLeft - ImageCardRowCardPadding)
        ) {
            item(contentType = "MEDIA_CARD_ROW_IMMERSIVE_LIST") {
                ImmersiveList(
                    background = {
                        ContentBlock(
                            modifier = Modifier
                                .width(screenWidth / 2)
                                .height(screenHeight - firstRowHeight - tabHeight - 20.dp),
                            model = ContentModel(title = title, subtitle = subTitle),
                            descriptionMaxLines = 3
                        )
                    },
                ) {
                    Column {
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(screenHeight - firstRowHeight - tabHeight - 5.dp)
                        )
                        MediaCardRow(
                            row = firstRow,
                            onlyImage = true,
                            onItemFocus = { _, mediaCard ->
                                title = mediaCard.title
                                subTitle = mediaCard.subTitle
                                backgroundState.type = ScreenBackgroundType.SCRIM
                                backgroundState.url = mediaCard.coverImageUrl
                            },
                            onItemClick = { _, mediaCard ->
                                navController.nav(
                                    NavigationItems.Detail(
                                        pluginPackage = pluginInfo.packageName,
                                        id = mediaCard.id,
                                        url = mediaCard.detailUrl,
                                    )
                                )
                            }
                        )
                    }
                }
            }

            itemsIndexed(
                items = rows.subList(1, rows.size),
                contentType = { index, _ -> "MEDIA_CARD_ROW_OTHER_$index" }
            ) { _, item ->
                MediaCardRow(
                    row = item,
                    onItemFocus = { _, mediaCard ->
                        title = mediaCard.title
                        subTitle = mediaCard.subTitle
                        backgroundState.type = ScreenBackgroundType.SCRIM
                        backgroundState.url = mediaCard.coverImageUrl
                    },
                    onItemClick = { _, mediaCard ->
                        navController.nav(
                            NavigationItems.Detail(
                                pluginPackage = pluginInfo.packageName,
                                id = mediaCard.id,
                                url = mediaCard.detailUrl,
                            )
                        )
                    }
                )
            }

            item(contentType = "MEDIA_CARD_ROWS_OTHER_BUTTON_ROW") {
                Row(
                    modifier = Modifier.padding(
                        start = ImageCardRowCardPadding,
                        bottom = ImageCardRowCardPadding
                    )
                ) {
                    OutlinedIconButton(onClick = { onRefresh() }) {
                        Icon(imageVector = Icons.Outlined.Refresh, contentDescription = "Refresh")
                    }
                }
            }
        }
    } else {
        FillTextScreen("这里什么都没有 (っ °Д °;)っ")
    }
}