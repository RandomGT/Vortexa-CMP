package com.vortexa.session

object AuthNavGate {
    private var navigating = false

    fun reset() {
        navigating = false
    }

    fun tryEnterUnauthorizedFlow(): Boolean {
        if (navigating) return false
        navigating = true
        return true
    }
}
