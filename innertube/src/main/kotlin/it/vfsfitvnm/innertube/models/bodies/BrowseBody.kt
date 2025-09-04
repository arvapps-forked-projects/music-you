package it.vfsfitvnm.innertube.models.bodies

import it.vfsfitvnm.innertube.models.Context
import it.vfsfitvnm.innertube.models.YouTubeClient
import kotlinx.serialization.Serializable

@Serializable
data class BrowseBody(
    val localized: Boolean = true,
    val context: Context = YouTubeClient.WEB_REMIX.toContext(localized = localized),
    val browseId: String,
    val params: String? = null
)
