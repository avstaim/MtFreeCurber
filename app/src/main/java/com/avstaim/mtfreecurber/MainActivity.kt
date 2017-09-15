package com.avstaim.mtfreecurber

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import org.jetbrains.anko.setContentView
import org.jetbrains.anko.toast
import android.content.pm.PackageManager

class MainActivity : AppCompatActivity() {
    private val ui = MainActivityUi()
    private val serviceConnection: CurbServiceConnection by lazy { CurbServiceConnection(this) }
    private val permissionManager: PermissionManager by lazy { PermissionManager(this) }

    val curbStatusListener: ((CurbStatus)->Unit) = {
        when (it) {
            CurbStatus.NOT_STARTED -> ui.setTexts(R.string.service_not_started, R.string.start_curb)
            CurbStatus.UNKNOWN -> ui.setTexts(R.string.service_is_starting, R.string.stop_curb)
            CurbStatus.NOT_CURBED -> ui.setTexts(R.string.not_curbed, R.string.stop_curb)
            CurbStatus.CURBED -> ui.setTexts(R.string.curbed, R.string.stop_curb)
            CurbStatus.ERROR -> ui.setTexts(R.string.wifi_error, R.string.start_curb)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui.setContentView(this)
        ui.setButtonEnabled(false)
        ui.setText(R.string.permissions_required)
    }

    override fun onStart() {
        super.onStart()
        checkPermissions()
    }

    override fun onStop() {
        super.onStop()
        if (serviceConnection.isBound) {
            serviceConnection.unbind()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            PermissionManager.REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkPermissions()
                } else {
                    ui.setText(R.string.permission_denied)
                }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun checkPermissions() {
        if (permissionManager.checkPermissions()) {
            serviceConnection.bind()
            ui.setButtonEnabled(true)
        }
    }

    fun toggle() {
        if (!serviceConnection.isBound) {
            toast(R.string.internal_error)
            return
        }

        if (!serviceConnection.isStarted) serviceConnection.start()
        else serviceConnection.stop()
    }

    fun onBind() {
        serviceConnection.binder?.setCurbStatusListener(curbStatusListener)
        curbStatusListener.invoke(serviceConnection.binder?.currentCurbStatus ?: CurbStatus.UNKNOWN)
    }
}