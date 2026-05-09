package com.vortexa.session

import java.util.concurrent.atomic.AtomicBoolean

/**
 * 401 跳转登录的互斥与重置，供 [SessionUnauthorizedHandler] 与 [com.vortexa.config.TokenConfig] 共用，避免模块间循环依赖。
 */
internal object AuthNavGate {

    private val navigating = AtomicBoolean(false)

    fun reset() {
        navigating.set(false)
    }

    fun tryEnterUnauthorizedFlow(): Boolean = navigating.compareAndSet(false, true)
}
