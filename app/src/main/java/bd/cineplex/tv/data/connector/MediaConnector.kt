package bd.cineplex.tv.data.connector

import bd.cineplex.tv.data.model.*

data class ConnectorCapabilities(
    val movies: Boolean,
    val series: Boolean,
    val search: Boolean,
    val playbackResolution: Boolean,
    val subtitles: Boolean,
    val remoteProgress: Boolean,
)

sealed interface ConnectorResult<out T> {
    data class Ready<T>(val value: T) : ConnectorResult<T>
    data class Unavailable(val reason: String, val retryable: Boolean = false) : ConnectorResult<Nothing>
}

/** Stable app-facing contract; implementations own each server's URL, HTML/API, auth and parsing rules. */
interface MediaConnector {
    val key: String
    val capabilities: ConnectorCapabilities
    suspend fun checkConnection(source: MediaSource): ConnectorResult<Unit>
    suspend fun categories(source: MediaSource): ConnectorResult<List<CatalogCategory>>
    suspend fun browse(source: MediaSource, category: CatalogCategory, page: Int): ConnectorResult<List<CatalogItem>>
    suspend fun search(source: MediaSource, query: String): ConnectorResult<List<CatalogItem>>
    suspend fun seasons(source: MediaSource, series: CatalogItem): ConnectorResult<List<Season>>
    suspend fun resolvePlayback(source: MediaSource, media: CatalogItem): ConnectorResult<PlaybackSource>
    suspend fun loadProgress(source: MediaSource, media: CatalogItem): ConnectorResult<PlaybackProgress?>
    suspend fun saveProgress(source: MediaSource, progress: PlaybackProgress): ConnectorResult<Unit>
}

class ConnectorRegistry(connectors: List<MediaConnector>) {
    private val byKey = connectors.associateBy(MediaConnector::key)
    fun get(key: String): MediaConnector? = byKey[key]
}
