package it.vfsfitvnm.vimusic.ui.screens.localplaylist

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import it.vfsfitvnm.innertube.Innertube
import it.vfsfitvnm.innertube.requests.playlistPage
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.models.Playlist
import it.vfsfitvnm.vimusic.models.SongPlaylistMap
import it.vfsfitvnm.vimusic.query
import it.vfsfitvnm.vimusic.transaction
import it.vfsfitvnm.vimusic.ui.components.TooltipIconButton
import it.vfsfitvnm.vimusic.ui.components.themed.ConfirmationDialog
import it.vfsfitvnm.vimusic.ui.components.themed.TextFieldDialog
import it.vfsfitvnm.vimusic.utils.asMediaItem
import it.vfsfitvnm.vimusic.utils.completed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun LocalPlaylistScreen(
    playlistId: Long,
    pop: () -> Unit,
    onGoToAlbum: (String) -> Unit,
    onGoToArtist: (String) -> Unit
) {
    var playlist: Playlist? by remember { mutableStateOf(null) }

    var isRenaming by rememberSaveable { mutableStateOf(false) }
    var isDeleting by rememberSaveable { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    LaunchedEffect(Unit) {
        Database.playlist(playlistId).filterNotNull().collect { playlist = it }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                title = {
                    Text(
                        text = playlist?.name ?: "",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = pop) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    if (!playlist?.browseId.isNullOrEmpty()) {
                        TooltipIconButton(
                            description = R.string.sync_playlist,
                            onClick = {
                                playlist?.browseId?.let { browseId ->
                                    transaction {
                                        runBlocking(Dispatchers.IO) {
                                            withContext(Dispatchers.IO) {
                                                Innertube.playlistPage(browseId = browseId)
                                                    ?.completed()
                                            }
                                        }?.getOrNull()?.let { remotePlaylist ->
                                            Database.clearPlaylist(playlistId)

                                            remotePlaylist.songsPage
                                                ?.items
                                                ?.map(Innertube.SongItem::asMediaItem)
                                                ?.onEach(Database::insert)
                                                ?.mapIndexed { position, mediaItem ->
                                                    SongPlaylistMap(
                                                        songId = mediaItem.mediaId,
                                                        playlistId = playlistId,
                                                        position = position
                                                    )
                                                }?.let(Database::insertSongPlaylistMaps)
                                        }
                                    }
                                }
                            },
                            icon = Icons.Outlined.Sync,
                            inTopBar = true
                        )
                    }

                    TooltipIconButton(
                        description = R.string.rename_playlist,
                        onClick = { isRenaming = true },
                        icon = Icons.Outlined.Edit,
                        inTopBar = true
                    )

                    TooltipIconButton(
                        description = R.string.delete_playlist,
                        onClick = { isDeleting = true },
                        icon = Icons.Outlined.Delete,
                        inTopBar = true
                    )
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            LocalPlaylistSongs(
                playlistId = playlistId,
                onGoToAlbum = onGoToAlbum,
                onGoToArtist = onGoToArtist
            )

            if (isRenaming) {
                TextFieldDialog(
                    title = stringResource(id = R.string.rename_playlist),
                    hintText = stringResource(id = R.string.playlist_name_hint),
                    initialTextInput = playlist?.name ?: "",
                    onDismiss = { isRenaming = false },
                    onDone = { text ->
                        query {
                            playlist?.copy(name = text)
                                ?.let(Database::update)
                        }
                    }
                )
            }

            if (isDeleting) {
                ConfirmationDialog(
                    title = stringResource(id = R.string.delete_playlist_dialog),
                    onDismiss = { isDeleting = false },
                    onConfirm = {
                        query {
                            playlist?.let(Database::delete)
                        }
                        pop()
                    }
                )
            }
        }
    }
}