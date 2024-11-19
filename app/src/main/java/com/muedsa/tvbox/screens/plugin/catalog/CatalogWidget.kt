package com.muedsa.tvbox.screens.plugin.catalog

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.OutlinedButton
import androidx.tv.material3.OutlinedIconButton
import androidx.tv.material3.Text
import com.muedsa.tvbox.api.data.MediaCatalogConfig
import com.muedsa.tvbox.api.data.MediaCatalogOption

@Composable
fun CatalogWidget(
    config: MediaCatalogConfig,
    catalogScreenViewModel: CatalogScreenViewModel
) {
    val selectedOptionsState = remember {
        mutableStateListOf<MediaCatalogOption>().apply {
            addAll(MediaCatalogOption.getDefault(config.catalogOptions))
        }
    }
    var optionsExpand by remember {
        mutableStateOf(false)
    }
    var pager = remember { catalogScreenViewModel.newPager(config, selectedOptionsState) }
    LaunchedEffect(optionsExpand) {
        if (!optionsExpand) {
            pager = catalogScreenViewModel.newPager(config, selectedOptionsState)
        }
    }
    BackHandler(enabled = optionsExpand) {
        optionsExpand = false
    }
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 30.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(onClick = {
                optionsExpand = !optionsExpand
            }) {
                Text(text = "筛选项")
                Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                Icon(
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                    imageVector = if (optionsExpand) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.ArrowDropDown,
                    contentDescription = "展开筛选项"
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            OutlinedIconButton(onClick = {
                selectedOptionsState.clear()
                selectedOptionsState.addAll(MediaCatalogOption.getDefault(config.catalogOptions))
                // catalogScreenViewModel.newPager(config, selectedOptionsState)
            }) {
                Icon(
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = "重置筛选项"
                )
            }
        }

        if (optionsExpand) {
            CatalogOptionsWidget(
                options = config.catalogOptions,
                selectedOptions = selectedOptionsState
            )
        } else {
            CatalogPagingWidget(
                config = config,
                pager = pager
            )
        }
    }
}