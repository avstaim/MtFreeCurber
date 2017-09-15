package com.avstaim.mtfreecurber

import android.content.Context

class MtFreeCurbController(private val context: Context) {
    val stateController: MtFreeCurbStateController by lazy { MtFreeCurbStateController(context) }
    private val curber: MtFreeCurber by lazy { MtFreeCurber(context) }
    var shouldCurbListener: ((Boolean)->Unit)? = null

    var status = false
        private set

    fun start(): Boolean {
        if (status) return true
        if (!curber.init()) return false

        status = true
        stateController.shouldCurbListener = {
            updateCurb(it)
        }
        stateController.startListening()
        updateCurb(stateController.shouldCurb)

        return true
    }

    fun stop() {
        if (!status) return

        status = false
        stateController.shouldCurbListener = null
        stateController.stopListening()
        updateCurb(false)
    }

    private fun updateCurb(shouldCurb: Boolean) {
        if (shouldCurb) {
            curber.disableMtFree()
        } else {
            curber.enableMtFree()
        }
        shouldCurbListener?.invoke(shouldCurb)
    }
}