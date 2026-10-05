package com.naze.launcher.core

/**
 * System-level intents the launcher can trigger. Every one of these is wired to a
 * REAL Android capability — there are deliberately no entries here that would render
 * as UI but do nothing:
 *
 *  WIFI_PANEL        — Settings.Panel.ACTION_INTERNET_CONNECTIVITY (Q+) / Wi-Fi settings (pre-Q)
 *  BLUETOOTH_SETTINGS — system Bluetooth settings page
 *  WALLPAPER_PICKER  — system wallpaper chooser (ACTION_SET_WALLPAPER)
 *  APP_INFO          — per-app details page (ACTION_APPLICATION_DETAILS_SETTINGS)
 */
enum class SystemIntent { WIFI_PANEL, BLUETOOTH_SETTINGS, WALLPAPER_PICKER, APP_INFO }
