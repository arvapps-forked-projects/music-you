package it.vfsfitvnm.vimusic.ui.screens.home

import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.PlaylistPlay
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerPadding
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.SongSortBy
import it.vfsfitvnm.vimusic.enums.SortOrder
import it.vfsfitvnm.vimusic.models.ActionInfo
import it.vfsfitvnm.vimusic.models.LocalMenuState
import it.vfsfitvnm.vimusic.models.Song
import it.vfsfitvnm.vimusic.query
import it.vfsfitvnm.vimusic.ui.components.SortingHeader
import it.vfsfitvnm.vimusic.ui.components.SwipeToActionBox
import it.vfsfitvnm.vimusic.ui.components.themed.InHistoryMediaItemMenu
import it.vfsfitvnm.vimusic.ui.items.LocalSongItem
import it.vfsfitvnm.vimusic.ui.styling.onOverlay
import it.vfsfitvnm.vimusic.ui.styling.overlay
import it.vfsfitvnm.vimusic.utils.asMediaItem
import it.vfsfitvnm.vimusic.utils.enqueue
import it.vfsfitvnm.vimusic.utils.forcePlayAtIndex
import it.vfsfitvnm.vimusic.utils.forcePlayFromBeginning
import it.vfsfitvnm.vimusic.utils.rememberPreference
import it.vfsfitvnm.vimusic.utils.songSortByKey
import it.vfsfitvnm.vimusic.utils.songSortOrderKey
import it.vfsfitvnm.vimusic.viewmodels.HomeSongsViewModel
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun HomeSongs(
    onGoToAlbum: (String) -> Unit,
    onGoToArtist: (String) -> Unit
) {
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current
    val playerPadding = LocalPlayerPadding.current

    var sortBy by rememberPreference(songSortByKey, SongSortBy.Title)
    var sortOrder by rememberPreference(songSortOrderKey, SortOrder.Ascending)

    val viewModel: HomeSongsViewModel = viewModel()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarMessage = stringResource(id = R.string.song_deleted_library)
    val snackBarActionLabel = stringResource(id = R.string.undo)

    LaunchedEffect(sortBy, sortOrder) {
        viewModel.loadSongs(
            sortBy = sortBy,
            sortOrder = sortOrder
        )
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = viewModel.items.isNotEmpty(),
                enter = fadeIn() + scaleIn(),
                exit = scaleOut() + fadeOut()
            ) {
                FloatingActionButton(
                    onClick = {
                        binder?.stopRadio()
                        binder?.player?.forcePlayFromBeginning(
                            viewModel.items.shuffled().map(Song::asMediaItem)
                        )
                    },
                    modifier = Modifier.padding(bottom = playerPadding)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Shuffle,
                        contentDescription = stringResource(id = R.string.shuffle)
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets(bottom = 0)
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 400.dp),
            contentPadding = PaddingValues(bottom = if (viewModel.items.isNotEmpty()) 16.dp + 72.dp + playerPadding else 16.dp + playerPadding),
            modifier = Modifier
                .fillMaxSize()
                .consumeWindowInsets(paddingValues)
        ) {
            item(
                key = "header",
                span = { GridItemSpan(maxLineSpan) }
            ) {
                SortingHeader(
                    sortBy = sortBy,
                    changeSortBy = { sortBy = it },
                    sortByEntries = SongSortBy.entries.toList(),
                    sortOrder = sortOrder,
                    toggleSortOrder = { sortOrder = !sortOrder },
                    size = viewModel.items.size,
                    itemCountText = R.plurals.number_of_songs
                )
            }

            itemsIndexed(
                items = viewModel.items,
                key = { _, song -> song.id }
            ) { index, song ->
                SwipeToActionBox(
                    modifier = Modifier.animateItem(),
                    primaryAction = ActionInfo(
                        onClick = { binder?.player?.enqueue(song.asMediaItem) },
                        icon = Icons.AutoMirrored.Outlined.PlaylistPlay,
                        description = R.string.enqueue
                    ),
                    destructiveAction = ActionInfo(
                        onClick = {
                            query {
                                binder?.cache?.removeResource(song.id)
                                Database.incrementTotalPlayTimeMs(
                                    id = song.id,
                                    addition = -song.totalPlayTimeMs
                                )
                            }

                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = snackbarMessage,
                                    actionLabel = snackBarActionLabel,
                                    withDismissAction = true,
                                    duration = SnackbarDuration.Short
                                )

                                if (result == SnackbarResult.ActionPerformed) {
                                    query {
                                        Database.insert(song)

                                        Database.incrementTotalPlayTimeMs(
                                            id = song.id,
                                            addition = song.totalPlayTimeMs
                                        )
                                    }
                                }
                            }
                        },
                        icon = Icons.Outlined.Delete,
                        description = R.string.hide
                    )
                ) {
                    LocalSongItem(
                        song = song,
                        onClick = {
                            binder?.stopRadio()
                            binder?.player?.forcePlayAtIndex(
                                viewModel.items.map(Song::asMediaItem),
                                index
                            )
                        },
                        onLongClick = {
                            menuState.display {
                                InHistoryMediaItemMenu(
                                    song = song,
                                    onDismiss = menuState::hide,
                                    onGoToAlbum = onGoToAlbum,
                                    onGoToArtist = onGoToArtist
                                )
                            }
                        },
                        onThumbnailContent = if (sortBy == SongSortBy.PlayTime) ({
                            Text(
                                text = song.formattedTotalPlayTime,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onOverlay,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                MaterialTheme.colorScheme.overlay
                                            )
                                        ),
                                        shape = MaterialTheme.shapes.medium
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .align(Alignment.BottomCenter)
                            )
                        }) else null
                    )
                }
            }
        }
    }
}