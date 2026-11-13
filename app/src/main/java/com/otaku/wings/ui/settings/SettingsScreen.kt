package com.otaku.wings.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.otaku.wings.BuildConfig
import com.otaku.wings.data.local.ThemeMode
import com.otaku.wings.ui.common.TabHeader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val GITHUB_URL = "https://github.com/looooooooord/Wings"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val uriHandler = LocalUriHandler.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let { viewModel.exportBackup(it) } }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { viewModel.importBackup(it) } }

    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeSnackbar()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            TabHeader(
                title = "الإعدادات",
                actions = {
                    IconButton(onClick = { showHelpDialog = true }) {
                        Icon(Icons.Outlined.HelpOutline, contentDescription = "المساعدة")
                    }
                }
            )

            SettingsSectionTitle("الشاشة")
            ListItem(
                headlineContent = { Text("المظهر") }
            )
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
            ) {
                SegmentedButton(
                    selected = state.themeMode == ThemeMode.LIGHT,
                    onClick = { viewModel.setThemeMode(ThemeMode.LIGHT) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                    modifier = Modifier.weight(1f)
                ) { Text("فاتح") }
                SegmentedButton(
                    selected = state.themeMode == ThemeMode.SYSTEM,
                    onClick = { viewModel.setThemeMode(ThemeMode.SYSTEM) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                    modifier = Modifier.weight(1f)
                ) { Text("النظام") }
                SegmentedButton(
                    selected = state.themeMode == ThemeMode.DARK,
                    onClick = { viewModel.setThemeMode(ThemeMode.DARK) },
                    shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                    modifier = Modifier.weight(1f)
                ) { Text("داكن") }
            }

            HorizontalDivider()

            SettingsSectionTitle("البيانات")
            ListItem(
                headlineContent = { Text("تصدير نسخة احتياطية") },
                modifier = Modifier.clickable {
                    val timestamp = SimpleDateFormat("yyMMddHHmm", Locale.getDefault()).format(Date())
                    exportLauncher.launch("WingsBackUp_$timestamp.json")
                }
            )
            ListItem(
                headlineContent = { Text("استيراد نسخة احتياطية") },
                modifier = Modifier.clickable { importLauncher.launch(arrayOf("application/json")) }
            )
            ListItem(
                headlineContent = { Text("حذف البيانات") },
                modifier = Modifier.clickable { showDeleteConfirm = true }
            )

            HorizontalDivider()

            SettingsSectionTitle("مساحة التخزين")
            ListItem(
                headlineContent = { Text("حذف ذاكرة الصور المؤقتة") },
                modifier = Modifier.clickable { viewModel.clearImageCache() }
            )

            Text(
                "v${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                textAlign = TextAlign.Center
            )
            Text(
                "Github",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { uriHandler.openUri(GITHUB_URL) }
                    .padding(top = 2.dp, bottom = 16.dp),
                textAlign = TextAlign.Center
            )
        }
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text("المساعدة") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("· من أيقونة الإعدادات أعلى القارئ يمكنك تغيير طريقة العرض واتجاه القراءة وعرض صفحتين.")
                    Text("· استخدم زر المحفوظات في صفحة التفاصيل لحفظ قصصك المفضلة وعرضها في تبويب مكتبتي.")
                    Text("· يتيح لك المظهر اختيار وضع إضاءة التطبيق: فاتح أو النظام أو داكن.")
                    Text("· يمكنك تصدير المحفوظات وسجل القراءة إلى ملف أو استعادتهما منه.")
                    Text("· يحذف حذف البيانات المحفوظات وسجل القراءة نهائيًا. يرجى الانتباه لأن العملية لا يمكن التراجع عنها.")
                    Text("· يحذف هذا الخيار ذاكرة الصور المؤقتة لتوفير مساحة التخزين.")
                }
            },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) { Text("موافق") }
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("حذف البيانات") },
            text = { Text("هل تريد حذف المحفوظات وسجل القراءة بالكامل؟ لا يمكن التراجع عن هذه العملية.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearAllData()
                    showDeleteConfirm = false
                }) { Text("حذف") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("إلغاء") }
            }
        )
    }
}

@Composable
private fun SettingsSectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
    )
}
