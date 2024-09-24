package it.vfsfitvnm.vimusic.ui.screens.search

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import it.vfsfitvnm.innertube.Innertube
import it.vfsfitvnm.innertube.models.bodies.SearchSuggestionsBody
import it.vfsfitvnm.innertube.requests.searchSuggestions
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.models.SearchQuery
import it.vfsfitvnm.vimusic.query
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.utils.pauseSearchHistoryKey
import it.vfsfitvnm.vimusic.utils.preferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun SearchScreen(
    pop: () -> Unit,
    onAlbumClick: (String) -> Unit,
    onArtistClick: (String) -> Unit,
    onPlaylistClick: (String) -> Unit
) {
    val context = LocalContext.current
    var history: List<SearchQuery> by remember { mutableStateOf(emptyList()) }
    var suggestionsResult: Result<List<String>?>? by remember { mutableStateOf(null) }

    var query by rememberSaveable { mutableStateOf("") }
    var expanded by rememberSaveable { mutableStateOf(true) }
    var searchText: String? by rememberSaveable { mutableStateOf(null) }
    val focusRequester = remember { FocusRequester() }

    fun onSearch(searchQuery: String) {
        query = searchQuery
        searchText = searchQuery
        expanded = false

        if (!context.preferences.getBoolean(pauseSearchHistoryKey, false)) {
            query {
                Database.insert(SearchQuery(query = query))
            }
        }
    }

    LaunchedEffect(query) {
        if (!context.preferences.getBoolean(pauseSearchHistoryKey, false)) {
            Database.queries("%$query%")
                .distinctUntilChanged { old, new -> old.size == new.size }
                .collect { history = it }
        }
    }

    LaunchedEffect(query) {
        suggestionsResult = if (query.isNotEmpty()) {
            delay(200)
            Innertube.searchSuggestions(SearchSuggestionsBody(input = query))
        } else null
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val onExpandedChange: (Boolean) -> Unit = { expandedState ->
            if (searchText.isNullOrEmpty() && !expandedState) pop()
            else expanded = expandedState
        }

        SearchBar(
            inputField = {
                SearchBarDefaults.InputField(
                    query = query,
                    onQueryChange = { query = it },
                    onSearch = { if (query.isNotBlank()) onSearch(query) },
                    expanded = expanded,
                    onExpandedChange = onExpandedChange,
                    placeholder = {
                        Text(text = stringResource(id = R.string.search))
                    },
                    leadingIcon = {
                        IconButton(onClick = pop) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = null
                            )
                        }
                    },
                    trailingIcon = {
                        if (query.isNotBlank() && expanded) {
                            IconButton(onClick = { query = "" }) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = null
                                )
                            }
                        }
                    }
                )
            },
            expanded = expanded,
            onExpandedChange = onExpandedChange,
            modifier = Modifier.focusRequester(focusRequester)
        ) {
            LazyColumn {
                items(
                    items = history,
                    key = SearchQuery::id
                ) { searchQuery ->
                    ListItem(
                        headlineContent = {
                            Text(text = searchQuery.query)
                        },
                        modifier = Modifier.clickable { onSearch(searchQuery.query) },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Outlined.History,
                                contentDescription = null
                            )
                        },
                        trailingContent = {
                            Row {
                                IconButton(
                                    onClick = {
                                        query {
                                            Database.delete(searchQuery)
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Delete,
                                        contentDescription = null
                                    )
                                }

                                IconButton(
                                    onClick = { query = searchQuery.query },
                                    modifier = Modifier.rotate(225F)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                                        contentDescription = null
                                    )
                                }
                            }
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = SearchBarDefaults.colors().containerColor
                        )
                    )
                }

                suggestionsResult?.getOrNull()?.let { suggestions ->
                    items(items = suggestions) { suggestion ->
                        ListItem(
                            headlineContent = {
                                Text(text = suggestion)
                            },
                            modifier = Modifier.clickable { onSearch(suggestion) },
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Outlined.Search,
                                    contentDescription = null
                                )
                            },
                            trailingContent = {
                                IconButton(
                                    onClick = { query = suggestion },
                                    modifier = Modifier.rotate(225F)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                                        contentDescription = null
                                    )
                                }
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = SearchBarDefaults.colors().containerColor
                            )
                        )
                    }
                } ?: suggestionsResult?.exceptionOrNull()?.let {
                    item {
                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = "An error has occurred.",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .alpha(Dimensions.mediumOpacity)
                            )
                        }
                    }
                }
            }
        }

        searchText?.let {
            SearchResults(
                query = query,
                onAlbumClick = onAlbumClick,
                onArtistClick = onArtistClick,
                onPlaylistClick = onPlaylistClick
            )
        }
    }

    LaunchedEffect(Unit) {
        if (searchText.isNullOrEmpty()) focusRequester.requestFocus()
    }
}