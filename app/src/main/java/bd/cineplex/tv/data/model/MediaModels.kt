package bd.cineplex.tv.data.model

import java.time.Instant

@JvmInline value class SourceId(val value: String)
@JvmInline value class MediaId(val value: String)

enum class MediaKind { MOVIE, SERIES, EPISODE, LIVE }
enum class StreamProtocol { PROGRESSIVE, HLS, DASH, UNKNOWN }

data class MediaSource(
    val id: SourceId,
    val displayName: String,
    val baseUrl: String,
    val connectorKey: String,
    val enabled: Boolean = true,
)

data class CatalogCategory(val id: String, val title: String, val sourceId: SourceId)

data class CatalogItem(
    val id: MediaId,
    val sourceId: SourceId,
    val kind: MediaKind,
    val title: String,
    val year: Int? = null,
    val posterUrl: String? = null,
    val backdropUrl: String? = null,
    val synopsis: String? = null,
    val genres: List<String> = emptyList(),
    val runtimeSeconds: Long? = null,
)

data class Season(val number: Int, val episodes: List<Episode>)
data class Episode(
    val mediaId: MediaId,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val title: String,
)

/** A resolved, short-lived playback grant. Persist the stable media identity, never this URL. */
data class PlaybackSource(
    val uri: String,
    val protocol: StreamProtocol,
    val requestHeaders: Map<String, String> = emptyMap(),
    val subtitleTracks: List<SubtitleTrack> = emptyList(),
    val expiresAt: Instant? = null,
)

data class SubtitleTrack(
    val uri: String,
    val language: String? = null,
    val label: String? = null,
    val mimeType: String? = null,
)

data class PlaybackProgress(
    val mediaId: MediaId,
    val positionMs: Long,
    val durationMs: Long?,
    val updatedAt: Instant,
)
