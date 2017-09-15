package com.avstaim.mtfreecurber

import android.content.Context
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.util.Log

val MT_FREE_LABEL_DRAFT = "mt_free"
val TAG = "MAIN_CURBER"

class MtFreeCurber(context: Context) {
    private val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager

    var mtFreeNetId = -1
        private set

    fun init(): Boolean {
        if (mtFreeNetId != -1) return true

        val checkIfMtFree = { ssid: String -> ssid.toLowerCase().contains(MT_FREE_LABEL_DRAFT) }
        val processMTFreeConfiguration = { conf: WifiConfiguration ->
            mtFreeNetId = conf.networkId
            Log.i(TAG, "MT_FREE found: id=$mtFreeNetId ssid=${conf.SSID}")
        }

        val configuredNetworks = wifiManager.configuredNetworks ?: return false

        for (conf in configuredNetworks) {
            Log.i(TAG, "network: ${conf.SSID}")
            if (checkIfMtFree(conf.SSID)) {
                processMTFreeConfiguration(conf)
            }
        }
        return mtFreeNetId != -1
    }

    fun enableMtFree() {
        val res = wifiManager.enableNetwork(mtFreeNetId, false)
        Log.i(TAG, "enable res=$res mtFreeNetId=$mtFreeNetId")
    }

    fun disableMtFree() {
        val res = wifiManager.disableNetwork(mtFreeNetId)
        Log.i(TAG, "disable res=$res mtFreeNetId=$mtFreeNetId")
    }
}

