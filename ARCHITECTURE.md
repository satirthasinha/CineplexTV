# Cineplex TV: Android architecture and implementation plan

**Status:** architecture and source scaffold are ready; Cineplex catalog ingestion and playback are not yet production-ready.

## 1. Live access check

Read-only checks were run on 23 November 2026 from this workstation:

| Check | Observed result | What it establishes |
|---|---|---|
| `GET http://103.136.200.66/` | HTTP 200, HTML, `Server: nginx`; title `CinePlex | Largest World` | The portal home page is reachable from this network. |
| Home HTML | Real links to `category.php`, `tcategory.php`, and `search.php`; catalog cards link to `player.php?id=…` | The public site exposes PHP portal navigation and catalog pages. This is not an API contract. |
| `GET /player.php?id=10718&h=` (a player link found in the live homepage) | HTTP 200; returned a player page with a media source, an English VTT track, and a progress endpoint reference | The web player resolves media dynamically. The source URL is short-lived and must not be persisted. |
| Headers-only request and a one-byte Range GET to that resolved source | Both returned HTTP 403 from this network | Direct media access was not demonstrated. Browser or app playback remains unverified. |

The media request failure may reflect token expiry, client/IP policy, required request headers, or a network-specific restriction. We cannot tell which from this check. A successful HTML player response is not proof that Android Media3 can fetch and decode its stream.

### What the supplied references show

The four uploaded HTML references are useful design and behavior references: a 3D-movie browse page, a Dune detail page, a Dune player page, and a Jowar Bhanta episode player. They show dark cinematic styling, poster grids, a wide hero, metadata and action controls, a media surface, captions, and series episode navigation. They include sample portal routes, a sample HLS path, VTT subtitles, and a sample progress endpoint. Those are **reference-file contents**, not independently confirmed working endpoints. The live test separately verified only the portal home, its linked player page, and the 403 response described above.

### Earlier DhakaFlix build guide

The PDF describes a different source shape: HTTP directory listings such as h5ai, Apache autoindex, or Nginx autoindex, plus folder walking and filename parsing. The live Cineplex root is an nginx-hosted PHP portal with catalog/detail/player pages, not an observed autoindex. Reuse the guide's gentle request limits, Media3 player idea, and TV navigation lessons, but do not reuse its listing parser assumptions for Cineplex.

## 2. Recommended Android architecture

Use a single Android app with feature-oriented layers and a connector boundary:

```text
Compose UI (phone + Android TV layouts)
  Home / Movies / Series / Search / Details / Player / My List / Settings
                    │
             ViewModels + use cases
                    │
        CatalogRepository / PlaybackRepository
                    │
         ConnectorRegistry → MediaConnector
           ├── CineplexPortalConnector (pending contract)
           ├── H5aiConnector (future, only if source supports it)
           ├── ApacheIndexConnector (future)
           ├── NginxIndexConnector (future)
           └── FtpConnector (future, explicit credentials/access)
                    │
         HTTP client / parsers / Media3 player
                    │
  Room catalog cache + watch progress + favorites
```

- **UI:** Jetpack Compose; layouts adapt between touch screens and television. TV uses a persistent side rail, large focus targets, and DPAD focus restoration; phone uses bottom navigation. Use the provided visual references for the dark palette, poster cards, cinematic hero, details layout, and series episodes.
- **App state:** ViewModels expose immutable UI state (loading, content, empty, and actionable error). UI never parses HTML or constructs server paths.
- **Data:** repositories call connectors and cache normalized catalog, categories, progress, and favorites in Room. DataStore holds source configuration and preferences. Signed media URLs stay in memory only.
- **Network:** source-specific HTTP clients with per-source cookies and headers, bounded concurrency (start at 2–4), timeouts, retry/backoff for safe GETs, and parser/version diagnostics. Never log credentials or full signed URLs.
- **Playback:** AndroidX Media3 ExoPlayer behind a `PlaybackController`. Choose HLS or progressive from the resolved response; pass only the headers required by the source; expose audio/subtitle selection, seek, resume, and player errors. Codec support varies by device, so report decoder errors and allow an external-player fallback only if the source operator permits it. No download feature until its authorization and endpoint are defined.
- **Metadata:** titles, synopsis, posters, and source taxonomy should come from the source first. Optional TMDB enrichment is a separate metadata provider and requires an API key/terms decision; it must not replace source IDs or imply availability.
- **Additional sources:** one connector per source protocol/site. Store `connectorKey`, base URL, optional user-managed credentials, enabled state, and display name. Avoid a generic “probe arbitrary host” crawler; require a supported connector type and conservative request policy.

Media3's ExoPlayer is the recommended native playback foundation and has modules for progressive media and HLS. [Android Media3 ExoPlayer documentation](https://developer.android.com/media/media3/exoplayer) · [Media3 release versions](https://developer.android.com/jetpack/androidx/releases/media3). Compose dependencies should be managed as a compatible set with the official BOM. [Jetpack Compose BOM](https://developer.android.com/develop/ui/compose/bom).

## 3. Connector and data model

The starter project defines these normalized entities in `app/src/main/java/bd/cineplex/tv/data/model/MediaModels.kt`:

- `MediaSource`: stable source ID, display name, base URL, connector key, enabled flag.
- `CatalogCategory`: stable source category ID and title.
- `CatalogItem`: source-scoped stable ID, kind (movie/series/episode/live), title/year/artwork/synopsis/genres/runtime.
- `Season` and `Episode`: season/episode ordering and stable episode media IDs.
- `PlaybackSource`: ephemeral resolved URI, protocol, required request headers, external subtitle tracks, and optional expiry.
- `PlaybackProgress`: stable media ID and last known position/duration.

`MediaConnector` owns connection checks, categories, paged browsing, search, seasons, and playback resolution. Errors must distinguish unsupported operation, access denied, rate limited, parse changed, network unavailable, and expired playback resolution. A connector can add richer site-specific operations without leaking them into common UI models.

For Cineplex, implement in this order once the operator confirms access rules: (1) HTTP client/session lifecycle, (2) parser fixtures captured from permitted pages, (3) home/category/search parsing, (4) detail and episode mapping, (5) resolver call and token expiry handling, (6) progress sync only if the API is explicitly supported. Do not put live IDs, media paths, or captured tokens in app constants.

## 4. Navigation and UI plan from the references

1. **Home:** cinematic featured row; Continue Watching; Latest Movies; Series; source/category shelves. Use verified source content only.
2. **Browse:** category chips and poster grid based on the 3D Movies reference. Add search and filters only as the connector supports them.
3. **Details:** wide backdrop/hero, poster, title/year/rating where available, synopsis, genres, Play/Resume, My List. Do not manufacture ratings or cast; TMDB is optional enrichment.
4. **Series:** show summary → season selector → numbered episode rows with duration/progress → next episode only after a real episode list is available.
5. **Player:** Media3 controls, seek, audio/subtitle menus, playback errors, and resume position. Store progress locally first; remote progress remains optional and unimplemented until API behavior is confirmed.
6. **Sources/settings:** add or disable supported connector types, test connection, select preferred source/audio language, clear cache, and view per-source diagnostics.

## 5. Project scaffold

`CineplexTV/` contains a native Kotlin/Compose app skeleton, Gradle files, Internet permission, source models, connector interface/registry, a deliberately pending Cineplex connector, and a transparent empty-state screen. It is a foundation, not a working streaming app. The captured 403 and the lack of an Android SDK/Gradle installation in this workspace mean I did not claim a successful build or playback check.

## 6. Staged implementation milestones

| Stage | Deliverable | Exit condition |
|---|---|---|
| 0. Access contract | Operator-confirmed allowed endpoints, auth/session flow, client restrictions, stream formats/codecs, and Android/ISP access scope | A test client can read catalog and fetch a stream without copying a browser's temporary token. |
| 1. Native shell | Branding, adaptive phone/TV navigation, focus behavior, source settings, error/empty states | UI can be navigated by touch and DPAD and restores focus when returning from details. |
| 2. Cineplex catalog | Parser/connector for home, categories, search, details, series/seasons; fixture-based compatibility checks | Catalog results match live pages and degrade safely when markup changes. |
| 3. Playback | Resolver, Media3, headers/cookies, HLS/progressive selection, subtitles/audio, resume | A real permitted title starts and seeks on target Android devices over the intended ISP route. |
| 4. Persistence | Room cache, favorites, local resume, source preferences | Restart/offline behavior is predictable; expiring stream URLs are never restored from storage. |
| 5. More connectors | Add one independently tested connector per supported BDIX/HTTP/FTP source | Each source declares protocol/auth/limits and maps to the common model. |
| 6. Release readiness | Security, accessibility, DPAD, performance, codec matrix, privacy and distribution review | Device/network matrix passes and operator permissions are documented. |

## 7. Exact unknowns and requirements to unblock implementation

1. **Android app access:** Is this server intended to be accessed by a third-party Android app, and is access restricted to a particular ISP/IP, browser session, cookie, user-agent, or Referer?
2. **Catalog contract:** Which category, search, detail, and series endpoints are supported and stable? Is scraping the public HTML allowed, or is there a supported JSON/API feed?
3. **Playback contract:** What should Android request to obtain the playable media? Is there a stable resolver endpoint, how are signed URLs renewed, which headers/cookies are required, and what token lifetime is expected? The observed media request returned 403 here.
4. **Formats:** Which video/audio codecs and containers are actually served (HLS, MP4, MKV, others), and are external subtitles available in VTT/SRT? The sample references alone do not establish device compatibility.
5. **Progress/download:** Does the progress endpoint accept app-origin requests? Is progress user/session scoped? Is download permitted and supported for the Android client?
6. **Additional servers:** For each BDIX/HTTP/FTP source, provide base URL, listing/API format, access scope, credentials handling, category map, and rate/concurrency guidance. FTP needs an explicit authentication and transport policy.
7. **Product targets:** Confirm minimum Android version and target devices (phone, Android TV/Google TV, or both), preferred audio language, TMDB API-key ownership, and the intended app distribution channel.

Without items 1–4, the app can be built and tested as a native shell, but source ingestion and real playback cannot be responsibly completed or represented as working.
