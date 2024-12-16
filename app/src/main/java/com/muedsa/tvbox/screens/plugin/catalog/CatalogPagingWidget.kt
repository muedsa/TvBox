package com.muedsa.tvbox.screens.plugin.catalog

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import com.muedsa.compose.tv.focusOnMount
import com.muedsa.compose.tv.model.ContentModel
import com.muedsa.compose.tv.theme.ImageCardRowCardPadding
import com.muedsa.compose.tv.theme.ScreenPaddingLeft
import com.muedsa.compose.tv.useLocalLastFocusedItemPerDestination
import com.muedsa.compose.tv.useLocalNavHostController
import com.muedsa.compose.tv.useLocalToastMsgBoxController
import com.muedsa.compose.tv.widget.ImageContentCard
import com.muedsa.compose.tv.widget.ScreenBackgroundType
import com.muedsa.compose.tv.widget.ToastMessageBoxController
import com.muedsa.tvbox.api.data.MediaCardType
import com.muedsa.tvbox.api.data.MediaCatalogConfig
import com.muedsa.tvbox.plugin.Plugin
import com.muedsa.tvbox.screens.NavigationItems
import com.muedsa.tvbox.screens.SPECIAL_DESTINATION_MEDIA_DETAIL
import com.muedsa.tvbox.screens.detail.INIT_FOCUSED_ITEM_KEY_MEDIA_DETAIL
import com.muedsa.tvbox.screens.nav
import com.muedsa.tvbox.screens.plugin.useLocalHomeScreenBackgroundState
import com.muedsa.tvbox.toCardType
import kotlinx.coroutines.launch
import kotlin.math.min

@Composable
fun CatalogPagingWidget(
    plugin: Plugin,
    config: MediaCatalogConfig,
    catalogScreenViewModel: CatalogScreenViewModel
) {
    val navController = useLocalNavHostController()
    val lastFocusedItemPerDestination = useLocalLastFocusedItemPerDestination()
    val toastController = useLocalToastMsgBoxController()
    val backgroundState = useLocalHomeScreenBackgroundState()
    val lazyPagingItems = catalogScreenViewModel.pageDataFlow.collectAsLazyPagingItems()
    val cardSize = remember { DpSize(config.cardWidth.dp, config.cardHeight.dp) }
    val circularSize = remember {
        val minSize = min(config.cardWidth, config.cardHeight).dp
        DpSize(minSize, minSize)
    }
    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(lazyPagingItems.loadState) {
        toastLoadStateError(
            loadState = lazyPagingItems.loadState.prepend,
            toastController = toastController
        )
        toastLoadStateError(
            loadState = lazyPagingItems.loadState.append,
            toastController = toastController
        )
        toastLoadStateError(
            loadState = lazyPagingItems.loadState.refresh,
            toastController = toastController
        )
    }

    BackHandler(enabled = gridState.canScrollBackward) {
        if (lazyPagingItems.itemCount > 0 && !gridState.isScrollInProgress) {
            coroutineScope.launch {
                gridState.animateScrollToItem(0)
            }
        }
    }

    LazyVerticalGrid(
        modifier = Modifier.padding(start = ScreenPaddingLeft),
        columns = GridCells.Adaptive(config.cardWidth.dp),
        verticalArrangement = Arrangement.spacedBy(ImageCardRowCardPadding),
        horizontalArrangement = Arrangement.spacedBy(ImageCardRowCardPadding),
        contentPadding = PaddingValues(
            top = ImageCardRowCardPadding,
            bottom = ImageCardRowCardPadding
        ),
        state = gridState
    ) {
        if (lazyPagingItems.loadState.refresh == LoadState.Loading) {
            item(
                contentType = lazyPagingItems.itemContentType { "refreshLoading" }
            ) {
                Box(
                    modifier = Modifier.size(cardSize)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(circularSize)
                    )
                }
            }
        }

        items(
            count = lazyPagingItems.itemCount,
            contentType = lazyPagingItems.itemContentType { "mediaCard" }
        ) { index ->
            val mediaCard = lazyPagingItems[index]
            if (mediaCard != null) {
                ImageContentCard(
                    modifier = Modifier.focusOnMount(itemKey = "catalogScreen, grid $index"),
                    url = if (config.cardType == MediaCardType.NOT_IMAGE) "" else mediaCard.coverImageUrl,
                    imageSize = cardSize,
                    type = config.cardType.toCardType(),
                    model = ContentModel(
                        title = mediaCard.title,
                        subtitle = mediaCard.subTitle
                    ),
                    onItemFocus = {
                        backgroundState.type = ScreenBackgroundType.BLUR
                        backgroundState.url = mediaCard.coverImageUrl
                    },
                    onItemClick = {
                        lastFocusedItemPerDestination[SPECIAL_DESTINATION_MEDIA_DETAIL] =
                            INIT_FOCUSED_ITEM_KEY_MEDIA_DETAIL
                        navController.nav(
                            NavigationItems.Detail(
                                pluginPackage = plugin.pluginInfo.packageName,
                                id = mediaCard.id,
                                url = mediaCard.detailUrl
                            )
                        )
                    }
                )
            } else {
                // Placeholder
                ImageContentCard(
                    url = "",
                    imageSize = cardSize,
                    type = config.cardType.toCardType(),
                    model = ContentModel(title = "--"),
                    onItemFocus = {},
                    onItemClick = {}
                )
            }
        }

        if (lazyPagingItems.loadState.append == LoadState.Loading) {
            item(
                contentType = lazyPagingItems.itemContentType { "appendLoading" }
            ) {
                Box(
                    modifier = Modifier.size(cardSize)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(circularSize)
                    )
                }
            }
        }
    }
}

fun toastLoadStateError(loadState: LoadState, toastController: ToastMessageBoxController) {
    if (loadState is LoadState.Error) {
        toastController.error(loadState.error)
    }
}