package com.vortexa.util

import kotlin.time.Clock

/**
 * ============================================================
 *
 * @author LTT
 * date    2025/7/28
 * desc    描述
 * ============================================================
 **/
object FastClickUtils {
    private var lastClickTime = 0L
    private const val DEFAULT_INTERVAL = 500L  // 默认点击间隔 500ms

    fun isFastClick(interval: Long = DEFAULT_INTERVAL): Boolean {
        val currentTime = System.currentTimeMillis()
        val isFast = currentTime - lastClickTime < interval
        if (!isFast) {
            lastClickTime = currentTime
        }
        return isFast
    }
}
