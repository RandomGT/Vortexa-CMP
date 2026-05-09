package com.vortexa.ui.page.teach.video

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * 1对1 视频直播页用户列表（Figma 283-31066）：水平排列，区分导师与学员。
 * 远端用户的昵称与头像由 [VideoViewModel] 按声网 uid 调用用户资料接口填充；「导师/学员」标签由资料中的
 * [com.vortexa.model.UserProfileInfo.teacherId] 与进房传入的本节课导师 ID 比对决定。
 *
 * @param users 用户项数据，每项含声网 uid、姓名、角色与头像
 */
@Composable
fun VideoRtcUserList(
    users: List<VideoRtcUser>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        users.forEach { user ->
            VideoRtcUserItem(
                name = user.name,
                isTutor = user.isTutor,
                avatarUrl = user.avatarUrl
            )
        }
    }
}

/**
 * 用户列表项数据：声网 uid、姓名、是否导师、头像 URL（可选）。
 * 远端展示名与头像来自 `GET /v/api/user/profile/{userId}`（userId 即 agoraUid）。
 */
data class VideoRtcUser(
    val agoraUid: Int,
    val name: String,
    val isTutor: Boolean,
    val avatarUrl: String? = null
)

@Composable
@Preview(backgroundColor = 0xFF101828)
private fun VideoRtcUserListPreview() {
    VideoRtcUserList(
        users = listOf(
            VideoRtcUser(agoraUid = 1001, name = "Mepo", isTutor = true),
            VideoRtcUser(agoraUid = 1002, name = "Niki", isTutor = false)
        )
    )
}
