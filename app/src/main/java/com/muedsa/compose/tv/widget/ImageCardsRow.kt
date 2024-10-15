package com.muedsa.compose.tv.widget

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.muedsa.compose.tv.model.ContentModel
import com.muedsa.compose.tv.model.KeyModel
import com.muedsa.compose.tv.theme.CardContentPadding
import com.muedsa.compose.tv.theme.HorizontalPosterSize
import com.muedsa.compose.tv.theme.ImageCardRowCardPadding
import com.muedsa.compose.tv.theme.TvTheme
import com.muedsa.compose.tv.theme.VerticalPosterSize
import com.muedsa.util.anyMatchWithIndex


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun <T> ImageCardsRow(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    title: String,
    modelList: List<T> = listOf(),
    imageFn: (index: Int, item: T) -> String,
    imageSize: DpSize = HorizontalPosterSize,
    backgroundColorFn: (index: Int, model: T) -> Color = { _, _ -> Color.Unspecified },
    contentFn: (index: Int, item: T) -> ContentModel? = { _, _ -> null },
    onItemFocus: (index: Int, item: T) -> Unit = { _, _ -> },
    onItemClick: (index: Int, item: T) -> Unit = { _, _ -> }
) {

    val (rowFR, firstItemFR) = remember { FocusRequester.createRefs() }

    Column(modifier) {
        Text(
            modifier = Modifier.padding(start = ImageCardRowCardPadding),
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(ImageCardRowCardPadding))
        LazyRow(
            modifier = Modifier
                .focusRequester(rowFR)
                .focusRestorer {
                    firstItemFR
                },
            state = state,
            contentPadding = PaddingValues(
                start = ImageCardRowCardPadding,
                bottom = ImageCardRowCardPadding,
                end = 100.dp
            )
        ) {
            modelList.forEachIndexed { index, it ->
                var itemModifier = Modifier.padding(end = ImageCardRowCardPadding)
                if (index == 0) {
                    itemModifier = itemModifier.focusRequester(firstItemFR)
                }
                item(key = if (it is KeyModel) it.key else null) {
                    ImageContentCard(
                        modifier = itemModifier,
                        url = imageFn(index, it),
                        imageSize = imageSize,
                        backgroundColor = backgroundColorFn(index, it),
                        type = CardType.COMPACT,
                        model = contentFn(index, it),
                        onItemFocus = { onItemFocus(index, it) },
                        onItemClick = { onItemClick(index, it) }
                    )
                }
            }
        }
    }

    LaunchedEffect(key1 = Unit) {
        state.scrollToItem(state.firstVisibleItemIndex)
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun <T> StandardImageCardsRow(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    title: String,
    modelList: List<T> = listOf(),
    imageFn: (index: Int, item: T) -> String,
    imageSize: DpSize = HorizontalPosterSize,
    backgroundColorFn: (index: Int, model: T) -> Color = { _, _ -> Color.Unspecified },
    contentFn: (index: Int, item: T) -> ContentModel? = { _, _ -> null },
    onItemFocus: (index: Int, item: T) -> Unit = { _, _ -> },
    onItemClick: (index: Int, item: T) -> Unit = { _, _ -> }
) {
    val (rowFR, firstItemFR) = remember { FocusRequester.createRefs() }

    val rowBottomPadding =
        if (modelList.isNotEmpty() && modelList.anyMatchWithIndex { index, item ->
                contentFn(index, item) != null
            }) ImageCardRowCardPadding - CardContentPadding
        else ImageCardRowCardPadding

    Column(modifier) {
        Text(
            modifier = Modifier.padding(start = ImageCardRowCardPadding),
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(10.dp))
        LazyRow(
            modifier = Modifier
                .focusRequester(rowFR)
                .focusRestorer {
                    firstItemFR
                },
            state = state,
            contentPadding = PaddingValues(
                start = ImageCardRowCardPadding,
                bottom = rowBottomPadding,
                end = 100.dp
            )
        ) {
            modelList.forEachIndexed { index, it ->
                item(key = if (it is KeyModel) it.key else null) {
                    var itemModifier = Modifier.padding(end = ImageCardRowCardPadding)
                    if (index == 0) {
                        itemModifier = itemModifier.focusRequester(firstItemFR)
                    }
                    ImageContentCard(
                        modifier = itemModifier,
                        url = imageFn(index, it),
                        imageSize = imageSize,
                        backgroundColor = backgroundColorFn(index, it),
                        type = CardType.STANDARD,
                        model = contentFn(index, it),
                        onItemFocus = { onItemFocus(index, it) },
                        onItemClick = { onItemClick(index, it) }
                    )
                }
            }
        }
    }

    LaunchedEffect(key1 = Unit) {
        state.scrollToItem(state.firstVisibleItemIndex)
    }
}

@Preview
@Composable
fun ImageCardsRowPreview() {
    val modelList = listOf("Item 1", "Item 2", "Item 3", "Item 4", "Item 5", "Item 5")
    TvTheme {
        ImageCardsRow(
            modifier = Modifier.fillMaxWidth(),
            title = "Row Title",
            modelList = modelList,
            imageFn = { _, _ -> "" },
            contentFn = { _, item -> ContentModel(item) }
        )
    }
}

@Preview
@Composable
fun VerticalImageCardsRowPreview() {
    val modelList = listOf("Item 1", "Item 2", "Item 3", "Item 4", "Item 5", "Item 5")
    TvTheme {
        ImageCardsRow(
            modifier = Modifier.fillMaxWidth(),
            title = "Row Title",
            modelList = modelList,
            imageFn = { _, _ -> "" },
            imageSize = VerticalPosterSize,
            contentFn = { _, item -> ContentModel(item) }
        )
    }
}

@Preview
@Composable
fun StandardImageCardsRowPreview() {
    val modelList = listOf("Item 1", "Item 2", "Item 3", "Item 4", "Item 5", "Item 5")
    TvTheme {
        StandardImageCardsRow(
            modifier = Modifier.fillMaxWidth(),
            title = "Standard Row Title",
            modelList = modelList,
            imageFn = { _, _ -> "" },
            contentFn = { _, item -> ContentModel(item) }
        )
    }
}

@Preview
@Composable
fun StandardVerticalImageCardsRowPreview() {
    val modelList = listOf("Item 1", "Item 2", "Item 3", "Item 4", "Item 5", "Item 5")
    TvTheme {
        StandardImageCardsRow(
            modifier = Modifier.fillMaxWidth(),
            title = "Standard Row Title",
            modelList = modelList,
            imageFn = { _, _ -> "" },
            imageSize = VerticalPosterSize,
            contentFn = { _, item -> ContentModel(item) }
        )
    }
}