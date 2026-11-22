package com.example.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface ConnectivityObserver {
    val isOnline: StateFlow<Boolean>
    fun unregister() {}
}

/**
 * Observes active internet-capable network connectivity via Android's [ConnectivityManager].
 * Safely degrades in restricted test/headless environments without crashing.
 */
class NetworkConnectivityMonitor(context: Context) : ConnectivityObserver {

    private val connectivityManager: ConnectivityManager? =
        context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    private val activeNetworks = mutableSetOf<Network>()
    private val _isOnline = MutableStateFlow(evaluateCurrentOnlineState())
    override val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            synchronized(activeNetworks) {
                activeNetworks.add(network)
            }
            refreshOnlineState()
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            val usable = hasUsableInternet(networkCapabilities)
            synchronized(activeNetworks) {
                if (usable) activeNetworks.add(network) else activeNetworks.remove(network)
            }
            refreshOnlineState()
        }

        override fun onLost(network: Network) {
            synchronized(activeNetworks) {
                activeNetworks.remove(network)
            }
            refreshOnlineState()
        }

        override fun onUnavailable() {
            synchronized(activeNetworks) {
                activeNetworks.clear()
            }
            _isOnline.value = false
        }
    }

    init {
        registerCallbackSafely()
    }

    private fun registerCallbackSafely() {
        val cm = connectivityManager ?: return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                cm.registerDefaultNetworkCallback(networkCallback)
            }
        } catch (_: Exception) {
            // Keep initial evaluated online state when callback registration isn't permitted
        }
    }

    private fun refreshOnlineState() {
        _isOnline.value = synchronized(activeNetworks) {
            activeNetworks.isNotEmpty()
        } || evaluateCurrentOnlineState()
    }

    private fun evaluateCurrentOnlineState(): Boolean {
        val cm = connectivityManager ?: return true
        return try {
            val network = cm.activeNetwork ?: return false
            val capabilities = cm.getNetworkCapabilities(network) ?: return false
            hasUsableInternet(capabilities)
        } catch (_: Exception) {
            false
        }
    }

    private fun hasUsableInternet(capabilities: NetworkCapabilities): Boolean {
        val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        if (!hasInternet) return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Allow validated networks or emulator/sandbox networks where VALIDATED capability is still pending
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) ||
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
        } else {
            true
        }
    }

    override fun unregister() {
        val cm = connectivityManager ?: return
        try {
            cm.unregisterNetworkCallback(networkCallback)
        } catch (_: Exception) {
            // Already unregistered or not registered
        }
    }
}
