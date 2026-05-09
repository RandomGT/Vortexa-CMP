package com.vortexa.util

import com.vortexa.platform.AppToast

object ToastUtil {
    fun show(message: String) = AppToast.show(message)
    fun show(context: Any?, message: String) = AppToast.show(message)
    fun showLong(message: String) = AppToast.show(message)
    fun showLong(context: Any?, message: String) = AppToast.show(message)
}
