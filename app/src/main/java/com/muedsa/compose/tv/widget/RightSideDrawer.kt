package com.muedsa.compose.tv.widget

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.tv.material3.DrawerState
import androidx.tv.material3.DrawerValue
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.ModalNavigationDrawer
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import com.muedsa.compose.tv.theme.surfaceContainer


@Composable
fun RightSideDrawer(
    controller: RightSideDrawerController = RightSideDrawerController(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            drawerContent = {
                BackHandler(enabled = it == DrawerValue.Open) {
                    controller.close()
                }
                if (it == DrawerValue.Open) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(all = 24.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxHeight(),
                                colors = SurfaceDefaults.colors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Box(
                                    Modifier
                                        .fillMaxHeight()
                                        .padding(all = 20.dp)
                                ) {
                                    controller.ContentCompose()
                                }
                            }
                        }
                    }
                }
            },
            drawerState = controller.drawerState,
            content = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Surface(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        content()
                    }
                }
            }
        )
    }
}


open class RightSideDrawerController {
    private val contentState: MutableState<@Composable () -> Unit> = mutableStateOf({})
    val drawerState: DrawerState = DrawerState(DrawerValue.Closed)

    @Composable
    fun ContentCompose() {
        contentState.value.invoke()
    }

    open fun pop(content: @Composable () -> Unit) {
        contentState.value = content
        drawerState.setValue(DrawerValue.Open)
    }

    open fun close() {
        drawerState.setValue(DrawerValue.Closed)
    }
}