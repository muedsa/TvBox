package com.muedsa.tvbox.screens.plugin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Tab
import androidx.tv.material3.TabDefaults
import androidx.tv.material3.TabRow
import androidx.tv.material3.TabRowDefaults
import androidx.tv.material3.Text
import com.muedsa.compose.tv.widget.ScreenBackground
import com.muedsa.compose.tv.widget.ScreenBackgroundState
import com.muedsa.compose.tv.widget.ScreenBackgroundType
import com.muedsa.compose.tv.widget.rememberScreenBackgroundState
import com.muedsa.tvbox.screens.plugin.catalog.CatalogScreen
import com.muedsa.tvbox.screens.plugin.catalog.CatalogScreenViewModel
import com.muedsa.tvbox.screens.plugin.favorites.FavoriteMediaScreen
import com.muedsa.tvbox.screens.plugin.favorites.FavoriteMediaScreenViewModel
import com.muedsa.tvbox.screens.plugin.home.PluginHomeScreen
import com.muedsa.tvbox.screens.plugin.home.PluginHomeViewModel
import com.muedsa.tvbox.screens.plugin.search.SearchScreen
import com.muedsa.tvbox.screens.plugin.search.SearchScreenViewModel
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds


private val LocalHomeScreenBackgroundState = compositionLocalOf<ScreenBackgroundState?> { null }

@Composable
fun LocalHomeScreenBackgroundStateProvider(
    backgroundState: ScreenBackgroundState = rememberScreenBackgroundState(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        value = LocalHomeScreenBackgroundState provides backgroundState,
        content = content
    )
}

@Composable
fun useLocalHomeScreenBackgroundState(): ScreenBackgroundState {
    return LocalHomeScreenBackgroundState.current
        ?: throw RuntimeException("Please wrap your app with LocalHomeScreenBackgroundState")
}

val tabs: Array<PluginScreenNavTab> = PluginScreenNavTab.entries.toTypedArray()

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PluginScreen(
    pluginHomeViewModel: PluginHomeViewModel = hiltViewModel(),
    searchScreenViewModel: SearchScreenViewModel = hiltViewModel(),
    favoriteMediaScreenViewModel: FavoriteMediaScreenViewModel = hiltViewModel(),
    catalogScreenViewModel: CatalogScreenViewModel = hiltViewModel(),
) {
    val backgroundState = rememberScreenBackgroundState()
    ScreenBackground(state = backgroundState)
    LocalHomeScreenBackgroundStateProvider(backgroundState) {
        var focusedTabIndex by rememberSaveable { mutableIntStateOf(0) }
        var selectedTabIndex by rememberSaveable { mutableIntStateOf(focusedTabIndex) }

        var tabPanelIndex by rememberSaveable { mutableIntStateOf(selectedTabIndex) }

        LaunchedEffect(selectedTabIndex) {
            delay(150.milliseconds)
            tabPanelIndex = selectedTabIndex
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            TabRow(
                modifier = Modifier
                    .align(alignment = Alignment.CenterHorizontally)
                    .padding(top = 24.dp, bottom = 24.dp)
                    .focusRestorer(),
                selectedTabIndex = selectedTabIndex,
                indicator = { tabPositions, doesTabRowHaveFocus ->
                    // FocusedTab's indicator
                    TabRowDefaults.PillIndicator(
                        currentTabPosition = tabPositions[focusedTabIndex],
                        doesTabRowHaveFocus = doesTabRowHaveFocus,
                        activeColor = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.4f),
                        inactiveColor = Color.Transparent
                    )

                    // SelectedTab's indicator
                    TabRowDefaults.PillIndicator(
                        currentTabPosition = tabPositions[selectedTabIndex],
                        doesTabRowHaveFocus = doesTabRowHaveFocus
                    )
                }
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onFocus = { focusedTabIndex = index },
                        onClick = {
                            if (selectedTabIndex != index) {
                                backgroundState.change(type = ScreenBackgroundType.BLUR)
                                selectedTabIndex = index
                            }
                        },
                        colors = TabDefaults.pillIndicatorTabColors(
                            selectedContentColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text(
                            tab.title,
                            fontSize = MaterialTheme.typography.labelLarge.fontSize,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
            }
            HomeContent(
                tabIndex = tabPanelIndex,
                pluginHomeViewModel = pluginHomeViewModel,
                searchScreenViewModel = searchScreenViewModel,
                favoriteMediaScreenViewModel = favoriteMediaScreenViewModel,
                catalogScreenViewModel = catalogScreenViewModel,
            )
        }
    }
}

@Composable
fun HomeContent(
    tabIndex: Int,
    pluginHomeViewModel: PluginHomeViewModel,
    searchScreenViewModel: SearchScreenViewModel,
    favoriteMediaScreenViewModel: FavoriteMediaScreenViewModel,
    catalogScreenViewModel: CatalogScreenViewModel,
) {
    val tab = tabs[tabIndex]
    when (tab) {
        PluginScreenNavTab.Main -> PluginHomeScreen(
            pluginHomeViewModel = pluginHomeViewModel
        )
        PluginScreenNavTab.Search -> SearchScreen(
            searchScreenViewModel = searchScreenViewModel
        )
        PluginScreenNavTab.Favorites -> FavoriteMediaScreen(
            favoriteMediaScreenViewModel = favoriteMediaScreenViewModel
        )
        PluginScreenNavTab.Catalog -> CatalogScreen(
            catalogScreenViewModel = catalogScreenViewModel
        )
    }
}