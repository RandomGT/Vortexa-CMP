package com.vortexa.router

/**
 * [AppSchemeRouter.open] 的执行结果，便于调用方埋点或降级。
 */
sealed class OpenResult {
    data object Success : OpenResult()
    data object Malformed : OpenResult()
    data object UnknownRoute : OpenResult()

    /** 已保存待跳转 URI 并拉起 [com.vortexa.ui.page.login.LoginActivity] */
    data object Unauthorized : OpenResult()
}
