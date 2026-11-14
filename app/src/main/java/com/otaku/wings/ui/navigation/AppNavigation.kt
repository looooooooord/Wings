package com.otaku.wings.ui.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.otaku.wings.R
import com.otaku.wings.data.model.displayTitle
import com.otaku.wings.ui.detail.DetailScreen
import com.otaku.wings.ui.home.HomeScreen
import com.otaku.wings.ui.list.ListScreen
import com.otaku.wings.ui.my.MyScreen
import com.otaku.wings.ui.search.SearchScreen
import com.otaku.wings.ui.settings.SettingsScreen
import com.otaku.wings.ui.viewer.ViewerScreen
import java.net.URLDecoder
import java.net.URLEncoder

private const val NAV_ANIMATION_DURATION = 220
private const val TAB_ANIMATION_DURATION = 180

private data class TabItem(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector
)

private val tabItems = listOf(
    TabItem("home", R.string.tab_latest, Icons.Default.NewReleases),
    TabItem("search", R.string.tab_explore, Icons.Default.Explore),
    TabItem("my", R.string.tab_library, Icons.Default.LocalLibrary),
    TabItem("settings", R.string.tab_settings, Icons.Default.Settings)
)

@Composable
fun AppNavigation() {
    val nav = rememberNavController()
    val backStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = tabItems.any { it.route == currentRoute }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                TabBar(
                    currentRoute = currentRoute,
                    onTabClick = { route ->
                        nav.navigate(route) {
                            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { scaffoldPadding ->
        NavHost(
            navController = nav,
            startDestination = "home",
            // The outer Scaffold owns the bottom inset; screens consume it exactly once here.
            modifier = Modifier.padding(bottom = scaffoldPadding.calculateBottomPadding()),
            enterTransition = {
                fadeIn(tween(NAV_ANIMATION_DURATION, easing = FastOutSlowInEasing)) +
                    slideInHorizontally(
                        initialOffsetX = { it / 10 },
                        animationSpec = tween(NAV_ANIMATION_DURATION, easing = FastOutSlowInEasing)
                    )
            },
            exitTransition = {
                fadeOut(tween(TAB_ANIMATION_DURATION, easing = FastOutSlowInEasing))
            },
            popEnterTransition = {
                fadeIn(tween(NAV_ANIMATION_DURATION, easing = FastOutSlowInEasing)) +
                    slideInHorizontally(
                        initialOffsetX = { -it / 10 },
                        animationSpec = tween(NAV_ANIMATION_DURATION, easing = FastOutSlowInEasing)
                    )
            },
            popExitTransition = {
                fadeOut(tween(TAB_ANIMATION_DURATION, easing = FastOutSlowInEasing)) +
                    slideOutHorizontally(
                        targetOffsetX = { it / 10 },
                        animationSpec = tween(NAV_ANIMATION_DURATION, easing = FastOutSlowInEasing)
                    )
            }
        ) {
            // Top-level tabs cross-fade instead of sliding; pushes keep the slide.
            composable(
                "home",
                enterTransition = { fadeIn(tween(TAB_ANIMATION_DURATION)) },
                exitTransition = { fadeOut(tween(TAB_ANIMATION_DURATION)) }
            ) {
                HomeScreen(
                    onSeriesClick = { series -> nav.navigate("detail/${encode(series.sourceUrl)}") },
                    onMoreClick = { category -> nav.navigate("list/$category") }
                )
            }
            composable(
                "search",
                enterTransition = { fadeIn(tween(TAB_ANIMATION_DURATION)) },
                exitTransition = { fadeOut(tween(TAB_ANIMATION_DURATION)) }
            ) {
                SearchScreen(
                    onSeriesClick = { series -> nav.navigate("detail/${encode(series.sourceUrl)}") }
                )
            }
            composable(
                "my",
                enterTransition = { fadeIn(tween(TAB_ANIMATION_DURATION)) },
                exitTransition = { fadeOut(tween(TAB_ANIMATION_DURATION)) }
            ) {
                MyScreen(
                    onSeriesClick = { series -> nav.navigate("detail/${encode(series.sourceUrl)}") },
                    onExploreClick = {
                        nav.navigate("search") {
                            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(
                "settings",
                enterTransition = { fadeIn(tween(TAB_ANIMATION_DURATION)) },
                exitTransition = { fadeOut(tween(TAB_ANIMATION_DURATION)) }
            ) { SettingsScreen() }
            composable(
                "list/{category}",
                arguments = listOf(navArgument("category") { type = NavType.StringType })
            ) { back ->
                val category = back.arguments?.getString("category") ?: return@composable
                ListScreen(
                    category = category,
                    onBack = { nav.popBackStack() },
                    onSeriesClick = { series -> nav.navigate("detail/${encode(series.sourceUrl)}") }
                )
            }
            composable(
                "detail/{url}",
                arguments = listOf(navArgument("url") { type = NavType.StringType })
            ) { back ->
                val url = URLDecoder.decode(back.arguments?.getString("url") ?: "", "UTF-8")
                DetailScreen(
                    seriesUrl = url,
                    onBack = { nav.popBackStack() },
                    onChapterClick = { chapter, series ->
                        nav.navigate(
                            viewerRoute(
                                chapterUrl = chapter.url,
                                title = chapter.displayTitle(series.title),
                                series = series
                            )
                        )
                    }
                )
            }
            composable(
                "viewer/{chapterUrl}/{title}/{seriesId}/{seriesUrl}/{seriesTitle}/{seriesAuthor}/{coverUrl}",
                arguments = listOf(
                    navArgument("chapterUrl") { type = NavType.StringType },
                    navArgument("title") { type = NavType.StringType },
                    navArgument("seriesId") { type = NavType.StringType },
                    navArgument("seriesUrl") { type = NavType.StringType },
                    navArgument("seriesTitle") { type = NavType.StringType },
                    navArgument("seriesAuthor") { type = NavType.StringType },
                    navArgument("coverUrl") { type = NavType.StringType }
                )
            ) { back ->
                // The reader fades: a slide would compete with page transitions.
                val chapterUrl = URLDecoder.decode(back.arguments?.getString("chapterUrl") ?: "", "UTF-8")
                val title = URLDecoder.decode(back.arguments?.getString("title") ?: "", "UTF-8")
                val seriesId = URLDecoder.decode(back.arguments?.getString("seriesId") ?: "", "UTF-8")
                val seriesUrl = URLDecoder.decode(back.arguments?.getString("seriesUrl") ?: "", "UTF-8")
                val seriesTitle = URLDecoder.decode(back.arguments?.getString("seriesTitle") ?: "", "UTF-8")
                val seriesAuthor = URLDecoder.decode(back.arguments?.getString("seriesAuthor") ?: "", "UTF-8")
                val coverUrl = URLDecoder.decode(back.arguments?.getString("coverUrl") ?: "", "UTF-8")
                ViewerScreen(
                    chapterUrl = chapterUrl,
                    chapterTitle = title,
                    seriesId = seriesId,
                    seriesUrl = seriesUrl,
                    seriesTitle = seriesTitle,
                    seriesAuthor = seriesAuthor,
                    coverUrl = coverUrl,
                    onBack = { nav.popBackStack() },
                    onNavigateChapter = { newChapterUrl, newTitle ->
                        // Replace the current viewer entry so Back still returns to the detail screen.
                        nav.navigate(
                            viewerRoute(newChapterUrl, newTitle, seriesId, seriesUrl, seriesTitle, seriesAuthor, coverUrl)
                        ) {
                            popUpTo("viewer/{chapterUrl}/{title}/{seriesId}/{seriesUrl}/{seriesTitle}/{seriesAuthor}/{coverUrl}") {
                                inclusive = true
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun TabBar(currentRoute: String?, onTabClick: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        // One surface only: the NavigationBar itself carries the shape and tonal elevation.
        NavigationBar(
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = 2.dp,
            windowInsets = WindowInsets(0, 0, 0, 0)
        ) {
            tabItems.forEach { tab ->
                val selected = currentRoute == tab.route
                val iconScale by animateFloatAsState(
                    targetValue = if (selected) 1.06f else 1f,
                    animationSpec = tween(NAV_ANIMATION_DURATION, easing = FastOutSlowInEasing),
                    label = "nav-icon-scale-${tab.route}"
                )
                val label = stringResource(tab.labelRes)
                NavigationBarItem(
                    selected = selected,
                    onClick = { if (!selected) onTabClick(tab.route) },
                    icon = {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = label,
                            modifier = Modifier.graphicsLayer {
                                scaleX = iconScale
                                scaleY = iconScale
                            }
                        )
                    },
                    label = { Text(label) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                    )
                )
            }
        }
    }
}

private fun encode(value: String): String = URLEncoder.encode(value, "UTF-8")

private fun viewerRoute(
    chapterUrl: String,
    title: String,
    series: com.otaku.wings.data.model.Series
): String = viewerRoute(
    chapterUrl = chapterUrl,
    title = title,
    seriesId = series.id,
    seriesUrl = series.sourceUrl,
    seriesTitle = series.title,
    seriesAuthor = series.author,
    coverUrl = series.coverUrl
)

private fun viewerRoute(
    chapterUrl: String,
    title: String,
    seriesId: String,
    seriesUrl: String,
    seriesTitle: String,
    seriesAuthor: String,
    coverUrl: String
): String = "viewer/${encode(chapterUrl)}/${encode(title)}/${encode(seriesId)}/" +
    "${encode(seriesUrl)}/${encode(seriesTitle)}/${encode(seriesAuthor)}/${encode(coverUrl)}"
