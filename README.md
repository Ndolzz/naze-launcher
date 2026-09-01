# Naze Launcher

A minimal, adaptive Android home-screen launcher: a large real-time clock, weather-
and time-aware ambience theming, an app drawer with search, a favorite dock, and
configurable gestures — built with Kotlin + Jetpack Compose.

## Opening the project

You have two options:

### Option A — Build from your phone via GitHub Actions (no computer needed)

This repo includes `.github/workflows/build.yml`, which builds a debug APK
automatically in the cloud every time you push. Steps, all doable from a phone
browser:

1. Create a free GitHub account if you don't have one.
2. Create a new **empty** repository (e.g. `naze-launcher`).
3. Upload this project's contents to it. Easiest way on mobile: on the repo page,
   tap **Add file → Upload files**, then upload the whole extracted folder (GitHub's
   web uploader supports dragging/selecting a folder; on some mobile browsers you may
   need "Desktop site" mode enabled for the upload button to appear).
4. Once the files are pushed to the `main` branch, go to the **Actions** tab. The
   "Build Naze Launcher APK" workflow will already be running (or tap **Run workflow**
   if it didn't start automatically).
5. Wait a few minutes for the green checkmark. Open the finished run, scroll to
   **Artifacts**, and download `naze-launcher-debug-apk` — that's a zip containing
   `app-debug.apk`.
6. Unzip it on your phone (any file manager / zip app), then tap `app-debug.apk` to
   install. You'll need to allow "install unknown apps" for your browser or file
   manager the first time Android asks.

The debug APK is auto-signed with a debug key by Gradle — fine for installing on
your own device, not for publishing to the Play Store.

### Option B — Android Studio

1. Open this folder in **Android Studio** (Koala/2024.1 or newer recommended).
2. Let Gradle sync — it will generate the wrapper `.jar` automatically the first
   time you sync or run `gradle wrapper` locally if you have Gradle installed.
3. Run on a device or emulator running **API 26+**.

## Before you run it

- **Weather API key**: `HomeViewModel.kt` constructs `OpenWeatherMapApi(apiKey = "YOUR_API_KEY")`.
  Get a free key at https://openweathermap.org/api and move it into
  `local.properties` / `BuildConfig` rather than leaving it inline before you ship.
- **Set as default launcher**: after installing, either let the in-app onboarding
  trigger the system "Set as Home app" prompt, or go to
  Settings → Apps → Default apps → Home app and pick Naze Launcher.

## Project layout

```
app/src/main/java/com/naze/launcher/
  core/         time-of-day + shared constants
  theme/        ThemeEngine (time+weather+temperature -> Ambience) and Compose theme
  clock/        self-ticking clock composable
  weather/      WeatherApi abstraction, OpenWeatherMap impl, caching repository
  location/     single-shot location provider (never polls continuously)
  home/         HomeScreen + HomeViewModel (wires everything together)
  appdrawer/    installed-app listing, search, sort
  dock/         favorite dock storage + UI
  gestures/     configurable swipe/double-tap bindings
  widgets/      thin wrapper over Android's real AppWidgetHost
  settings/     DataStore-backed preferences + Settings UI
  search/       quick search screen
  onboarding/   first-launch flow
  storage/      DataStore keys/provider shared by every repository
```

## What's intentionally NOT included yet

- **Widget placement UI** — `NazeWidgetHost` wraps the real platform APIs
  (`AppWidgetHost`/`AppWidgetManager`), but the drag/resize/place-on-home-screen UI
  around it isn't wired up yet. This is the next logical chunk of work.
- **Naze AI integration** — `MainActivity.openNazeAi()` is a deliberate no-op
  extension point (long-press-the-clock hook), per the spec's requirement that the
  launcher be fully usable without it.
- **App categories in the drawer** — search + name/recency sort are implemented;
  simple category grouping is not yet.
- **Contacts in Quick Search** — the search screen currently matches app names only;
  contacts search needs its own permission prompt, deliberately not pre-requested.

## Design notes worth knowing

- The weather engine only ever fetches on home-screen open/resume, gated by a
  30-minute minimum interval (`Constants.WEATHER_MIN_REFRESH_INTERVAL_MS`), and
  falls back to a cached reading (up to 3 hours old) on any network/API failure —
  see `WeatherRepository`.
- Location is requested as a single fresh fix per weather refresh, never a
  continuous listener — see `LocationProvider`.
- `ThemeEngine` produces a small, restrained palette (background gradient + accent),
  not a full re-skin, so the app keeps reading as a premium OS surface rather than
  a themed game.
