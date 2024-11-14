package com.muedsa.tvbox.screens.plugin.search

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.muedsa.compose.tv.model.ContentModel
import com.muedsa.compose.tv.theme.ImageCardRowCardPadding
import com.muedsa.compose.tv.theme.ScreenPaddingLeft
import com.muedsa.compose.tv.useLocalNavHostController
import com.muedsa.compose.tv.widget.ImageContentCard
import com.muedsa.compose.tv.widget.ScreenBackgroundType
import com.muedsa.tvbox.api.data.MediaCardRow
import com.muedsa.tvbox.api.data.MediaCardType
import com.muedsa.tvbox.screens.NavigationItems
import com.muedsa.tvbox.screens.nav
import com.muedsa.tvbox.screens.plugin.useLocalHomeScreenBackgroundState
import com.muedsa.tvbox.toCardType

@Composable
fun SearchResult(row: MediaCardRow) {
    val backgroundState = useLocalHomeScreenBackgroundState()
    val navController = useLocalNavHostController()

    LazyVerticalGrid(
        modifier = Modifier.padding(start = ScreenPaddingLeft),
        columns = GridCells.Adaptive(row.cardWidth.dp + ImageCardRowCardPadding),
        contentPadding = PaddingValues(
            top = ImageCardRowCardPadding,
            bottom = ImageCardRowCardPadding
        )
    ) {
        itemsIndexed(row.list) { _, item ->
            ImageContentCard(
                modifier = Modifier.padding(end = ImageCardRowCardPadding),
                url = if (row.cardType == MediaCardType.NOT_IMAGE) "" else item.coverImageUrl,
                imageSize = DpSize(row.cardWidth.dp, row.cardHeight.dp),
                type = row.cardType.toCardType(),
                model = ContentModel(
                    title = item.title,
                    subtitle = item.subTitle
                ),
                onItemFocus = {
                    backgroundState.url = item.coverImageUrl
                    backgroundState.type = ScreenBackgroundType.BLUR
                },
                onItemClick = {
                    navController.nav(NavigationItems.Detail(item.id, item.detailUrl))
                }
            )
        }

        // 最后一行占位
        item {
            Spacer(modifier = Modifier.height(250.dp))
        }
    }
}