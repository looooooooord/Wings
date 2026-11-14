package com.otaku.wings.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.otaku.wings.ui.theme.Dimens
import com.otaku.wings.ui.theme.Spacing
import com.otaku.wings.ui.theme.WingsType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode

/** Titled group of settings rows. */
@Composable
fun SettingsGroup(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = WingsType.label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(
                start = Spacing.lg,
                end = Spacing.lg,
                top = Spacing.xxl,
                bottom = Spacing.sm
            )
        )
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg)
        ) {
            Column(content = content)
        }
    }
}

/**
 * One settings line: optional leading icon, title, supporting text, and a trailing slot.
 * The whole row is clickable when [onClick] is provided; toggles should use [WingsSwitchRow].
 */
@Composable
fun SettingsRow(
    title: String,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    icon: ImageVector? = null,
    destructive: Boolean = false,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Dimens.SettingsRowMinHeight)
            .then(if (onClick != null) Modifier.clickable(enabled = enabled, onClick = onClick) else Modifier)
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = if (destructive) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.padding(Spacing.sm).size(Dimens.IconInline)
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = WingsType.settingsTitle,
                color = when {
                    !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    destructive -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurface
                },
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (supporting != null) {
                Text(
                    text = supporting,
                    style = WingsType.settingsSupporting,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Spacing.xxs)
                )
            }
        }
        trailing?.invoke()
    }
}

/** Settings row whose whole surface toggles the switch. */
@Composable
fun WingsSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    supporting: String? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Dimens.SettingsRowMinHeight)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = WingsType.settingsTitle, color = MaterialTheme.colorScheme.onSurface)
            if (supporting != null) {
                Text(
                    text = supporting,
                    style = WingsType.settingsSupporting,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Spacing.xxs)
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

/** Single-choice segmented control bound to an arbitrary option type. */
@Composable
fun <T> WingsSegmentedRow(
    options: List<T>,
    selected: T,
    label: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        options.forEachIndexed { index, option ->
            SegmentedButton(
                selected = option == selected,
                onClick = { onSelect(option) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                modifier = Modifier.weight(1f),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    activeBorderColor = MaterialTheme.colorScheme.primary,
                    inactiveContainerColor = Color.Transparent,
                    inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    inactiveBorderColor = MaterialTheme.colorScheme.outline
                )
            ) {
                Text(text = label(option), style = WingsType.label, maxLines = 2)
            }
        }
    }
}

@Preview(name = "Settings rows")
@Composable
private fun SettingsPreview() {
    WingsPreview {
        Column(Modifier.fillMaxWidth()) {
            SettingsGroup("المظهر") {
                SettingsRow(
                    title = "المظهر",
                    supporting = "اختر وضع الإضاءة.",
                    icon = Icons.Outlined.DarkMode,
                    onClick = {}
                )
                WingsSwitchRow(title = "عرض صفحتين معًا", checked = true, onCheckedChange = {})
            }
        }
    }
}
