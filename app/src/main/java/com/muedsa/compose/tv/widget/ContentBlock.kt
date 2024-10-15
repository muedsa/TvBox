package com.muedsa.compose.tv.widget

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.muedsa.compose.tv.model.ContentModel
import com.muedsa.compose.tv.theme.TvTheme

@Composable
fun ContentBlock(
    modifier: Modifier = Modifier,
    model: ContentModel,
    type: ContentBlockType = ContentBlockType.CAROUSEL,
    verticalArrangement: Arrangement.Vertical = Arrangement.Bottom,
    textAlign: TextAlign? = null,
    descriptionMaxLines: Int = Int.MAX_VALUE,
) {
    Column(
        modifier = modifier,
        verticalArrangement = verticalArrangement
    ) {
        if (ContentBlockType.CAROUSEL == type) {
            if (!model.subtitle.isNullOrEmpty()) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = model.subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = textAlign,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
            }
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .basicMarquee(),
                text = model.title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineLarge,
                textAlign = textAlign,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
            if (!model.description.isNullOrEmpty()) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = model.description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = textAlign,
                    style = MaterialTheme.typography.bodyLarge,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = descriptionMaxLines
                )
            }
        } else if (ContentBlockType.CARD == type) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .basicMarquee(),
                text = model.title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                textAlign = textAlign,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
            if (!model.subtitle.isNullOrEmpty()) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = model.subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = textAlign,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
            }
            if (!model.description.isNullOrEmpty()) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = model.description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = textAlign,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = descriptionMaxLines
                )
            }
        }
    }

}

enum class ContentBlockType {
    CARD,
    CAROUSEL
}

@Preview
@Composable
fun CarouselContentBlockPreview() {
    TvTheme {
        ContentBlock(
            model = ContentModel(
                title = "Power Sisters",
                subtitle = "Superhero/Action • 2022 • 2h 15m",
                description = "A dynamic duo of superhero siblings " +
                        "join forces to save their city from a sini" +
                        "ster villain, redefining sisterhood in action."
            ),
            type = ContentBlockType.CAROUSEL
        )
    }
}

@Preview
@Composable
fun CardContentBlockPreview() {
    TvTheme {
        ContentBlock(
            model = ContentModel(
                title = "Power Sisters",
                subtitle = "Superhero/Action • 2022 • 2h 15m",
                description = "A dynamic duo of superhero siblings " +
                        "join forces to save their city from a sini" +
                        "ster villain, redefining sisterhood in action."
            ),
            type = ContentBlockType.CARD,
        )
    }
}

@Preview
@Composable
fun CardCenterContentBlockPreview() {
    TvTheme {
        ContentBlock(
            model = ContentModel(
                title = "Power Sisters",
                subtitle = "Superhero/Action • 2022 • 2h 15m",
                description = "A dynamic duo of superhero siblings " +
                        "join forces to save their city from a sini" +
                        "ster villain, redefining sisterhood in action."
            ),
            type = ContentBlockType.CARD,
            verticalArrangement = Arrangement.Top,
            textAlign = TextAlign.Center
        )
    }
}