package it.vfsfitvnm.vimusic.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.vimusic.models.ActionInfo

@Composable
fun SwipeToActionBox(
    modifier: Modifier = Modifier,
    primaryAction: ActionInfo? = null,
    destructiveAction: ActionInfo? = null,
    content: @Composable () -> Unit
) {
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    primaryAction?.onClick?.invoke()
                    return@rememberSwipeToDismissBoxState false
                }

                SwipeToDismissBoxValue.EndToStart -> {
                    destructiveAction?.onClick?.invoke()
                    return@rememberSwipeToDismissBoxState true
                }

                else -> {
                    return@rememberSwipeToDismissBoxState false
                }
            }
        }
    )

    SwipeToDismissBox(
        state = state,
        modifier = modifier,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = when (state.targetValue) {
                    SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primaryContainer
                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                    SwipeToDismissBoxValue.Settled -> Color.Transparent
                },
                label = "background"
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = when (state.targetValue) {
                    SwipeToDismissBoxValue.StartToEnd -> Arrangement.Start
                    SwipeToDismissBoxValue.EndToStart -> Arrangement.End
                    SwipeToDismissBoxValue.Settled -> Arrangement.Center
                },
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (state.targetValue == SwipeToDismissBoxValue.StartToEnd && primaryAction != null) {
                    Icon(
                        imageVector = primaryAction.icon,
                        contentDescription = stringResource(id = primaryAction.description),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                } else if (state.targetValue == SwipeToDismissBoxValue.EndToStart && destructiveAction != null) {
                    Icon(
                        imageVector = destructiveAction.icon,
                        contentDescription = stringResource(id = destructiveAction.description),
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

        },
        enableDismissFromStartToEnd = primaryAction != null && primaryAction.enabled,
        enableDismissFromEndToStart = destructiveAction != null && destructiveAction.enabled
    ) {
        content()
    }
}