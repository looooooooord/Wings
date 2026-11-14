package com.otaku.wings.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.otaku.wings.R
import com.otaku.wings.ui.theme.Dimens
import com.otaku.wings.ui.theme.Spacing
import com.otaku.wings.ui.theme.WingsType

/** Standard empty state: icon in a tonal disc, title, body, optional action. */
@Composable
fun EmptyState(
    title: String,
    message: String? = null,
    icon: ImageVector = Icons.Outlined.Inbox,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.xxxl, vertical = Spacing.huge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.padding(Spacing.lg).size(Dimens.Icon)
            )
        }
        Text(
            text = title,
            style = WingsType.emptyStateTitle,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = Spacing.lg)
        )
        if (message != null) {
            Text(
                text = message,
                style = WingsType.emptyStateBody,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = Spacing.sm)
                    .widthIn(max = Dimens.MaxContentWidth)
            )
        }
        if (actionLabel != null && onAction != null) {
            Button(
                onClick = onAction,
                modifier = Modifier.padding(top = Spacing.xl)
            ) {
                Text(actionLabel, style = WingsType.button)
            }
        }
    }
}

/** Standard error state with an explanation and a retry action. */
@Composable
fun ErrorState(
    kind: ErrorKind,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val title = when (kind) {
        ErrorKind.OFFLINE -> stringResource(R.string.error_offline_title)
        ErrorKind.GENERIC -> stringResource(R.string.error_generic_title)
    }
    val body = when (kind) {
        ErrorKind.OFFLINE -> stringResource(R.string.error_offline_body)
        ErrorKind.GENERIC -> stringResource(R.string.error_generic_body)
    }
    EmptyState(
        title = title,
        message = body,
        icon = when (kind) {
            ErrorKind.OFFLINE -> Icons.Outlined.CloudOff
            ErrorKind.GENERIC -> Icons.Outlined.Inbox
        },
        actionLabel = stringResource(R.string.action_retry),
        onAction = onRetry,
        modifier = modifier
    )
}

/**
 * Non-blocking banner for a failed background refresh while stale content stays visible.
 */
@Composable
fun InlineMessageBanner(
    text: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Text(
                text = text,
                style = WingsType.settingsSupporting,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            if (actionLabel != null && onAction != null) {
                TextButton(onClick = onAction) { Text(actionLabel, style = WingsType.label) }
            }
        }
    }
}

/** Convenience for centering a state inside a Box-based screen body. */
@Composable
fun BoxedState(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) { content() }
}
