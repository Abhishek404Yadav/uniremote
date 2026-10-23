package com.sony.braviaremote.model

import com.google.gson.annotations.SerializedName

/**
 * Represents a Sony TV device detected on the local Wi-Fi or manually configured.
 */
data class TvDevice(
    @SerializedName("ipAddress")
    val ipAddress: String,

    @SerializedName("friendlyName")
    val friendlyName: String = "Sony BRAVIA TV",

    @SerializedName("modelName")
    val modelName: String = "KD-32W830K",

    @SerializedName("psk")
    val psk: String = "1234",

    @SerializedName("port")
    val port: Int = 80,

    @SerializedName("isOnline")
    val isOnline: Boolean = false,

    @SerializedName("lastSeen")
    val lastSeen: Long = System.currentTimeMillis()
)
