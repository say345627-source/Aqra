package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.QuranUiState
import com.example.ui.QuranViewModel
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SurahReaderScreen
import com.example.ui.screens.SurahsScreen
import com.example.ui.theme.QuranAppTheme

private object Routes {
    const val HOME = "home"
    const val SURAHS = "surahs"
    const val FAVORITES = "favorites"
    const val SEARCH = "search"
    const val SETTINGS = "settings"
    const val READER = "reader/{surahNumber}/{verseNumber}"

    fun readerRoute(surahNumber: Int, verseNumber: Int = 1): String =
        "reader/$surahNumber/$verseNumber"
}

private data class NavTabItem(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
    val testTag: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: QuranViewModel = viewModel(
                factory = QuranViewModel.Factory(application)
            )
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                QuranAppTheme(darkTheme = isDarkMode) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        QuranRootApp(viewModel = viewModel, isDarkMode = isDarkMode)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuranRootApp(
    viewModel: QuranViewModel,
    isDarkMode: Boolean
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is QuranUiState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = stringResource(R.string.loading_quran),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }

        is QuranUiState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadQuranData() }) {
                        Text(stringResource(R.string.retry))
                    }
                }
            }
        }

        is QuranUiState.Ready -> {
            QuranMainNavigation(
                viewModel = viewModel,
                readyState = state,
                isDarkMode = isDarkMode
            )
        }
    }
}

@Composable
private fun QuranMainNavigation(
    viewModel: QuranViewModel,
    readyState: QuranUiState.Ready,
    isDarkMode: Boolean
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val lastPosition by viewModel.lastReadingPosition.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val fontSizeSp by viewModel.fontSizeSp.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()

    val favoriteKeys = remember(favorites) { favorites.map { it.key }.toSet() }

    val tabs = remember {
        listOf(
            NavTabItem(
                route = Routes.HOME,
                labelRes = R.string.nav_home,
                icon = Icons.Outlined.Home,
                selectedIcon = Icons.Rounded.Home,
                testTag = "nav_tab_home"
            ),
            NavTabItem(
                route = Routes.SURAHS,
                labelRes = R.string.nav_surahs,
                icon = Icons.AutoMirrored.Outlined.MenuBook,
                selectedIcon = Icons.AutoMirrored.Rounded.MenuBook,
                testTag = "nav_tab_surahs"
            ),
            NavTabItem(
                route = Routes.FAVORITES,
                labelRes = R.string.nav_favorites,
                icon = Icons.Outlined.BookmarkBorder,
                selectedIcon = Icons.Rounded.Bookmark,
                testTag = "nav_tab_favorites"
            ),
            NavTabItem(
                route = Routes.SEARCH,
                labelRes = R.string.nav_search,
                icon = Icons.Rounded.Search,
                selectedIcon = Icons.Rounded.Search,
                testTag = "nav_tab_search"
            ),
            NavTabItem(
                route = Routes.SETTINGS,
                labelRes = R.string.nav_settings,
                icon = Icons.Outlined.Settings,
                selectedIcon = Icons.Rounded.Settings,
                testTag = "nav_tab_settings"
            )
        )
    }

    val isReaderScreen = currentRoute?.startsWith("reader/") == true

    fun navigateToTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 600.dp && !isReaderScreen

        Row(modifier = Modifier.fillMaxSize()) {
            if (useNavigationRail) {
                NavigationRail(
                    modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)
                ) {
                    Spacer(modifier = Modifier.height(12.dp))
                    tabs.forEach { tab ->
                        val selected = currentRoute == tab.route
                        NavigationRailItem(
                            selected = selected,
                            onClick = { navigateToTab(tab.route) },
                            icon = {
                                Icon(
                                    imageVector = if (selected) tab.selectedIcon else tab.icon,
                                    contentDescription = stringResource(tab.labelRes)
                                )
                            },
                            label = { Text(stringResource(tab.labelRes)) },
                            modifier = Modifier.testTag(tab.testTag)
                        )
                    }
                }
            }

            Scaffold(
                modifier = Modifier.weight(1f),
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                bottomBar = {
                    if (!isReaderScreen && !useNavigationRail) {
                        NavigationBar(
                            windowInsets = WindowInsets.navigationBars
                        ) {
                            tabs.forEach { tab ->
                                val selected = currentRoute == tab.route
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navigateToTab(tab.route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) tab.selectedIcon else tab.icon,
                                            contentDescription = stringResource(tab.labelRes)
                                        )
                                    },
                                    label = { Text(stringResource(tab.labelRes)) },
                                    modifier = Modifier.testTag(tab.testTag)
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = Routes.HOME,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = innerPadding.calculateBottomPadding())
                ) {
                    composable(Routes.HOME) {
                        HomeScreen(
                            lastPosition = lastPosition,
                            favoritesCount = favorites.size,
                            onContinueReading = {
                                val pos = lastPosition
                                navController.navigate(
                                    Routes.readerRoute(
                                        surahNumber = pos?.surahNumber ?: 1,
                                        verseNumber = pos?.verseNumber ?: 1
                                    )
                                )
                            },
                            onBrowseSurahs = { navigateToTab(Routes.SURAHS) },
                            onOpenFavorites = { navigateToTab(Routes.FAVORITES) },
                            onOpenSearch = { navigateToTab(Routes.SEARCH) },
                            onOpenSettings = { navigateToTab(Routes.SETTINGS) }
                        )
                    }

                    composable(Routes.SURAHS) {
                        SurahsScreen(
                            surahs = readyState.summaries,
                            onSelectSurah = { surahNumber ->
                                navController.navigate(Routes.readerRoute(surahNumber, 1))
                            }
                        )
                    }

                    composable(Routes.FAVORITES) {
                        FavoritesScreen(
                            favorites = favorites,
                            onOpenFavorite = { surahNumber, verseNumber ->
                                navController.navigate(Routes.readerRoute(surahNumber, verseNumber))
                            },
                            onRemoveFavorite = { surahNumber, verseNumber ->
                                viewModel.removeFavorite(surahNumber, verseNumber)
                            }
                        )
                    }

                    composable(Routes.SEARCH) {
                        SearchScreen(
                            query = searchQuery,
                            results = searchResults,
                            isSearching = isSearching,
                            onQueryChange = viewModel::updateSearchQuery,
                            onSelectResult = { surahNumber, verseNumber ->
                                navController.navigate(Routes.readerRoute(surahNumber, verseNumber))
                            }
                        )
                    }

                    composable(Routes.SETTINGS) {
                        SettingsScreen(
                            isDarkMode = isDarkMode,
                            fontSizeSp = fontSizeSp,
                            integrityInfo = readyState.integrity,
                            onDarkModeChange = viewModel::setDarkMode,
                            onFontSizeChange = viewModel::setFontSize
                        )
                    }

                    composable(
                        route = Routes.READER,
                        arguments = listOf(
                            navArgument("surahNumber") { type = NavType.IntType },
                            navArgument("verseNumber") { type = NavType.IntType }
                        )
                    ) { entry ->
                        val surahNumber = entry.arguments?.getInt("surahNumber") ?: 1
                        val verseNumber = entry.arguments?.getInt("verseNumber") ?: 1
                        SurahReaderScreen(
                            surahs = readyState.surahs,
                            initialSurahNumber = surahNumber,
                            initialVerseNumber = verseNumber,
                            favoriteKeys = favoriteKeys,
                            fontSizeSp = fontSizeSp,
                            onBack = { navController.popBackStack() },
                            onPositionChanged = viewModel::saveReadingPosition,
                            onToggleFavorite = viewModel::toggleFavorite
                        )
                    }
                }
            }
        }
    }
}
