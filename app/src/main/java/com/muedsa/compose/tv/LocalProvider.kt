package com.muedsa.compose.tv

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import com.muedsa.compose.tv.widget.RightSideDrawerController
import com.muedsa.compose.tv.widget.ToastMessageBoxController

private val LocalNavHostController = compositionLocalOf<NavHostController?> { null }
private val LocalToastMsgBoxController = compositionLocalOf<ToastMessageBoxController?> { null }
private val LocalRightSideDrawerController = compositionLocalOf<RightSideDrawerController?> { null }

@Composable
fun LocalNavHostControllerProvider(
    navHostController: NavHostController,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalNavHostController provides remember { navHostController },
        content = content
    )
}

@Composable
fun LocalToastMsgBoxControllerProvider(
    toastMessageBoxController: ToastMessageBoxController,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalToastMsgBoxController provides toastMessageBoxController,
        content = content
    )
}

@Composable
fun LocalRightSideDrawerControllerProvider(
    rightSideDrawerController: RightSideDrawerController,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalRightSideDrawerController provides rightSideDrawerController,
        content = content
    )
}

@Composable
fun useLocalNavHostController(): NavHostController {
    return LocalNavHostController.current
        ?: throw RuntimeException("Please wrap your app with LocalNavHostControllerProvider")
}

@Composable
fun useLocalToastMsgBoxController(): ToastMessageBoxController {
    return LocalToastMsgBoxController.current
        ?: throw RuntimeException("Please wrap your app with LocalToastMsgBoxController")
}

@Composable
fun useLocalRightSideDrawerController(): RightSideDrawerController {
    return LocalRightSideDrawerController.current
        ?: throw RuntimeException("Please wrap your app with LocalRightSideDrawerController")
}