package com.naze.launcher.gestures

/**
 * Every action here is backed by a real implementation in HomeScreen/MainActivity.
 * The previous LOCK_SCREEN (needed device-admin to actually work), OPEN_NAZE_AI
 * (extension point that did nothing) and NEXT/PREVIOUS_PAGE (no page system existed)
 * were removed rather than shipped as silent no-ops — a bound gesture must always
 * do exactly what it promises.
 */
enum class GestureAction {
    OPEN_APP_DRAWER, OPEN_SEARCH, OPEN_NOTIFICATIONS, OPEN_QUICK_ACTIONS, OPEN_SETTINGS, OPEN_NAZE_LOCK, NONE;

    fun label(): String = when (this) {
        OPEN_APP_DRAWER -> "Open app drawer"
        OPEN_SEARCH -> "Open search"
        OPEN_NOTIFICATIONS -> "Expand notifications"
        OPEN_QUICK_ACTIONS -> "Open Naze menu"
        OPEN_SETTINGS -> "Open settings"
        OPEN_NAZE_LOCK -> "Open Naze Lock"
        NONE -> "Do nothing"
    }
}

enum class GestureTrigger {
    SWIPE_UP, SWIPE_DOWN, SWIPE_LEFT, SWIPE_RIGHT, DOUBLE_TAP, LONG_PRESS;

    fun label(): String = when (this) {
        SWIPE_UP -> "Swipe up"
        SWIPE_DOWN -> "Swipe down"
        SWIPE_LEFT -> "Swipe left"
        SWIPE_RIGHT -> "Swipe right"
        DOUBLE_TAP -> "Double tap"
        LONG_PRESS -> "Long press"
    }
}

data class GestureBindings(
    val swipeUp: GestureAction = GestureAction.OPEN_APP_DRAWER,
    val swipeDown: GestureAction = GestureAction.OPEN_NOTIFICATIONS,
    val doubleTap: GestureAction = GestureAction.OPEN_SEARCH,
    val swipeLeft: GestureAction = GestureAction.NONE,
    val swipeRight: GestureAction = GestureAction.NONE,
    val longPress: GestureAction = GestureAction.OPEN_QUICK_ACTIONS
) {
    fun actionFor(trigger: GestureTrigger): GestureAction = when (trigger) {
        GestureTrigger.SWIPE_UP -> swipeUp
        GestureTrigger.SWIPE_DOWN -> swipeDown
        GestureTrigger.SWIPE_LEFT -> swipeLeft
        GestureTrigger.SWIPE_RIGHT -> swipeRight
        GestureTrigger.DOUBLE_TAP -> doubleTap
        GestureTrigger.LONG_PRESS -> longPress
    }
}
