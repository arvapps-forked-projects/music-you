package it.vfsfitvnm.vimusic.ui.screens.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import coil3.compose.AsyncImage
import it.vfsfitvnm.innertube.Innertube
import it.vfsfitvnm.innertube.requests.visitorData
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.service.LoginRequiredException
import it.vfsfitvnm.vimusic.service.PlayableFormatNotFoundException
import it.vfsfitvnm.vimusic.service.UnplayableException
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.utils.DisposableListener
import it.vfsfitvnm.vimusic.utils.currentWindow
import it.vfsfitvnm.vimusic.utils.forceSeekToNext
import it.vfsfitvnm.vimusic.utils.forceSeekToPrevious
import it.vfsfitvnm.vimusic.utils.playerGesturesEnabledKey
import it.vfsfitvnm.vimusic.utils.rememberPreference
import it.vfsfitvnm.vimusic.utils.thumbnail
import kotlinx.coroutines.runBlocking
import me.saket.swipe.SwipeAction
import me.saket.swipe.SwipeableActionsBox
import java.net.UnknownHostException
import java.nio.channels.UnresolvedAddressException

@OptIn(ExperimentalFoundationApi::class)
@ExperimentalAnimationApi
@Composable
fun Thumbnail(
    isShowingLyrics: Boolean,
    onShowLyrics: (Boolean) -> Unit,
    fullScreenLyrics: Boolean,
    toggleFullScreenLyrics: () -> Unit,
    isShowingStatsForNerds: Boolean,
    onShowStatsForNerds: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val binder = LocalPlayerServiceBinder.current
    val player = binder?.player ?: return

    var playerGesturesEnabled by rememberPreference(playerGesturesEnabledKey, true)
    var nullableWindow by remember { mutableStateOf(player.currentWindow) }
    var error by remember { mutableStateOf<PlaybackException?>(player.playerError) }
    var errorCounter by remember(error) { mutableIntStateOf(0) }

    val (thumbnailSizeDp, thumbnailSizePx) = Dimensions.thumbnails.player.song.let {
        it to (it - 64.dp).px
    }

    val retry = {
        when (error?.cause?.cause) {
            is UnresolvedAddressException,
            is UnknownHostException,
            is PlayableFormatNotFoundException,
            is UnplayableException,
            is LoginRequiredException -> player.prepare()

            else -> {
                runBlocking {
                    Innertube.visitorData = Innertube.visitorData().getOrNull()
                }
                player.prepare()
            }
        }
    }

    player.DisposableListener {
        object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                nullableWindow = player.currentWindow
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                error = player.playerError
            }

            @androidx.annotation.OptIn(UnstableApi::class)
            override fun onPlayerError(playbackException: PlaybackException) {
                error = playbackException

                if (errorCounter == 0) {
                    retry()
                    errorCounter += 1
                }
            }
        }
    }

    val window = nullableWindow ?: return

    AnimatedContent(
        targetState = window,
        transitionSpec = {
            val duration = 500
            val slideDirection =
                if (targetState.firstPeriodIndex > initialState.firstPeriodIndex) AnimatedContentTransitionScope.SlideDirection.Left else AnimatedContentTransitionScope.SlideDirection.Right

            ContentTransform(
                targetContentEnter = slideIntoContainer(
                    towards = slideDirection,
                    animationSpec = tween(duration)
                ) + fadeIn(
                    animationSpec = tween(duration)
                ) + scaleIn(
                    initialScale = 0.85f,
                    animationSpec = tween(duration)
                ),
                initialContentExit = slideOutOfContainer(
                    towards = slideDirection,
                    animationSpec = tween(duration)
                ) + fadeOut(
                    animationSpec = tween(duration)
                ) + scaleOut(
                    targetScale = 0.85f,
                    animationSpec = tween(duration)
                ),
                sizeTransform = SizeTransform(clip = false)
            )
        },
        contentAlignment = Alignment.Center,
        label = "thumbnail"
    ) { currentWindow ->
        val thumbnailContent: @Composable BoxScope.() -> Unit = @Composable {
            Box(
                modifier = Modifier.then(
                    if (fullScreenLyrics) Modifier
                        .width(thumbnailSizeDp)
                        .fillMaxHeight() else Modifier
                        .aspectRatio(1f)
                        .size(thumbnailSizeDp)
                )
            ) {
                AsyncImage(
                    model = currentWindow.mediaItem.mediaMetadata.artworkUri.thumbnail(
                        thumbnailSizePx
                    ),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .combinedClickable(
                            onClick = { onShowLyrics(true) },
                            onLongClick = { onShowStatsForNerds(true) }
                        )
                        .fillMaxSize()
                )

                Lyrics(
                    mediaId = currentWindow.mediaItem.mediaId,
                    isDisplayed = isShowingLyrics && error == null,
                    onDismiss = {
                        onShowLyrics(false)
                        if (fullScreenLyrics) toggleFullScreenLyrics()
                    },
                    ensureSongInserted = { Database.insert(currentWindow.mediaItem) },
                    size = thumbnailSizeDp,
                    mediaMetadataProvider = currentWindow.mediaItem::mediaMetadata,
                    durationProvider = player::getDuration,
                    fullScreenLyrics = fullScreenLyrics,
                    toggleFullScreenLyrics = toggleFullScreenLyrics
                )

                if (isShowingStatsForNerds) {
                    StatsForNerds(
                        mediaId = currentWindow.mediaItem.mediaId,
                        onDismiss = { onShowStatsForNerds(false) }
                    )
                }

                PlaybackError(
                    error = error,
                    onDismiss = retry
                )
            }
        }

        val startAction = SwipeAction(
            onSwipe = { binder.player.forceSeekToPrevious() },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.SkipPrevious,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 32.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            },
            background = MaterialTheme.colorScheme.primaryContainer
        )

        val endAction = SwipeAction(
            onSwipe = { binder.player.forceSeekToNext() },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.SkipNext,
                    contentDescription = null,
                    modifier = Modifier.padding(start = 32.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            },
            background = MaterialTheme.colorScheme.primaryContainer
        )

        if (playerGesturesEnabled) {
            SwipeableActionsBox(
                modifier = modifier.clip(shape = MaterialTheme.shapes.large),
                startActions = listOf(startAction),
                endActions = listOf(endAction),
                content = thumbnailContent
            )
        } else {
            Box(
                modifier = modifier.clip(shape = MaterialTheme.shapes.large),
                content = thumbnailContent
            )
        }
    }
}