package com.otaku.wings.ui.my

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.otaku.wings.ui.common.SeriesListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyScreen(
    onSeriesClick: (com.otaku.wings.data.model.Series) -> Unit,
    viewModel: MyViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val items = (state.bookmarked + state.recent).distinctBy { it.id }

    Scaffold(
        topBar = { TopAppBar(title = { Text("مكتبتي") }) }
    ) { padding ->
        if (items.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("مكتبتك فارغة")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
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
