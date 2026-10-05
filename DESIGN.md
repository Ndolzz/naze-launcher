# NAZE DESIGN SYSTEM

Internal documentation, rewritten for 0.3.0 — **Naze Lock first**. Every screen in the
launcher is built from the tokens and rules below; no screen has a private style.

---

## 1. Naze Lock

The first thing you see when you wake the phone, and the screen that sets the tone for
everything after it: one large typographic clock, one date line, two real shortcuts.

### 1.1 What it is — and what it is not

Android does not let a launcher replace the system lock screen, and Naze does not pretend
otherwise. Naze Lock is a **glanceable clock screen** (`lock/LockScreenActivity`) declared
`showWhenLocked` + `turnScreenOn`, so it can appear *over* the system keyguard.

- It holds **no credentials**. There is no in-app PIN pad. "Swipe up to unlock" calls
  `KeyguardManager.requestDismissKeyguard`, so the user's real PIN / pattern / biometric
  is what gates access. After a successful system unlock, Naze opens Home.
- It never claims to secure anything. The web prototype's PIN pad and sample
  notifications were mock-ups and are intentionally **not** ported (see §9, honesty rules).

### 1.2 Layout (top → bottom)

1. **Brand lockup** — gradient `N` mark (28 dp, 9 dp radius) + `NAZE` wordmark
   (display font Medium, 14 sp, +6 sp tracking). Right-aligned **battery chip**
   (level, "charging" when plugged in).
2. **Clock** — one of three styles (§1.3).
3. **Date** — `EEEE, d MMMM` in the device locale (Inter Medium 17 sp, 80 % alpha);
   `AM/PM` appended in 12-hour mode.
4. **Bottom row** — flashlight button · unlock affordance · camera button.

No hardcoded pixel positions: `statusBarsPadding` / `navigationBarsPadding` + a weighted
spacer between the date and the bottom row.

### 1.3 Clock styles (`LockClockStyle`)

| Style | Description | Ticks |
|---|---|---|
| **STACK** (default) | Hours over minutes, Space Grotesk Medium 148 sp, tabular figures, −4 sp tracking. Minutes carry the **brand gradient**; hours use `onBackground`. | once a minute |
| **ANALOG** | 236 dp dial, 12 ticks (quarters heavier), hour hand in `onBackground`, minute hand in the brand gradient, optional second hand in brand pink. | 1 s (60 s in Battery saver) |
| **TERMINAL** | Monospace `naze@lock ~ $ date` prompt, `HH:mm:ss` at 52 sp, blinking gradient cursor. | 1 s (60 s in Battery saver) |

Tap the clock to cycle styles; the choice is persisted (`lock_clock_style`) and can also
be set in **Settings → Clock → Naze Lock clock**.

### 1.4 Data and actions — all real

| Element | Source / behaviour |
|---|---|
| Time, date | `System.currentTimeMillis()` + device locale / 12–24 h setting |
| Battery chip | `DeviceStatusMonitor` (sticky `ACTION_BATTERY_CHANGED`) |
| Flashlight | `CameraManager.setTorchMode` via `DeviceStatusMonitor.toggleTorch()`; active state is the brand gradient |
| Camera | `INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE` while the keyguard is locked, the normal still-image intent otherwise |
| Unlock | swipe up ≥ 120 dp **or** tap the unlock affordance (accessible path) → `requestDismissKeyguard` → `MainActivity` |
| Back | closes Naze Lock (default activity back handling) |

### 1.5 Ambience, motion and performance

- Background is the same **time-of-day ambience** as Home (`ThemeEngine.resolve` with
  time only — weather/temperature are neutral on the lock screen), so the lock screen and
  Home never disagree. Light ambiences stay light; Naze Lock is not forced dark.
- Two static accent circles (10 % / 7 % alpha) add depth; drawn only when
  `PerformanceMode.showsAmbientGlow`.
- The terminal cursor blinks only when `PerformanceMode.animatesAmbient` (BALANCED).
- In **BATTERY_SAVER** the screen never ticks seconds and shows no second hand.

### 1.6 Entry points (current)

- Gesture action **Open Naze Lock** (`GestureAction.OPEN_NAZE_LOCK`) — bindable to any
  trigger in Settings → Gestures. No default binding, so existing setups are unchanged.
- **Settings → Clock → Preview Naze Lock.**

### 1.7 Not built yet (deliberately)

- **Automatic appearance on screen-on or while charging** — the honest mechanism is a
  `DreamService` (screensaver); not included until it can be tested on a device.
- **Notifications** — would need an opt-in `NotificationListenerService`. Until then the
  lock screen shows no notification cards rather than sample ones.
- **Weather line** — to be reused from `WeatherRepository`'s cache (no extra network call).

---

## 2. Identity

- **Name / wordmark**: `NAZE` — display font, wide letter-spacing (6–7 sp), used on the
  lock screen, home, onboarding and settings. Never replaced by generic branding.
- **Brand gradient**: blue `#4F7CFF` → violet `#8B5CF6` → pink `#EC4899`. It is an
  *accent*: the lock-screen minutes, the `N` mark and active states only.
- **Color philosophy**: deep blue / electric blue / blue-purple as accent, never a flood.
  Backgrounds are near-black navy at night; color appears in the clock, chips, active
  states and ambient glow.

## 3. Color (ThemeEngine)

| Ambience | Background | Character |
|---|---|---|
| MORNING | cool light blue | bright, airy light mode |
| AFTERNOON | light blue | clean daylight |
| EVENING | blue-purple dark | dusk |
| NIGHT | deep navy | primary polished dark mode |

Derived tokens (on `Ambience`): `surface` = onBackground @ 6 %, `surfaceHigh` @ 10 %,
`outline` @ 14 %. Pure black is never used as a background — depth comes from surface
layers, opacity and spacing. Theme changes cross-fade over ~500 ms (`NazeMotion.ambience`).

## 4. Typography

- **Display**: Space Grotesk (clock, wordmark, section titles), tabular figures (`tnum`)
  so clock digits never jitter. Only Light and Medium are loaded.
- **UI**: Inter (labels, values, body). **Mono**: platform monospace, used only by the
  Terminal lock clock.
- Fonts load via Google Fonts *Downloadable Fonts* with `FontFamily.SansSerif` fallback,
  so first paint never blocks on the network.
- Hierarchy by weight and letter-spacing, not extra families: clock (Light/Medium, 96–148 sp)
  → date/section labels (Medium, 11–17 sp, tracked) → values (Medium 14–15 sp) → hints (alpha-reduced).

## 5. Motion

One source of truth: `core/NazeMotion.kt`.

| Class | Duration | Used for |
|---|---|---|
| micro | ~130 ms | press scale, haptic feedback |
| normal | ~210 ms | sheets, drawer, search, fades |
| large | ~300 ms | ambience cross-fade, full-screen transitions |

All durations scale by `PerformanceMode.motionScale`. Springs for sheets and press
feedback, tweens elsewhere. Motion is fast and intentional: no decorative loops
(the Terminal cursor is the single, mode-gated exception).

## 6. Performance policy

`PerformanceMode` (user-selectable, persisted):

- **PERFORMANCE** — static background, no staggered entrances, shortened motion.
- **BALANCED** (default) — drifting ambient glow, staggered drawer entrances, rolling clock digits, blinking cursor.
- **BATTERY_SAVER** — no glow layer, instant digit swaps, no seconds on the lock screen.

Effects consult flags on the mode (`animatesAmbient`, `staggersEntrances`,
`animatesClockDigits`, `showsAmbientGlow`). The UI stays fully usable with every effect off.

## 7. Layout (Home)

- Home = top context row (wordmark, battery chip, settings) / center clock + weather /
  bottom search pill + dock + hint. `statusBarsPadding`/`navigationBarsPadding` + weights everywhere.
- Drawer/search are full-screen overlays; home content scales to 0.94 and dims behind them.
- Quick actions, device info and per-app actions are bottom sheets (28 dp top radius, scrim, spring slide).
- Portrait-only (launcher-class app).

## 8. Components (`ui/`)

`pressScale`, `NazeIconButton`, `NazeChip`, `NazeLoader`, `EmptyState`, `SearchField`,
`formatBytes`. Icons: hand-drawn 24×24 / 2 dp round-stroke vectors in `NazeIcons`
(0.3.0 adds `Camera`) — one consistent family, **no emoji as UI icons**.

## 9. Honesty rules (no fake functionality)

Every visible control does exactly what it claims.

- Removed in 0.2.0: `LOCK_SCREEN` (needed device-admin to work), `OPEN_NAZE_AI`,
  `NEXT/PREVIOUS_PAGE`, the dead "Web search" row, the hardcoded `YOUR_API_KEY` weather fetch.
- 0.3.0 adds **Naze Lock**, which is *not* the removed `LOCK_SCREEN` action: it never
  locks the device and never stores a credential. It displays information and hands off to
  the real system unlock.
- The web prototype's PIN pad, fake notifications, "hidden apps" PIN and double-tap-to-sleep
  were demo mock-ups. They are not ported until they can be backed by a real mechanism
  (system keyguard, `NotificationListenerService`, an app-level lock with a stored hash, an
  accessibility/device-admin lock).
- Quick actions map to real system capabilities: torch via `CameraManager.setTorchMode`,
  Wi-Fi via `Settings.Panel`, Bluetooth/wallpaper/app-info via `Settings` intents,
  notifications via `StatusBarManager.expandNotificationsPanel` (HOME role).
- Weather states name their failure reason and offer the matching action.

## 10. Settings

Separate `SettingsActivity` (own task, dark console styling, same ambience engine).
Sections: Appearance, Clock (now including **Naze Lock clock** + preview), App Drawer,
Gestures (now including **Open Naze Lock**), Weather, About Naze. Every control maps to a
consumed preference.

## 11. Key decisions log

- **0.3.0 — Naze Lock is an activity, not a keyguard replacement.** Chosen because Android
  forbids replacing the system lock screen; unlocking delegates to `requestDismissKeyguard`.
- **0.3.0 — web prototype → native mapping.** Visual language (stacked clock with gradient
  minutes, N-mark lockup, Analog/Terminal styles, torch + camera shortcuts) is ported;
  anything that was simulated in the prototype is left out (see §9).
- **Evolve, don't rewrite**: package-per-feature structure kept; repositories, DataStore,
  weather API and widget host untouched.
- **`PerformanceMode` replaces `reduceAnimations` + `batterySaver`** booleans (0.2.0).
- **Fonts via Downloadable Fonts** rather than bundled TTFs — smaller APK, offline fallback.
- **Clock uses typography as the design** — per-digit roll transitions, breathing colon,
  auto-fit width; explicitly *not* a widget-style clock.
- **Settings as its own Activity** so the system back gesture dismisses it independently.
