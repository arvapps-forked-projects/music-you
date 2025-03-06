package it.vfsfitvnm.innertube.models.bodies

import it.vfsfitvnm.innertube.models.Context
import it.vfsfitvnm.innertube.models.YouTubeClient
import kotlinx.serialization.Serializable

@Serializable
data class SearchBody(
    val context: Context = YouTubeClient.WEB_REMIX.toContext(),
    val query: String,
    val params: String
)
