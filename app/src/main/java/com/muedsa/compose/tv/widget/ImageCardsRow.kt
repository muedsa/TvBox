package com.muedsa.compose.tv.widget

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.muedsa.compose.tv.focusOnMount
import com.muedsa.compose.tv.model.ContentModel
import com.muedsa.compose.tv.theme.CardContentPadding
import com.muedsa.compose.tv.theme.CommonRowCardPadding
import com.muedsa.compose.tv.theme.HorizontalPosterSize
import com.muedsa.compose.tv.theme.TvTheme
import com.muedsa.compose.tv.theme.VerticalPosterSize
import com.muedsa.util.anyMatchWithIndex

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun <T> ImageCardsRow(
    modifier: Modifier = Modifier,
    rowFocusOnMountKey: String,
    state: LazyListState = rememberLazyListState(),
    title: String,
    modelList: List<T> = listOf(),
    imageFn: (index: Int, item: T) -> String,
    imageHttpHeadersFn: (index: Int, item: T) -> Map<String, List<String>>? = { _, _ -> null },
    imageSize: DpSize = HorizontalPosterSize,
    backgroundColorFn: (index: Int, model: T) -> Color = { _, _ -> Color.Unspecified },
    contentFn: (index: Int, item: T) -> ContentModel? = { _, _ -> null },
    onItemFocus: (index: Int, item: T) -> Unit = { _, _ -> },
    onItemClick: (index: Int, item: T) -> Unit = { _, _ -> },
) {
    val cardHorizontalPadding = remember(imageSize) { imageSize.width * 0.08f }
    val lazyRowFR = remember { FocusRequester() }
    Column(modifier.focusGroup()) {
        Text(
            modifier = Modifier.padding(start = cardHorizontalPadding),
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(CommonRowCardPadding))
        AnimatedContent(
            targetState = modelList,
            label = "",
        ) { modelState ->
            LazyRow(
                modifier = Modifier.focusRequester(lazyRowFR),
                state = state,
                contentPadding = PaddingValues(
                    start = cardHorizontalPadding,
                    bottom = CommonRowCardPadding,
                    end = 100.dp
                )
            ) {
                itemsIndexed(
                    items = modelState,
                ) { index, item ->
                    ImageContentCard(
                        modifier = Modifier
                            .focusOnMount(
                                itemKey = "$rowFocusOnMountKey, col $index",
                            )
                            .padding(end = cardHorizontalPadding),
                        url = imageFn(index, item),
                        httpHeaders = imageHttpHeadersFn(index, item),
                        imageSize = imageSize,
                        backgroundColor = backgroundColorFn(index, item),
                        type = CardType.COMPACT,
                        model = contentFn(index, item),
                        onItemFocus = { onItemFocus(index, item) },
                        onItemClick = {
                            lazyRowFR.saveFocusedChild()
                            onItemClick(index, item)
                        })
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun <T> StandardImageCardsRow(
    modifier: Modifier = Modifier,
    rowFocusOnMountKey: String,
    state: LazyListState = rememberLazyListState(),
    title: String,
    modelList: List<T> = listOf(),
    imageFn: (index: Int, item: T) -> String,
    imageHttpHeadersFn: (index: Int, item: T) -> Map<String, List<String>>? = { _, _ -> null },
    imageSize: DpSize = HorizontalPosterSize,
    backgroundColorFn: (index: Int, model: T) -> Color = { _, _ -> Color.Unspecified },
    contentFn: (index: Int, item: T) -> ContentModel? = { _, _ -> null },
    onItemFocus: (index: Int, item: T) -> Unit = { _, _ -> },
    onItemClick: (index: Int, item: T) -> Unit = { _, _ -> },
) {
    val cardHorizontalPadding = remember(imageSize) { imageSize.width * 0.075f }
    val lazyRowFR = remember { FocusRequester() }
    var rowBottomPadding = remember(modelList) { CommonRowCardPadding }

    LaunchedEffect(modelList) {
        rowBottomPadding = if (modelList.isNotEmpty() && modelList.anyMatchWithIndex { index, item ->
                contentFn(index, item) != null
            }) CommonRowCardPadding - CardContentPadding
        else CommonRowCardPadding
    }

    Column(modifier.focusGroup()) {
        Text(
            modifier = Modifier.padding(start = cardHorizontalPadding),
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(10.dp))
        AnimatedContent(
            targetState = modelList,
            label = "",
        ) { modelState ->
            LazyRow(
                modifier = Modifier.focusRequester(lazyRowFR),
                state = state,
                contentPadding = PaddingValues(
                    start = cardHorizontalPadding,
                    bottom = rowBottomPadding,
                    end = 100.dp
                )
            ) {
                itemsIndexed(
                    items = modelState,
                ) { index, item ->
                    ImageContentCard(
                        modifier = Modifier
                            .focusOnMount(
                                itemKey = "$rowFocusOnMountKey, col $index",
                            )
                            .padding(end = cardHorizontalPadding),
                        url = imageFn(index, item),
                        httpHeaders = imageHttpHeadersFn(index, item),
                        imageSize = imageSize,
                        backgroundColor = backgroundColorFn(index, item),
                        type = CardType.STANDARD,
                        model = contentFn(index, item),
                        onItemFocus = { onItemFocus(index, item) },
                        onItemClick = {
                            lazyRowFR.saveFocusedChild()
                            onItemClick(index, item)
                        })
                }
            }
        }
    }
}

@Preview
@Composable
fun ImageCardsRowPreview() {
    val modelList = listOf("Item 1", "Item 2", "Item 3", "Item 4", "Item 5", "Item 5")
    TvTheme {
        ImageCardsRow(
            modifier = Modifier.fillMaxWidth(), rowFocusOnMountKey = "row",
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
            modifier = Modifier.fillMaxWidth(), rowFocusOnMountKey = "row",
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
            modifier = Modifier.fillMaxWidth(), rowFocusOnMountKey = "row",
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
            modifier = Modifier.fillMaxWidth(), rowFocusOnMountKey = "row",
            title = "Standard Row Title",
            modelList = modelList,
            imageFn = { _, _ -> "" },
            imageSize = VerticalPosterSize,
            contentFn = { _, item -> ContentModel(item) }
        )
    }
}