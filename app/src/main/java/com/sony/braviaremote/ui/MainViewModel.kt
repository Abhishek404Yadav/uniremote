package com.sony.braviaremote.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sony.braviaremote.data.PreferencesManager
import com.sony.braviaremote.model.IrccCommand
import com.sony.braviaremote.model.TvDevice
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
    private val client = SonyBraviaClient()
    private val discovery = SsdpDiscovery(application)

    private val _activeIp = MutableStateFlow(prefs.activeTvIp)
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

    private val _discoveredDevices = MutableStateFlow<List<TvDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<TvDevice>> = _discoveredDevices.asStateFlow()

    private val _messageEvents = MutableSharedFlow<String>()
    val messageEvents: SharedFlow<String> = _messageEvents.asSharedFlow()

    init {
        if (_activeIp.value.isNotBlank()) {
            checkConnection()
        }
    }

    fun checkConnection() {
        val ip = _activeIp.value
        val psk = _activePsk.value
        if (ip.isBlank()) return

        viewModelScope.launch {
            _isConnecting.value = true
            val result = client.checkStatus(ip, psk)
            _isConnecting.value = false
            _isConnected.value = result.isSuccess
        }
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
            val result = client.sendIrcc(command, ip, psk)
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
        val cleanIp = ip.trim()
        val cleanPsk = if (psk.isBlank()) "1234" else psk.trim()

        prefs.activeTvIp = cleanIp
        prefs.activeTvPsk = cleanPsk
        _activeIp.value = cleanIp
        _activePsk.value = cleanPsk

        viewModelScope.launch {
            _isConnecting.value = true
            val result = client.checkStatus(cleanIp, cleanPsk)
            _isConnecting.value = false
            _isConnected.value = result.isSuccess

            if (result.isSuccess) {
                _messageEvents.emit("Connected to Sony TV at $cleanIp")
                prefs.saveDevice(
                    TvDevice(
                        ipAddress = cleanIp,
                        friendlyName = "Sony BRAVIA (KD-32W830K)",
                        psk = cleanPsk,
                        isOnline = true
                    )
                )
            } else {
                _messageEvents.emit("Saved TV IP: $cleanIp. Check PSK & IP control settings if TV does not respond.")
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
}
