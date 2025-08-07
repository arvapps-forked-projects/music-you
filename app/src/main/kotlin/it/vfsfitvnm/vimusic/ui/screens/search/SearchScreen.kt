package it.vfsfitvnm.vimusic.ui.screens.search

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopSearchBar
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import it.vfsfitvnm.innertube.Innertube
import it.vfsfitvnm.innertube.requests.searchSuggestions
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.models.SearchQuery
import it.vfsfitvnm.vimusic.query
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.utils.pauseSearchHistoryKey
import it.vfsfitvnm.vimusic.utils.preferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

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
    var searchText: String? by rememberSaveable { mutableStateOf(null) }
    var history: List<SearchQuery> by remember { mutableStateOf(emptyList()) }
    var suggestionsResult: Result<List<String>?>? by remember { mutableStateOf(null) }

    val scope = rememberCoroutineScope()
    val textFieldState = rememberTextFieldState()
    val searchBarState = rememberSearchBarState(initialValue = SearchBarValue.Expanded)

    val onSearch: (String) -> Unit = { query ->
        if (query.isNotEmpty()) {
            textFieldState.setTextAndPlaceCursorAtEnd(text = query)
            searchText = query

            scope.launch {
                searchBarState.animateToCollapsed()
            }

            if (!context.preferences.getBoolean(pauseSearchHistoryKey, false)) {
                query {
                    Database.insert(SearchQuery(query = query))
                }
            }
        }
    }

    val inputField = @Composable {
        SearchInputField(
            textFieldState = textFieldState,
            searchBarState = searchBarState,
            searchText = searchText,
            onSearch = onSearch,
            pop = pop,
            scope = scope
        )
    }

    LaunchedEffect(textFieldState.text) {
        if (!context.preferences.getBoolean(pauseSearchHistoryKey, false)) {
            Database.queries("%${textFieldState.text}%")
                .distinctUntilChanged { old, new -> old.size == new.size }
                .collect { history = it }
        }
    }

    LaunchedEffect(textFieldState.text) {
        suggestionsResult = if (textFieldState.text.isNotEmpty()) {
            delay(200)
            Innertube.searchSuggestions(input = "${textFieldState.text}")
        } else null
    }

    Scaffold(
        topBar = {
            TopSearchBar(
                state = searchBarState,
                inputField = inputField,
            )

            ExpandedFullScreenSearchBar(
                state = searchBarState,
                inputField = inputField
            ) {
                BackHandler(
                    enabled = searchBarState.currentValue == SearchBarValue.Expanded && searchText == null,
                    onBack = pop
                )

                LazyColumn {
                    items(
                        items = history,
                        key = SearchQuery::id
                    ) { query ->
                        ListItem(
                            headlineContent = {
                                Text(text = query.query)
                            },
                            modifier = Modifier.clickable { onSearch(query.query) },
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
                                            query { Database.delete(query) }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Delete,
                                            contentDescription = null
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            textFieldState.setTextAndPlaceCursorAtEnd(text = query.query)
                                        },
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
                                        onClick = { textFieldState.setTextAndPlaceCursorAtEnd(text = suggestion) },
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
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            searchText?.let { query ->
                SearchResults(
                    query = query,
                    onAlbumClick = onAlbumClick,
                    onArtistClick = onArtistClick,
                    onPlaylistClick = onPlaylistClick
                )
            }
        }
    }
}