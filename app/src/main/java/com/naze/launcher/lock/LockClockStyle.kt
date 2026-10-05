package com.naze.launcher.lock

/** Clock presentations available on Naze Lock. Persisted by enum name. */
enum class LockClockStyle(val label: String) {
    STACK("Stack"),
    ANALOG("Analog"),
    TERMINAL("Terminal");

    fun next(): LockClockStyle = values()[(ordinal + 1) % values().size]

    companion object {
        fun fromName(raw: String?): LockClockStyle =
            raw?.let { runCatching { valueOf(it) }.getOrNull() } ?: STACK
    }
}
