package com.vortexa.util

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.vortexa.VortexaApplication

/**
 * Toast 统一工具：支持无 Context 调用（使用 Application 上下文）及指定 Context；
 * 自动切换到主线程执行，可在任意线程调用。
 */
object ToastUtil {

    private val mainHandler = Handler(Looper.getMainLooper())

    /**
     * 使用 Application 上下文显示短 Toast，可在任意线程调用。
     * @param message 文案
     */
    @JvmStatic
    fun show(message: String) {
        show(message, Toast.LENGTH_SHORT)
    }

    /**
     * 使用 Application 上下文显示短/长 Toast。
     * @param message 文案
     * @param duration Toast.LENGTH_SHORT 或 Toast.LENGTH_LONG
     */
    @JvmStatic
    fun show(message: String, duration: Int) {
        val ctx = try {
            VortexaApplication.instance.applicationContext
        } catch (_: Throwable) {
            null
        } ?: return
        runOnMain { Toast.makeText(ctx, message, duration).show() }
    }

    /**
     * 使用指定 Context 显示短 Toast。
     * @param context 用于展示的 Context（建议使用 Activity 或 Application）
     * @param message 文案
     */
    @JvmStatic
    fun show(context: Context, message: String) {
        show(context, message, Toast.LENGTH_SHORT)
    }

    /**
     * 使用指定 Context 显示 Toast。
     * @param context 用于展示的 Context
     * @param message 文案
     * @param duration Toast.LENGTH_SHORT 或 Toast.LENGTH_LONG
     */
    @JvmStatic
    fun show(context: Context, message: String, duration: Int) {
        val ctx = context.applicationContext
        runOnMain { Toast.makeText(ctx, message, duration).show() }
    }

    /**
     * 使用指定 Context 显示字符串资源短 Toast。
     * @param context 用于展示的 Context
     * @param resId 字符串资源 id
     */
    @JvmStatic
    fun show(context: Context, resId: Int) {
        show(context, context.getString(resId), Toast.LENGTH_SHORT)
    }

    /**
     * 长 Toast（Application 上下文）。
     */
    @JvmStatic
    fun showLong(message: String) {
        show(message, Toast.LENGTH_LONG)
    }

    /**
     * 长 Toast（指定 Context）。
     */
    @JvmStatic
    fun showLong(context: Context, message: String) {
        show(context, message, Toast.LENGTH_LONG)
    }

    private fun runOnMain(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            block()
        } else {
            mainHandler.post(block)
        }
    }
}
