package it.vfsfitvnm.vimusic.ui.screens.player

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import it.vfsfitvnm.vimusic.enums.DragValue
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwipeToOpenBox(
    modifier: Modifier = Modifier,
    openAction: () -> Unit,
    closeAction: (() -> Unit)? = null,
    anchorValue: Float = 200F,
    content: @Composable () -> Unit
) {
    val state = remember {
        AnchoredDraggableState(
            initialValue = DragValue.Center,
            anchors = DraggableAnchors {
                DragValue.Top at -anchorValue
                DragValue.Center at 0F
                if (closeAction != null) DragValue.Bottom at anchorValue
            },
            positionalThreshold = { distance: Float -> distance * 0.5F },
            velocityThreshold = { anchorValue },
            animationSpec = tween(),
            confirmValueChange = { value ->
                when (value) {
                    DragValue.Top -> {
                        openAction()
                        return@AnchoredDraggableState false
                    }

                    DragValue.Bottom -> {
                        if (closeAction != null) closeAction()
                        return@AnchoredDraggableState true
                    }

                    DragValue.Center -> return@AnchoredDraggableState true
                }
            }
        )
    }

    val color by animateColorAsState(
        targetValue = when (state.targetValue) {
            DragValue.Top -> MaterialTheme.colorScheme.primaryContainer
            DragValue.Bottom -> MaterialTheme.colorScheme.errorContainer
            DragValue.Center -> Color.Transparent
        },
        label = "background"
    )

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .matchParentSize()
                .background(color),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (state.offset >= 0) Icons.Outlined.Close else Icons.Outlined.KeyboardArrowUp,
                contentDescription = null,
                tint = if (state.offset >= 0) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = 0,
                        y = state
                            .requireOffset()
                            .roundToInt()
                    )
                }
                .anchoredDraggable(
                    state = state,
                    orientation = Orientation.Vertical
                )
        ) {
            content()
        }
    }
}