package com.muedsa.tvbox.screens.plugin.favorites

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.OutlinedButton
import androidx.tv.material3.Text
import com.muedsa.compose.tv.focusOnMount
import com.muedsa.compose.tv.model.ContentModel
import com.muedsa.compose.tv.theme.CommonRowCardPadding
import com.muedsa.compose.tv.theme.ScreenPaddingLeft
import com.muedsa.compose.tv.useLocalLastFocusedItemPerDestination
import com.muedsa.compose.tv.useLocalNavHostController
import com.muedsa.compose.tv.widget.CardType
import com.muedsa.compose.tv.widget.ImageContentCard
import com.muedsa.compose.tv.widget.ScreenBackgroundType
import com.muedsa.tvbox.screens.NavigationItems
import com.muedsa.tvbox.screens.SPECIAL_DESTINATION_MEDIA_DETAIL
import com.muedsa.tvbox.screens.detail.INIT_FOCUSED_ITEM_KEY_MEDIA_DETAIL
import com.muedsa.tvbox.screens.nav
import com.muedsa.tvbox.screens.plugin.useLocalHomeScreenBackgroundState
import com.muedsa.tvbox.tool.LenientJson

@Composable
fun FavoriteMediaScreen(
    favoriteMediaScreenViewModel: FavoriteMediaScreenViewModel
) {
    val navController = useLocalNavHostController()
    val lastFocusedItemPerDestination = useLocalLastFocusedItemPerDestination()
    val backgroundState = useLocalHomeScreenBackgroundState()

    val favoriteMediaList by favoriteMediaScreenViewModel.favoriteMediasSF.collectAsState()
    var deleteMode by remember {
        mutableStateOf(false)
    }

    val focusManager = LocalFocusManager.current

    BackHandler(enabled = deleteMode) {
        deleteMode = false
    }

    Column(modifier = Modifier.padding(start = ScreenPaddingLeft)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (deleteMode) "删除模式" else "我的收藏",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.width(30.dp))
            OutlinedButton(
                modifier = Modifier.testTag("favoritesScreen_deleteModeButton"),
                onClick = { deleteMode = !deleteMode }
            ) {
                Text(if (deleteMode) "退出" else "删除模式")
                Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                Icon(
                    imageVector = if (deleteMode) Icons.Outlined.Check else Icons.Outlined.Delete,
                    contentDescription = if (deleteMode) "退出" else "删除模式"
                )
            }
        }

        if (favoriteMediaList.isNotEmpty()) {
            val cardSize = remember(favoriteMediaList) {
                DpSize(
                    width = favoriteMediaList.maxOfOrNull { it.cardWidth }!!.dp,
                    height = favoriteMediaList.maxOfOrNull { it.cardHeight }!!.dp
                )
            }
            val cardVerticalSpace = remember(favoriteMediaList) { cardSize.height * 0.08f }
            val cardHorizontalSpace = remember(favoriteMediaList) { cardSize.width * 0.08f }
            LazyVerticalGrid(
                modifier = Modifier
                    .padding(start = 0.dp, top = 20.dp, end = 20.dp, bottom = 20.dp),
                columns = GridCells.Adaptive(cardSize.width),
                verticalArrangement = Arrangement.spacedBy(cardVerticalSpace),
                horizontalArrangement = Arrangement.spacedBy(cardHorizontalSpace),
                contentPadding = PaddingValues(
                    top = CommonRowCardPadding,
                    bottom = CommonRowCardPadding
                )
            ) {
                itemsIndexed(
                    items = favoriteMediaList,
                    key = { _, item -> item.mediaId }
                ) { index, item ->
                    val httpHeaders = item.coverImageUrlHttpHeaders?.let {
                        LenientJson.decodeFromString<Map<String, List<String>>>(it)
                    }
                    ImageContentCard(
                        modifier = Modifier.focusOnMount(itemKey = "favoriteScreen, grid $index"),
                        url = item.coverImageUrl,
                        httpHeaders = httpHeaders,
                        imageSize = DpSize(item.cardWidth.dp, item.cardHeight.dp),
                        type = CardType.STANDARD,
                        model = ContentModel(
                            title = item.mediaTitle,
                            subtitle = item.mediaSubTitle,
                        ),
                        onItemFocus = {
                            backgroundState.change(
                                url = item.coverImageUrl,
                                type = ScreenBackgroundType.BLUR,
                                headers = httpHeaders,
                            )
                        },
                        onItemClick = {
                            if (deleteMode) {
                                if (index + 1 < favoriteMediaList.size) {
                                    focusManager.moveFocus(FocusDirection.Next)
                                } else {
                                    focusManager.moveFocus(FocusDirection.Previous)
                                }
                                favoriteMediaScreenViewModel.remove(item)
                            } else {
                                lastFocusedItemPerDestination[SPECIAL_DESTINATION_MEDIA_DETAIL] =
                                    INIT_FOCUSED_ITEM_KEY_MEDIA_DETAIL
                                navController.nav(
                                    NavigationItems.Detail(
                                        pluginPackage = item.pluginPackage,
                                        id = item.mediaId,
                                        url = item.mediaDetailUrl,
                                    )
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}