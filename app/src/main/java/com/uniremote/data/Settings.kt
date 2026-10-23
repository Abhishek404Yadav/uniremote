package com.uniremote.data

import android.content.Context

/** User preferences. */
class Settings(context: Context) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var haptics: Boolean
        get() = prefs.getBoolean("haptics", true)
        set(value) = prefs.edit().putBoolean("haptics", value).apply()

    /** The phone's volume buttons change the TV's volume while the remote is open. */
    var volumeKeys: Boolean
        get() = prefs.getBoolean("volumeKeys", true)
        set(value) = prefs.edit().putBoolean("volumeKeys", value).apply()

    var keepScreenOn: Boolean
        get() = prefs.getBoolean("keepScreenOn", false)
        set(value) = prefs.edit().putBoolean("keepScreenOn", value).apply()

    /**
     * The remote's middle: Simple (the default), D-pad, Touchpad, or Full.
     */
    var padMode: PadMode
        get() {
            if (!prefs.getBoolean("simple_default_set_v1", false)) {
                prefs.edit()
                    .putString("padMode", PadMode.SIMPLE.name)
                    .putBoolean("simple_default_set_v1", true)
                    .apply()
                return PadMode.SIMPLE
            }
            return prefs.getString("padMode", null)?.let { runCatching { PadMode.valueOf(it) }.getOrNull() }
                ?: PadMode.SIMPLE
        }
        set(value) = prefs.edit().putString("padMode", value.name).putBoolean("touchpad", value == PadMode.TOUCHPAD).apply()

    /** Open the phone keyboard when the TV focuses a text field. */
    var autoKeyboard: Boolean
        get() = prefs.getBoolean("autoKeyboard", true)
        set(value) = prefs.edit().putBoolean("autoKeyboard", value).apply()
}

/** What the middle of the remote shows: see PadLayout. */
enum class PadMode {
    /** A smaller D-pad, a swipe strip and a row of extra keys. */
    FULL,
    DPAD,
    TOUCHPAD,
    SIMPLE,
}
