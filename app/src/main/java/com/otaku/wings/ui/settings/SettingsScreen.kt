package com.otaku.wings.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.otaku.wings.BuildConfig
import com.otaku.wings.R
import com.otaku.wings.data.local.ReadingDirection
import com.otaku.wings.data.local.ThemeMode
import com.otaku.wings.data.model.ViewerMode
import com.otaku.wings.ui.common.ConfirmDialog
import com.otaku.wings.ui.common.SettingsGroup
import com.otaku.wings.ui.common.SettingsRow
import com.otaku.wings.ui.common.TabHeader
import com.otaku.wings.ui.common.WingsPreview
import com.otaku.wings.ui.common.WingsSegmentedRow
import com.otaku.wings.ui.common.WingsSnackbarHost
import com.otaku.wings.ui.common.WingsSwitchRow
import com.otaku.wings.ui.theme.Dimens
import com.otaku.wings.ui.theme.Spacing
import com.otaku.wings.ui.theme.WingsType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val GITHUB_URL = "https://github.com/looooooooord/Wings"

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val uriHandler = LocalUriHandler.current
    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }
    var showHelp by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showImportConfirm by remember { mutableStateOf(false) }
    var showCacheConfirm by remember { mutableStateOf(false) }
    var pendingImportUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let { viewModel.exportBackup(it) } }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            pendingImportUri = uri
            showImportConfirm = true
        }
    }

    val messageRes = state.message?.let { messageRes(it) }
    val messageText = messageRes?.let { stringResource(it) }
    LaunchedEffect(state.message, messageText) {
        if (messageText != null) {
            snackbarHostState.showSnackbar(messageText)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        snackbarHost = { WingsSnackbarHost(snackbarHostState) },
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .widthIn(max = Dimens.MaxContentWidth)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                TabHeader(
                    title = stringResource(R.string.tab_settings),
                    actions = {
                        IconButton(onClick = { showHelp = true }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                                contentDescription = stringResource(R.string.settings_help_open),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                )

                if (state.isBusy) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.lg),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                }

                SettingsGroup(title = stringResource(R.string.settings_group_appearance)) {
                    SettingsRow(
                        title = stringResource(R.string.settings_theme),
                        supporting = stringResource(R.string.settings_theme_support),
                        icon = Icons.Outlined.DarkMode
                    )
                    WingsSegmentedRow(
                        options = themeOptions(),
                        selected = state.themeMode,
                        label = { themeLabel(it) },
                        onSelect = viewModel::setThemeMode,
                        modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md)
                    )
                }

                SettingsGroup(title = stringResource(R.string.settings_group_reader)) {
                    SettingsRow(
                        title = stringResource(R.string.settings_reader_mode),
                        supporting = stringResource(R.string.settings_reader_mode_support),
                        icon = Icons.AutoMirrored.Outlined.MenuBook
                    )
                    WingsSegmentedRow(
                        options = listOf(ViewerMode.WEBTOON, ViewerMode.MANGA),
                        selected = state.viewerMode,
                        label = { modeLabel(it) },
                        onSelect = viewModel::setViewerMode,
                        modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md)
                    )
                    SettingsRow(
                        title = stringResource(R.string.settings_reading_direction),
                        supporting = stringResource(R.string.settings_reading_direction_support)
                    )
                    WingsSegmentedRow(
                        options = listOf(ReadingDirection.LTR, ReadingDirection.RTL),
                        selected = state.readingDirection,
                        label = { directionLabel(it) },
                        onSelect = viewModel::setReadingDirection,
                        modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md)
                    )
                    WingsSwitchRow(
                        title = stringResource(R.string.settings_two_page),
                        supporting = stringResource(R.string.settings_two_page_support),
                        checked = state.twoPageMode,
                        onCheckedChange = viewModel::setTwoPageMode
                    )
                    if (state.twoPageMode) {
                        SettingsRow(title = stringResource(R.string.settings_first_page))
                        WingsSegmentedRow(
                            options = listOf(true, false),
                            selected = state.firstPageSingle,
                            label = {
                                if (it) stringResource(R.string.settings_first_page_single)
                                else stringResource(R.string.settings_first_page_double)
                            },
                            onSelect = viewModel::setFirstPageSingle,
                            modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md)
                        )
                    }
                }

                SettingsGroup(title = stringResource(R.string.settings_group_data)) {
                    SettingsRow(
                        title = stringResource(R.string.settings_export),
                        supporting = stringResource(R.string.settings_export_support),
                        icon = Icons.Outlined.FileDownload,
                        enabled = !state.isBusy,
                        onClick = {
                            val timestamp = SimpleDateFormat("yyMMddHHmm", Locale.getDefault()).format(Date())
                            exportLauncher.launch("WingsBackUp_$timestamp.json")
                        }
                    )
                    SettingsRow(
                        title = stringResource(R.string.settings_import),
                        supporting = stringResource(R.string.settings_import_support),
                        icon = Icons.Outlined.FileUpload,
                        enabled = !state.isBusy,
                        onClick = { importLauncher.launch(arrayOf("application/json")) }
                    )
                    SettingsRow(
                        title = stringResource(R.string.settings_delete_data),
                        supporting = stringResource(R.string.settings_delete_data_support),
                        icon = Icons.Outlined.DeleteForever,
                        destructive = true,
                        enabled = !state.isBusy,
                        onClick = { showDeleteConfirm = true }
                    )
                }

                SettingsGroup(title = stringResource(R.string.settings_group_storage)) {
                    SettingsRow(
                        title = stringResource(R.string.settings_clear_cache),
                        supporting = stringResource(R.string.settings_clear_cache_support),
                        icon = Icons.Outlined.PhotoLibrary,
                        enabled = !state.isBusy,
                        onClick = { showCacheConfirm = true }
                    )
                }

                SettingsGroup(title = stringResource(R.string.settings_group_about)) {
                    SettingsRow(
                        title = stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
                        supporting = stringResource(R.string.app_name),
                        icon = Icons.Outlined.Info
                    )
                    SettingsRow(
                        title = stringResource(R.string.settings_github),
                        icon = Icons.AutoMirrored.Outlined.OpenInNew,
                        onClick = { uriHandler.openUri(GITHUB_URL) }
                    )
                    SettingsRow(
                        title = stringResource(R.string.settings_help),
                        icon = Icons.AutoMirrored.Outlined.HelpOutline,
                        onClick = { showHelp = true }
                    )
                }

                Box(modifier = Modifier.size(Spacing.xxl))
            }
        }
    }

    if (showHelp) {
        HelpDialog(onDismiss = { showHelp = false })
    }

    if (showDeleteConfirm) {
        ConfirmDialog(
            title = stringResource(R.string.settings_delete_data),
            message = stringResource(R.string.confirm_delete_data_body),
            confirmLabel = stringResource(R.string.action_delete),
            onConfirm = {
                viewModel.clearAllData()
                showDeleteConfirm = false
            },
            onDismiss = { showDeleteConfirm = false },
            destructive = true
        )
    }

    if (showCacheConfirm) {
        ConfirmDialog(
            title = stringResource(R.string.confirm_clear_cache_title),
            message = stringResource(R.string.confirm_clear_cache_body),
            confirmLabel = stringResource(R.string.action_confirm),
            onConfirm = {
                viewModel.clearImageCache()
                showCacheConfirm = false
            },
            onDismiss = { showCacheConfirm = false }
        )
    }

    if (showImportConfirm) {
        ConfirmDialog(
            title = stringResource(R.string.confirm_import_title),
            message = stringResource(R.string.confirm_import_body),
            confirmLabel = stringResource(R.string.settings_import),
            onConfirm = {
                pendingImportUri?.let { viewModel.importBackup(it) }
                pendingImportUri = null
                showImportConfirm = false
            },
            onDismiss = {
                pendingImportUri = null
                showImportConfirm = false
            }
        )
    }
}

private fun messageRes(message: SettingsMessage): Int = when (message) {
    SettingsMessage.EXPORT_SUCCESS -> R.string.snack_export_success
    SettingsMessage.EXPORT_FAILURE -> R.string.snack_export_failure
    SettingsMessage.IMPORT_SUCCESS -> R.string.snack_import_success
    SettingsMessage.IMPORT_FAILURE -> R.string.snack_import_failure
    SettingsMessage.DELETE_SUCCESS -> R.string.snack_delete_success
    SettingsMessage.CACHE_SUCCESS -> R.string.snack_cache_success
    SettingsMessage.CACHE_FAILURE -> R.string.snack_cache_failure
}

private fun themeOptions(): List<ThemeMode> =
    listOf(ThemeMode.LIGHT, ThemeMode.SYSTEM, ThemeMode.DARK)

@Composable
private fun themeLabel(mode: ThemeMode): String = stringResource(
    when (mode) {
        ThemeMode.LIGHT -> R.string.theme_light
        ThemeMode.SYSTEM -> R.string.theme_system
        ThemeMode.DARK -> R.string.theme_dark
    }
)

@Composable
private fun modeLabel(mode: ViewerMode): String = stringResource(
    when (mode) {
        ViewerMode.WEBTOON -> R.string.reader_mode_scroll
        ViewerMode.MANGA -> R.string.reader_mode_swipe
    }
)

@Composable
private fun directionLabel(direction: ReadingDirection): String = stringResource(
    when (direction) {
        ReadingDirection.LTR -> R.string.direction_ltr
        ReadingDirection.RTL -> R.string.direction_rtl
    }
)

/** Scannable help dialog: one bullet per feature, scrollable on short screens. */
@Composable
private fun HelpDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = { Text(stringResource(R.string.settings_help), style = WingsType.emptyStateTitle) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                listOf(
                    R.string.help_item_reader,
                    R.string.help_item_bookmarks,
                    R.string.help_item_theme,
                    R.string.help_item_backup,
                    R.string.help_item_delete,
                    R.string.help_item_cache
                ).forEach { entry ->
                    Text(
                        text = "• " + stringResource(entry),
                        style = WingsType.settingsSupporting,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_confirm), style = WingsType.button)
            }
        }
    )
}

@Preview(name = "Settings light", heightDp = 900, widthDp = 400)
@Composable
private fun SettingsPreview() {
    WingsPreview {
        Column(Modifier.fillMaxSize()) {
            TabHeader(title = "الإعدادات")
            SettingsGroup(title = "المظهر") {
                SettingsRow(
                    title = "المظهر",
                    supporting = "اختر وضع الإضاءة، أو اتبع إعداد النظام.",
                    icon = Icons.Outlined.DarkMode
                )
                WingsSegmentedRow(
                    options = listOf(ThemeMode.LIGHT, ThemeMode.SYSTEM, ThemeMode.DARK),
                    selected = ThemeMode.SYSTEM,
                    label = { if (it == ThemeMode.DARK) "داكن" else "فاتح" },
                    onSelect = {},
                    modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md)
                )
            }
        }
    }
}
