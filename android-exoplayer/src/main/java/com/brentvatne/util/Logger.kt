package com.brentvatne.util

import android.util.Log
import okhttp3.internal.toHexString

object Logger {
    private const val TAG = "Multiview"

    fun log(classObj: Any, message: String) {
        log("${classObj.javaClass.simpleName}@[${classObj.hashCode().toHexString()}]", message)
    }

    private fun log(tag: String, message: String) {
        Log.i(TAG, "$tag: $message")
    }

    fun warn(classObj: Any, message: String) {
        warn("${classObj.javaClass.simpleName}@[${classObj.hashCode().toHexString()}]", message)
    }

    private fun warn(tag: String, message: String) {
        Log.w(TAG, "$tag: $message")
    }
}