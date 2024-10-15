package com.muedsa.compose.tv.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ButtonBorder
import androidx.tv.material3.ButtonGlow
import androidx.tv.material3.ButtonScale
import androidx.tv.material3.ButtonShape
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.ProvideTextStyle
import androidx.tv.material3.WideButton
import androidx.tv.material3.WideButtonContentColor
import androidx.tv.material3.WideButtonDefaults


@Composable
fun TwoSideWideButton(
    onClick: () -> Unit,
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    icon: (@Composable () -> Unit)? = null,
    subtitle: (@Composable () -> Unit)? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    background: @Composable () -> Unit = {
        WideButtonDefaults.Background(
            enabled = enabled,
            interactionSource = interactionSource
        )
    },
    scale: ButtonScale = WideButtonDefaults.scale(),
    glow: ButtonGlow = WideButtonDefaults.glow(),
    shape: ButtonShape = WideButtonDefaults.shape(),
    contentColor: WideButtonContentColor = WideButtonDefaults.contentColor(),
    border: ButtonBorder = WideButtonDefaults.border(),
    rightSideContent: @Composable () -> Unit,
) {
    WideButton(
        onClick = onClick,
        modifier = modifier,
        onLongClick = onLongClick,
        interactionSource = interactionSource,
        background = background,
        scale = scale,
        glow = glow,
        shape = shape,
        contentColor = contentColor,
        border = border,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // left side
            Row {
                if (icon != null) {
                    icon()
                    Spacer(
                        modifier = Modifier.padding(end = 12.dp)
                    )
                }
                Column {
                    ProvideTextStyle(
                        value = MaterialTheme.typography.titleMedium,
                        content = {
                            Box(
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                            ) {
                                title()
                            }
                        }
                    )
                    if (subtitle != null) {
                        ProvideTextStyle(
                            value = MaterialTheme.typography.bodySmall.copy(
                                color = LocalContentColor.current.copy(alpha = 0.8f)
                            ),
                            content = subtitle
                        )
                    }
                }
            }

            // right side
            rightSideContent()
        }
    }
}


@Composable
fun WideButtonDefaults.NoBackground(interactionSource: InteractionSource) {
    val isFocused = interactionSource.collectIsFocusedAsState().value
    val isPressed = interactionSource.collectIsPressedAsState().value
    if (isFocused || isPressed) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.onSurface)
        )
    }
}