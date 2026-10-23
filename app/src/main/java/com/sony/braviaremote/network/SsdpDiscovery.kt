package com.sony.braviaremote.network

import android.content.Context
import android.net.wifi.WifiManager
import com.sony.braviaremote.model.TvDevice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

/**
 * Discovers Sony Bravia TVs on the local Wi-Fi network using SSDP UPnP multicast
 * with a high-speed subnet sweep fallback.
 */
class SsdpDiscovery(private val context: Context) {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(500, TimeUnit.MILLISECONDS)
        .readTimeout(500, TimeUnit.MILLISECONDS)
        .build()

    /**
     * Scans for Sony TVs and returns a list of discovered devices.
     */
    suspend fun discoverDevices(defaultPsk: String = "1234"): List<TvDevice> = withContext(Dispatchers.IO) {
        val discoveredList = mutableMapOf<String, TvDevice>()

        // 1. First attempt: SSDP Multicast discovery
        val ssdpDevices = runSsdpScan(defaultPsk)
        ssdpDevices.forEach { device ->
            discoveredList[device.ipAddress] = device
        }

        // 2. Fallback: If SSDP discovered nothing, perform fast subnet probe
        if (discoveredList.isEmpty()) {
            val subnetDevices = runSubnetSweep(defaultPsk)
            subnetDevices.forEach { device ->
                discoveredList[device.ipAddress] = device
            }
        }

        discoveredList.values.toList()
    }

    private fun runSsdpScan(psk: String): List<TvDevice> {
        val results = mutableListOf<TvDevice>()
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        val multicastLock = wifiManager?.createMulticastLock("bravia_ssdp_lock")?.apply {
            setReferenceCounted(true)
            acquire()
        }

        var socket: DatagramSocket? = null
        try {
            socket = DatagramSocket().apply {
                soTimeout = 2500
                broadcast = true
            }

            val ssdpQueries = listOf(
                "urn:schemas-sony-com:service:IRCC:1",
                "urn:schemas-upnp-org:device:MediaRenderer:1",
                "upnp:rootdevice"
            )

            for (target in ssdpQueries) {
                val message = "M-SEARCH * HTTP/1.1\r\n" +
                        "HOST: 239.255.255.250:1900\r\n" +
                        "MAN: \"ssdp:discover\"\r\n" +
                        "MX: 2\r\n" +
                        "ST: $target\r\n\r\n"

                val sendData = message.toByteArray()
                val sendPacket = DatagramPacket(
                    sendData,
                    sendData.size,
                    InetAddress.getByName("239.255.255.250"),
                    1900
                )
                socket.send(sendPacket)
            }

            val receiveBuffer = ByteArray(4096)
            val startTime = System.currentTimeMillis()

            while (System.currentTimeMillis() - startTime < 3000) {
                try {
                    val packet = DatagramPacket(receiveBuffer, receiveBuffer.size)
                    socket.receive(packet)
                    val response = String(packet.data, 0, packet.length)
                    val ip = packet.address.hostAddress ?: continue

                    // Check if response contains Sony Bravia indicators
                    val isSony = response.contains("sony", ignoreCase = true) ||
                            response.contains("IRCC", ignoreCase = true) ||
                            response.contains("bravia", ignoreCase = true) ||
                            response.contains("KD-32W830K", ignoreCase = true)

                    if (isSony) {
                        var model = "KD-32W830K"
                        val modelMatcher = Pattern.compile("modelName>(.*?)</modelName", Pattern.CASE_INSENSITIVE).matcher(response)
                        if (modelMatcher.find()) {
                            model = modelMatcher.group(1).orEmpty()
                        }

                        var friendlyName = "Sony BRAVIA ($model)"
                        val fnMatcher = Pattern.compile("friendlyName>(.*?)</friendlyName", Pattern.CASE_INSENSITIVE).matcher(response)
                        if (fnMatcher.find()) {
                            friendlyName = fnMatcher.group(1).orEmpty()
                        }

                        results.add(
                            TvDevice(
                                ipAddress = ip,
                                friendlyName = friendlyName,
                                modelName = model,
                                psk = psk,
                                isOnline = true
                            )
                        )
                    }
                } catch (_: java.net.SocketTimeoutException) {
                    break
                }
            }
        } catch (_: Exception) {
            // Multicast failed or not permitted
        } finally {
            socket?.close()
            try {
                if (multicastLock?.isHeld == true) {
                    multicastLock.release()
                }
            } catch (_: Exception) {}
        }

        return results.distinctBy { it.ipAddress }
    }

    private suspend fun runSubnetSweep(psk: String): List<TvDevice> = withContext(Dispatchers.IO) {
        val baseIp = getLocalSubnetPrefix() ?: return@withContext emptyList()
        val found = mutableListOf<TvDevice>()

        // Scan local subnet range (1..254) concurrently
        val deferredProbes = (1..254).map { host ->
            async {
                val ip = "$baseIp.$host"
                if (probeSonyIp(ip)) {
                    TvDevice(
                        ipAddress = ip,
                        friendlyName = "Sony BRAVIA (KD-32W830K)",
                        modelName = "KD-32W830K",
                        psk = psk,
                        isOnline = true
                    )
                } else null
            }
        }

        deferredProbes.awaitAll().filterNotNull()
    }

    private fun probeSonyIp(ip: String): Boolean {
        // Quick TCP socket probe on HTTP port 80 or Sony Simple IP port 20060
        try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(ip, 80), 200)
            }
            // If port 80 is open, check if it responds to Sony IRCC / System endpoint
            val request = Request.Builder()
                .url("http://$ip/sony/system")
                .header("Content-Type", "application/json")
                .get()
                .build()

            httpClient.newCall(request).execute().use { response ->
                // Sony TVs respond on /sony/system with 200, 401, 403, or 405
                return response.code in 200..499
            }
        } catch (_: Exception) {
            // Also check port 20060 (Sony Simple IP Control)
            try {
                Socket().use { socket ->
                    socket.connect(InetSocketAddress(ip, 20060), 150)
                    return true
                }
            } catch (_: Exception) {
                return false
            }
        }
    }

    private fun getLocalSubnetPrefix(): String? {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        val ipInt = wifiManager?.connectionInfo?.ipAddress ?: return null
        if (ipInt == 0) return null

        val b1 = ipInt and 0xff
        val b2 = (ipInt shr 8) and 0xff
        val b3 = (ipInt shr 16) and 0xff
        return "$b1.$b2.$b3"
    }
}
