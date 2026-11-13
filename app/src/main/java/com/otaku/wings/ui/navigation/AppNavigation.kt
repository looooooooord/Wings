package com.otaku.wings.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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

private const val TRANSITION_DURATION = 420

private data class TabItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private val tabItems = listOf(
    TabItem("home", "الأحدث", Icons.Default.NewReleases),
    TabItem("search", "استكشف", Icons.Default.Explore),
    TabItem("my", "مكتبتي", Icons.Default.LocalLibrary),
    TabItem("settings", "الإعدادات", Icons.Default.Settings)
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp,
                        shadowElevation = 14.dp
                    ) {
                        NavigationBar(
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = Color.Transparent,
                            tonalElevation = 0.dp
                        ) {
                            tabItems.forEach { tab ->
                                val selected = currentRoute == tab.route
                                val iconScale by animateFloatAsState(
                                    targetValue = if (selected) 1.14f else 1f,
                                    animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing),
                                    label = "nav-icon-scale-${tab.route}"
                                )
                                val iconOffset by animateDpAsState(
                                    targetValue = if (selected) (-3).dp else 0.dp,
                                    animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing),
                                    label = "nav-icon-offset-${tab.route}"
                                )
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = {
                                        nav.navigate(tab.route) {
                                            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = tab.icon,
                                            contentDescription = tab.label,
                                            modifier = Modifier.graphicsLayer {
                                                scaleX = iconScale
                                                scaleY = iconScale
                                                translationY = iconOffset.toPx()
                                            }
                                        )
                                    },
                                    label = { Text(tab.label) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { scaffoldPadding ->
        NavHost(
            navController = nav,
            startDestination = "home",
            modifier = Modifier.padding(bottom = scaffoldPadding.calculateBottomPadding()),
            enterTransition = {
                fadeIn(tween(TRANSITION_DURATION, easing = FastOutSlowInEasing)) + slideInHorizontally(
                    initialOffsetX = { it / 6 },
                    animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing)
                )
            },
            exitTransition = {
                fadeOut(tween(TRANSITION_DURATION, easing = FastOutSlowInEasing)) + slideOutHorizontally(
                    targetOffsetX = { -it / 6 },
                    animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing)
                )
            },
            popEnterTransition = {
                fadeIn(tween(TRANSITION_DURATION, easing = FastOutSlowInEasing)) + slideInHorizontally(
                    initialOffsetX = { -it / 6 },
                    animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing)
                )
            },
            popExitTransition = {
                fadeOut(tween(TRANSITION_DURATION, easing = FastOutSlowInEasing)) + slideOutHorizontally(
                    targetOffsetX = { it / 6 },
                    animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing)
                )
            }
        ) {
            composable("home") {
                HomeScreen(
                    onSeriesClick = { series ->
                        val encoded = URLEncoder.encode(series.sourceUrl, "UTF-8")
                        nav.navigate("detail/$encoded")
                    },
                    onMoreClick = { category -> nav.navigate("list/$category") }
                )
            }
            composable("search") {
                SearchScreen(
                    onSeriesClick = { series ->
                        val encoded = URLEncoder.encode(series.sourceUrl, "UTF-8")
                        nav.navigate("detail/$encoded")
                    }
                )
            }
            composable("my") {
                MyScreen(
                    onSeriesClick = { series ->
                        val encoded = URLEncoder.encode(series.sourceUrl, "UTF-8")
                        nav.navigate("detail/$encoded")
                    }
                )
            }
            composable("settings") { SettingsScreen() }
            composable(
                "list/{category}",
                arguments = listOf(navArgument("category") { type = NavType.StringType })
            ) { back ->
                val category = back.arguments?.getString("category") ?: return@composable
                ListScreen(
                    category = category,
                    onBack = { nav.popBackStack() },
                    onSeriesClick = { series ->
                        val encoded = URLEncoder.encode(series.sourceUrl, "UTF-8")
                        nav.navigate("detail/$encoded")
                    }
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
                        val encodedChapterUrl = URLEncoder.encode(chapter.url, "UTF-8")
                        val titleEncoded = URLEncoder.encode(chapter.displayTitle(series.title), "UTF-8")
                        val seriesIdEncoded = URLEncoder.encode(series.id, "UTF-8")
                        val seriesUrlEncoded = URLEncoder.encode(series.sourceUrl, "UTF-8")
                        val seriesTitleEncoded = URLEncoder.encode(series.title, "UTF-8")
                        val seriesAuthorEncoded = URLEncoder.encode(series.author, "UTF-8")
                        val coverUrlEncoded = URLEncoder.encode(series.coverUrl, "UTF-8")
                        nav.navigate("viewer/$encodedChapterUrl/$titleEncoded/$seriesIdEncoded/$seriesUrlEncoded/$seriesTitleEncoded/$seriesAuthorEncoded/$coverUrlEncoded")
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
                        val encodedChapterUrl = URLEncoder.encode(newChapterUrl, "UTF-8")
                        val titleEncoded = URLEncoder.encode(newTitle, "UTF-8")
                        val seriesIdEncoded = URLEncoder.encode(seriesId, "UTF-8")
                        val seriesUrlEncoded = URLEncoder.encode(seriesUrl, "UTF-8")
                        val seriesTitleEncoded = URLEncoder.encode(seriesTitle, "UTF-8")
                        val seriesAuthorEncoded = URLEncoder.encode(seriesAuthor, "UTF-8")
                        val coverUrlEncoded = URLEncoder.encode(coverUrl, "UTF-8")
                        nav.popBackStack()
                        nav.navigate("viewer/$encodedChapterUrl/$titleEncoded/$seriesIdEncoded/$seriesUrlEncoded/$seriesTitleEncoded/$seriesAuthorEncoded/$coverUrlEncoded")
                    }
                )
            }
        }
    }
}
