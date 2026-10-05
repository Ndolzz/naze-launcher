package com.naze.launcher.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import android.widget.Toast
import com.naze.launcher.appdrawer.AppInfo
import com.naze.launcher.appdrawer.AppDrawerScreen
import com.naze.launcher.appdrawer.AppSortMode
import com.naze.launcher.clock.ClockView
import com.naze.launcher.core.BatteryStatus
import com.naze.launcher.core.DeviceSnapshot
import com.naze.launcher.core.NazeMotion
import com.naze.launcher.core.SystemIntent
import com.naze.launcher.dock.FavoriteDock
import com.naze.launcher.gestures.GestureAction
import com.naze.launcher.gestures.homeGestures
import com.naze.launcher.search.QuickSearchScreen
import com.naze.launcher.search.SearchAction
import com.naze.launcher.theme.LocalNazeDisplayFont
import com.naze.launcher.theme.LocalNazeUiFont
import com.naze.launcher.theme.animateAmbience
import com.naze.launcher.ui.AmbientBackground
import com.naze.launcher.ui.NazeChip
import com.naze.launcher.ui.NazeIconButton
import com.naze.launcher.ui.NazeIcons
import com.naze.launcher.ui.NazeLoader
import com.naze.launcher.ui.formatBytes
import com.naze.launcher.ui.pressScale
import com.naze.launcher.weather.WeatherSummaryView
import java.util.Locale
import androidx.compose.ui.graphics.vector.ImageVector

private enum class Overlay { DRAWER, SEARCH }
private enum class Sheet { NONE, QUICK_ACTIONS, DEVICE_INFO }

/**
 * The Naze home screen — a calm personal dashboard:
 *
 *   top     NAZE wordmark · battery chip · settings
 *   center  the clock (the centerpiece) + honest weather line
 *   bottom  search pill · favorite dock · swipe hint
 *
 * Drawer and search render as full-screen overlays behind which the home content
 * scales down and dims; quick actions, device info and per-app actions live in
 * bottom sheets. Settings runs in its own Activity (see MainActivity) so the system
 * back gesture dismisses it independently of the home screen.
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenSettings: () -> Unit,
    onExpandNotifications: () -> Unit,
    onSystemAction: (SystemIntent, String?) -> Unit,
    onOpenLock: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val battery by viewModel.battery.collectAsState()
    val torchOn by viewModel.torchOn.collectAsState()
    val haptics = LocalHapticFeedback.current
    val context = LocalContext.current

    val settings = state.settings
    val mode = settings.performanceMode
    val ambience = animateAmbience(state.ambience, mode)
    val sheetDuration = NazeMotion.normal(mode)

    var overlay by remember { mutableStateOf<Overlay?>(null) }
    var sheet by remember { mutableStateOf(Sheet.NONE) }
    var editApp by remember { mutableStateOf<AppInfo?>(null) }

    val handleGesture: (GestureAction) -> Unit = { action ->
        if (overlay == null && sheet == Sheet.NONE && editApp == null) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            when (action) {
                GestureAction.OPEN_APP_DRAWER -> overlay = Overlay.DRAWER
                GestureAction.OPEN_SEARCH -> overlay = Overlay.SEARCH
                GestureAction.OPEN_NOTIFICATIONS -> onExpandNotifications()
                GestureAction.OPEN_QUICK_ACTIONS -> sheet = Sheet.QUICK_ACTIONS
                GestureAction.OPEN_SETTINGS -> onOpenSettings()
                GestureAction.OPEN_NAZE_LOCK -> onOpenLock()
                GestureAction.NONE -> Unit
            }
        }
    }

    BackHandler(enabled = overlay != null || sheet != Sheet.NONE || editApp != null) 

{
        when {
            overlay != null -> overlay = null
            editApp != null -> editApp = null
            else -> sheet = Sheet.NONE
        }
    }

    val homeScale by animateFloatAsState(
        targetValue = if (overlay == null) 1f else 0.94f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumLow,
            stiffness = 700f
        ),
        label = "homeScale"
    )
    val homeAlpha by animateFloatAsState(
        targetValue = if (overlay == null) 1f else 0.55f,
        animationSpec = tween(180),
        label = "homeAlpha"
    )

    val searchActions = listOf(
        SearchAction(
            title = "Toggle flashlight",
            subtitle = if (torchOn) "Turn the torch off" else "Turn the torch on",
            icon = NazeIcons.Flash,
            run = { overlay = null; viewModel.toggleTorch() }
        ),
        SearchAction(
            title = "Wi-Fi & internet",
            subtitle = "Open the network panel",
            icon = NazeIcons.Wifi,
            run = { overlay = null; onSystemAction(SystemIntent.WIFI_PANEL, null) }
        ),
        SearchAction(
            title = "Bluetooth",
            subtitle = "Open Bluetooth settings",
            icon = NazeIcons.Bluetooth,
            run = { overlay = null; onSystemAction(SystemIntent.BLUETOOTH_SETTINGS, null) }
        ),
        SearchAction(
            title = "Change wallpaper",
            subtitle = "Open the wallpaper picker",
            icon = NazeIcons.Wallpaper,
            run = { overlay = null; onSystemAction(SystemIntent.WALLPAPER_PICKER, null) }
        ),
        SearchAction(
            title = "Expand notifications",
            subtitle = "Open the notification shade",
            icon = NazeIcons.Bell,
            run = { overlay = null; onExpandNotifications() }
        ),
        SearchAction(
            title = "Launcher settings",
            subtitle = "Customize Naze",
            icon = NazeIcons.Settings
,

            run = { overlay = null; onOpenSettings() }

        )
    )

    Box(Modifier.fillMaxSize()) {
        AmbientBackground(ambience = ambience, mode = mode, modifier = Modifier.fillMaxSize())

        // ── Home content ────────────────────────────────────────────────────────
        Column(
            modifier = Modifier.fillMaxSize().graphicsLayer {
                    scaleX = homeScale
                    scaleY = homeScale
                    alpha = homeAlpha
                }
                .statusBarsPadding()
                .navigationBarsPadding()
                .homeGestures(settings.gestureBindings, handleGesture)
        ) {
            HomeTopRow(
                battery = battery,
                textColor = ambience.onBackground,
                accent = ambience.accent,
                onBatteryClick = { sheet = Sheet.DEVICE_INFO },
                onSettingsClick = onOpenSettings
            )

            Column(
                modifier = Modifier.weight(1f).fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                ClockView(
                    use24Hour = settings.clock24Hour,
                    showSeconds = settings.clockShowSeconds,
                    sizeScale = settings.clockSizeScale,
                    textColor = ambience.onBackground,
                    performanceMode = mode
                )
                WeatherSummaryView(
                    result = state.weather,
                    useFahrenheit = settings.tempUnitFahrenheit,
                    textColor = ambience.onBackground,
                    accent = ambience.accent,
                    onRetry = { viewModel.refreshWeather(forceRefresh = true) },
                    onOpenSettings = onOpenSettings,
             
  
     modifier = Modifier.padding(top = 14.dp)
          
      )
            }

            SearchPill(
                textColor = ambience.onBackground,
                accent = ambience.accent,
                onClick = { overlay = Overlay.SEARCH }
            )

            if (state.dockApps.isEmpty()) {
                NazeChip(
                    text = "Pin favorite apps from the drawer",
                    icon = NazeIcons.Plus,
                    tint = ambience.onBackground.copy(alpha = 0.65f),
                    background = ambience.onBackground.copy(alpha = 0.06f),
                    onClick = { overlay = Overlay.DRAWER },
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 12.dp)
                )
            } else {
                FavoriteDock(
                    dockApps = state.dockApps,
                    onAppClick = viewModel::launchApp,
                    onAppLongPress = { app ->
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        editApp = app
                    },
                    backdrop = ambience.onBackground.copy(alpha = 0.07f)
                )
            }

            Text(
                text = "Swipe up for apps",
                color = ambience.onBackground.copy(alpha = 0.38f),
                fontSize = 11.sp,
                fontFamily = LocalNazeUiFont.current,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(bottom = 14.dp, top = 6.dp)
            )
        }

        // ── App drawer overlay ──────────────────────────────────────────────────
        AnimatedVisibility(
            visible = overlay == Overlay.DRAWER,
            enter = fadeIn(tween(sheetDuration)) +
                slideInVertically(tween(sheetDuration)) { it / 12 },
            exit = fadeOut(tween((sheetDuration * 0.6f).toInt())) +
                slideOutVertically(tween((sheetDuration * 0.6f).toInt())) { it /
 16 },
            modifier = Modifier.fillMaxSize()
    
    ) {
            AppDrawerScreen(
                apps = state.allApps,
                recentPackageNames = state.recentPackages,
                sortMode = settings.drawerSort,
                showLabels = settings.showAppLabels,
                columns = settings.drawerColumns,
                performanceMode = mode,
                onAppClick = {
                    viewModel.launchApp(it)
                    overlay = null
                },
                onAppLongPress = { app ->
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    editApp = app
                },
                onSortModeChange = viewModel::setDrawerSort,
                onDismiss = { overlay = null },
                scrimColor = ambience.backgroundBottom.copy(alpha = 0.94f),
                textColor = ambience.onBackground,
                accent = ambience.accent
            )
        }

        // ── Search overlay ──────────────────────────────────────────────────────
        AnimatedVisibility(
            visible = overlay == Overlay.SEARCH,
            enter = fadeIn(tween(sheetDuration)),
            exit = fadeOut(tween((sheetDuration * 0.6f).toInt())),
            modifier = Modifier.fillMaxSize()
        ) {
            QuickSearchScreen(
                apps = state.allApps,
                actions = searchActions,
                onAppClick = {
                    viewModel.launchApp(it)
                    overlay = null
                },
                onDismiss = { overlay = null },
                backgroundColor = ambience.backgroundBottom.copy(alpha = 0.94f),
                textColor = ambience.onBackground,
                accent = ambience.accent
            )
        }

        // ── Sheets ──────────────────────────────────────────────────────────────
        // Always composed so closing plays the slide/fade exit animation; the
        // content simply switches between the two sheet types.
        NazeSheet(
  
          visible = sheet != Sheet.NONE,
            duration = sheetDuration,
            sheetColor = ambience.backgroundBottom,
            scrimColor = Color.Black.copy(alpha = 0.45f),
            onDismiss = { sheet = Sheet.NONE }
        ) {
            when (sheet) {
                Sheet.QUICK_ACTIONS -> QuickActionsContent(
                    torchOn = torchOn,
                    textColor = ambience.onBackground,
                    accent = ambience.accent,
                    onToggleTorch = {
                        if (!viewModel.toggleTorch()) {
                            Toast.makeText(context, "No flash unit on this device", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onSystemAction = onSystemAction,
                    onOpenSettings = onOpenSettings,
                    onExpandNotifications = onExpandNotifications,
                    onOpenDeviceInfo = { sheet = Sheet.DEVICE_INFO }
                )
                Sheet.DEVICE_INFO -> DeviceInfoContent(
                    loadSnapshot = viewModel::deviceSnapshot,
                    textColor = ambience.onBackground,
                    accent = ambience.accent
                )
                Sheet.NONE -> Spacer(Modifier.height(0.dp))
            }
        }

        NazeSheet(
            visible = editApp != null,
            duration = sheetDuration,
            sheetColor = ambience.backgroundBottom,
            scrimColor = Color.Black.copy(alpha = 0.45f),
            onDismiss = { editApp = null }
        ) {
            val app = editApp
            if (app != null) {
                AppActionsContent(
                    app = app,
                    inDock = app.packageName in settings.dockPackages,
                    textColor = ambience.onBackground,
                    accent = ambience.accent,
                    onDockToggle = {
                        if (app.packageName in settings.dockPackages) {
                     
       viewModel.removeFromDock(app)
                        } else {
                            viewModel.addToDock(app)
                        }
                        editApp = null
                    },
                    onAppInfo = {
                        editApp = null
                        onSystemAction(SystemIntent.APP_INFO, app.packageName)
                    }
                )
            }
        }
    }
}

// ── Top row ─────────────────────────────────────────────────────────────────────

@Composable
private fun HomeTopRow(
    battery: BatteryStatus?,
    textColor: Color,
    accent: Color,
    onBatteryClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "NAZE",
            color = textColor.copy(alpha = 0.92f),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = LocalNazeDisplayFont.current,
            letterSpacing = 7.sp
        )
        Spacer(Modifier.weight(1f))
        if (battery != null && battery.levelPercent >= 0) {
            NazeChip(
                text = "${battery.levelPercent}%",
                icon = NazeIcons.Battery,
                tint = if (battery.charging) accent else textColor.copy(alpha = 0.85f),
                background = textColor.copy(alpha = 0.07f),
                onClick = onBatteryClick,
                fontSize = 12.sp
            )
            Spacer(Modifier.width(10.dp))
        }
        NazeIconButton(
            icon = NazeIcons.Settings,
            contentDescription = "Open Naze settings",
            onClick = onSettingsClick,
            tint = textColor.copy(alpha = 0.85f),
            background = textColor.copy(alpha = 0.07f),
            size = 38.dp,
  
    
      iconSize = 17.dp
        )
    }
}

// ── Search pill ─────────────────────────────────────────────────────────────────

@Composable
private fun SearchPill(
    textColor: Color,
    accent: Color,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 24.dp)
            .pressScale(interaction, pressedScale = 0.97f)
            .clip(RoundedCornerShape(50))
            .background(textColor.copy(alpha = 0.08f))
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            NazeIcons.Search,
            contentDescription = null,
            tint = textColor.copy(alpha = 0.55f),
            modifier = Modifier.size(17.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = "Search apps and actions",
            color = textColor.copy(alpha = 0.55f),
            fontSize = 15.sp,
            fontFamily = LocalNazeUiFont.current
        )
        Spacer(Modifier.weight(1f))
        Box(
            Modifier
                .size(width = 3.dp, height = 16.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(accent.copy(alpha = 0.8f))
        )
    }
}

// ── Bottom sheet container ──────────────────────────────────────────────────────

@Composable
private fun BoxScope.NazeSheet(
    visible: Boolean,
    duration: Int,
    sheetColor: Color,
    scrimColor: Color,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(duration)),
        exit = fadeOut(tween((duration * 0.6f).toInt())),
        modifier = Modifier.fillMaxSize()
    ) {
        val interaction = remember { MutableInteractionSource() }
    
    Box(
            Modifier.fillMaxSize()
            .background(scrimColor)
                .clickable(interactionSource = interaction, indication = null, onClick = onDismiss)
        )
    }
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            animationSpec = spring(dampingRatio = 0.95f, stiffness = 420f)
        ) { it },
        exit = slideOutVertically(tween(duration)) { it },
        modifier = Modifier
            .align(Alignment.BottomCenter).fillMaxWidth()
    ) {
        val interaction = remember { MutableInteractionSource() }
        Column(
            modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(sheetColor)
                // Consume stray taps on the sheet body so they don't fall through
                // to the dismiss scrim underneath.
                .clickable(interactionSource = interaction, indication = null, onClick = {})
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            content = { content() }
        )
    }
}

// ── Quick actions sheet ─────────────────────────────────────────────────────────

@Composable
private fun QuickActionsContent(
    torchOn: Boolean,
    textColor: Color,
    accent: Color,
    onToggleTorch: () -> Unit,
    onSystemAction: (SystemIntent, String?) -> Unit,
    onOpenSettings: () -> Unit,
    onExpandNotifications: () -> Unit,
    onOpenDeviceInfo: () -> Unit
) {
    SheetTitle("Quick actions", textColor)

    val tiles = listOf(
        QuickActionTileData(NazeIcons.Flash, "Flashlight", torchOn, onToggleTorch),
        QuickActionTileData(NazeIcons.Wifi, "Wi-Fi", active = false) { onSystemAction(SystemIntent.WIFI_PANEL, null) },
        QuickActionTileData(NazeIcons.Bluetooth, "Bluetooth", active = false) { onSystemAction(SystemIntent.BLUETOOTH_SETTINGS, null) }
,
        QuickActionTileData(NazeIcons.Bell, "Notifs", active = false, onClick = onExpandNotifications),
        QuickActionTileData(NazeIcons.Wallpaper, "Wallpaper", active = false) { onSystemAction(SystemIntent.WALLPAPER_PICKER, null) },
        QuickActionTileData(NazeIcons.Info, "Device", active = false, onClick = onOpenDeviceInfo),
        QuickActionTileData(NazeIcons.Settings, "Settings", active = false, onClick = onOpenSettings)
    )

    tiles.chunked(3).forEach { rowTiles ->
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            rowTiles.forEach { tile ->
                QuickActionTile(tile, textColor, accent)
            }
        }
    }
}

private data class QuickActionTileData(
    val icon: androidx.compose.ui.graphics.ImageVector,
    val label: String,
    val active: Boolean,
    val onClick: () -> Unit
)

@Composable
private fun QuickActionTile(
    tile: QuickActionTileData,
    textColor: Color,
    accent: Color
) {
    val interaction = remember { MutableInteractionSource() }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(96.dp)
            .pressScale(interaction, pressedScale = 0.92f)
            .clickable(interactionSource = interaction, indication = null, onClick = tile.onClick)
            .padding(vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier.size(54.dp).clip(CircleShape).background(
                    if (tile.active) accent.copy(alpha = 0.22f)
                    else textColor.copy(alpha = 0.08f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                tile.icon,
                contentDescription = tile.label,
                tint = if (tile.active) accent else textColor.copy(alpha = 0.85f),
                modifier = Modifier.size(22.dp)
        
    )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = tile.label,
            color = if (tile.active) accent else textColor.copy(alpha = 0.75f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = LocalNazeUiFont.current,
            maxLines = 1
        )
    }
}

// ── Device info sheet ───────────────────────────────────────────────────────────

@Composable
private fun DeviceInfoContent(
    loadSnapshot: suspend () -> DeviceSnapshot,
    textColor: Color,
    accent: Color
) {
    SheetTitle("Device", textColor)

    var snapshot by remember { mutableStateOf<DeviceSnapshot?>(null) }
    LaunchedEffect(Unit) { snapshot = loadSnapshot() }

    if (snapshot == null) {
        Row(
            modifier = Modifier.padding(top = 28.dp, bottom = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NazeLoader(color = accent, dotSize = 5.dp)
            Spacer(Modifier.width(10.dp))
            Text(
                "Reading device status",
                color = textColor.copy(alpha = 0.6f),
                fontSize = 14.sp,
                fontFamily = LocalNazeUiFont.current
            )
        }
        return
    }

    val s = snapshot!!
    Column(Modifier.padding(top = 14.dp, bottom = 8.dp)) {
        val batteryLine = s.battery?.let { b ->
            if (b.levelPercent >= 0) {
                if (b.charging) "${b.levelPercent}% · charging" else "${b.levelPercent}%"
            } else null
        } ?: "—"
        val tempLine = s.battery?.temperatureCelsius
            ?.let { String.format(Locale.US, "%.1f°C", it) } ?: "—"

        InfoRow("Battery", batteryLine, textColor)
        InfoRow("Battery temp", tempLine, textColor)
        InfoRow("Memory", "${formatBytes(s.ramAvailableBytes)} free of ${formatBytes(s.ramTotalBytes)}", textColor)
        InfoRow("Storage", "${formatBytes(s.storageAvailableBytes)} free of ${formatBytes(s.storageTotalBytes)}", textColor)
        InfoRow("System", s.androidVersion, textColor)
        InfoRow("Device", s.deviceModel, textColor)
    }
}

@Composable
private fun InfoRow(label: String, value: String, textColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = textColor.copy(alpha = 0.55f),
            fontSize = 14.sp,
            fontFamily = LocalNazeUiFont.current
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = value,
            color = textColor.copy(alpha = 0.9f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = LocalNazeUiFont.current,
            maxLines = 1
        )
    }
}

// ── Per-app actions sheet (dock pin/unpin + app info) ───────────────────────────

@Composable
private fun AppActionsContent(
    app: AppInfo,
    inDock: Boolean,
    textColor: Color,
    accent: Color,
    onDockToggle: () -> Unit,
    onAppInfo: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(bottom = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            bitmap = app.icon.toBitmap(96, 96).asImageBitmap(),
            contentDescription = null,
            modifier = Modifier.size(46.dp)
        )
        Spacer(Modifier.width(14.dp))
        Column {
            Text(
                text = app.label,
                color = textColor.copy(alpha = 0.95f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = LocalNazeUiFont.current,
                maxLines = 1
            )
            Text(
                text = app.packageName,
                color = textColor.copy(alpha = 0.45f),
        
       
 fontSize = 11.sp,
                fontFamily = LocalNazeUiFont.current,
                maxLines = 1
            )
        }
    }

    SheetActionRow(
        icon = if (inDock) NazeIcons.Minus else NazeIcons.Plus,
        label = if (inDock) "Remove from dock" else "Pin to dock",
        textColor = textColor,
        accent = accent,
        onClick = onDockToggle
    )
    SheetActionRow(
        icon = NazeIcons.Info,
        label = "App info",
        textColor = textColor,
        accent = accent,
        onClick = onAppInfo
    )
}

@Composable
private fun SheetActionRow(
    icon: androidx.compose.ui.graphics.ImageVector,
    label: String,
    textColor: Color,
    accent: Color,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(vertical = 3.dp)
            .pressScale(interaction, pressedScale = 0.97f)
            .clip(RoundedCornerShape(16.dp))
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(14.dp))
        Text(
            text = label,
            color = textColor.copy(alpha = 0.9f),
            fontSize = 15.sp,
            fontFamily = LocalNazeUiFont.current
        )
    }
}

@Composable
private fun SheetTitle(text: String, textColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(width = 3.dp, height = 14.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(textColor.copy(alpha = 0.4f))
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = text.uppercase(),
            color = textColor.copy(alpha = 0.55f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = LocalNazeDisplayFont.current,
            letterSpacing = 2.sp
        )
    }
}
