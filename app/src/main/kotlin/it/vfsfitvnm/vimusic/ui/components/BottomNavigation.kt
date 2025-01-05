package it.vfsfitvnm.vimusic.ui.components

import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import it.vfsfitvnm.vimusic.enums.NavigationLabelsVisibility
import it.vfsfitvnm.vimusic.models.Screen
import it.vfsfitvnm.vimusic.utils.homeScreenTabIndexKey
import it.vfsfitvnm.vimusic.utils.navigationLabelsVisibilityKey
import it.vfsfitvnm.vimusic.utils.rememberPreference

@Composable
fun BottomNavigation(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    var navigationLabelsVisibility by rememberPreference(
        navigationLabelsVisibilityKey,
        NavigationLabelsVisibility.Visible
    )
    val (_, onScreenChanged) = rememberPreference(
        homeScreenTabIndexKey,
        defaultValue = 0
    )

    val homeScreens = listOf(
        Screen.Home,
        Screen.Songs,
        Screen.Artists,
        Screen.Albums,
        Screen.Playlists
    )

    NavigationBar(
        modifier = if (navigationLabelsVisibility == NavigationLabelsVisibility.Hidden) Modifier.heightIn(
            max = 90.dp
        ) else Modifier
    ) {
        homeScreens.forEachIndexed { index, screen ->
            val selected =
                currentDestination?.hierarchy?.any { it.route == screen.route } == true

            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        onScreenChanged(index)
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id)
                            launchSingleTop = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                        contentDescription = stringResource(id = screen.resourceId)
                    )
                },
                label = {
                    if (navigationLabelsVisibility != NavigationLabelsVisibility.Hidden) {
                        Text(
                            text = stringResource(id = screen.resourceId),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                alwaysShowLabel = navigationLabelsVisibility != NavigationLabelsVisibility.VisibleWhenActive
            )
        }
    }
}