package com.muedsa.compose.tv.widget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.SecureFlagPolicy
import androidx.navigation.NavController
import androidx.tv.material3.DrawerValue
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import com.muedsa.compose.tv.theme.surfaceContainer

fun FullWidthDialogProperties(
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = true,
    securePolicy: SecureFlagPolicy = SecureFlagPolicy.Inherit,
    decorFitsSystemWindows: Boolean = true
) = DialogProperties(
    dismissOnBackPress = dismissOnBackPress,
    dismissOnClickOutside = dismissOnClickOutside,
    securePolicy = securePolicy,
    usePlatformDefaultWidth = false,
    decorFitsSystemWindows = decorFitsSystemWindows,
)


@Composable
fun RightSideDrawerWithNavDrawerContent(
    controller: RightSideDrawerController
) {
    if (controller.drawerState.currentValue == DrawerValue.Open) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(all = 24.dp),
            horizontalArrangement = Arrangement.End
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

class RightSideDrawerWithNavController(
    val navController: NavController,
    val drawerRoute: String,
) : RightSideDrawerController() {

    override fun pop(content: @Composable () -> Unit) {
        super.pop(content)
        navController.navigate(drawerRoute)
    }

    override fun close() {
        super.close()
        navController.popBackStack()
    }
}