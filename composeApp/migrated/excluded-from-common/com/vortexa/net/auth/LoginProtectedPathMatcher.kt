package com.vortexa.net.auth

/**
 * 与 [docs/login-requested-apis.md] 一致：无 token 时不应发出的请求。
 * 运行时路径为 Retrofit 的 `/v/api/...`（文档中的 `/api/...` 会规范化后匹配）。
 */
object LoginProtectedPathMatcher {

    fun requiresLogin(httpMethod: String, encodedPath: String): Boolean {
        val path = normalizePath(encodedPath)
        val method = httpMethod.uppercase()
        if (isAnonymousAccountPath(path)) return false
        if (matchesAccountProtected(path)) return true
        if (matchesCommunicate(method, path)) return true
        if (matchesUser(method, path)) return true
        if (matchesMessage(path)) return true
        if (matchesDynamic(path)) return true
        if (matchesC2c(method, path)) return true
        return false
    }

    private fun normalizePath(encodedPath: String): String {
        var p = encodedPath.trim().ifEmpty { "/" }
        if (!p.startsWith("/")) p = "/$p"
        if (p.startsWith("/api/") && !p.startsWith("/v/api/")) {
            p = "/v$p"
        }
        return p.trimEnd('/').ifEmpty { "/" }
    }

    /** 登录前允许的账号类接口 */
    private fun isAnonymousAccountPath(path: String): Boolean {
        return path.startsWith("/v/api/account/auth") ||
            path.startsWith("/v/api/account/sms/") ||
            path.startsWith("/v/api/account/reset")
    }

    private fun matchesAccountProtected(path: String): Boolean {
        return path.startsWith("/v/api/account/logout") ||
            path.startsWith("/v/api/account/token/refresh")
    }

    private fun matchesCommunicate(method: String, path: String): Boolean {
        if (method != "POST") return false
        return path == "/v/api/home/post/insert" ||
            path == "/v/api/home/post/image" ||
            path == "/v/api/home/comment/image" ||
            path == "/v/api/home/discussion/comments"
    }

    private fun isUserCenterPublicGet(method: String, path: String): Boolean {
        if (method != "GET") return false
        return path == "/v/api/user/center/info/posts" ||
            path == "/v/api/user/center/info/comments"
    }

    private fun matchesUser(method: String, path: String): Boolean {
        if (path.startsWith("/v/api/user/like/")) return true
        if (path.startsWith("/v/api/user/collect/")) return true
        if (path.startsWith("/v/api/user/follow/")) return true
        if (path.startsWith("/v/api/user/center/")) {
            if (isUserCenterPublicGet(method, path)) return false
            return true
        }
        if (method == "POST" && path == "/v/api/user/avatar") return true
        if (path.startsWith("/v/api/user/collections")) return true
        if (method == "POST" && path.startsWith("/v/api/user/post/promote")) return true
        if (path.startsWith("/v/api/user/posts")) return true
        if (method == "GET" && path == "/v/api/user/creator/activities") return true
        if (path.startsWith("/v/api/user/creator/data/")) return true
        if (method == "GET" && path == "/v/api/user/creator/tasks") return true
        if (method == "GET" && path == "/v/api/user/wallet/point") return true
        if (method == "GET" && path == "/v/api/user/wallet/coin") return true
        if (method == "POST" && path == "/v/api/user/interactions") return true
        if (path.startsWith("/v/api/user/viewHistory")) return true
        return false
    }

    private fun matchesMessage(path: String): Boolean =
        path.startsWith("/v/api/message/")

    private fun matchesDynamic(path: String): Boolean =
        path.startsWith("/v/api/dynamic/")

    private fun matchesC2c(method: String, path: String): Boolean {
        if (path.startsWith("/v/api/c2c/teacher/reserve/time")) return false
        if (path.startsWith("/v/api/c2c/teacher/reserve")) return true
        if (path.startsWith("/v/api/c2c/teacher/session/")) return true
        if (path == "/v/api/c2c/token") return true
        if (path.startsWith("/v/api/c2c/teacher/my")) return true
        if (method == "POST" && path.startsWith("/v/api/c2c/teacher/batch/")) return true
        return false
    }
}
