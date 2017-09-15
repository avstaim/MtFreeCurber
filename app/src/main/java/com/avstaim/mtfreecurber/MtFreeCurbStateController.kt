package com.avstaim.mtfreecurber

import android.content.Context
import android.telephony.TelephonyManager
import android.telephony.PhoneStateListener
import android.telephony.ServiceState

class MtFreeCurbStateController(context: Context) {
    companion object {
        val MIN_SIGNAL_STRENGTH = 0
    }

    var shouldCurb = false
        private set
    private var shouldCurbInternal: Boolean? = null
        set(value) {
            field = value
            shouldCurb = value?.let { it } ?: false
        }
        get() = field
    var shouldCurbListener: ((Boolean)->Unit)? = null

    private val telephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    val stateListener = CurbPhoneStateListener(context)

    init {
        stateListener.listeners.add {
            onPhoneStateUpdate(it)
        }
    }

    fun startListening() {
        telephonyManager.listen(stateListener, PhoneStateListener.LISTEN_SERVICE_STATE
                or PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
                or PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)
    }

    fun stopListening() {
        telephonyManager.listen(stateListener, PhoneStateListener.LISTEN_NONE)
    }

    private fun onPhoneStateUpdate(state: PhoneState) {
        val isInService = (state.serviceState == ServiceState.STATE_IN_SERVICE)
        val hasDataConnection = (state.connectionState > TelephonyManager.DATA_CONNECTING)
        val enoughStrength = (state.signalStrength > MIN_SIGNAL_STRENGTH)

        val normalMobileInternet = isInService && hasDataConnection && enoughStrength
        updateCurbStateIfNeeded(normalMobileInternet)
    }

    private fun updateCurbStateIfNeeded(newCurbState: Boolean) {
        if (newCurbState == shouldCurbInternal) return

        shouldCurbInternal = newCurbState
        shouldCurbListener?.invoke(shouldCurb)
    }
}