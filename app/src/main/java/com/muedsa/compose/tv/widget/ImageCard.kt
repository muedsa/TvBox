package com.muedsa.compose.tv.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Card
import coil3.compose.AsyncImagePainter
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.muedsa.compose.tv.model.ContentModel
import com.muedsa.compose.tv.theme.CardContentPadding
import com.muedsa.compose.tv.theme.HorizontalPosterSize
import com.muedsa.compose.tv.theme.TvTheme
import com.muedsa.compose.tv.theme.VerticalPosterSize

@Composable
fun ImageContentCard(
    modifier: Modifier = Modifier,
    url: String,
    imageSize: DpSize,
    backgroundColor: Color = Color.Unspecified,
    type: CardType = CardType.STANDARD,
    model: ContentModel? = null,
    onItemFocus: () -> Unit = {},
    onItemClick: () -> Unit = {},
) {

    if (model == null) {
        ImageCard(
            modifier = modifier,
            url = url,
            imageSize = imageSize,
            backgroundColor = backgroundColor,
            onItemFocus = onItemFocus,
            onItemClick = onItemClick
        )
    } else if (type == CardType.STANDARD) {
        StandardImageContentCard(
            modifier = modifier,
            url = url,
            imageSize = imageSize,
            backgroundColor = backgroundColor,
            model = model,
            onItemFocus = onItemFocus,
            onItemClick = onItemClick
        )
    } else if (type == CardType.COMPACT) {
        CompactImageContentCard(
            modifier = modifier,
            url = url,
            imageSize = imageSize,
            backgroundColor = backgroundColor,
            model = model,
            onItemFocus = onItemFocus,
            onItemClick = onItemClick
        )
    } else if (type == CardType.WIDE_STANDARD) {
        WideStandardImageContentCard(
            modifier = modifier,
            url = url,
            imageSize = imageSize,
            backgroundColor = backgroundColor,
            model = model,
            onItemFocus = onItemFocus,
            onItemClick = onItemClick
        )
    }
}

enum class CardType {
    STANDARD, COMPACT, WIDE_STANDARD,
}

@Composable
fun ImageCard(
    modifier: Modifier = Modifier,
    url: String,
    imageSize: DpSize,
    backgroundColor: Color = Color.Unspecified,
    showCircularProgressIndicator: Boolean = true,
    onItemFocus: () -> Unit = {},
    onItemClick: () -> Unit = {},
    content: @Composable () -> Unit = {}
) {
    SubcomposeAsyncImage(
        model = ImageRequest
            .Builder(LocalContext.current)
            .data(url)
            .crossfade(true)
            .build(),
        contentDescription = null,
        contentScale = ContentScale.FillBounds
    ) {
        val state by painter.state.collectAsState()
        Card(
            onClick = { onItemClick() },
            modifier = modifier
                .size(imageSize)
                .background(backgroundColor)
                .onFocusChanged {
                    if (it.isFocused) {
                        onItemFocus()
                    }
                }
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                if (state is AsyncImagePainter.State.Success) {
                    this@SubcomposeAsyncImage.SubcomposeAsyncImageContent()
                } else if (showCircularProgressIndicator
                    && (state is AsyncImagePainter.State.Loading
                            || state is AsyncImagePainter.State.Empty)
                ) {
                    CircularProgressIndicator()
                }
                content()
            }
        }
    }
}

@Composable
fun StandardImageContentCard(
    modifier: Modifier = Modifier,
    url: String,
    imageSize: DpSize,
    backgroundColor: Color = Color.Unspecified,
    model: ContentModel,
    onItemFocus: () -> Unit = {},
    onItemClick: () -> Unit = {},
) {
    Column(modifier) {
        ImageCard(
            url = url,
            imageSize = imageSize,
            backgroundColor = backgroundColor,
            onItemFocus = onItemFocus,
            onItemClick = onItemClick,
        )
        ContentBlock(
            modifier = Modifier
                .width(imageSize.width)
                .padding(
                    start = 8.dp, top = 0.dp, end = 8.dp, bottom = 8.dp
                ),
            model = model,
            type = ContentBlockType.CARD,
            verticalArrangement = Arrangement.Top,
            textAlign = TextAlign.Center,
            descriptionMaxLines = 2
        )
    }
}

@Composable
fun CompactImageContentCard(
    modifier: Modifier = Modifier,
    url: String,
    imageSize: DpSize,
    backgroundColor: Color = Color.Unspecified,
    model: ContentModel,
    onItemFocus: () -> Unit = {},
    onItemClick: () -> Unit = {},
) {
    ImageCard(
        modifier = modifier,
        url = url,
        imageSize = imageSize,
        backgroundColor = backgroundColor,
        showCircularProgressIndicator = false,
        onItemFocus = onItemFocus,
        onItemClick = onItemClick,
    ) {
        ContentBlock(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight()
                .padding(CardContentPadding),
            model = model,
            type = ContentBlockType.CARD,
            verticalArrangement = Arrangement.Bottom,
            descriptionMaxLines = 2
        )
    }
}


@Composable
fun WideStandardImageContentCard(
    modifier: Modifier = Modifier,
    url: String,
    imageSize: DpSize,
    backgroundColor: Color = Color.Unspecified,
    model: ContentModel,
    onItemFocus: () -> Unit = {},
    onItemClick: () -> Unit = {},
) {
    Row(modifier) {
        ImageCard(
            modifier = modifier,
            url = url,
            imageSize = imageSize,
            backgroundColor = backgroundColor,
            onItemFocus = onItemFocus,
            onItemClick = onItemClick,
        )
        ContentBlock(
            modifier = Modifier
                .size(imageSize)
                .padding(CardContentPadding),
            model = model,
            type = ContentBlockType.CARD,
            verticalArrangement = Arrangement.Top
        )
    }
}


@Preview
@Composable
fun NoContentImageContentCardPreview() {
    TvTheme {
        ImageContentCard(url = "", imageSize = HorizontalPosterSize)
    }
}

@Preview
@Composable
fun VerticalNoContentImageContentCardPreview() {
    TvTheme {
        ImageContentCard(url = "", imageSize = VerticalPosterSize)
    }
}

@Preview
@Composable
fun StandardImageContentCardPreview() {
    TvTheme {
        ImageContentCard(
            url = "",
            imageSize = HorizontalPosterSize,
            type = CardType.STANDARD,
            model = ContentModel(
                title = "Power Sisters",
                subtitle = "Superhero/Action • 2022 • 2h 15m",
                description = "A dynamic duo of superhero siblings " + "join forces to save their city from a sini" + "ster villain, redefining sisterhood in action."
            )
        )
    }
}

@Preview
@Composable
fun VerticalStandardImageContentCardPreview() {
    TvTheme {
        ImageContentCard(
            url = "",
            imageSize = VerticalPosterSize,
            type = CardType.STANDARD,
            model = ContentModel(
                title = "Power Sisters",
                subtitle = "Superhero/Action • 2022 • 2h 15m",
                description = "A dynamic duo of superhero siblings " + "join forces to save their city from a sini" + "ster villain, redefining sisterhood in action."
            )
        )
    }
}


@Preview
@Composable
fun WideStandardImageContentCardPreview() {
    TvTheme {
        ImageContentCard(
            url = "",
            imageSize = HorizontalPosterSize,
            type = CardType.WIDE_STANDARD,
            model = ContentModel(
                title = "Power Sisters",
                subtitle = "Superhero/Action • 2022 • 2h 15m",
                description = "A dynamic duo of superhero siblings " + "join forces to save their city from a sini" + "ster villain, redefining sisterhood in action."
            )
        )
    }
}

@Preview
@Composable
fun VerticalWideStandardImageContentCardPreview() {
    TvTheme {
        ImageContentCard(
            url = "",
            imageSize = VerticalPosterSize,
            type = CardType.WIDE_STANDARD,
            model = ContentModel(
                title = "Power Sisters",
                subtitle = "Superhero/Action • 2022 • 2h 15m",
                description = "A dynamic duo of superhero siblings " + "join forces to save their city from a sini" + "ster villain, redefining sisterhood in action."
            )
        )
    }
}


@Preview
@Composable
fun CompactImageContentCardPreview() {
    TvTheme {
        ImageContentCard(
            url = "",
            imageSize = HorizontalPosterSize,
            type = CardType.COMPACT,
            model = ContentModel(
                title = "Power Sisters",
                subtitle = "Superhero/Action • 2022 • 2h 15m",
                description = "A dynamic duo of superhero siblings " + "join forces to save their city from a sini" + "ster villain, redefining sisterhood in action."
            )
        )
    }
}

@Preview
@Composable
fun VerticalCompactImageContentCardPreview() {
    TvTheme {
        ImageContentCard(
            url = "", imageSize = VerticalPosterSize, type = CardType.COMPACT, model = ContentModel(
                title = "Power Sisters",
                subtitle = "Superhero/Action • 2022 • 2h 15m",
                description = "A dynamic duo of superhero siblings " + "join forces to save their city from a sini" + "ster villain, redefining sisterhood in action."
            )
        )
    }
}