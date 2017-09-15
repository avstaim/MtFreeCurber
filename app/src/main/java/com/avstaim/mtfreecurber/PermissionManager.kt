package com.avstaim.mtfreecurber

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat

class PermissionManager(private val activity: Activity) {
    companion object {
        private val READ_PHONE_STATE = Manifest.permission.READ_PHONE_STATE
        private val ACCESS_WIFI_STATE = Manifest.permission.ACCESS_WIFI_STATE
        private val CHANGE_WIFI_STATE = Manifest.permission.CHANGE_WIFI_STATE
        private val ACCESS_NETWORK_STATE = Manifest.permission.ACCESS_NETWORK_STATE

        val REQUEST_CODE = 237
    }

    fun checkPermissions() = checkPermission(READ_PHONE_STATE) &&
            checkPermission(ACCESS_WIFI_STATE) &&
            checkPermission(CHANGE_WIFI_STATE) &&
            checkPermission(ACCESS_NETWORK_STATE)

    private fun checkPermission(permission: String): Boolean {
        if (ContextCompat.checkSelfPermission(activity, permission)
                == PackageManager.PERMISSION_GRANTED) {
            return true
        } else {
            ActivityCompat.requestPermissions(activity, arrayOf(permission), REQUEST_CODE)
            return false
        }
    }



}