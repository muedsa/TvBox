package com.muedsa.compose.tv.widget

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.OutlinedIconButtonDefaults
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import com.muedsa.compose.tv.theme.TvTheme

@Composable
fun OutlinedIconBox(
    modifier: Modifier = Modifier,
    inverse: Boolean = false,
    scaleUp: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .scale(if (scaleUp) 1f else 1.1f)
            .size(OutlinedIconButtonDefaults.MediumButtonSize),
        shape = CircleShape,
        border = Border(
            border = BorderStroke(
                width = 2.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            ),
            shape = CircleShape
        ),
        colors = SurfaceDefaults.colors(
            containerColor = if (inverse) MaterialTheme.colorScheme.onSurface else Color.Transparent,
            contentColor = if (inverse) MaterialTheme.colorScheme.inverseOnSurface else MaterialTheme.colorScheme.onSurface,
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
            content = content
        )
    }
}

@Preview
@Composable
fun OutlinedIconBoxPreview() {
    TvTheme {
        Column(modifier = Modifier.padding(5.dp)) {
            OutlinedIconBox {
                Icon(Icons.Outlined.AccountBox, contentDescription = "")
            }
            Spacer(modifier = Modifier.height(5.dp))
            OutlinedIconBox(inverse = true) {
                Icon(Icons.Outlined.AccountBox, contentDescription = "")
            }
            Spacer(modifier = Modifier.height(5.dp))
            OutlinedIconBox(scaleUp = true) {
                Icon(Icons.Outlined.AccountBox, contentDescription = "")
            }
            Spacer(modifier = Modifier.height(5.dp))
            OutlinedIconBox(inverse = true, scaleUp = true) {
                Icon(Icons.Outlined.AccountBox, contentDescription = "")
            }
        }
    }
}