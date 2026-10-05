package com.naze.launcher.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naze.launcher.appdrawer.AppSortMode
import com.naze.launcher.core.PerformanceMode
import com.naze.launcher.gestures.GestureAction
import com.naze.launcher.gestures.GestureTrigger
import com.naze.launcher.theme.Ambience
import com.naze.launcher.theme.LocalNazeDisplayFont
import com.naze.launcher.theme.LocalNazeUiFont
import com.naze.launcher.ui.NazeIconButton
import com.naze.launcher.ui.NazeIcons
import com.naze.launcher.ui.SearchField
import com.naze.launcher.ui.pressScale

/** Action callbacks split out so the Composable stays free of ViewModel/coroutine details. */
data class SettingsActions(
    val setDynamicWeather: (Boolean) -> Unit,
    val setDynamicTemperature: (Boolean) -> Unit,
    val setDynamicTime: (Boolean) -> Unit,
    val setPerformanceMode: (PerformanceMode) -> Unit,
    val setClock24Hour: (Boolean) -> Unit,
    val setClockShowSeconds: (Boolean) -> Unit,
    val setClockSizeScale: (Float) -> Unit,
    val setLockClockStyle: (com.naze.launcher.lock.LockClockStyle) -> Unit,
    val openNazeLock: () -> Unit,
    val setDrawerSort: (AppSortMode) -> Unit,
    val setDrawerColumns: (Int) -> Unit,
    val setShowAppLabels: (Boolean) -> Unit,
    val setAutomaticLocation: (Boolean) -> Unit,
    val setFahrenheit: (Boolean) -> Unit,
    val setWeatherApiKey: (String) -> Unit,
    val setManualLocation: (name: String, lat: Double, lon: Double) -> Unit,
    val setGestureAction: (GestureTrigger, GestureAction) -> Unit
)

/**
 * Naze settings — a categorized set of cards consistent with the home screen's
 * design language. Every control here maps to a real, consumed preference: nothing
 * is decorative.
 */
@Composable
fun SettingsScreen(
    settings: AppSettings,
    ambience: Ambience,
    actions: SettingsActions,
    onBack: () -> Unit,
    versionName: String
) {
    val textColor = ambience.onBackground
    val accent = ambience.accent
    val surface = ambience.surface
    val surfaceHigh = ambience.surfaceHigh

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .imePadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NazeIconButton(
                icon = NazeIcons.Close,
                contentDescription = "Close settings",
                onClick = onBack,
                tint = textColor,
                background = surface
            )
            Spacer(Modifier.width(12.dp))
            Text(
                "NAZE SETTINGS",
                color = textColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = LocalNazeDisplayFont.current,
                letterSpacing = 1.5.sp
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 20.dp, end = 20.dp, bottom = 48.dp
            ),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            // ------------------------------------------------------ Appearance
            item {
                SettingsSection("APPEARANCE", textColor) {
                    SettingsCard(surface) {
                        SettingLabel("Performance mode", "Scales motion & ambient effects", textColor)
                        Spacer(Modifier.height(10.dp))
                        PerformanceModeSelector(settings.performanceMode, accent, textColor, surfaceHigh) {
                            actions.setPerformanceMode(it)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    SettingsCard(surface) {
                        ToggleRow(
                            "Dynamic weather theming",
                            "Background shifts with conditions",
                            settings.dynamicWeatherEnabled, accent, textColor
                        ) { actions.setDynamicWeather(it) }
                        SettingDivider(textColor)
                        ToggleRow(
                            "Dynamic temperature tint",
                            "Subtle warm/cool shift",
                            settings.dynamicTemperatureEnabled, accent, textColor
                        ) { actions.setDynamicTemperature(it) }
                        SettingDivider(textColor)
                        ToggleRow(
                            "Dynamic time of day",
                            "Light by day, deep blue by night",
                            settings.dynamicTimeEnabled, accent, textColor
                        ) { actions.setDynamicTime(it) }
                    }
                }
            }

            // ------------------------------------------------------ Clock
            item {
                SettingsSection("CLOCK", textColor) {
                    SettingsCard(surface) {
                        ToggleRow(
                            "24-hour format",
                            null,
                            settings.clock24Hour, accent, textColor
                        ) { actions.setClock24Hour(it) }
                        SettingDivider(textColor)
                        ToggleRow(
                            "Show seconds",
                            null,
                            settings.clockShowSeconds, accent, textColor
                        ) { actions.setClockShowSeconds(it) }
                        SettingDivider(textColor)
                        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                            SettingLabel("Clock size", null, textColor)
                            Slider(
                                value = settings.clockSizeScale,
                                onValueChange = { actions.setClockSizeScale(it) },
                                valueRange = 0.7f..1.4f,
                                steps = 6,
                                colors = androidx.compose.material3.SliderDefaults.colors(
                                    thumbColor = accent,
                                    activeTrackColor = accent
                                )
                            )
                            Text(
                                "${(settings.clockSizeScale * 100).toInt()}%",
                                color = textColor.copy(alpha = 0.55f),
                                fontSize = 12.sp,
                                fontFamily = LocalNazeUiFont.current
                            )
                        }
                        SettingDivider(textColor)
                        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                            SettingLabel(
                                "Naze Lock clock",
                                "Tap the clock on Naze Lock to cycle styles",
                                textColor
                            )
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                com.naze.launcher.lock.LockClockStyle.values().forEach { style ->
                                    ChoiceChip(
                                        style.label,
                                        settings.lockClockStyle == style,
                                        accent, textColor, surfaceHigh
                                    ) { actions.setLockClockStyle(style) }
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            Row {
                                ChoiceChip("Preview Naze Lock", false, accent, textColor, surfaceHigh) {
                                    actions.openNazeLock()
                                }
                            }
                        }
                    }
                }
            }

            // ------------------------------------------------------ App drawer
            item {
                SettingsSection("APP DRAWER", textColor) {
                    SettingsCard(surface) {
                        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                            SettingLabel("Default sorting", null, textColor)
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ChoiceChip("A–Z", settings.drawerSort == AppSortMode.NAME, accent, textColor, surfaceHigh) {
                                    actions.setDrawerSort(AppSortMode.NAME)
                                }
                                ChoiceChip("Recent", settings.drawerSort == AppSortMode.RECENT, accent, textColor, surfaceHigh) {
                                    actions.setDrawerSort(AppSortMode.RECENT)
                                }
                            }
                        }
                        SettingDivider(textColor)
                        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                            SettingLabel("Grid columns", null, textColor)
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ChoiceChip("4", settings.drawerColumns == 4, accent, textColor, surfaceHigh) {
                                    actions.setDrawerColumns(4)
                                }
                                ChoiceChip("5", settings.drawerColumns == 5, accent, textColor, surfaceHigh) {
                                    actions.setDrawerColumns(5)
                                }
                                ChoiceChip("6", settings.drawerColumns == 6, accent, textColor, surfaceHigh) {
                                    actions.setDrawerColumns(6)
                                }
                            }
                        }
                        SettingDivider(textColor)
                        ToggleRow(
                            "Show app labels",
                            null,
                            settings.showAppLabels, accent, textColor
                        ) { actions.setShowAppLabels(it) }
                    }
                }
            }

            // ------------------------------------------------------ Gestures
            item {
                SettingsSection("GESTURES", textColor) {
                    SettingsCard(surface) {
                        Column(Modifier.padding(vertical = 6.dp)) {
                            GestureTrigger.values().forEach { trigger ->
                                GestureRow(
                                    trigger = trigger,
                                    current = settings.gestureBindings.actionFor(trigger),
                                    accent = accent,
                                    textColor = textColor,
                                    surfaceHigh = surfaceHigh,
                                    onSelect = { actions.setGestureAction(trigger, it) }
                                )
                            }
                        }
                    }
                }
            }

            // ------------------------------------------------------ Weather
            item {
                SettingsSection("WEATHER", textColor) {
                    SettingsCard(surface) {
                        ToggleRow(
                            "Automatic location",
                            "Single GPS fix on refresh, never background tracking",
                            settings.useAutomaticLocation, accent, textColor
                        ) { actions.setAutomaticLocation(it) }
                        SettingDivider(textColor)
                        ToggleRow(
                            "Use °F instead of °C",
                            null,
                            settings.tempUnitFahrenheit, accent, textColor
                        ) { actions.setFahrenheit(it) }
                        SettingDivider(textColor)
                        ApiKeyField(
                            initial = settings.weatherApiKey.orEmpty(),
                            textColor = textColor,
                            surfaceHigh = surfaceHigh,
                            onSave = actions.setWeatherApiKey
                        )
                        SettingDivider(textColor)
                        ManualLocationFields(
                            initialName = settings.manualLocationName.orEmpty(),
                            initialLat = settings.manualLat,
                            initialLon = settings.manualLon,
                            textColor = textColor,
                            surfaceHigh = surfaceHigh,
                            accent = accent,
                            onSave = actions.setManualLocation
                        )
                    }
                }
            }

            // ------------------------------------------------------ About
            item {
                SettingsSection("ABOUT NAZE", textColor) {
                    SettingsCard(surface) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                "NAZE LAUNCHER",
                                color = textColor,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = LocalNazeDisplayFont.current,
                                letterSpacing = 2.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Version $versionName",
                                color = textColor.copy(alpha = 0.55f),
                                fontSize = 13.sp,
                                fontFamily = LocalNazeUiFont.current
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "A personal operating environment — a calm, adaptive home screen designed to feel like it came from a few years ahead, without ever wasting a frame or a milliamp to do it.",
                                color = textColor.copy(alpha = 0.65f),
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                                fontFamily = LocalNazeUiFont.current
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------- sub-components

@Composable
private fun SettingsSection(title: String, textColor: Color, content: @Composable () -> Unit) {
    Column {
        Text(
            title,
            color = textColor.copy(alpha = 0.45f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = LocalNazeUiFont.current,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        content()
    }
}

@Composable
private fun SettingsCard(surface: Color, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(surface)
            .padding(vertical = 6.dp)
    ) {
        content()
    }
}

@Composable
private fun SettingLabel(title: String, subtitle: String?, textColor: Color) {
    Text(
        title,
        color = textColor.copy(alpha = 0.9f),
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = LocalNazeUiFont.current,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
    if (subtitle != null) {
        Text(
            subtitle,
            color = textColor.copy(alpha = 0.5f),
            fontSize = 12.sp,
            fontFamily = LocalNazeUiFont.current,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun SettingDivider(textColor: Color) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .height(0.6.dp)
            .background(textColor.copy(alpha = 0.10f))
    )
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String?,
    checked: Boolean,
    accent: Color,
    textColor: Color,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                title,
                color = textColor.copy(alpha = 0.9f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = LocalNazeUiFont.current
            )
            if (subtitle != null) {
                Text(
                    subtitle,
                    color = textColor.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    fontFamily = LocalNazeUiFont.current
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedTrackColor = accent,
                checkedThumbColor = Color.White,
                uncheckedTrackColor = textColor.copy(alpha = 0.15f),
                uncheckedThumbColor = textColor.copy(alpha = 0.7f)
            )
        )
    }
}

@Composable
private fun PerformanceModeSelector(
    current: PerformanceMode,
    accent: Color,
    textColor: Color,
    surfaceHigh: Color,
    onSelect: (PerformanceMode) -> Unit
) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
        PerformanceMode.values().forEach { mode ->
            val selected = mode == current
            val interaction = remember { MutableInteractionSource() }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .pressScale(interaction, pressedScale = 0.98f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (selected) accent.copy(alpha = 0.14f) else surfaceHigh)
                    .clickable(interactionSource = interaction, indication = null) { onSelect(mode) }
                    .padding(horizontal = 14.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (selected) NazeIcons.Check else NazeIcons.Minus,
                    contentDescription = null,
                    tint = if (selected) accent else textColor.copy(alpha = 0.35f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        mode.name.lowercase().replaceFirstChar { it.uppercase() },
                        color = textColor.copy(alpha = if (selected) 1f else 0.8f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = LocalNazeUiFont.current
                    )
                    Text(
                        when (mode) {
                            PerformanceMode.PERFORMANCE -> "Static ambience, snappy motion — for low-end devices"
                            PerformanceMode.BALANCED -> "Full experience — ambient light, staggered motion"
                            PerformanceMode.BATTERY_SAVER -> "Minimal effects, maximum battery"
                        },
                        color = textColor.copy(alpha = 0.5f),
                        fontSize = 11.5.sp,
                        fontFamily = LocalNazeUiFont.current
                    )
                }
            }
        }
    }
}

@Composable
private fun ChoiceChip(
    text: String,
    selected: Boolean,
    accent: Color,
    textColor: Color,
    surfaceHigh: Color,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .pressScale(interaction, pressedScale = 0.94f)
            .clip(RoundedCornerShape(50))
            .background(if (selected) accent.copy(alpha = 0.16f) else surfaceHigh)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text,
            color = if (selected) accent else textColor.copy(alpha = 0.75f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = LocalNazeUiFont.current
        )
    }
}

@Composable
private fun GestureRow(
    trigger: GestureTrigger,
    current: GestureAction,
    accent: Color,
    textColor: Color,
    surfaceHigh: Color,
    onSelect: (GestureAction) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column(Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(surfaceHigh)
                .clickable { expanded = !expanded }
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                trigger.label(),
                color = textColor.copy(alpha = 0.9f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = LocalNazeUiFont.current,
                modifier = Modifier.weight(1f)
            )
            Text(
                current.label(),
                color = accent,
                fontSize = 12.5.sp,
                fontFamily = LocalNazeUiFont.current
            )
            Spacer(Modifier.width(6.dp))
            Icon(
                NazeIcons.ChevronDown,
                contentDescription = null,
                tint = textColor.copy(alpha = 0.5f),
                modifier = Modifier.size(15.dp)
            )
        }
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(tween(180)) + fadeIn(tween(180)),
            exit = shrinkVertically(tween(160)) + fadeOut(tween(120))
        ) {
            Column(Modifier.padding(top = 4.dp)) {
                GestureAction.values().forEach { action ->
                    val selected = action == current
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selected) accent.copy(alpha = 0.12f) else Color.Transparent)
                            .clickable {
                                onSelect(action)
                                expanded = false
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (selected) {
                            Icon(
                                NazeIcons.Check,
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(
                            action.label(),
                            color = if (selected) accent else textColor.copy(alpha = 0.75f),
                            fontSize = 13.sp,
                            fontFamily = LocalNazeUiFont.current
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ApiKeyField(
    initial: String,
    textColor: Color,
    surfaceHigh: Color,
    onSave: (String) -> Unit
) {
    var value by remember(initial) { mutableStateOf(initial) }
    Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        SettingLabel(
            "OpenWeatherMap API key",
            "Free key from openweathermap.org — stored only on this device",
            textColor
        )
        Spacer(Modifier.height(8.dp))
        SearchField(
            query = value,
            onQueryChange = {
                value = it
                onSave(it)
            },
            hint = "Paste your API key",
            textColor = textColor,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun ManualLocationFields(
    initialName: String,
    initialLat: Double?,
    initialLon: Double?,
    textColor: Color,
    surfaceHigh: Color,
    accent: Color,
    onSave: (name: String, lat: Double, lon: Double) -> Unit
) {
    var name by remember(initialName) { mutableStateOf(initialName) }
    var lat by remember(initialLat) {
        mutableStateOf(initialLat?.toString().orEmpty())
    }
    var lon by remember(initialLon) {
        mutableStateOf(initialLon?.toString().orEmpty())
    }
    val latValid = lat.toDoubleOrNull() != null && (lat.toDoubleOrNull()!! in -90.0..90.0)
    val lonValid = lon.toDoubleOrNull() != null && (lon.toDoubleOrNull()!! in -180.0..180.0)
    val canSave = name.isNotBlank() && latValid && lonValid

    Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        SettingLabel(
            "Manual location",
            "Used when automatic location is off or unavailable",
            textColor
        )
        Spacer(Modifier.height(8.dp))
        SearchField(
            query = name,
            onQueryChange = { name = it },
            hint = "City name",
            textColor = textColor,
            fontSize = 14.sp
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.weight(1f)) {
                SearchField(
                    query = lat,
                    onQueryChange = { lat = it },
                    hint = "Latitude",
                    textColor = if (lat.isEmpty() || latValid) textColor else textColor.copy(alpha = 0.4f),
                    fontSize = 14.sp
                )
            }
            Box(Modifier.weight(1f)) {
                SearchField(
                    query = lon,
                    onQueryChange = { lon = it },
                    hint = "Longitude",
                    textColor = if (lon.isEmpty() || lonValid) textColor else textColor.copy(alpha = 0.4f),
                    fontSize = 14.sp
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        val interaction = remember { MutableInteractionSource() }
        Row(
            modifier = Modifier
                .pressScale(interaction)
                .clip(RoundedCornerShape(50))
                .background(if (canSave) accent.copy(alpha = 0.16f) else surfaceHigh)
                .clickable(
                    interactionSource = interaction,
                    indication = null,
                    enabled = canSave
                ) {
                    onSave(name.trim(), lat.toDouble(), lon.toDouble())
                }
                .padding(horizontal = 18.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Save location",
                color = if (canSave) accent else textColor.copy(alpha = 0.45f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = LocalNazeUiFont.current
            )
        }
    }
}
