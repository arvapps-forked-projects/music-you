package it.vfsfitvnm.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class Context(
    val client: Client,
    val thirdParty: ThirdParty? = null
) {
    @Serializable
    data class Client(
        val clientName: String,
        val clientVersion: String,
        val clientId: String,
        val platform: String,
        val hl: String = "en",
        val visitorData: String?,
        val userAgent: String,
        val osVersion: String?
    )

    @Serializable
    data class ThirdParty(
        val embedUrl: String
    )
}