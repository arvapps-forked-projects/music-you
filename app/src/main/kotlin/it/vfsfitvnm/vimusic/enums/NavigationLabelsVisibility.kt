package it.vfsfitvnm.vimusic.enums

import androidx.annotation.StringRes
import it.vfsfitvnm.vimusic.R

enum class NavigationLabelsVisibility(
    @StringRes val resourceId: Int
) {
    Visible(resourceId = R.string.visible),
    VisibleWhenActive(resourceId = R.string.visible_when_active),
    Hidden(resourceId = R.string.hidden)
}