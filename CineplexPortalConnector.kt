package bd.cineplex.tv.data.connector

import bd.cineplex.tv.data.model.*

/**
 * Intentionally not a scraper yet. Live public HTML has been observed, but the app-facing
 * catalog, authentication, and media access contract is not established. Fill this adapter
 * only after the operator confirms supported endpoints and the app's access path.
 */
class CineplexPortalConnector : MediaConnector {
    override val key: String = "cineplex-portal-v1"
    override val capabilities = ConnectorCapabilities(
        movies = false,
        series = false,
        search = false,
        playbackResolution = false,
        subtitles = false,
        remoteProgress = false,
    )

    private fun pending(): ConnectorResult.Unavailable = ConnectorResult.Unavailable(
        reason = "Cineplex app connector needs a confirmed catalog and playback contract.",
    )

    override suspend fun checkConnection(source: MediaSource) = pending()
    override suspend fun categories(source: MediaSource) = pending()
    override suspend fun browse(source: MediaSource, category: CatalogCategory, page: Int) = pending()
    override suspend fun search(source: MediaSource, query: String) = pending()
    override suspend fun seasons(source: MediaSource, series: CatalogItem) = pending()
    override suspend fun resolvePlayback(source: MediaSource, media: CatalogItem) = pending()
    override suspend fun loadProgress(source: MediaSource, media: CatalogItem) = pending()
    override suspend fun saveProgress(source: MediaSource, progress: PlaybackProgress) = pending()
}
