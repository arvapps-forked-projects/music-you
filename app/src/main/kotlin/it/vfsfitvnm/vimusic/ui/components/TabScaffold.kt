package it.vfsfitvnm.vimusic.ui.components

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import it.vfsfitvnm.vimusic.models.Section

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@ExperimentalAnimationApi
@Composable
fun TabScaffold(
    pagerState: PagerState,
    topIconButtonId: ImageVector,
    onTopIconButtonClick: () -> Unit,
    sectionTitle: String,
    appBarActions: @Composable (() -> Unit)? = null,
    tabColumnContent: List<Section>,
    content: @Composable (Int) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Column {
                MediumTopAppBar(
                    title = {
                        Text(
                            text = sectionTitle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onTopIconButtonClick) {
                            Icon(
                                imageVector = topIconButtonId,
                                contentDescription = null
                            )
                        }
                    },
                    actions = { appBarActions?.invoke() },
                    colors = TopAppBarDefaults.mediumTopAppBarColors(scrolledContainerColor = MaterialTheme.colorScheme.surface),
                    scrollBehavior = scrollBehavior
                )

                if (tabColumnContent.size > 1) {
                    TabGroup(
                        pagerState = pagerState,
                        content = tabColumnContent
                    )
                }
            }
        }
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .consumeCustomWindowInsets(paddingValues)
        ) {
            content(it)
        }
    }
}