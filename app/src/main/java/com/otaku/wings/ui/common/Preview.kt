package com.otaku.wings.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.otaku.wings.data.model.Series
import com.otaku.wings.ui.theme.Spacing
import com.otaku.wings.ui.theme.WingsPreviewTheme
import com.otaku.wings.ui.theme.WingsType

/** Sample data for previews only — never used at runtime. */
object PreviewData {
    val series = Series(
        id = "s1",
        title = "حكاية السيف الأزرق",
        author = "مؤلف تجريبي",
        coverUrl = "",
        synopsis = "قصة تجريبية لعرض التخطيط.",
        genre = "أكشن",
        latestChapter = "الفصل 12",
        sourceUrl = "https://example.com/s1"
    )
    val seriesList = listOf(
        series,
        series.copy(id = "s2", title = "ظل المدينة", author = "كاتب آخر"),
        series.copy(id = "s3", title = "رحلة الشمال", latestChapter = "الفصل 4")
    )
}

/** Renders a component inside the app theme, forced to RTL, in light or dark. */
@Composable
fun WingsPreview(content: @Composable () -> Unit) {
    WingsPreviewTheme(dark = false, content)
}

@Composable
fun WingsPreviewDark(content: @Composable () -> Unit) {
    WingsPreviewTheme(dark = true, content)
}

@Preview(name = "Component gallery", showBackground = true, widthDp = 400)
@Composable
private fun PreviewGallery() {
    WingsPreview {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Text("معاينة", style = WingsType.screenTitle)
            SectionHeader("قسم", onMoreClick = {})
            SeriesListItem(PreviewData.series, onClick = {}, selectable = true, selected = true)
            ChapterRow(title = "الفصل 12", onClick = {}, date = "12/03", isRead = true)
            ChapterRow(title = "الفصل 11", onClick = {}, isLastRead = true)
        }
    }
}

@Preview(name = "Card preview", widthDp = 200)
@Composable
private fun CardPreview() {
    WingsPreview {
        Column(Modifier.width(160.dp)) {
            SeriesCard(PreviewData.series, onClick = {})
        }
    }
}
