package com.vortexa.config

import com.vortexa.model.AuthResponse
import com.vortexa.model.UserInfo
import com.vortexa.util.sp.SpHelper

/**
 * 用户信息配置，用于持久化登录/注册接口返回的 userInfo
 *
 * @author LuXin
 * @createTime 2026/2/28
 */
object UserConfig {

    private const val KEY_USER_ID = "user_id"
    private const val KEY_AVATAR = "user_avatar"
    private const val KEY_NICKNAME = "user_nickname"
    private const val KEY_ROLE = "user_role"
    private const val KEY_TEACHER_ID = "user_teacher_id"

    private var cachedUserId: Long = 0
    private var cachedAvatar: String? = null
    private var cachedNickname: String? = null
    private var cachedRole: String? = null
    private var cachedTeacherId: Long = 0

    /**
     * 保存认证接口返回值（同时更新 TokenConfig 的 token）
     */
    fun saveFromAuthResponse(response: AuthResponse) {
        TokenConfig.updateToken(response.token)
        saveUserInfo(response.userInfoLogin)
    }

    /**
     * 仅保存用户信息
     */
    fun saveUserInfo(info: UserInfo) {
        cachedUserId = info.id
        cachedAvatar = info.avatar
        cachedNickname = info.nickname
        cachedRole = info.role
        SpHelper.putAll {
            putLong(KEY_USER_ID, info.id)
            putString(KEY_AVATAR, info.avatar ?: "")
            putString(KEY_NICKNAME, info.nickname ?: "")
            putString(KEY_ROLE, info.role ?: "")
        }
    }

    fun getUserId(): Long {
        if (cachedUserId == 0L) {
            cachedUserId = SpHelper.getLong(KEY_USER_ID, 0)
        }
        return cachedUserId
    }

    fun getAvatar(): String? {
        if (cachedAvatar == null) {
            val s = SpHelper.getString(KEY_AVATAR, "")
            cachedAvatar = if (s.isEmpty()) null else s
        }
        return cachedAvatar
    }

    fun getNickname(): String? {
        if (cachedNickname == null) {
            val s = SpHelper.getString(KEY_NICKNAME, "")
            cachedNickname = if (s.isEmpty()) null else s
        }
        return cachedNickname
    }

    fun getRole(): String? {
        if (cachedRole == null) {
            val s = SpHelper.getString(KEY_ROLE, "")
            cachedRole = if (s.isEmpty()) null else s
        }
        return cachedRole
    }

    /**
     * 持久化当前用户的教师 ID（来自个人中心接口）；无教师身份传 null 或 0 清除。
     */
    fun setTeacherId(id: Long?) {
        val v = id ?: 0L
        cachedTeacherId = v
        if (v == 0L) {
            SpHelper.remove(KEY_TEACHER_ID)
        } else {
            SpHelper.putLong(KEY_TEACHER_ID, v)
        }
    }

    /** 已保存的教师 ID，未设置或非教师为 0 */
    fun getTeacherId(): Long {
        if (cachedTeacherId == 0L) {
            cachedTeacherId = SpHelper.getLong(KEY_TEACHER_ID, 0L)
        }
        return cachedTeacherId
    }

    /**
     * 获取已保存的用户信息
     */
    fun getUserInfo(): UserInfo {
        return UserInfo(
            id = getUserId(),
            avatar = getAvatar(),
            nickname = getNickname(),
            role = getRole()
        )
    }

    /**
     * 清除用户信息（登出时调用，不清 token，由调用方决定是否清 TokenConfig）
     */
    fun clear() {
        cachedUserId = 0
        cachedAvatar = null
        cachedNickname = null
        cachedRole = null
        cachedTeacherId = 0
        SpHelper.remove(KEY_USER_ID)
        SpHelper.remove(KEY_AVATAR)
        SpHelper.remove(KEY_NICKNAME)
        SpHelper.remove(KEY_ROLE)
        SpHelper.remove(KEY_TEACHER_ID)
    }
}
