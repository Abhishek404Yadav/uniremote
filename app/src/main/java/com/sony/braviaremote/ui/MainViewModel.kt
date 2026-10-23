package com.sony.braviaremote.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sony.braviaremote.data.PreferencesManager
import com.sony.braviaremote.model.IrccCommand
import com.sony.braviaremote.model.TvDevice
import com.sony.braviaremote.network.GoogleTvRemoteClient
import com.sony.braviaremote.network.SonyBraviaClient
import com.sony.braviaremote.network.SsdpDiscovery
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = PreferencesManager(application)
    private val googleTvClient = GoogleTvRemoteClient(application)
    private val braviaClient = SonyBraviaClient()
    private val discovery = SsdpDiscovery(application)

    private fun cleanHost(ip: String): String = ip.trim().removePrefix("http://").removePrefix("https://").substringBefore(':').trimEnd('/')

    private val _activeIp = MutableStateFlow(cleanHost(prefs.activeTvIp))
    val activeIp: StateFlow<String> = _activeIp.asStateFlow()

    private val _activePsk = MutableStateFlow(prefs.activeTvPsk)
    val activePsk: StateFlow<String> = _activePsk.asStateFlow()

    private val _tvName = MutableStateFlow(prefs.activeTvName)
    val tvName: StateFlow<String> = _tvName.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _isConnecting = MutableStateFlow(false)
    val isConnecting: StateFlow<Boolean> = _isConnecting.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    val isPairing: StateFlow<Boolean> = googleTvClient.isPairing
    val awaitingPin: StateFlow<Boolean> = googleTvClient.awaitingPin

    private val _discoveredDevices = MutableStateFlow<List<TvDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<TvDevice>> = _discoveredDevices.asStateFlow()

    private val _messageEvents = MutableSharedFlow<String>()
    val messageEvents: SharedFlow<String> = _messageEvents.asSharedFlow()

    init {
        viewModelScope.launch {
            googleTvClient.isConnected.collect { connected ->
                if (connected) {
                    _isConnected.value = true
                }
            }
        }
        if (_activeIp.value.isNotBlank()) {
            checkConnection()
        }
    }

    fun checkConnection() {
        val ip = _activeIp.value
        if (ip.isBlank()) return

        viewModelScope.launch {
            _isConnecting.value = true
            // Attempt connection via Google TV protocol first
            val gtvResult = googleTvClient.connect(ip)
            if (gtvResult.isSuccess) {
                _isConnected.value = true
                _isConnecting.value = false
                return@launch
            }

            // Fallback: check status via Sony HTTP API
            val psk = _activePsk.value
            val result = braviaClient.checkStatus(ip, psk)
            _isConnecting.value = false
            _isConnected.value = result.isSuccess
        }
    }

    fun startPairing() {
        val ip = _activeIp.value
        if (ip.isBlank()) {
            viewModelScope.launch { _messageEvents.emit("Please enter your TV IP address first.") }
            return
        }

        viewModelScope.launch {
            _isConnecting.value = true
            val result = googleTvClient.startPairing(ip)
            _isConnecting.value = false
            if (result.isSuccess) {
                _messageEvents.emit("Pairing started! Check your TV for the 6-character code.")
            } else {
                _messageEvents.emit(result.exceptionOrNull()?.localizedMessage ?: "Could not initiate pairing")
            }
        }
    }

    fun submitPairingCode(pin: String) {
        viewModelScope.launch {
            _isConnecting.value = true
            val result = googleTvClient.submitPairingCode(pin)
            _isConnecting.value = false
            if (result.isSuccess) {
                prefs.isGoogleTvPaired = true
                _isConnected.value = true
                _messageEvents.emit("Paired & connected successfully to your Google TV!")
            } else {
                _messageEvents.emit(result.exceptionOrNull()?.localizedMessage ?: "Pairing failed")
            }
        }
    }

    fun cancelPairing() {
        googleTvClient.cancelPairing()
    }

    fun sendCommand(command: IrccCommand) {
        val ip = _activeIp.value
        val psk = _activePsk.value
        if (ip.isBlank()) {
            viewModelScope.launch {
                _messageEvents.emit("Please set your TV IP address first.")
            }
            return
        }

        viewModelScope.launch {
            // Priority 1: Google TV Remote Protocol v2
            if (googleTvClient.isConnected.value) {
                val sendResult = if (command.appUrl != null) {
                    googleTvClient.launchAppLink(command.appUrl)
                } else if (command.androidKeyCode != 0) {
                    googleTvClient.sendKey(command.androidKeyCode)
                } else {
                    Result.success(Unit)
                }

                if (sendResult.isSuccess) {
                    _isConnected.value = true
                    return@launch
                }
            } else {
                // If not connected, try connecting via Google TV protocol
                val connResult = googleTvClient.connect(ip)
                if (connResult.isSuccess) {
                    _isConnected.value = true
                    if (command.appUrl != null) {
                        googleTvClient.launchAppLink(command.appUrl)
                    } else if (command.androidKeyCode != 0) {
                        googleTvClient.sendKey(command.androidKeyCode)
                    }
                    return@launch
                } else if (prefs.isGoogleTvPaired) {
                    // Try reconnecting next time
                } else {
                    // Not paired yet: Automatically prompt user for pairing!
                    val pairResult = googleTvClient.startPairing(ip)
                    if (pairResult.isSuccess) {
                        _messageEvents.emit("Pairing started! Enter the 6-character code shown on your TV.")
                        return@launch
                    }
                }
            }

            // Priority 2: Fallback to Sony IRCC protocol
            val result = braviaClient.sendIrcc(command, ip, psk)
            if (result.isSuccess) {
                _isConnected.value = true
            } else {
                _isConnected.value = false
                val errorMsg = result.exceptionOrNull()?.localizedMessage ?: "Failed to send ${command.displayName}"
                _messageEvents.emit(errorMsg)
            }
        }
    }

    fun updateConnection(ip: String, psk: String) {
        val cleanIp = cleanHost(ip)
        val cleanPsk = if (psk.isBlank()) "1234" else psk.trim()

        prefs.activeTvIp = cleanIp
        prefs.activeTvPsk = cleanPsk
        _activeIp.value = cleanIp
        _activePsk.value = cleanPsk

        viewModelScope.launch {
            _isConnecting.value = true
            // Attempt Google TV connection
            val gtvResult = googleTvClient.connect(cleanIp)
            if (gtvResult.isSuccess) {
                _isConnecting.value = false
                _isConnected.value = true
                _messageEvents.emit("Connected to Google TV at $cleanIp")
                prefs.saveDevice(
                    TvDevice(
                        ipAddress = cleanIp,
                        friendlyName = "Sony BRAVIA (KD-32W830K)",
                        psk = cleanPsk,
                        isOnline = true
                    )
                )
                return@launch
            }

            // If not connected yet, start pairing so TV displays PIN on screen
            val pairResult = googleTvClient.startPairing(cleanIp)
            _isConnecting.value = false
            if (pairResult.isSuccess) {
                _messageEvents.emit("Check your TV screen for the 6-character code!")
                prefs.saveDevice(
                    TvDevice(
                        ipAddress = cleanIp,
                        friendlyName = "Sony BRAVIA (KD-32W830K)",
                        psk = cleanPsk,
                        isOnline = true
                    )
                )
            } else {
                // Fallback to Sony HTTP API check
                val result = braviaClient.checkStatus(cleanIp, cleanPsk)
                _isConnected.value = result.isSuccess
                if (result.isSuccess) {
                    _messageEvents.emit("Connected to Sony TV at $cleanIp")
                } else {
                    _messageEvents.emit("Saved TV IP: $cleanIp. Tap 'Pair with TV' to connect.")
                }
            }
        }
    }

    fun scanNetwork() {
        viewModelScope.launch {
            _isScanning.value = true
            try {
                val devices = discovery.discoverDevices(_activePsk.value)
                _discoveredDevices.value = devices
                if (devices.isEmpty()) {
                    _messageEvents.emit("No Sony TVs auto-detected. Enter IP manually or verify Wi-Fi.")
                } else {
                    _messageEvents.emit("Found ${devices.size} Sony device(s) on Wi-Fi!")
                }
            } catch (e: Exception) {
                _messageEvents.emit("Scan error: ${e.localizedMessage ?: "Unknown"}")
            } finally {
                _isScanning.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        googleTvClient.disconnect()
    }
}
