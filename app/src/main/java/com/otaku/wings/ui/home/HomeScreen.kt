package com.otaku.wings.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.otaku.wings.data.model.Series
import com.otaku.wings.ui.common.SeriesCarouselSection
import com.otaku.wings.ui.common.TabHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSeriesClick: (Series) -> Unit,
    onMoreClick: (String) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val hasContent = state.latest.isNotEmpty() || state.popular.isNotEmpty()

    Scaffold { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabHeader(
                title = "الأحدث",
                actions = {
                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    } else {
                        IconButton(onClick = { viewModel.refresh() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "تحديث")
                        }
                    }
                }
            )
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            when {
                // تحديث 중에도 이미 있던 콘텐츠는 그대로 보여주고, 최초 로딩일 때만 전체 화면 스피너
                state.isLoading && !hasContent -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                state.error != null && !hasContent -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(state.error!!, color = MaterialTheme.colorScheme.error)
                        Button(onClick = { viewModel.refresh() }) { Text("إعادة المحاولة") }
                    }
                }
                !hasContent -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("لا يوجد محتوى", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Button(onClick = { viewModel.refresh() }) { Text("تحديث") }
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 16.dp)
                    ) {
                        state.error?.let {
                            Text(
                                it,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(16.dp)
                            )
                        }

                        SeriesCarouselSection(
                            title = "أحدث القصص المصورة",
                            items = state.latest,
                            onItemClick = onSeriesClick,
                            onMoreClick = { onMoreClick("latest") }
                        )
                        SeriesCarouselSection(
                            title = "القصص المصورة الشائعة",
                            items = state.popular,
                            onItemClick = onSeriesClick,
                            onMoreClick = { onMoreClick("popular") }
                        )
                        SeriesCarouselSection(
                            title = "المحفوظات",
                            items = state.bookmarked,
                            onItemClick = onSeriesClick,
                            onMoreClick = { onMoreClick("bookmarked") }
                        )
                        SeriesCarouselSection(
                            title = "آخر ما تمت مشاهدته",
                            items = state.recent,
                            onItemClick = onSeriesClick,
                            onMoreClick = { onMoreClick("recent") }
                        )
                    }
                }
            }
            }
        }
    }
}
