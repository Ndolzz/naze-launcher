package com.naze.launcher

import android.app.Application

/**
 * No Hilt/Dagger/Koin here on purpose — the object graph is small enough that every
 * repository just takes a Context and is constructed where it's used (see HomeViewModel).
 * If the app grows past ~15-20 injectable classes, revisit and introduce a DI framework.
 */
class NazeApplication : Application()
