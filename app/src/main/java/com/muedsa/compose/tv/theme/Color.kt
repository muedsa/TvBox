package com.muedsa.compose.tv.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.tv.material3.ColorScheme
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import androidx.tv.material3.darkColorScheme


val TvDarkColorScheme = darkColorScheme(
//    background = background,
//    onBackground = onBackground,
//    surface = surface,
//    onSurface = onSurface,
//    surfaceVariant = surfaceVariant,
//    onSurfaceVariant = onSurfaceVariant
)

val ColorScheme.surfaceContainerLowest: Color
    get() = Color(red = 14, green = 14, blue = 14) // N-4 0E0E0E

val ColorScheme.surfaceContainerLowe: Color
    get() = Color(red = 27, green = 27, blue = 27) // N-10 1B1B1B

val ColorScheme.surfaceContainer: Color
    get() = Color(red = 30, green = 31, blue = 32) // N-12 1E1F20

val ColorScheme.surfaceContainerHigh: Color
    get() = Color(red = 40, green = 42, blue = 44) // N-17 282A2C

val ColorScheme.surfaceContainerHighest: Color
    get() = Color(red = 51, green = 53, blue = 55) // N-22 333537

val ColorScheme.outline: Color
    get() = Color(red = 147, green = 143, blue = 153) // PaletteTokens.NeutralVariant60

val ColorScheme.outlineVariant: Color
    get() = Color(red = 73, green = 69, blue = 79) // PaletteTokens.NeutralVariant30

@Composable
fun TvColorPreview(modifier: Modifier = Modifier) {
    LazyColumn(modifier) {
        item {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary),
                text = "[On]Primary",
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Black
            )
        }
        item {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer),
                text = "[On]PrimaryContainer",
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Black
            )
        }
        item {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.inversePrimary),
                text = "InversePrimary",
                fontWeight = FontWeight.Black
            )
        }
        item {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.secondary),
                text = "[On]Secondary",
                color = MaterialTheme.colorScheme.onSecondary,
                fontWeight = FontWeight.Black
            )
        }
        item {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                text = "[On]SecondaryContainer",
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.Black
            )
        }
        item {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.tertiary),
                text = "[On]Tertiary",
                color = MaterialTheme.colorScheme.onTertiary,
                fontWeight = FontWeight.Black
            )
        }
        item {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.tertiaryContainer),
                text = "[On]TertiaryContainer",
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                fontWeight = FontWeight.Black
            )
        }
        item {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background),
                text = "[On]Background",
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Black
            )
        }
        item {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface),
                text = "[On]Surface",
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Black
            )
        }
        item {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                text = "[On]SurfaceVariant",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Black
            )
        }
        item {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceTint),
                text = "SurfaceTint",
                fontWeight = FontWeight.Black
            )
        }
        item {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.inverseSurface),
                text = "[On]InverseSurface",
                color = MaterialTheme.colorScheme.inverseOnSurface,
                fontWeight = FontWeight.Black
            )
        }
        item {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.error),
                text = "[On]Error",
                color = MaterialTheme.colorScheme.onError,
                fontWeight = FontWeight.Black
            )
        }
        item {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.errorContainer),
                text = "[On]ErrorContainer",
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontWeight = FontWeight.Black
            )
        }
        item {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.border),
                text = "Border",
                fontWeight = FontWeight.Black
            )
        }
        item {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.borderVariant),
                text = "BorderVariant",
                fontWeight = FontWeight.Black
            )
        }
        item {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.scrim),
                text = "Scrim",
                fontWeight = FontWeight.Black
            )
        }
    }
}


@Preview
@Composable
fun TvThemeColorPreview() {
    TvTheme {
        TvColorPreview()
    }
}