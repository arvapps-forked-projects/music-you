package it.vfsfitvnm.vimusic.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.vimusic.models.ActionInfo
import it.vfsfitvnm.vimusic.utils.listGesturesEnabledKey
import it.vfsfitvnm.vimusic.utils.rememberPreference
import kotlinx.coroutines.launch

@Composable
fun SwipeToActionBox(
    modifier: Modifier = Modifier,
    state: SwipeToDismissBoxState = rememberSwipeToDismissBoxState(),
    primaryAction: ActionInfo? = null,
    destructiveAction: ActionInfo? = null,
    content: @Composable RowScope.() -> Unit
) {
    var listGesturesEnabled by rememberPreference(listGesturesEnabledKey, true)
    val scope = rememberCoroutineScope()

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

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 32.dp)
            ) {
                if (primaryAction != null && state.dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
                    Icon(
                        imageVector = primaryAction.icon,
                        contentDescription = stringResource(id = primaryAction.description),
                        modifier = Modifier.align(Alignment.CenterStart),
                        tint = if (state.targetValue == SwipeToDismissBoxValue.StartToEnd) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else MaterialTheme.colorScheme.onSurface
                    )
                }

                if (destructiveAction != null && state.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                    Icon(
                        imageVector = destructiveAction.icon,
                        contentDescription = stringResource(id = destructiveAction.description),
                        modifier = Modifier.align(Alignment.CenterEnd),
                        tint = if (state.targetValue == SwipeToDismissBoxValue.EndToStart) {
                            MaterialTheme.colorScheme.onErrorContainer
                        } else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        enableDismissFromStartToEnd = primaryAction != null && primaryAction.enabled,
        enableDismissFromEndToStart = destructiveAction != null && destructiveAction.enabled,
        gesturesEnabled = listGesturesEnabled,
        onDismiss = { value ->
            if (value == SwipeToDismissBoxValue.StartToEnd) {
                primaryAction?.onClick?.invoke()
                scope.launch { state.reset() }
            } else if (value == SwipeToDismissBoxValue.EndToStart) destructiveAction?.onClick?.invoke()
        },
        content = content
    )
}