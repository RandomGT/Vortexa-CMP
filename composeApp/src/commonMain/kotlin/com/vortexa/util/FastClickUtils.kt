package com.vortexa.util

import kotlin.time.Clock
import kotlin.time.ExperimentalTime

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

    @OptIn(ExperimentalTime::class)
    fun isFastClick(interval: Long = DEFAULT_INTERVAL): Boolean {
        val currentTime = Clock.System.now().toEpochMilliseconds()
        val isFast = currentTime - lastClickTime < interval
        if (!isFast) {
            lastClickTime = currentTime
        }
        return isFast
    }
}
