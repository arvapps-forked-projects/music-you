package it.vfsfitvnm.vimusic.ui.screens.player

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import coil3.compose.AsyncImage
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.utils.DisposableListener
import it.vfsfitvnm.vimusic.utils.forceSeekToNext
import it.vfsfitvnm.vimusic.utils.forceSeekToPrevious
import it.vfsfitvnm.vimusic.utils.miniplayerGesturesEnabledKey
import it.vfsfitvnm.vimusic.utils.positionAndDurationState
import it.vfsfitvnm.vimusic.utils.rememberPreference
import it.vfsfitvnm.vimusic.utils.shouldBePlaying
import it.vfsfitvnm.vimusic.utils.thumbnail
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiniPlayer(
    openPlayer: () -> Unit,
    stopPlayer: () -> Unit
) {
    val binder = LocalPlayerServiceBinder.current
    binder?.player ?: return

    var miniplayerGesturesEnabled by rememberPreference(miniplayerGesturesEnabledKey, true)
    var shouldBePlaying by remember { mutableStateOf(binder.player.shouldBePlaying) }

    var nullableMediaItem by remember {
        mutableStateOf(binder.player.currentMediaItem, neverEqualPolicy())
    }

    binder.player.DisposableListener {
        object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                nullableMediaItem = mediaItem
            }

            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                shouldBePlaying = binder.player.shouldBePlaying
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                shouldBePlaying = binder.player.shouldBePlaying
            }
        }
    }

    val mediaItem = nullableMediaItem ?: return
    val positionAndDuration by binder.player.positionAndDurationState()

    val dismissState = rememberSwipeToDismissBoxState()
    val scope = rememberCoroutineScope()

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.Settled) Color.Transparent else MaterialTheme.colorScheme.primaryContainer,
                label = "background"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(76.dp)
                    .background(color)
                    .padding(horizontal = 32.dp),
            ) {
                if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
                    Icon(
                        imageVector = Icons.Outlined.SkipPrevious,
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.CenterStart),
                        tint = if (dismissState.targetValue == SwipeToDismissBoxValue.StartToEnd) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else MaterialTheme.colorScheme.onSurface
                    )
                }

                if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                    Icon(
                        imageVector = Icons.Outlined.SkipNext,
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.CenterEnd),
                        tint = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        gesturesEnabled = miniplayerGesturesEnabled,
        onDismiss = { value ->
            if (value == SwipeToDismissBoxValue.StartToEnd) binder.player.forceSeekToPrevious()
            else if (value == SwipeToDismissBoxValue.EndToStart) binder.player.forceSeekToNext()
            scope.launch { dismissState.reset() }
        }
    ) {
        Column(modifier = Modifier.clickable(onClick = openPlayer)) {
            ListItem(
                headlineContent = {
                    Text(
                        text = mediaItem.mediaMetadata.title?.toString() ?: "",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                supportingContent = {
                    Text(
                        text = mediaItem.mediaMetadata.artist?.toString() ?: "",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingContent = {
                    AsyncImage(
                        model = mediaItem.mediaMetadata.artworkUri.thumbnail(Dimensions.thumbnails.song.px),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.medium)
                            .size(52.dp)
                    )
                },
                trailingContent = {
                    Row {
                        IconButton(
                            onClick = {
                                if (shouldBePlaying) binder.player.pause()
                                else {
                                    if (binder.player.playbackState == Player.STATE_IDLE) {
                                        binder.player.prepare()
                                    } else if (binder.player.playbackState == Player.STATE_ENDED) {
                                        binder.player.seekToDefaultPosition(0)
                                    }
                                    binder.player.play()
                                }
                            }
                        ) {
                            Icon(
                                imageVector =
                                    if (shouldBePlaying) Icons.Outlined.Pause
                                    else if (binder.player.playbackState == Player.STATE_ENDED) Icons.Outlined.Replay
                                    else Icons.Outlined.PlayArrow,
                                contentDescription = null,
                            )
                        }

                        IconButton(
                            onClick = stopPlayer
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Clear,
                                contentDescription = null,
                            )
                        }
                    }
                },
                colors = ListItemDefaults.colors(
                    containerColor = BottomSheetDefaults.ContainerColor
                )
            )

            LinearProgressIndicator(
                progress = { positionAndDuration.first.toFloat() / positionAndDuration.second.absoluteValue },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}