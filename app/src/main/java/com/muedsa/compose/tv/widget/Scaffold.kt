package com.muedsa.compose.tv.widget

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceColors
import androidx.tv.material3.SurfaceDefaults
import com.muedsa.compose.tv.LocalRightSideDrawerControllerProvider
import com.muedsa.compose.tv.LocalToastMsgBoxControllerProvider


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Scaffold(
    holdBack: Boolean = true,
    enableDrawer: Boolean = true,
    colors: SurfaceColors = SurfaceDefaults.colors(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ),
    content: @Composable () -> Unit
) {
    val toastMessageBoxController = remember { ToastMessageBoxController() }

    LocalToastMsgBoxControllerProvider(toastMessageBoxController) {
        if (holdBack) {
            AppBackHandler {
                toastMessageBoxController.warning("再次点击返回键退出")
            }
        }
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .semantics {
                    testTagsAsResourceId = true
                },
            shape = RectangleShape,
            colors = colors
        ) {
            ToastMessageBox(controller = toastMessageBoxController) {
                if (enableDrawer) {
                    val drawerController = remember { RightSideDrawerController() }
                    RightSideDrawer(
                        controller = drawerController
                    ) {
                        LocalRightSideDrawerControllerProvider(drawerController, content)
                    }
                } else content()
            }
        }
    }
}