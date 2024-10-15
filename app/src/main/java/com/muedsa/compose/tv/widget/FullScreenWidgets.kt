package com.muedsa.compose.tv.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.OutlinedIconButton
import androidx.tv.material3.Text

@Composable
fun NotFoundScreen(model: Boolean = false) {
    FillTextScreen(context = "404 Not Found (っ °Д °;)っ", model = model)
}

@Composable
fun NotImplementScreen(model: Boolean = false) {
    FillTextScreen(context = "Not Implement (っ °Д °;)っ", model = model)
}

@Composable
fun LoadingScreen(model: Boolean = false) {
    FillTextScreen(context = "Loading... {{{(>_<)}}}", model = model)
}

@Composable
fun EmptyDataScreen(model: Boolean = false) {
    FillTextScreen(context = "Empty(っ °Д °;)っ", model = model)
}


@Composable
fun ErrorScreen(
    onError: (() -> Unit)? = null,
    onRefresh: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Error (っ °Д °;)っ", color = Color.White)
        if (onRefresh != null) {
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedIconButton(onClick = { onRefresh() }) {
                Icon(imageVector = Icons.Outlined.Refresh, contentDescription = "Refresh")
            }
        }
    }

    if (onError != null) {
        LaunchedEffect(key1 = Unit) {
            onError.invoke()
        }
    }
}


@Composable
fun FillTextScreen(context: String, model: Boolean = false) {
    var modifier = Modifier.fillMaxSize()
    if (model) {
        modifier = modifier.background(MaterialTheme.colorScheme.surface.copy(0.35f))
    }
    Column(
        modifier = modifier
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = context, color = Color.White)
    }
}