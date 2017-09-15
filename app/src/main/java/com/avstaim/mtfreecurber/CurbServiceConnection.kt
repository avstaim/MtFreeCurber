package com.avstaim.mtfreecurber

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder

class CurbServiceConnection(private val context: MainActivity) : ServiceConnection {
    val curbServiceClass = MtFreeCurbService::class.java
    val intent get() = Intent(context, curbServiceClass)

    var binder: MtFreeCurbService.CurbBinder? = null
        private set

    val isBound get() = (binder != null)
    val isStarted get() = binder?.isStarted ?: false

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        binder = service as MtFreeCurbService.CurbBinder
        context.onBind()
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        binder = null
    }

    fun bind() = context.bindService(intent, this, Context.BIND_AUTO_CREATE);
    fun start() = context.startService(intent)
    fun unbind() = context.unbindService(this)
    fun stop() = binder?.stop()
}