package com.avstaim.mtfreecurber

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Binder

class MtFreeCurbService : Service() {
    private val notify_id = 101
    val controller: MtFreeCurbController by lazy { MtFreeCurbController(this) }
    val binder = CurbBinder()

    val notificationManager: NotificationManager by lazy {
        getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
    }

    var startStatus = false
    var wasError = false
    var currentCurbStatus: CurbStatus = CurbStatus.NOT_STARTED

    private var curbStatusListener: ((CurbStatus)->Unit)? = null

    override fun onBind(intent: Intent?) = binder

    override fun onUnbind(intent: Intent?): Boolean {
        curbStatusListener = null
        return false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        doStart()
        return START_STICKY
    }


    override fun onDestroy() {
        super.onDestroy()
        doStop()
    }

    private fun stopService() {
        doStop()
        stopSelf()
    }


    private fun doStart() {
        onCurbStatusChange(CurbStatus.UNKNOWN)

        if (controller.start()) {
            startStatus = true
            controller.shouldCurbListener = {
                onCurbStatusChange(if (it) CurbStatus.CURBED else CurbStatus.NOT_CURBED)
            }
        } else {
            onCurbStatusChange(CurbStatus.ERROR)
            stopService()
        }
    }

    private fun doStop() {
        controller.stop()
        onCurbStatusChange(CurbStatus.NOT_STARTED)
    }

    private fun onCurbStatusChange(curbStatus: CurbStatus) {
        if (wasError && curbStatus == CurbStatus.NOT_STARTED) {
            return
        }

        wasError = (curbStatus == CurbStatus.ERROR)

        startStatus = when(curbStatus) {
            CurbStatus.NOT_STARTED, CurbStatus.ERROR -> false
            CurbStatus.UNKNOWN, CurbStatus.NOT_CURBED, CurbStatus.CURBED -> true
        }

        currentCurbStatus = curbStatus
        sendNotificationIfNeeded(curbStatus)
        curbStatusListener?.invoke(curbStatus)
    }

    private fun sendNotificationIfNeeded(curbStatus: CurbStatus) {
        when (curbStatus) {
            CurbStatus.NOT_STARTED -> notificationManager.cancel(notify_id);
            CurbStatus.NOT_CURBED -> sendNotification(R.string.not_curbed_short,
                    R.string.not_curbed_desc, R.drawable.ic_not_curb)
            CurbStatus.CURBED -> sendNotification(R.string.curbed_short,
                    R.string.curbed_desc, R.drawable.ic_curb)
            CurbStatus.UNKNOWN -> sendNotification(R.string.service_is_starting,
                    R.string.empty_string, R.drawable.ic_starting)
            CurbStatus.ERROR -> sendNotification(R.string.error, R.string.wifi_error_desc,
                    R.drawable.ic_error)
        }
    }

    private fun sendNotification(titleId: Int, textId: Int, iconId: Int) {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT)

        val res = resources
        val builder = Notification.Builder(this)

        builder.setContentIntent(pendingIntent)
                .setSmallIcon(iconId)
                .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.big_curber))
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentTitle(res.getString(titleId))
                .setContentText(res.getString(textId))

        val notification = builder.build()
        notificationManager.notify(notify_id, notification)
    }

    inner class CurbBinder : Binder() {
        val currentCurbStatus get() = this@MtFreeCurbService.currentCurbStatus
        val isStarted get() = this@MtFreeCurbService.startStatus

        fun setCurbStatusListener(value: ((CurbStatus)->Unit)?) {
            this@MtFreeCurbService.curbStatusListener = value
        }

        fun stop() {
            stopService()
        }
    }

}
