package it.vfsfitvnm.vimusic.ui.screens.home

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerPadding
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.AlbumSortBy
import it.vfsfitvnm.vimusic.enums.SortOrder
import it.vfsfitvnm.vimusic.models.Album
import it.vfsfitvnm.vimusic.ui.components.SortingHeader
import it.vfsfitvnm.vimusic.ui.items.LocalAlbumItem
import it.vfsfitvnm.vimusic.utils.albumSortByKey
import it.vfsfitvnm.vimusic.utils.albumSortOrderKey
import it.vfsfitvnm.vimusic.utils.rememberPreference

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun HomeAlbums(onAlbumClick: (Album) -> Unit) {
    val playerPadding = LocalPlayerPadding.current

    var items: List<Album> by remember { mutableStateOf(emptyList()) }
    var sortBy by rememberPreference(albumSortByKey, AlbumSortBy.Title)
    var sortOrder by rememberPreference(albumSortOrderKey, SortOrder.Ascending)

    LaunchedEffect(sortBy, sortOrder) {
        Database.albums(sortBy, sortOrder).collect { items = it }
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        contentPadding = PaddingValues(start = 8.dp, end = 8.dp, bottom = 16.dp + playerPadding),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item(
            key = "header",
            span = { GridItemSpan(maxCurrentLineSpan) }
        ) {
            SortingHeader(
                sortBy = sortBy,
                changeSortBy = { sortBy = it },
                sortByEntries = AlbumSortBy.entries.toList(),
                sortOrder = sortOrder,
                toggleSortOrder = { sortOrder = !sortOrder },
                size = items.size,
                itemCountText = R.plurals.number_of_albums
            )
        }

        items(
            items = items,
            key = Album::id
        ) { album ->
            LocalAlbumItem(
                modifier = Modifier.animateItem(),
                album = album,
                onClick = { onAlbumClick(album) }
            )
        }
    }
}