package com.sony.braviaremote.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sony.braviaremote.model.TvDevice

/**
 * Manages persistent storage of TV connection details and user preferences.
 */
class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("bravia_remote_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    var activeTvIp: String
        get() = prefs.getString(KEY_TV_IP, "").orEmpty()
        set(value) = prefs.edit().putString(KEY_TV_IP, value).apply()

    var activeTvPsk: String
        get() = prefs.getString(KEY_TV_PSK, "1234").orEmpty()
        set(value) = prefs.edit().putString(KEY_TV_PSK, value).apply()

    var activeTvName: String
        get() = prefs.getString(KEY_TV_NAME, "Sony BRAVIA (KD-32W830K)").orEmpty()
        set(value) = prefs.edit().putString(KEY_TV_NAME, value).apply()

    var isHapticEnabled: Boolean
        get() = prefs.getBoolean(KEY_HAPTIC, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTIC, value).apply()

    fun getSavedDevices(): List<TvDevice> {
        val json = prefs.getString(KEY_SAVED_DEVICES, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<TvDevice>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveDevice(device: TvDevice) {
        val currentList = getSavedDevices().toMutableList()
        currentList.removeAll { it.ipAddress == device.ipAddress }
        currentList.add(0, device)
        val json = gson.toJson(currentList)
        prefs.edit().putString(KEY_SAVED_DEVICES, json).apply()

        // Also make it the active device
        activeTvIp = device.ipAddress
        activeTvPsk = device.psk
        activeTvName = device.friendlyName
    }

    companion object {
        private const val KEY_TV_IP = "key_tv_ip"
        private const val KEY_TV_PSK = "key_tv_psk"
        private const val KEY_TV_NAME = "key_tv_name"
        private const val KEY_HAPTIC = "key_haptic"
        private const val KEY_SAVED_DEVICES = "key_saved_devices"
    }
}
