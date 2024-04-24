package com.example.mypistureeditor.utils

import android.content.Context
import android.graphics.PointF
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import kotlin.math.sqrt

fun logger(msg: String, tag: String = "TTT") {
    Log.d(tag, msg)
}

infix fun PointF.distance(pointF: PointF): Float = sqrt((this.x - pointF.x).square() - (this.y - pointF.y).square())

fun View.showSoftKeyboard() {
    post {
        if (this.requestFocus()) {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
        }
    }
}

fun Float.square(): Float = this * this

class OnSingleClickListener : View.OnClickListener {

    private val onClickListener: View.OnClickListener
    constructor(listener: View.OnClickListener) {
        onClickListener = listener
    }
    constructor(listener: (View) -> Unit) {
        onClickListener = View.OnClickListener { listener.invoke(it) }
    }
    override fun onClick(v: View) {
        val currentTimeMillis = System.currentTimeMillis()

        if (currentTimeMillis >= previousClickTimeMillis + DELAY_MILLIS) {
            previousClickTimeMillis = currentTimeMillis
            onClickListener.onClick(v)
        }
    }
    companion object {
        private const val DELAY_MILLIS = 500L
        private var previousClickTimeMillis = 0L
    }
}
fun View.setOnSingleClickListener(l: View.OnClickListener) {
    setOnClickListener(OnSingleClickListener(l))
}

fun View.setOnSingleClickListener(l: (View) -> Unit) {
    setOnClickListener(OnSingleClickListener(l))
}
