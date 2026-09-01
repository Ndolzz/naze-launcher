package com.naze.launcher.widgets

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context

/**
 * Thin wrapper around the platform's own AppWidgetHost/AppWidgetManager — per the spec,
 * we do NOT invent a custom widget system. This class only adds the bookkeeping a
 * launcher needs: starting/stopping listening with the activity lifecycle, and
 * persisting which widget IDs are currently placed (left to the caller / DataStore).
 */
class NazeWidgetHost(private val context: Context, hostId: Int = HOST_ID) {

    val appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(context)
    val appWidgetHost: AppWidgetHost = AppWidgetHost(context, hostId)

    fun startListening() = appWidgetHost.startListening()
    fun stopListening() = appWidgetHost.stopListening()

    fun allocateWidgetId(): Int = appWidgetHost.allocateAppWidgetId()

    fun deleteWidgetId(appWidgetId: Int) = appWidgetHost.deleteAppWidgetId(appWidgetId)

    fun createHostView(appWidgetId: Int, providerInfo: AppWidgetProviderInfo): AppWidgetHostView =
        appWidgetHost.createView(context, appWidgetId, providerInfo)

    fun getProviderInfo(appWidgetId: Int): AppWidgetProviderInfo? =
        appWidgetManager.getAppWidgetInfo(appWidgetId)

    companion object {
        private const val HOST_ID = 1042 // arbitrary but stable per-launcher host id
    }
}
