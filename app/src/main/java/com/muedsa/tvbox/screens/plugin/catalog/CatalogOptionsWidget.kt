package com.muedsa.tvbox.screens.plugin.catalog

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.FilterChip
import androidx.tv.material3.FilterChipDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.muedsa.compose.tv.theme.ImageCardRowCardPadding
import com.muedsa.compose.tv.theme.ScreenPaddingLeft
import com.muedsa.compose.tv.useLocalToastMsgBoxController
import com.muedsa.tvbox.api.data.MediaCatalogOption

@OptIn(ExperimentalLayoutApi::class, ExperimentalTvMaterial3Api::class)
@Composable
fun CatalogOptionsWidget(
    options: List<MediaCatalogOption>,
    selectedOptions: SnapshotStateList<MediaCatalogOption>,
) {
    val toastController = useLocalToastMsgBoxController()

    LazyColumn(
        modifier = Modifier.padding(start = ScreenPaddingLeft),
        contentPadding = PaddingValues(top = ImageCardRowCardPadding)
    ) {
        items(items = options) { option ->
            Text(
                text = "${option.name}${if (option.required) "*" else ""}",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow {
                option.items.forEach { item ->
                    val selected = selectedOptions
                        .any { selectedOption ->
                            selectedOption.value == option.value
                                    && selectedOption.items
                                .any { selectedItem -> selectedItem.value == item.value }
                        }
                    FilterChip(
                        modifier = Modifier.padding(8.dp),
                        selected = selected,
                        leadingIcon = if (selected) {
                            {
                                Icon(
                                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = "选择${item.name}"
                                )
                            }
                        } else null,
                        onClick = {
                            val existOption =
                                selectedOptions.find { selectedOption -> selectedOption.value == option.value }
                            if (selected) {
                                // 当前是被选中状态 点击移除选中状态
                                if (existOption != null) {
                                    if (existOption.multiple && existOption.items.size > 1) {
                                        selectedOptions.remove(existOption)
                                        selectedOptions.add(
                                            option.copy(
                                                items = existOption.items.filter { existItem -> existItem.value != item.value }
                                            )
                                        )
                                    } else {
                                        if (!existOption.required) {
                                            selectedOptions.remove(existOption)
                                        } else {
                                            toastController.warning("当前选项是必选的")
                                        }
                                    }
                                }
                            } else {
                                // 当前是未被选中状态 点击选中
                                if (existOption != null) {
                                    selectedOptions.remove(existOption)
                                }
                                selectedOptions.add(
                                    option.copy(
                                        items = buildList {
                                            if (existOption?.multiple == true) {
                                                addAll(existOption.items)
                                            }
                                            add(item)
                                        }
                                    ))
                            }
                        }
                    ) {
                        Text(text = item.name)
                    }
                }
            }
            HorizontalDivider(modifier = Modifier.padding(bottom = 10.dp))
        }
    }
}