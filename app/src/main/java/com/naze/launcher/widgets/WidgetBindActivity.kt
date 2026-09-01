package com.naze.launcher.widgets

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.os.Bundle

/**
 * Some widget providers require an explicit bind permission grant, which Android
 * routes through startActivityForResult(ACTION_APPWIDGET_BIND). This translucent,
 * no-UI activity exists purely as that callback target and finishes immediately.
 */
class WidgetBindActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
        setResult(if (appWidgetId != -1) RESULT_OK else RESULT_CANCELED)
        finish()
    }
}
