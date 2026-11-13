package com.otaku.wings.ui.my

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.otaku.wings.ui.common.SeriesListItem
import com.otaku.wings.ui.common.TabHeader

@Composable
fun MyScreen(
    onSeriesClick: (com.otaku.wings.data.model.Series) -> Unit,
    viewModel: MyViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val items = (state.bookmarked + state.recent).distinctBy { it.id }

    Scaffold { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabHeader(title = "مكتبتي")
            if (items.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("مكتبتك فارغة")
            }
            } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(items, key = { it.id }) { series ->
                    SeriesListItem(
                        series = series,
                        onClick = { onSeriesClick(series) }
                    )
                }
            }
            }
        }
    }
}
