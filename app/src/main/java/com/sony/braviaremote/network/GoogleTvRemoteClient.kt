package com.sony.braviaremote.network

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.security.cert.X509Certificate
import javax.net.ssl.SSLSocket

class GoogleTvRemoteClient(context: Context) {

    private val certManager = GoogleTvCertificateManager(context)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var pairingSocket: SSLSocket? = null
    private var pairingServerCert: X509Certificate? = null
    private var pairingHost: String? = null

    private var remoteSocket: SSLSocket? = null
    private var remoteReaderJob: Job? = null

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _isPairing = MutableStateFlow(false)
    val isPairing: StateFlow<Boolean> = _isPairing.asStateFlow()

    private val _awaitingPin = MutableStateFlow(false)
    val awaitingPin: StateFlow<Boolean> = _awaitingPin.asStateFlow()

    companion object {
        private const val TAG = "GoogleTvRemoteClient"
        const val PAIRING_PORT = 6467
        const val REMOTE_PORT = 6466
    }

    /**
     * Initiates the pairing process on port 6467.
     * When successful, the TV displays a 6-character code on screen.
     */
    suspend fun startPairing(tvHost: String): Result<Unit> = withContext(Dispatchers.IO) {
        val host = sanitizeHost(tvHost)
        try {
            closePairing()
            _isPairing.value = true
            _awaitingPin.value = false

            val sslContext = certManager.createSslContext()
            val socket = sslContext.socketFactory.createSocket() as SSLSocket
            socket.connect(InetSocketAddress(host, PAIRING_PORT), 4000)
            socket.soTimeout = 30000 // Allow time for user to input PIN
            socket.startHandshake()

            val serverCert = socket.session.peerCertificates.firstOrNull() as? X509Certificate
                ?: throw IllegalStateException("No peer certificate found from TV")

            pairingSocket = socket
            pairingServerCert = serverCert
            pairingHost = host

            val input = socket.inputStream
            val output = socket.outputStream

            // Step 1: Send PairingRequest
            val reqPayload = ProtoWriter().apply {
                writeString(1, "androidtv-remote")
                writeString(2, "Sony Remote")
            }
            val reqMsg = ProtoWriter().apply {
                writeInt32(1, 2) // protocol_version = 2
                writeInt32(2, 200) // status = STATUS_OK (200)
                writeMessage(10, reqPayload) // pairing_request = 10
            }
            ProtoStreamHelper.writeDelimitedMessage(output, reqMsg)

            // Step 2: Read PairingRequestAck
            val ackBytes = ProtoStreamHelper.readDelimitedMessage(input)
            // Step 3: Read PairingOption
            val optBytes = ProtoStreamHelper.readDelimitedMessage(input)

            // Step 4: Send PairingConfiguration (Input role, Hexadecimal, 6 symbols)
            val encPayload = ProtoWriter().apply {
                writeInt32(1, 3) // ENCODING_TYPE_HEXADECIMAL = 3
                writeInt32(2, 6) // symbol_length = 6
            }
            val cfgPayload = ProtoWriter().apply {
                writeMessage(1, encPayload) // encoding = 1
                writeInt32(2, 1) // client_role = ROLE_TYPE_INPUT (1)
            }
            val cfgMsg = ProtoWriter().apply {
                writeInt32(1, 2)
                writeInt32(2, 200)
                writeMessage(30, cfgPayload) // pairing_configuration = 30
            }
            ProtoStreamHelper.writeDelimitedMessage(output, cfgMsg)

            // Step 5: Read PairingConfigurationAck
            val cfgAckBytes = ProtoStreamHelper.readDelimitedMessage(input)

            // TV is now displaying the code!
            _awaitingPin.value = true
            Result.success(Unit)
        } catch (e: Exception) {
            closePairing()
            _isPairing.value = false
            _awaitingPin.value = false
            Result.failure(Exception("Failed to initiate pairing: ${e.localizedMessage ?: e.message}"))
        }
    }

    /**
     * Submits the 6-character PIN shown on the TV screen.
     */
    suspend fun submitPairingCode(pin: String): Result<Unit> = withContext(Dispatchers.IO) {
        val socket = pairingSocket ?: return@withContext Result.failure(Exception("Pairing session expired. Please restart pairing."))
        val serverCert = pairingServerCert ?: return@withContext Result.failure(Exception("Missing server certificate."))
        val host = pairingHost ?: return@withContext Result.failure(Exception("Missing TV host."))

        try {
            val hashSecret = certManager.computePairingSecret(serverCert, pin)

            val secPayload = ProtoWriter().apply {
                writeBytes(1, hashSecret) // secret = 1
            }
            val secMsg = ProtoWriter().apply {
                writeInt32(1, 2)
                writeInt32(2, 200)
                writeMessage(40, secPayload) // pairing_secret = 40
            }

            val output = socket.outputStream
            ProtoStreamHelper.writeDelimitedMessage(output, secMsg)

            val input = socket.inputStream
            val secretAckBytes = ProtoStreamHelper.readDelimitedMessage(input)
            val reader = ProtoReader(secretAckBytes)
            var status = 0
            while (reader.hasMore()) {
                val tag = reader.readTag() ?: break
                if (tag.first == 2 && tag.second == 0) {
                    status = reader.readInt32()
                } else {
                    reader.skipField(tag.second)
                }
            }

            if (status != 200 && status != 0) {
                throw IllegalStateException("Pairing rejected by TV with status $status")
            }

            closePairing()
            _isPairing.value = false
            _awaitingPin.value = false

            // Pairing succeeded! Immediately connect to remote session (port 6466)
            connect(host)
        } catch (e: Exception) {
            Result.failure(Exception("Pairing failed: ${e.localizedMessage ?: e.message}"))
        }
    }

    /**
     * Connects to the remote control session on port 6466.
     */
    suspend fun connect(tvHost: String): Result<Unit> = withContext(Dispatchers.IO) {
        val host = sanitizeHost(tvHost)
        disconnect()

        try {
            val sslContext = certManager.createSslContext()
            val socket = sslContext.socketFactory.createSocket() as SSLSocket
            socket.connect(InetSocketAddress(host, REMOTE_PORT), 4000)
            socket.soTimeout = 15000
            socket.startHandshake()

            val input = socket.inputStream
            val output = socket.outputStream

            // Initial Handshake on port 6466:
            // 1. Read RemoteConfigure from TV
            val configFromTv = ProtoStreamHelper.readDelimitedMessage(input)

            // 2. Reply with our RemoteConfigure
            val devInfo = ProtoWriter().apply {
                writeString(1, "Pixel 8 Pro") // model
                writeString(2, "Google") // vendor
                writeInt32(3, 1)
                writeString(4, "1")
                writeString(5, "com.sony.braviaremote")
                writeString(6, "1.0.0")
            }
            val configReply = ProtoWriter().apply {
                writeInt32(1, 622)
                writeMessage(2, devInfo)
            }
            val msg1 = ProtoWriter().apply {
                writeMessage(1, configReply) // remote_configure = 1
            }
            ProtoStreamHelper.writeDelimitedMessage(output, msg1)

            // 3. Read RemoteSetActive from TV
            val activeFromTv = ProtoStreamHelper.readDelimitedMessage(input)

            // 4. Reply with RemoteSetActive
            val activeReply = ProtoWriter().apply {
                writeInt32(1, 622)
            }
            val msg2 = ProtoWriter().apply {
                writeMessage(2, activeReply) // remote_set_active = 2
            }
            ProtoStreamHelper.writeDelimitedMessage(output, msg2)

            remoteSocket = socket
            _isConnected.value = true

            startReaderLoop(socket)
            Result.success(Unit)
        } catch (e: Exception) {
            disconnect()
            Result.failure(Exception("Could not connect to Google TV on port $REMOTE_PORT: ${e.localizedMessage ?: e.message}"))
        }
    }

    /**
     * Sends a keypress injection to the Google TV.
     */
    suspend fun sendKey(keyCode: Int): Result<Unit> = withContext(Dispatchers.IO) {
        val socket = remoteSocket
        if (socket == null || socket.isClosed) {
            _isConnected.value = false
            return@withContext Result.failure(Exception("Not connected to Google TV. Please pair or connect first."))
        }

        try {
            val keyInject = ProtoWriter().apply {
                writeInt32(1, keyCode) // key_code
                writeInt32(2, 3) // direction = SHORT (3)
            }
            val msg = ProtoWriter().apply {
                writeMessage(10, keyInject) // remote_key_inject = 10
            }
            synchronized(socket) {
                ProtoStreamHelper.writeDelimitedMessage(socket.outputStream, msg)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            _isConnected.value = false
            disconnect()
            Result.failure(Exception("Failed to send key: ${e.localizedMessage ?: e.message}"))
        }
    }

    /**
     * Launches a deep link application URL (e.g. YouTube, Netflix, Prime Video) on Google TV.
     */
    suspend fun launchAppLink(appLink: String): Result<Unit> = withContext(Dispatchers.IO) {
        val socket = remoteSocket
        if (socket == null || socket.isClosed) {
            _isConnected.value = false
            return@withContext Result.failure(Exception("Not connected to Google TV."))
        }

        try {
            val appPayload = ProtoWriter().apply {
                writeString(1, appLink)
            }
            val msg = ProtoWriter().apply {
                writeMessage(22, appPayload) // remote_app_link_launch_request = 22
            }
            synchronized(socket) {
                ProtoStreamHelper.writeDelimitedMessage(socket.outputStream, msg)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            _isConnected.value = false
            disconnect()
            Result.failure(Exception("Failed to launch app: ${e.localizedMessage ?: e.message}"))
        }
    }

    private fun startReaderLoop(socket: SSLSocket) {
        remoteReaderJob?.cancel()
        remoteReaderJob = scope.launch {
            try {
                val input = socket.inputStream
                val output = socket.outputStream

                while (isActive && !socket.isClosed) {
                    val messageBytes = ProtoStreamHelper.readDelimitedMessage(input)
                    val reader = ProtoReader(messageBytes)

                    while (reader.hasMore()) {
                        val tag = reader.readTag() ?: break
                        when (tag.first) {
                            8 -> { // remote_ping_request
                                val pingReader = ProtoReader(reader.readBytes())
                                var val1 = 0
                                while (pingReader.hasMore()) {
                                    val pTag = pingReader.readTag() ?: break
                                    if (pTag.first == 1) val1 = pingReader.readInt32()
                                    else pingReader.skipField(pTag.second)
                                }
                                // Reply with remote_ping_response (9)
                                val pingResp = ProtoWriter().apply { writeInt32(1, val1) }
                                val respMsg = ProtoWriter().apply { writeMessage(9, pingResp) }
                                synchronized(socket) {
                                    ProtoStreamHelper.writeDelimitedMessage(output, respMsg)
                                }
                            }
                            else -> reader.skipField(tag.second)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "Reader loop ended: ${e.localizedMessage}")
            } finally {
                _isConnected.value = false
            }
        }
    }

    fun cancelPairing() {
        closePairing()
        _isPairing.value = false
        _awaitingPin.value = false
    }

    private fun closePairing() {
        try { pairingSocket?.close() } catch (_: Exception) {}
        pairingSocket = null
        pairingServerCert = null
    }

    fun disconnect() {
        remoteReaderJob?.cancel()
        remoteReaderJob = null
        try { remoteSocket?.close() } catch (_: Exception) {}
        remoteSocket = null
        _isConnected.value = false
    }

    private fun sanitizeHost(host: String): String {
        return host.trim()
            .removePrefix("http://")
            .removePrefix("https://")
            .substringBefore(':')
            .trimEnd('/')
    }
}
