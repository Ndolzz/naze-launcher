package com.naze.launcher.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.naze.launcher.appdrawer.AppDrawerScreen
import com.naze.launcher.appdrawer.AppSortMode
import com.naze.launcher.clock.ClockView
import com.naze.launcher.dock.FavoriteDock
import com.naze.launcher.gestures.GestureAction
import com.naze.launcher.gestures.homeGestures
import com.naze.launcher.search.QuickSearchScreen
import com.naze.launcher.theme.animateAmbience
import com.naze.launcher.weather.WeatherSummaryView

private enum class Screen { HOME, APP_DRAWER, SEARCH }

/**
 * Settings has its own top-level Activity (see MainActivity) rather than being a
 * Screen case here, since Android Settings is conventionally a separate task the
 * system back-gesture can dismiss independently of the home screen.
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onLockScreen: () -> Unit,
    onOpenNazeAi: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var screen by remember { mutableStateOf(Screen.HOME) }
    val ambience = animateAmbience(state.ambience, state.settings.reduceAnimations)

    val handleGesture: (GestureAction) -> Unit = { action ->
        when (action) {
            GestureAction.OPEN_APP_DRAWER -> screen = Screen.APP_DRAWER
            GestureAction.OPEN_SEARCH -> screen = Screen.SEARCH
            GestureAction.LOCK_SCREEN -> onLockScreen()
            GestureAction.OPEN_NAZE_AI -> onOpenNazeAi()
            GestureAction.NEXT_PAGE, GestureAction.PREVIOUS_PAGE, GestureAction.NONE -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(ambience.backgroundTop, ambience.backgroundBottom)))
    ) {
        when (screen) {
            Screen.HOME -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .homeGestures(state.settings.gestureBindings, handleGesture),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().weight(1f).padding(top = 72.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    ClockView(
                        use24Hour = state.settings.clock24Hour,
                        showSeconds = state.settings.clockShowSeconds,
                        sizeScale = state.settings.clockSizeScale,
                        textColor = ambience.onBackground
                    )
                    WeatherSummaryView(
                        result = state.weather,
                        useFahrenheit = state.settings.tempUnitFahrenheit,
                        textColor = ambience.onBackground,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }

                FavoriteDock(dockApps = state.dockApps, onAppClick = viewModel::launchApp)

                Text(
                    text = "Swipe Up",
                    color = ambience.onBackground.copy(alpha = 0.4f),
                    modifier = Modifier.padding(bottom = 18.dp)
                )
            }

            Screen.APP_DRAWER -> AppDrawerScreen(
                apps = state.allApps,
                recentPackageNames = state.recentPackages,
                sortMode = AppSortMode.NAME,
                onAppClick = { viewModel.launchApp(it); screen = Screen.HOME },
                onDismiss = { screen = Screen.HOME },
                backgroundColor = ambience.backgroundBottom,
                textColor = ambience.onBackground
            )

            Screen.SEARCH -> QuickSearchScreen(
                apps = state.allApps,
                onAppClick = { viewModel.launchApp(it); screen = Screen.HOME },
                onDismiss = { screen = Screen.HOME },
                backgroundColor = ambience.backgroundBottom,
                textColor = ambience.onBackground
            )
        }
    }
}
