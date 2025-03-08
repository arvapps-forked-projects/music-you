package it.vfsfitvnm.vimusic.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.Lifecycle
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import it.vfsfitvnm.vimusic.enums.BuiltInPlaylist
import it.vfsfitvnm.vimusic.enums.SettingsSection
import it.vfsfitvnm.vimusic.models.Screen
import it.vfsfitvnm.vimusic.ui.screens.album.AlbumScreen
import it.vfsfitvnm.vimusic.ui.screens.artist.ArtistScreen
import it.vfsfitvnm.vimusic.ui.screens.builtinplaylist.BuiltInPlaylistScreen
import it.vfsfitvnm.vimusic.ui.screens.home.HomeScreen
import it.vfsfitvnm.vimusic.ui.screens.localplaylist.LocalPlaylistScreen
import it.vfsfitvnm.vimusic.ui.screens.playlist.PlaylistScreen
import it.vfsfitvnm.vimusic.ui.screens.search.SearchScreen
import it.vfsfitvnm.vimusic.ui.screens.settings.SettingsPage
import it.vfsfitvnm.vimusic.ui.screens.settings.SettingsScreen
import it.vfsfitvnm.vimusic.utils.homeScreenTabIndexKey
import it.vfsfitvnm.vimusic.utils.rememberPreference
import kotlinx.coroutines.launch
import soup.compose.material.motion.animation.materialFadeThroughIn
import soup.compose.material.motion.animation.materialFadeThroughOut
import soup.compose.material.motion.animation.materialSharedAxisXIn
import soup.compose.material.motion.animation.materialSharedAxisXOut
import soup.compose.material.motion.animation.rememberSlideDistance

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalAnimationApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun Navigation(
    navController: NavHostController,
    sheetState: SheetState
) {
    val scope = rememberCoroutineScope()
    val slideDistance = rememberSlideDistance()
    val (screenIndex, _) = rememberPreference(homeScreenTabIndexKey, defaultValue = 0)
    val homeRoutes = listOf(
        Screen.Home,
        Screen.Songs,
        Screen.Artists,
        Screen.Albums,
        Screen.Playlists
    ).map { it.route }

    NavHost(
        navController = navController,
        startDestination = homeRoutes.getOrElse(screenIndex) { Screen.Home.route },
        enterTransition = {
            if (homeRoutes.contains(targetState.destination.route)) materialFadeThroughIn()
            else materialSharedAxisXIn(
                forward = true,
                slideDistance = slideDistance
            )
        },
        exitTransition = {
            if (homeRoutes.contains(targetState.destination.route)) materialFadeThroughOut()
            else materialSharedAxisXOut(
                forward = true,
                slideDistance = slideDistance
            )
        },
        popEnterTransition = {
            if (
                homeRoutes.contains(initialState.destination.route) &&
                homeRoutes.contains(targetState.destination.route)
            ) materialFadeThroughIn()
            else materialSharedAxisXIn(
                forward = false,
                slideDistance = slideDistance
            )
        },
        popExitTransition = {
            if (
                homeRoutes.contains(initialState.destination.route) &&
                homeRoutes.contains(targetState.destination.route)
            ) materialFadeThroughOut()
            else materialSharedAxisXOut(
                forward = false,
                slideDistance = slideDistance
            )
        }
    ) {
        val navigateToAlbum =
            { browseId: String -> navController.navigate(route = "album/$browseId") }
        val navigateToArtist =
            { browseId: String -> navController.navigate("artist/$browseId") }
        val popDestination = {
            if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED)
                navController.popBackStack()
        }

        fun NavGraphBuilder.playerComposable(
            route: String,
            arguments: List<NamedNavArgument> = emptyList(),
            content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
        ) {
            composable(
                route = route,
                arguments = arguments
            ) { navBackStackEntry ->
                content(navBackStackEntry)

                BackHandler(enabled = sheetState.currentValue == SheetValue.Expanded) {
                    scope.launch { sheetState.partialExpand() }
                }
            }
        }

        playerComposable(route = "home") {
            HomeScreen(
                navController = navController,
                screenIndex = 0
            )
        }

        playerComposable(route = "songs") {
            HomeScreen(
                navController = navController,
                screenIndex = 1
            )
        }

        playerComposable(route = "artists") {
            HomeScreen(
                navController = navController,
                screenIndex = 2
            )
        }

        playerComposable(route = "albums") {
            HomeScreen(
                navController = navController,
                screenIndex = 3
            )
        }

        playerComposable(route = "playlists") {
            HomeScreen(
                navController = navController,
                screenIndex = 4
            )
        }

        playerComposable(
            route = "artist/{id}",
            arguments = listOf(
                navArgument(
                    name = "id",
                    builder = { type = NavType.StringType }
                )
            )
        ) { navBackStackEntry ->
            val id = navBackStackEntry.arguments?.getString("id") ?: ""

            ArtistScreen(
                browseId = id,
                pop = popDestination,
                onAlbumClick = navigateToAlbum
            )
        }

        playerComposable(
            route = "album/{id}",
            arguments = listOf(
                navArgument(
                    name = "id",
                    builder = { type = NavType.StringType }
                )
            )
        ) { navBackStackEntry ->
            val id = navBackStackEntry.arguments?.getString("id") ?: ""

            AlbumScreen(
                browseId = id,
                pop = popDestination,
                onAlbumClick = navigateToAlbum,
                onGoToArtist = navigateToArtist
            )
        }

        playerComposable(
            route = "playlist/{id}",
            arguments = listOf(
                navArgument(
                    name = "id",
                    builder = { type = NavType.StringType }
                )
            )
        ) { navBackStackEntry ->
            val id = navBackStackEntry.arguments?.getString("id") ?: ""

            PlaylistScreen(
                browseId = id,
                pop = popDestination,
                onGoToAlbum = navigateToAlbum,
                onGoToArtist = navigateToArtist
            )
        }

        playerComposable(route = "settings") {
            SettingsScreen(
                pop = popDestination,
                onGoToSettingsPage = { index -> navController.navigate("settingsPage/$index") }
            )
        }

        playerComposable(
            route = "settingsPage/{index}",
            arguments = listOf(
                navArgument(
                    name = "index",
                    builder = { type = NavType.IntType }
                )
            )
        ) { navBackStackEntry ->
            val index = navBackStackEntry.arguments?.getInt("index") ?: 0

            SettingsPage(
                section = SettingsSection.entries[index],
                pop = popDestination
            )
        }

        playerComposable(route = "search") {
            SearchScreen(
                pop = popDestination,
                onAlbumClick = navigateToAlbum,
                onArtistClick = navigateToArtist,
                onPlaylistClick = { browseId -> navController.navigate("playlist/$browseId") }
            )
        }

        playerComposable(
            route = "builtInPlaylist/{index}",
            arguments = listOf(
                navArgument(
                    name = "index",
                    builder = { type = NavType.IntType }
                )
            )
        ) { navBackStackEntry ->
            val index = navBackStackEntry.arguments?.getInt("index") ?: 0

            BuiltInPlaylistScreen(
                builtInPlaylist = BuiltInPlaylist.entries[index],
                pop = popDestination,
                onGoToAlbum = navigateToAlbum,
                onGoToArtist = navigateToArtist
            )
        }

        playerComposable(
            route = "localPlaylist/{id}",
            arguments = listOf(
                navArgument(
                    name = "id",
                    builder = { type = NavType.LongType }
                )
            )
        ) { navBackStackEntry ->
            val id = navBackStackEntry.arguments?.getLong("id") ?: 0L

            LocalPlaylistScreen(
                playlistId = id,
                pop = popDestination,
                onGoToAlbum = navigateToAlbum,
                onGoToArtist = navigateToArtist
            )
        }
    }
}