package com.vortexa.model

/**
 * 声网频道成员对应的业务用户信息。
 *
 * @param agoraUid 声网 uid
 * @param nickName 业务昵称
 * @param avatar 业务头像 URL
 * @param role 业务角色，接口接入后可用于区分导师/学员
 * @param teacherId 用户资料接口返回的导师 ID；与进房传入的课程 [teacherId] 一致则为导师
 */
data class RtcChannelUserProfile(
    val agoraUid: Int,
    val nickName: String?,
    val avatar: String?,
    val role: String? = null,
    val teacherId: Long? = null
)
