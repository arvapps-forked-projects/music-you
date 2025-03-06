package it.vfsfitvnm.innertube.models

class YouTubeClient(
    val clientName: String,
    val clientVersion: String,
    val clientId: String,
    val platform: String,
    val userAgent: String,
    val osVersion: String? = null
) {
    fun toContext(visitorData: String? = null) = Context(
        client = Context.Client(
            clientName = clientName,
            clientVersion = clientVersion,
            clientId = clientId,
            platform = platform,
            visitorData = visitorData,
            userAgent = userAgent,
            osVersion = osVersion,
        )
    )

    companion object {
        val WEB_REMIX = YouTubeClient(
            clientName = "WEB_REMIX",
            clientVersion = "1.20250122.01.00",
            clientId = "67",
            platform = "DESKTOP",
            userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:128.0) Gecko/20100101 Firefox/128.0"
        )

        val IOS = YouTubeClient(
            clientName = "IOS",
            clientVersion = "20.03.02",
            clientId = "5",
            platform = "MOBILE",
            userAgent = "com.google.ios.youtube/20.03.02 (iPhone16,2; U; CPU iOS 18_2_1 like Mac OS X;)",
            osVersion = "18.2.1.22C161"
        )

        val TVHTML5_SIMPLY_EMBEDDED_PLAYER = YouTubeClient(
            clientName = "TVHTML5_SIMPLY_EMBEDDED_PLAYER",
            clientVersion = "2.0",
            clientId = "85",
            platform = "TV",
            userAgent = "Mozilla/5.0 (PlayStation; PlayStation 4/12.00) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.4 Safari/605.1.15"
        )
    }
}