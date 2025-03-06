package it.vfsfitvnm.innertube.models.bodies

import it.vfsfitvnm.innertube.models.Context
import it.vfsfitvnm.innertube.models.YouTubeClient
import kotlinx.serialization.Serializable

@Serializable
data class PlayerBody(
    val context: Context = YouTubeClient.IOS.toContext(),
    val videoId: String,
    val playlistId: String? = null
)