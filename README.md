# Cineplex TV Android scaffold

This is a native Android starter project for the Cineplex TV architecture. It includes the module boundaries, source and catalog models, connector contract, and an honest empty-state UI. It does **not** claim that the live catalog can yet be consumed in-app or that playback works.

## Open it

Open this folder in Android Studio with Android SDK 36 and a JDK 17 installation. The project uses Gradle Kotlin DSL, Jetpack Compose, and Media3. Dependency versions are pinned in `build.gradle.kts` and `app/build.gradle.kts`; Gradle sync and device installation have not been run in this environment.

## GitHub Actions

The workflow at `.github/workflows/android.yml` builds a debug APK on pushes, pull requests, or manual dispatch. Put the contents of this folder at the repository root (so the workflow path is `.github/workflows/android.yml`), then push to GitHub. The workflow uploads `app-debug.apk` as the `cineplex-tv-debug` artifact. It does not publish to Google Play or distribute a release build.

The first server is configured as `http://103.136.200.66/`. It is plain HTTP. The manifest temporarily enables cleartext traffic for the first-source discovery work; production should restrict cleartext to the approved server or use HTTPS if the operator provides it.

## Current boundary

`MediaConnector` is an extension contract. `CineplexPortalConnector` deliberately reports that app integration is pending instead of scraping undocumented behavior or inventing data. See `../ARCHITECTURE.md` for verified access, unknowns, and the implementation sequence.

The captured player-page URL contained a short-lived token and returned HTTP 403 to a one-byte media-range request from the inspection network. It is not stored in this project.
