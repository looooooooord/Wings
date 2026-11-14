package com.otaku.wings.ui.viewer

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.otaku.wings.R
import com.otaku.wings.data.local.ReadingDirection
import com.otaku.wings.data.model.MangaPage
import com.otaku.wings.data.model.ViewerMode
import com.otaku.wings.data.model.displayTitle
import com.otaku.wings.ui.common.WingsSegmentedRow
import com.otaku.wings.ui.common.WingsSwitchRow
import com.otaku.wings.ui.theme.Dimens
import com.otaku.wings.ui.theme.ReaderAccent
import com.otaku.wings.ui.theme.ReaderCanvas
import com.otaku.wings.ui.theme.ReaderOnScrim
import com.otaku.wings.ui.theme.ReaderOnScrimMuted
import com.otaku.wings.ui.theme.ReaderScrim
import com.otaku.wings.ui.theme.Spacing
import com.otaku.wings.ui.theme.WingsShapeTokens
import com.otaku.wings.ui.theme.WingsType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun ViewerScreen(
    chapterUrl: String,
    chapterTitle: String,
    seriesId: String,
    seriesUrl: String,
    seriesTitle: String,
    seriesAuthor: String,
    coverUrl: String,
    onBack: () -> Unit,
    onNavigateChapter: (chapterUrl: String, chapterTitle: String) -> Unit
) {
    val app = LocalContext.current.applicationContext as android.app.Application
    val viewModel: ViewerViewModel = viewModel(
        key = chapterUrl,
        factory = viewModelFactory {
            initializer {
                ViewerViewModel(
                    app,
                    chapterUrl,
                    chapterTitle,
                    seriesId,
                    seriesUrl,
                    seriesTitle,
                    seriesAuthor,
                    coverUrl
                )
            }
        }
    )
    val state by viewModel.state.collectAsState()

    // Immersive: hide the system bars together with the controls, and always restore them on exit.
    val view = LocalView.current
    val activity = LocalContext.current.findActivity()
    val hideBars = !state.showControls && !state.showSettings && !state.isLoading && state.error == null
    DisposableEffect(hideBars) {
        val window = activity?.window
        if (window != null) {
            val controller = WindowCompat.getInsetsController(window, view)
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            if (hideBars) {
                controller.hide(WindowInsetsCompat.Type.systemBars())
            } else {
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
        onDispose {
            activity?.window?.let { window ->
                WindowCompat.getInsetsController(window, view)
                    .show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    var seekTarget by remember { mutableStateOf<Int?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ReaderCanvas)
    ) {
        when {
            state.isLoading -> ReaderMessage(
                message = stringResource(R.string.viewer_loading),
                showProgress = true
            )

            state.error != null -> ReaderError(
                message = stringResource(
                    when (state.error) {
                        com.otaku.wings.ui.common.ErrorKind.OFFLINE -> R.string.error_offline_title
                        else -> R.string.viewer_error_title
                    }
                ),
                onRetry = viewModel::retry,
                onBack = onBack
            )

            state.pages.isEmpty() -> ReaderError(
                message = stringResource(R.string.viewer_empty_chapter),
                onRetry = viewModel::retry,
                onBack = onBack
            )

            else -> {
                val toggle: () -> Unit = { viewModel.toggleControls() }
                when (state.mode) {
                    ViewerMode.WEBTOON -> WebtoonViewer(
                        pages = state.pages,
                        initialPage = state.currentPage,
                        seekTo = seekTarget,
                        onSeekHandled = { seekTarget = null },
                        seriesTitle = seriesTitle,
                        chapterTitle = chapterTitle,
                        hasNextChapter = state.nextChapter != null,
                        onNextChapter = {
                            state.nextChapter?.let {
                                onNavigateChapter(it.url, it.displayTitle(seriesTitle))
                            }
                        },
                        onBackToDetails = onBack,
                        onPageVisible = viewModel::onPageChanged,
                        onTap = toggle
                    )

                    ViewerMode.MANGA -> MangaViewer(
                        pages = state.pages,
                        initialPage = state.currentPage,
                        readingDirection = state.readingDirection,
                        twoPageMode = state.twoPageMode,
                        firstPageSingle = state.firstPageSingle,
                        seekTo = seekTarget,
                        onSeekHandled = { seekTarget = null },
                        onPageChanged = viewModel::onPageChanged,
                        onTap = toggle
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = state.showControls && !state.isLoading && state.pages.isNotEmpty(),
            enter = fadeIn() + slideInVertically { -it / 4 },
            exit = fadeOut() + slideOutVertically { -it / 4 },
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            ViewerTopBar(
                chapterTitle = chapterTitle,
                seriesTitle = seriesTitle,
                onBack = onBack,
                onSettingsClick = viewModel::toggleSettings
            )
        }

        AnimatedVisibility(
            visible = state.showControls && !state.isLoading && state.pages.isNotEmpty(),
            enter = fadeIn() + slideInVertically { it / 4 },
            exit = fadeOut() + slideOutVertically { it / 4 },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            ViewerBottomBar(
                current = state.currentPage.coerceAtLeast(0),
                total = state.pages.size,
                prevEnabled = state.prevChapter != null,
                nextEnabled = state.nextChapter != null,
                onSeek = { page ->
                    viewModel.onPageChanged(page)
                    seekTarget = page
                },
                onPrev = {
                    state.prevChapter?.let {
                        onNavigateChapter(it.url, it.displayTitle(seriesTitle))
                    }
                },
                onNext = {
                    state.nextChapter?.let {
                        onNavigateChapter(it.url, it.displayTitle(seriesTitle))
                    }
                }
            )
        }
    }

    if (state.showSettings) {
        ViewerSettingsSheet(
            state = state,
            onDismiss = viewModel::toggleSettings,
            onModeChange = viewModel::setMode,
            onReadingDirectionChange = viewModel::setReadingDirection,
            onTwoPageModeChange = viewModel::setTwoPageMode,
            onFirstPageSingleChange = viewModel::setFirstPageSingle
        )
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

// ── Reader canvas states ─────────────────────────────────────

@Composable
private fun ReaderMessage(message: String, showProgress: Boolean = false) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            if (showProgress) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(Spacing.xxxl),
                    color = ReaderOnScrim
                )
            }
            Text(
                text = message,
                style = WingsType.emptyStateBody,
                color = ReaderOnScrimMuted
            )
        }
    }
}

@Composable
private fun ReaderError(message: String, onRetry: () -> Unit, onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .widthIn(max = Dimens.MaxContentWidth)
                .padding(Spacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Icon(
                imageVector = Icons.Outlined.BrokenImage,
                contentDescription = null,
                tint = ReaderOnScrimMuted,
                modifier = Modifier.size(Spacing.xxl)
            )
            Text(
                text = message,
                style = WingsType.emptyStateTitle,
                color = ReaderOnScrim
            )
            Button(onClick = onRetry) {
                Text(stringResource(R.string.action_retry), style = WingsType.button)
            }
            OutlinedButton(onClick = onBack) {
                Text(stringResource(R.string.action_back), style = WingsType.button)
            }
        }
    }
}

// ── Webtoon mode ─────────────────────────────────────────────

@Composable
private fun WebtoonViewer(
    pages: List<MangaPage>,
    initialPage: Int,
    seekTo: Int?,
    onSeekHandled: () -> Unit,
    seriesTitle: String,
    chapterTitle: String,
    hasNextChapter: Boolean,
    onNextChapter: () -> Unit,
    onBackToDetails: () -> Unit,
    onPageVisible: (Int) -> Unit,
    onTap: () -> Unit
) {
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = initialPage.coerceIn(0, (pages.size - 1).coerceAtLeast(0))
    )

    // Debounced, distinct progress tracking: no database write per frame or per recomposition.
    LaunchedEffect(listState, pages.size) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collectLatest { index ->
                delay(250)
                onPageVisible(index)
            }
    }

    LaunchedEffect(seekTo) {
        seekTo?.let {
            listState.scrollToItem(it.coerceIn(0, (pages.size - 1).coerceAtLeast(0)))
            onSeekHandled()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState
    ) {
        itemsIndexed(
            items = pages,
            key = { _, page -> page.index },
            contentType = { _, _ -> "page" }
        ) { index, page ->
            ZoomableImage(
                imageUrl = page.imageUrl,
                contentDescription = stringResource(R.string.viewer_cover_description, index + 1),
                contentScale = ContentScale.FillWidth,
                onTap = onTap,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 240.dp)
            )
        }
        item(key = "chapter-end") {
            ChapterEndFooter(
                hasNextChapter = hasNextChapter,
                onNextChapter = onNextChapter,
                onBackToDetails = onBackToDetails
            )
        }
    }
}

@Composable
private fun ChapterEndFooter(
    hasNextChapter: Boolean,
    onNextChapter: () -> Unit,
    onBackToDetails: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.xxl, vertical = Spacing.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Text(
            text = stringResource(R.string.viewer_end_chapter),
            style = WingsType.settingsSupporting,
            color = ReaderOnScrimMuted
        )
        if (hasNextChapter) {
            Button(onClick = onNextChapter) {
                Text(stringResource(R.string.viewer_chapter_next), style = WingsType.button)
            }
        }
        OutlinedButton(onClick = onBackToDetails) {
            Text(stringResource(R.string.viewer_back_to_details), style = WingsType.button)
        }
    }
}

// ── Manga mode ───────────────────────────────────────────────

@Composable
private fun MangaViewer(
    pages: List<MangaPage>,
    initialPage: Int,
    readingDirection: ReadingDirection,
    twoPageMode: Boolean,
    firstPageSingle: Boolean,
    seekTo: Int?,
    onSeekHandled: () -> Unit,
    onPageChanged: (Int) -> Unit,
    onTap: () -> Unit
) {
    val spreads = remember(pages.size, twoPageMode, firstPageSingle) {
        buildSpreads(pages.size, twoPageMode, firstPageSingle)
    }
    val pagerState = rememberPagerState(
        initialPage = spreadIndexForPage(spreads, initialPage.coerceAtLeast(0)),
        pageCount = { spreads.size }
    )

    LaunchedEffect(pagerState, spreads) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { index ->
                spreads.getOrNull(index)?.firstOrNull()?.let(onPageChanged)
            }
    }

    LaunchedEffect(seekTo) {
        seekTo?.let {
            pagerState.scrollToPage(spreadIndexForPage(spreads, it))
            onSeekHandled()
        }
    }

    // Forced LTR: page order must not depend on the app's RTL UI direction.
    androidx.compose.runtime.CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        HorizontalPager(
            state = pagerState,
            reverseLayout = reverseLayoutFor(readingDirection),
            modifier = Modifier.fillMaxSize()
        ) { pagerIndex ->
            val spread = spreads[pagerIndex]
            if (spread.size == 1) {
                ZoomableImage(
                    imageUrl = pages[spread[0]].imageUrl,
                    contentDescription = stringResource(
                        R.string.viewer_cover_description,
                        spread[0] + 1
                    ),
                    contentScale = ContentScale.Fit,
                    onTap = onTap,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Row(modifier = Modifier.fillMaxSize()) {
                    orderedSpread(spread, readingDirection).forEach { pageIndex ->
                        ZoomableImage(
                            imageUrl = pages[pageIndex].imageUrl,
                            contentDescription = stringResource(
                                R.string.viewer_cover_description,
                                pageIndex + 1
                            ),
                            contentScale = ContentScale.Fit,
                            onTap = onTap,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                        )
                    }
                }
            }
        }
    }

}

// ── Page image with zoom, double tap, retry ──────────────────

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun ZoomableImage(
    imageUrl: String,
    contentDescription: String?,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.FillWidth
) {
    val context = LocalContext.current
    var scale by remember(imageUrl) { mutableFloatStateOf(1f) }
    var offset by remember(imageUrl) { mutableStateOf(Offset.Zero) }
    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    var retryKey by remember(imageUrl) { mutableIntStateOf(0) }
    val maxScale = 4f

    fun clampOffset(candidate: Offset, forScale: Float): Offset {
        val maxX = ((forScale - 1f).coerceAtLeast(0f) * boxSize.width) / 2f
        val maxY = ((forScale - 1f).coerceAtLeast(0f) * boxSize.height) / 2f
        return Offset(
            candidate.x.coerceIn(-maxX, maxX),
            candidate.y.coerceIn(-maxY, maxY)
        )
    }

    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        val nextScale = (scale * zoomChange).coerceIn(1f, maxScale)
        scale = nextScale
        offset = if (nextScale > 1f) clampOffset(offset + panChange, nextScale) else Offset.Zero
    }

    val request = remember(imageUrl, retryKey) {
        ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(true)
            .build()
    }

    Box(
        modifier = modifier.onSizeChanged { boxSize = it },
        contentAlignment = Alignment.Center
    ) {
        SubcomposeAsyncImage(
            model = request,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
                .transformable(state = transformState, canPan = { scale > 1f })
                .pointerInput(imageUrl) {
                    detectTapGestures(
                        onTap = { onTap() },
                        onDoubleTap = { tap ->
                            val target = if (scale > 1.01f) 1f else 2.5f
                            scale = target
                            offset = if (target == 1f) {
                                Offset.Zero
                            } else {
                                clampOffset(
                                    Offset(
                                        -(tap.x - boxSize.width / 2f) * (target - 1f),
                                        -(tap.y - boxSize.height / 2f) * (target - 1f)
                                    ),
                                    target
                                )
                            }
                        }
                    )
                },
            loading = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ReaderCanvas),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(Spacing.xxxl),
                        color = ReaderOnScrimMuted
                    )
                }
            },
            error = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .heightIn(min = 200.dp)
                        .background(ReaderCanvas),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.BrokenImage,
                        contentDescription = null,
                        tint = ReaderOnScrimMuted,
                        modifier = Modifier.size(Spacing.xxl)
                    )
                    Text(
                        text = stringResource(R.string.viewer_page_error),
                        style = WingsType.settingsSupporting,
                        color = ReaderOnScrimMuted,
                        modifier = Modifier.padding(top = Spacing.sm)
                    )
                    OutlinedButton(onClick = { retryKey++ }) {
                        Text(stringResource(R.string.viewer_page_retry), style = WingsType.label)
                    }
                }
            }
        )
    }
}

// ── Controls ─────────────────────────────────────────────────

@Composable
private fun ViewerTopBar(
    chapterTitle: String,
    seriesTitle: String,
    onBack: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(ReaderScrim, Color.Transparent)
                )
            )
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.xs, vertical = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.action_back),
                    tint = ReaderOnScrim
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = Spacing.sm)
            ) {
                Text(
                    text = chapterTitle,
                    style = WingsType.readerTitle,
                    color = ReaderOnScrim,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (seriesTitle.isNotBlank()) {
                    Text(
                        text = seriesTitle,
                        style = WingsType.readerMeta,
                        color = ReaderOnScrimMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.viewer_settings_open),
                    tint = ReaderOnScrim
                )
            }
        }
    }
}

@Composable
private fun ViewerBottomBar(
    current: Int,
    total: Int,
    prevEnabled: Boolean,
    nextEnabled: Boolean,
    onSeek: (Int) -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    val sliderLabel = stringResource(R.string.viewer_page_slider)
    val pageProgress = stringResource(R.string.viewer_page_progress, current + 1, total)
    // Track the drag locally and commit once on release, so scrubbing does not write progress
    // to the database on every frame.
    var sliderValue by remember(current) { mutableFloatStateOf(current.toFloat()) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color.Transparent, ReaderScrim)))
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm)
    ) {
        if (total > 1) {
            Slider(
                value = sliderValue.coerceIn(0f, (total - 1).toFloat()),
                onValueChange = { sliderValue = it },
                onValueChangeFinished = { onSeek(sliderValue.toInt()) },
                valueRange = 0f..(total - 1).toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = ReaderAccent,
                    activeTrackColor = ReaderAccent,
                    inactiveTrackColor = ReaderOnScrim.copy(alpha = 0.24f)
                ),
                modifier = Modifier.semantics {
                    contentDescription = sliderLabel
                }
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrev, enabled = prevEnabled) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.viewer_chapter_previous),
                    tint = if (prevEnabled) ReaderOnScrim else ReaderOnScrimMuted.copy(alpha = 0.4f)
                )
            }
            Text(
                text = stringResource(R.string.viewer_page_indicator, current + 1, total),
                style = WingsType.readerMeta,
                color = ReaderOnScrim,
                modifier = Modifier
                    .weight(1f)
                    .clearAndSetSemantics {
                        contentDescription = pageProgress
                        stateDescription = pageProgress
                    },
                textAlign = TextAlign.Center
            )
            IconButton(onClick = onNext, enabled = nextEnabled) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = stringResource(R.string.viewer_chapter_next),
                    tint = if (nextEnabled) ReaderOnScrim else ReaderOnScrimMuted.copy(alpha = 0.4f)
                )
            }
        }
    }
}

// ── Reader settings sheet ────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ViewerSettingsSheet(
    state: ViewerUiState,
    onDismiss: () -> Unit,
    onModeChange: (ViewerMode) -> Unit,
    onReadingDirectionChange: (ReadingDirection) -> Unit,
    onTwoPageModeChange: (Boolean) -> Unit,
    onFirstPageSingleChange: (Boolean) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = WingsShapeTokens.Sheet,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
            Column(
                modifier = Modifier
                    .widthIn(max = Dimens.MaxContentWidth)
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.xl)
                    .padding(bottom = Spacing.xxl)
            ) {
                Text(
                    text = stringResource(R.string.viewer_settings),
                    style = WingsType.emptyStateTitle,
                    color = MaterialTheme.colorScheme.onSurface
                )

                ReaderSettingLabel(stringResource(R.string.settings_reader_mode))
                WingsSegmentedRow(
                    options = listOf(ViewerMode.WEBTOON, ViewerMode.MANGA),
                    selected = state.mode,
                    label = {
                        stringResource(
                            if (it == ViewerMode.WEBTOON) R.string.reader_mode_scroll
                            else R.string.reader_mode_swipe
                        )
                    },
                    onSelect = onModeChange
                )

                if (state.mode == ViewerMode.MANGA) {
                    ReaderSettingLabel(stringResource(R.string.settings_reading_direction))
                    WingsSegmentedRow(
                        options = listOf(ReadingDirection.LTR, ReadingDirection.RTL),
                        selected = state.readingDirection,
                        label = {
                            stringResource(
                                if (it == ReadingDirection.LTR) R.string.direction_ltr
                                else R.string.direction_rtl
                            )
                        },
                        onSelect = onReadingDirectionChange
                    )

                    WingsSwitchRow(
                        title = stringResource(R.string.settings_two_page),
                        supporting = stringResource(R.string.settings_two_page_support),
                        checked = state.twoPageMode,
                        onCheckedChange = onTwoPageModeChange
                    )

                    if (state.twoPageMode) {
                        ReaderSettingLabel(stringResource(R.string.settings_first_page))
                        WingsSegmentedRow(
                            options = listOf(true, false),
                            selected = state.firstPageSingle,
                            label = {
                                stringResource(
                                    if (it) R.string.settings_first_page_single
                                    else R.string.settings_first_page_double
                                )
                            },
                            onSelect = onFirstPageSingleChange
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReaderSettingLabel(text: String) {
    Text(
        text = text,
        style = WingsType.label,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = Spacing.xl, bottom = Spacing.sm)
    )
}

