# NAZE LAUNCHER

A calm, adaptive Android home screen. Naze is designed to feel like a launcher from a few years ahead — large typographic clock, honest weather, a grid drawer, universal search and real quick actions — while staying light enough for daily use on any device.

> A personal operating environment, not an Android launcher with a nice wallpaper.

## Highlights

- **Typographic home screen** — Space Grotesk clock as the centerpiece, date, battery chip, weather line, favorite dock and a search pill. Nothing you don't need.
- **Naze Lock** — a glanceable clock screen (Stack / Analog / Terminal) that can appear over the system lock screen, with real flashlight and camera shortcuts. It never replaces the system lock: unlocking uses your real PIN/biometric. Bind it to a gesture or preview it from Settings → Clock.
- **Adaptive ambience** — the background palette shifts with time of day, weather and temperature (each layer can be toggled).
- **Grid app drawer** — swipe up, search as you type, A–Z / recent sorting, configurable column count and labels, staggered entrance motion.
- **Universal search** — apps plus real launcher actions (flashlight, Wi-Fi panel, Bluetooth, wallpaper, settings…). No dead rows.
- **Real quick actions** — flashlight via CameraManager torch (no permission), Wi-Fi panel, Bluetooth settings, wallpaper picker, notification shade expansion. Everything wired; nothing fake.
- **Gestures** — swipe up/down/left/right, double tap and long press, all rebindable in Settings, all with haptic feedback.
- **Performance modes** — Performance / Balanced / Battery saver scale motion durations and ambient effects so low-end devices stay smooth.
- **Honest weather** — OpenWeatherMap with your own API key, cached for offline use, with actionable empty/error states (never a blank widget).

## Getting started

1. Install the debug APK (built automatically by CI on every push to `main`) or build it yourself:
   ```bash
   ./gradlew assembleDebug
   ```
2. Open Naze and follow onboarding — set it as your default launcher (location permission is optional; weather also works with a manually entered city).
3. For weather, paste a free [OpenWeatherMap](https://openweathermap.org/api) API key in **Settings → Weather**.

## Tech

Kotlin, Jetpack Compose (Material 3), DataStore Preferences, Downloadable Fonts (Space Grotesk + Inter). No heavy dependencies: no DI framework, no image loading library, no networking library beyond `HttpURLConnection`.

See [DESIGN.md](DESIGN.md) for the design system, motion rules and the engineering decisions behind the 0.2.0 overhaul.

## License

See the repository's license file.
