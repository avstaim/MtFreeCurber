package com.avstaim.mtfreecurber

import android.content.Context
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.ServiceState
import android.telephony.SignalStrength
import android.telephony.TelephonyManager
import android.net.ConnectivityManager
import android.support.annotation.RequiresApi

class CurbPhoneStateListener(context: Context) : PhoneStateListener() {
    var phoneState: PhoneState private set
    var listeners: MutableList<((PhoneState)->Unit)> = mutableListOf()
    val connectivityService =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    init {
        phoneState = PhoneState(
                serviceState = ServiceState.STATE_POWER_OFF,
                connectionState = TelephonyManager.DATA_DISCONNECTED,
                signalStrength = 0
        )
    }

    override fun onServiceStateChanged(serviceState: ServiceState) {
        changeStateAndNotify(serviceState = serviceState.state)
    }

    override fun onDataConnectionStateChanged(state: Int) {
        changeStateAndNotify(connectionState = state)
    }

    override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
        changeStateAndNotify(signalStrength = getLevel(signalStrength))
    }

    private fun changeStateAndNotify(
            serviceState: Int = phoneState.serviceState,
            connectionState: Int = phoneState.connectionState,
            signalStrength: Int = phoneState.signalStrength
    ) {
        phoneState = PhoneState(serviceState, connectionState, signalStrength)

        for (listener in listeners) {
            listener(phoneState)
        }
    }

    fun getLevel(signalStrength: SignalStrength) =
            if (Build.VERSION.SDK_INT > 23) getLevelModern(signalStrength)
            else getCompatLevel(signalStrength)

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getLevelModern(signalStrength: SignalStrength) = if (is2g()) 0 else signalStrength.level

    private fun getCompatLevel(signalStrength: SignalStrength): Int {
        if (!is2g()) {
            if (signalStrength.isGsm) return getGsmLevel(signalStrength)
            if (isLte()) return 3
        }
        return 0
    }

    private fun getGsmLevel(signalStrength: SignalStrength) =
            when (signalStrength.gsmSignalStrength) {
                in 2..4 -> 1
                in 5..8 -> 2
                in 9..12 -> 3
                in 13..98 -> 4
                else -> 0
            }

    private fun getNetworkInfo() = connectivityService.activeNetworkInfo

    private fun isLte(): Boolean {
        val info = getNetworkInfo()
        if (info == null || !info.isConnected)
            return false

        val type = info.type
        val subtype = info.subtype

        return if (type == ConnectivityManager.TYPE_MOBILE) {
            subtype == TelephonyManager.NETWORK_TYPE_LTE
        } else {
            false
        }
    }

    private fun is2g(): Boolean {
        val info = getNetworkInfo()
        if (info == null || !info.isConnected)
            return false

        val type = info.type
        val subtype = info.subtype

        return if (type == ConnectivityManager.TYPE_MOBILE) {
            when (subtype) {
                TelephonyManager.NETWORK_TYPE_EDGE,
                TelephonyManager.NETWORK_TYPE_CDMA,
                TelephonyManager.NETWORK_TYPE_1xRTT,
                TelephonyManager.NETWORK_TYPE_GPRS -> true
                else -> false
            }
        } else {
            false
        }
    }

}
