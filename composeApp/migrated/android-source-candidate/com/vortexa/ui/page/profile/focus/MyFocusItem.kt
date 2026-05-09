package com.vortexa.ui.page.profile.focus

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vortexa.ui.component.FollowButton
import com.vortexa.ui.component.FollowButtonSize
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.ui.theme.FontRegular
import com.vortexa.util.extension.click

/**
 * 我的关注列表项。
 * 左侧头像，中间昵称+简介，右侧关注状态按钮。
 *
 * @param userId 目标用户 ID
 * @param nickname 用户昵称
 * @param bio 用户简介/签名
 * @param isFollowing 是否已关注
 * @param followLoading 关注请求加载态
 * @param unfollowLoading 取消关注请求加载态
 * @param onFollowClick 关注点击回调
 * @param onUnfollowConfirm 取消关注确认回调
 * @param onItemClick 整条点击回调
 */
@Composable
fun MyFocusItem(
    userId: Long,
    nickname: String,
    bio: String,
    isFollowing: Boolean,
    followLoading: Boolean,
    unfollowLoading: Boolean,
    onFollowClick: () -> Unit,
    onUnfollowConfirm: () -> Unit,
    onItemClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .click(onClickListener = onItemClick)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 头像 40dp
        Row(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Colors.gray_F3F5F7),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = nickname.firstOrNull()?.toString() ?: "?",
                style = FontMedium(fontSize = 16, color = Colors.gray_6A7282)
            )
        }

        // 中间文本区域
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = nickname,
                style = FontMedium(fontSize = 15, color = Colors.black_101828),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (bio.isNotEmpty()) {
                Text(
                    text = bio,
                    style = FontRegular(fontSize = 13, color = Colors.gray_6A7282),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        // 右侧按钮：关注/已关注，统一使用 FollowButton
        FollowButton(
            userId = userId,
            isFollowing = isFollowing,
            followLoading = followLoading,
            unfollowLoading = unfollowLoading,
            onFollowClick = onFollowClick,
            onUnfollowConfirm = onUnfollowConfirm,
            size = FollowButtonSize.Small
        )
    }
}

@Composable
@Preview(backgroundColor = 0xFFFFFFFF, showBackground = true)
private fun MyFocusItemPreview() {
    Column {
        MyFocusItem(
            userId = 1L,
            nickname = "用户昵称",
            bio = "这是用户简介，超出部分会显示省略号...",
            isFollowing = true,
            followLoading = false,
            unfollowLoading = false,
            onFollowClick = {},
            onUnfollowConfirm = {},
            onItemClick = {}
        )
        MyFocusItem(
            userId = 2L,
            nickname = "未关注用户",
            bio = "简介内容",
            isFollowing = false,
            followLoading = false,
            unfollowLoading = false,
            onFollowClick = {},
            onUnfollowConfirm = {},
            onItemClick = {}
        )
    }
}
