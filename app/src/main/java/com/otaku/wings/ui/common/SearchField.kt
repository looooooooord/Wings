package com.otaku.wings.ui.common

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import com.otaku.wings.R
import com.otaku.wings.ui.theme.Dimens
import com.otaku.wings.ui.theme.Spacing
import com.otaku.wings.ui.theme.WingsType

/** The app's only search input: 52dp tall, 12dp radius, clear button, IME search. */
@Composable
fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = stringResource(R.string.search_placeholder)
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.height(Dimens.SearchFieldHeight),
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        textStyle = WingsType.settingsTitle,
        placeholder = {
            Text(
                text = placeholder,
                style = WingsType.settingsTitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.height(Dimens.Icon)
            )
        },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.action_clear_search),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                IconButton(onClick = onSearch) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(R.string.action_search),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

/** Small pill used for genres and other short metadata. */
@Composable
fun MetaChip(text: String, modifier: Modifier = Modifier) {
    androidx.compose.material3.Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Text(
            text = text,
            style = WingsType.label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs)
        )
    }
}
