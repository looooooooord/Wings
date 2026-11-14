package com.otaku.wings.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.otaku.wings.R
import com.otaku.wings.ui.theme.Dimens
import com.otaku.wings.ui.theme.Spacing
import com.otaku.wings.ui.theme.WingsType

/**
 * Title + actions header for the four top-level tabs.
 * Same typography as [WingsTopBar] so the shell reads as one system.
 */
@Composable
fun TabHeader(
    title: String,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .heightIn(min = Dimens.TouchTarget)
            .padding(start = Spacing.xl, end = Spacing.sm, top = Spacing.md, bottom = Spacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = WingsType.screenTitle,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
        Row(verticalAlignment = Alignment.CenterVertically, content = actions)
    }
}

/**
 * Compact bar for pushed screens: back button, single-line title, optional actions.
 * Colors are parameterised so the detail screen can fade it in as the header scrolls away.
 */
@Composable
fun WingsTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    showTitle: Boolean = true,
    navigationIconColor: Color = MaterialTheme.colorScheme.onSurface,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Surface(color = containerColor, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .heightIn(min = Dimens.TouchTarget)
                .padding(horizontal = Spacing.xs, vertical = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.action_back),
                    tint = navigationIconColor
                )
            }
            Text(
                text = title,
                style = WingsType.sectionHeading,
                color = if (showTitle) titleColor else Color.Transparent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = Spacing.sm)
            )
            Row(verticalAlignment = Alignment.CenterVertically, content = actions)
        }
    }
}

/** Section heading with an optional trailing "show more" action. */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    onMoreClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = Spacing.lg, end = Spacing.sm, top = Spacing.xxl, bottom = Spacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = WingsType.sectionHeading,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
        if (onMoreClick != null) {
            TextButton(onClick = onMoreClick) {
                Text(
                    text = stringResource(R.string.action_more),
                    style = WingsType.label,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = Spacing.xs).size(Dimens.IconInline)
                )
            }
        }
    }
}
