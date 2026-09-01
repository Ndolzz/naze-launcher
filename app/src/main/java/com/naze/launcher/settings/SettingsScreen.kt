package com.naze.launcher.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class SettingsToggle(val label: String, val checked: Boolean, val onToggle: (Boolean) -> Unit)
private data class SettingsSection(val title: String, val toggles: List<SettingsToggle>)

/**
 * A single flat list of grouped toggles. Deliberately simple (Compose Switch rows) —
 * the spec asks for a "professional" settings page, not a bespoke design system; this
 * reads clean and matches stock Android settings conventions users already know.
 */
@Composable
fun SettingsScreen(
    settings: AppSettings,
    onSettingsChange: SettingsActions
) {
    val sections = listOf(
        SettingsSection(
            "Appearance",
            listOf(
                SettingsToggle("Dynamic Weather", settings.dynamicWeatherEnabled, onSettingsChange.setDynamicWeather),
                SettingsToggle("Dynamic Temperature", settings.dynamicTemperatureEnabled, onSettingsChange.setDynamicTemperature),
                SettingsToggle("Dynamic Time", settings.dynamicTimeEnabled, onSettingsChange.setDynamicTime)
            )
        ),
        SettingsSection(
            "Clock",
            listOf(
                SettingsToggle("24-hour format", settings.clock24Hour, onSettingsChange.setClock24Hour),
                SettingsToggle("Show seconds", settings.clockShowSeconds, onSettingsChange.setClockShowSeconds)
            )
        ),
        SettingsSection(
            "Weather",
            listOf(
                SettingsToggle("Automatic location", settings.useAutomaticLocation, onSettingsChange.setAutomaticLocation),
                SettingsToggle("Use °F instead of °C", settings.tempUnitFahrenheit, onSettingsChange.setFahrenheit)
            )
        ),
        SettingsSection(
            "Performance",
            listOf(
                SettingsToggle("Reduce animations", settings.reduceAnimations, onSettingsChange.setReduceAnimations),
                SettingsToggle("Battery saver mode", settings.batterySaver, onSettingsChange.setBatterySaver)
            )
        )
    )

    LazyColumn(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
        items(sections) { section ->
            Text(
                text = section.title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)
            )
            section.toggles.forEach { toggle ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(toggle.label, fontSize = 15.sp)
                    Switch(checked = toggle.checked, onCheckedChange = toggle.onToggle)
                }
            }
            Divider()
        }
    }
}

/** Action callbacks split out so the Composable stays free of ViewModel/coroutine details. */
data class SettingsActions(
    val setDynamicWeather: (Boolean) -> Unit,
    val setDynamicTemperature: (Boolean) -> Unit,
    val setDynamicTime: (Boolean) -> Unit,
    val setClock24Hour: (Boolean) -> Unit,
    val setClockShowSeconds: (Boolean) -> Unit,
    val setAutomaticLocation: (Boolean) -> Unit,
    val setFahrenheit: (Boolean) -> Unit,
    val setReduceAnimations: (Boolean) -> Unit,
    val setBatterySaver: (Boolean) -> Unit
)
