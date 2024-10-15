package com.muedsa.compose.tv.widget

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import com.muedsa.compose.tv.theme.TvTheme
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds


@Composable
fun ToastMessageBox(
    controller: ToastMessageBoxController = remember { ToastMessageBoxController() },
    content: @Composable () -> Unit = {}
) {
    LaunchedEffect(key1 = controller.duration) {
        delay(1.seconds)
        if (controller.duration > 0) {
            controller.duration--
        }
    }
    content()
    AnimatedVisibility(
        visible = controller.duration > 0,
        enter = slideInHorizontally { -it },
        exit = slideOutHorizontally { -it / 2 } + fadeOut()
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .height(IntrinsicSize.Min)
                .padding(top = 100.dp)
        ) {
            Text(
                modifier = Modifier
                    .weight(8f)
                    .shadow(
                        elevation = 10.dp,
                        shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)
                    )
                    .background(color = controller.type.containerColor)
                    .padding(20.dp)
                    .wrapContentWidth(Alignment.Start),
                text = controller.message ?: "something happened",
                color = controller.type.contentColor,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.weight(2f))
        }

    }
}


@Stable
class ToastMessageBoxController(
    initMessage: String? = null,
    initType: ToastMessageType = ToastMessageType.INFO,
    initDuration: Int = 0
) {
    var message by mutableStateOf(initMessage)
    var type by mutableStateOf(initType)
    var duration by mutableIntStateOf(initDuration)

    fun prompt(
        message: String,
        type: ToastMessageType = ToastMessageType.INFO,
        duration: SnackbarDuration = SnackbarDuration.Short
    ) {
        this.message = message
        this.type = type
        this.duration = when (duration) {
            SnackbarDuration.Short -> 3
            SnackbarDuration.Long -> 5
            SnackbarDuration.Indefinite -> -1
            else -> -1
        }
    }

    fun info(message: String, duration: SnackbarDuration = SnackbarDuration.Short) {
        prompt(message = message, type = ToastMessageType.INFO, duration = duration)
    }

    fun warning(message: String, duration: SnackbarDuration = SnackbarDuration.Short) {
        prompt(message = message, type = ToastMessageType.WARNING, duration = duration)
    }

    fun success(message: String = "success", duration: SnackbarDuration = SnackbarDuration.Short) {
        prompt(message = message, type = ToastMessageType.SUCCESS, duration = duration)
    }

    fun error(message: String = "error", duration: SnackbarDuration = SnackbarDuration.Short) {
        prompt(message = message, type = ToastMessageType.ERROR, duration = duration)
    }

    fun error(error: Throwable?, duration: SnackbarDuration = SnackbarDuration.Short) {
        val message = error?.localizedMessage ?: error?.message ?: "error"
        error(message = message, duration = duration)
    }

    fun clear() {
        this.message = null
        this.type = ToastMessageType.INFO
        this.duration = 0
    }
}

enum class ToastMessageType(
    val contentColor: Color,
    val containerColor: Color,
) {
    INFO(
        contentColor = Color(0xFF_00_4A_77),
        containerColor = Color(0xFF_7F_CF_FF)
    ),
    SUCCESS(
        contentColor = Color(0xFF_0F_52_23),
        containerColor = Color(0xFF_6D_D5_8C)
    ),
    WARNING(
        contentColor = Color(0xFF_FF_FF_FF),
        containerColor = Color(0xFF_FF_88_00)
    ),
    ERROR(
        contentColor = Color(0xFF_F9_DE_DC),
        containerColor = Color(0xFF_8C_1D_18),
    ),
    ;
}

@Preview(device = "id:tv_4k")
@Composable
fun ToastMessageBoxPreview() {
    val controller = remember { ToastMessageBoxController() }
    TvTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize()
        ) {
            ToastMessageBox(controller = controller) {

                Column {
                    Button(onClick = { controller.info("info") }) { Text("info") }
                    Button(onClick = { controller.success("success") }) { Text("success") }
                    Button(onClick = { controller.warning("warning") }) { Text("warning") }
                    Button(onClick = { controller.error("error") }) { Text("error") }
                }

            }
        }

    }
}