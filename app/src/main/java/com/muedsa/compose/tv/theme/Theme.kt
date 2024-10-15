package com.muedsa.compose.tv.theme

import androidx.compose.runtime.Composable
import androidx.tv.material3.MaterialTheme


@Composable
fun TvTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TvDarkColorScheme,
        content = content
    )
}