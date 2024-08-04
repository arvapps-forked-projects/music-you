package it.vfsfitvnm.vimusic.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier

fun Modifier.consumeCustomWindowInsets(paddingValues: PaddingValues): Modifier {
    return this
        .padding(top = paddingValues.calculateTopPadding())
        .consumeWindowInsets(PaddingValues(bottom = paddingValues.calculateBottomPadding()))
}