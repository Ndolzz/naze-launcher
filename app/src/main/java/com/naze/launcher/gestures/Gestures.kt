package com.naze.launcher.gestures

enum class GestureAction {
    OPEN_APP_DRAWER, OPEN_SEARCH, LOCK_SCREEN, NEXT_PAGE, PREVIOUS_PAGE, OPEN_NAZE_AI, NONE
}

enum class GestureTrigger { SWIPE_UP, SWIPE_DOWN, SWIPE_LEFT, SWIPE_RIGHT, DOUBLE_TAP }

data class GestureBindings(
    val swipeUp: GestureAction = GestureAction.OPEN_APP_DRAWER,
    val swipeDown: GestureAction = GestureAction.OPEN_SEARCH,
    val doubleTap: GestureAction = GestureAction.LOCK_SCREEN,
    val swipeLeft: GestureAction = GestureAction.NEXT_PAGE,
    val swipeRight: GestureAction = GestureAction.PREVIOUS_PAGE
) {
    fun actionFor(trigger: GestureTrigger): GestureAction = when (trigger) {
        GestureTrigger.SWIPE_UP -> swipeUp
        GestureTrigger.SWIPE_DOWN -> swipeDown
        GestureTrigger.SWIPE_LEFT -> swipeLeft
        GestureTrigger.SWIPE_RIGHT -> swipeRight
        GestureTrigger.DOUBLE_TAP -> doubleTap
    }
}
