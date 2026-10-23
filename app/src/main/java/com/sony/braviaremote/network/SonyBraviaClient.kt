package com.sony.braviaremote.network

import com.google.gson.JsonParser
import com.sony.braviaremote.model.IrccCommand
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * Sony Bravia IP Control Client over Local Wi-Fi HTTP / REST API.
 * Communicates with Sony KD-32W830K using Pre-Shared Key (PSK) authentication.
 */
class SonyBraviaClient(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(3, TimeUnit.SECONDS)
        .writeTimeout(3, TimeUnit.SECONDS)
        .build()
) {

    @Volatile
    private var cachedIrccPath: String = "/sony/IRCC"

    /**
     * Sanitizes and extracts the pure IP or host from input,
     * stripping accidental schemes, trailing slashes, and whitespace.
     */
    private fun sanitizeIp(ip: String): String {
        return ip.trim()
            .removePrefix("http://")
            .removePrefix("https://")
            .trimEnd('/')
    }

    /**
     * Sends an IRCC command over HTTP POST to the TV.
     * Note: Sony Bravia IP Control endpoints are case-sensitive (/sony/IRCC).
     */
    suspend fun sendIrcc(
        command: IrccCommand,
        tvIp: String,
        psk: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val host = sanitizeIp(tvIp)
        if (host.isBlank()) {
            return@withContext Result.failure(Exception("TV IP address is missing or blank."))
        }

        val soapBody = """
            <?xml version="1.0" encoding="utf-8"?>
            <s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
                <s:Body>
                    <u:X_SendIRCC xmlns:u="urn:schemas-sony-com:service:IRCC:1">
                        <IRCCCode>${command.code}</IRCCCode>
                    </u:X_SendIRCC>
                </s:Body>
            </s:Envelope>
        """.trimIndent()

        val mediaType = "text/xml; charset=UTF-8".toMediaType()
        val requestBody = soapBody.toRequestBody(mediaType)

        // Try the cached path first (defaults to "/sony/IRCC"), fallback to "/sony/ircc" if 404
        val pathsToTry = if (cachedIrccPath == "/sony/IRCC") {
            listOf("/sony/IRCC", "/sony/ircc")
        } else {
            listOf("/sony/ircc", "/sony/IRCC")
        }

        var lastError: Exception? = null

        for (path in pathsToTry) {
            val endpoint = "http://$host$path"
            val request = Request.Builder()
                .url(endpoint)
                .header("SOAPACTION", "\"urn:schemas-sony-com:service:IRCC:1#X_SendIRCC\"")
                .header("X-Auth-PSK", psk)
                .header("Content-Type", "text/xml; charset=UTF-8")
                .post(requestBody)
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    when {
                        response.isSuccessful -> {
                            cachedIrccPath = path
                            return@withContext Result.success(Unit)
                        }
                        response.code == 404 -> {
                            // 404 Not Found - continue loop to test alternate case sensitivity
                            lastError = Exception("TV returned HTTP 404. Please verify 'Simple IP Control' is set to ON in TV Settings -> Network -> IP Control.")
                        }
                        response.code == 401 || response.code == 403 -> {
                            return@withContext Result.failure(Exception("Authentication failed (HTTP ${response.code}). Check your TV's Pre-Shared Key (PSK)."))
                        }
                        else -> {
                            return@withContext Result.failure(Exception("TV returned HTTP error: ${response.code}"))
                        }
                    }
                }
            } catch (e: Exception) {
                lastError = Exception("Unable to reach TV at $host: ${e.localizedMessage ?: e.message}")
            }
        }

        Result.failure(lastError ?: Exception("Failed to send command to TV at $host"))
    }

    /**
     * Checks if the TV is reachable and retrieves power status.
     */
    suspend fun checkStatus(
        tvIp: String,
        psk: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        val host = sanitizeIp(tvIp)
        if (host.isBlank()) return@withContext Result.failure(Exception("TV IP is blank"))

        val endpoint = "http://$host/sony/system"
        val jsonPayload = """{"id":20,"method":"getPowerStatus","version":"1.0","params":[]}"""
        val mediaType = "application/json; charset=UTF-8".toMediaType()
        val requestBody = jsonPayload.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(endpoint)
            .header("X-Auth-PSK", psk)
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string().orEmpty()
                    val json = JsonParser.parseString(bodyString).asJsonObject
                    val resultArr = json.getAsJsonArray("result")
                    val statusObj = resultArr?.get(0)?.asJsonObject
                    val status = statusObj?.get("status")?.asString
                    val isOn = status.equals("active", ignoreCase = true)
                    Result.success(isOn)
                } else {
                    // Even if 403, the TV responded on the IP address!
                    Result.success(false)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Launches a specific application directly via Sony Bravia AppControl REST API.
     */
    suspend fun launchApp(
        appUri: String,
        tvIp: String,
        psk: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val host = sanitizeIp(tvIp)
        if (host.isBlank()) return@withContext Result.failure(Exception("TV IP is blank"))

        val endpoint = "http://$host/sony/appControl"
        val jsonPayload = """{"id":22,"method":"setActiveApp","version":"1.0","params":[{"uri":"$appUri"}]}"""
        val mediaType = "application/json; charset=UTF-8".toMediaType()
        val requestBody = jsonPayload.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(endpoint)
            .header("X-Auth-PSK", psk)
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Failed to launch app: HTTP ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
