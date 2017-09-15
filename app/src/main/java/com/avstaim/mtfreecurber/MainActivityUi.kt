package com.avstaim.mtfreecurber

import android.widget.Button
import android.widget.TextView
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

@Suppress("EXPERIMENTAL_FEATURE_WARNING")
class MainActivityUi : AnkoComponent<MainActivity> {
    private var textView: TextView? = null
    private var button: Button? = null

    override fun createView(ui: AnkoContext<MainActivity>) = with(ui) {
        verticalLayout {
            imageView(R.drawable.big_curber) {
                padding = dip(20)
            }.lparams(width = dip(250), height = dip(250))
            textView = textView(R.string.service_not_started)
                    .lparams(width = matchParent, weight = 1f)
            button = button(R.string.start_curb) {
                onClick {
                    owner.toggle()
                }
            }.lparams(width = matchParent, height = wrapContent)
        }
    }

    fun setText(resId: Int) = textView?.setText(resId)
    fun setButtonText(resId: Int) = button?.setText(resId)

    fun setTexts(textId: Int, buttonId: Int) {
        setText(textId)
        setButtonText(buttonId)
    }

    fun setButtonEnabled(enabled: Boolean) {
        button?.isEnabled = enabled
    }
}