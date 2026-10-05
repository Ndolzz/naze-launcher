package com.naze.launcher

import android.app.Application
import android.util.Log
import java.io.File

/**
 * No Hilt/Dagger/Koin here on purpose — the object graph is small enough that every
 * repository just takes a Context and is constructed where it's used (see HomeViewModel).
 * If the app grows past ~15-20 injectable classes, revisit and introduce a DI framework.
 *
 * A tiny crash recorder: any uncaught exception is written to last_crash.txt so the
 * next launch can show the exact stack trace on screen (no adb needed).
 */
class NazeApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            runCatching {
                File(filesDir, CRASH_FILE).writeText(
                    buildString {
                        append("time: ").append(System.currentTimeMillis()).append("\n")
                        append(Log.getStackTraceString(throwable))
                    }
                )
            }
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    companion object {
        const val CRASH_FILE = "last_crash.txt"
    }
}
