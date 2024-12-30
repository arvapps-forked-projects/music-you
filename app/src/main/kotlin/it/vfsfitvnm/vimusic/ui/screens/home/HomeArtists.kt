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
import it.vfsfitvnm.vimusic.enums.ArtistSortBy
import it.vfsfitvnm.vimusic.enums.SortOrder
import it.vfsfitvnm.vimusic.models.Artist
import it.vfsfitvnm.vimusic.ui.components.SortingHeader
import it.vfsfitvnm.vimusic.ui.items.LocalArtistItem
import it.vfsfitvnm.vimusic.utils.artistSortByKey
import it.vfsfitvnm.vimusic.utils.artistSortOrderKey
import it.vfsfitvnm.vimusic.utils.rememberPreference

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun HomeArtistList(onArtistClick: (Artist) -> Unit) {
    val playerPadding = LocalPlayerPadding.current

    var sortBy by rememberPreference(artistSortByKey, ArtistSortBy.Name)
    var sortOrder by rememberPreference(artistSortOrderKey, SortOrder.Ascending)
    var items: List<Artist> by remember { mutableStateOf(emptyList()) }

    LaunchedEffect(sortBy, sortOrder) {
        Database.artists(sortBy, sortOrder).collect { items = it }
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 100.dp),
        contentPadding = PaddingValues(start = 8.dp, end = 8.dp, bottom = 16.dp + playerPadding),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item(
            key = "header",
            span = { GridItemSpan(maxLineSpan) }
        ) {
            SortingHeader(
                sortBy = sortBy,
                changeSortBy = { sortBy = it },
                sortByEntries = ArtistSortBy.entries.toList(),
                sortOrder = sortOrder,
                toggleSortOrder = { sortOrder = !sortOrder },
                size = items.size,
                itemCountText = R.plurals.number_of_artists
            )
        }

        items(items = items, key = Artist::id) { artist ->
            LocalArtistItem(
                modifier = Modifier.animateItem(),
                artist = artist,
                onClick = { onArtistClick(artist) }
            )
        }
    }
}