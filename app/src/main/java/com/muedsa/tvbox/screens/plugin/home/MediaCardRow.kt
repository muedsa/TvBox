package com.muedsa.tvbox.screens.plugin.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.muedsa.compose.tv.model.ContentModel
import com.muedsa.compose.tv.widget.ImageCardsRow
import com.muedsa.compose.tv.widget.StandardImageCardsRow
import com.muedsa.tvbox.api.data.MediaCard
import com.muedsa.tvbox.api.data.MediaCardRow
import com.muedsa.tvbox.api.data.MediaCardType

@Composable
fun MediaCardRow(
    rowFocusOnMountKey: String,
    row: MediaCardRow,
    onlyImage: Boolean = false,
    onItemFocus: (index: Int, item: MediaCard) -> Unit = { _, _ -> },
    onItemClick: (index: Int, item: MediaCard) -> Unit = { _, _ -> },
) {
    when (row.cardType) {
        MediaCardType.STANDARD -> StandardImageCardsRow(
            rowFocusOnMountKey = rowFocusOnMountKey,
            title = row.title,
            modelList = row.list,
            imageFn = { _, item -> item.coverImageUrl },
            imageHttpHeadersFn = { _, item -> item.coverImageHttpHeaders },
            imageSize = DpSize(row.cardWidth.dp, row.cardHeight.dp),
            backgroundColorFn = { _, item -> Color(item.backgroundColor) },
            contentFn = { _, item ->
                if (onlyImage) null else ContentModel(item.title, item.subTitle)
            },
            onItemFocus = onItemFocus,
            onItemClick = onItemClick,
        )

        MediaCardType.COMPACT, MediaCardType.NOT_IMAGE -> ImageCardsRow(
            rowFocusOnMountKey = rowFocusOnMountKey,
            title = row.title,
            modelList = row.list,
            imageFn = { _, item -> if (row.cardType == MediaCardType.NOT_IMAGE) "" else item.coverImageUrl },
            imageHttpHeadersFn = { _, item -> if (row.cardType == MediaCardType.NOT_IMAGE) null else item.coverImageHttpHeaders },
            imageSize = DpSize(row.cardWidth.dp, row.cardHeight.dp),
            backgroundColorFn = { _, item -> Color(item.backgroundColor) },
            contentFn = { _, item ->
                if (onlyImage) null else ContentModel(item.title, item.subTitle)
            },
            onItemFocus = onItemFocus,
            onItemClick = onItemClick,
        )
    }
}