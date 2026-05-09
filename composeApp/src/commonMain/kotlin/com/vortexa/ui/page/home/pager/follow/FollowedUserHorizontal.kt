package com.vortexa.ui.page.home.pager.follow

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vortexa.model.FollowedUser
import com.vortexa.ui.component.AvatarImage
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontRegular
import vortexa.composeapp.generated.resources.Res
import vortexa.composeapp.generated.resources.profile_default

/**
 * 单个已关注用户项（Figma 747-85100 Story-uihut）
 * 48dp 圆形头像、可选小红点、12px 用户名。
 *
 * @param user 用户数据
 * @param onClick 点击回调
 */
@Composable
fun FollowedUserItem(
    user: FollowedUser,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier.size(54.dp),
            contentAlignment = Alignment.Center
        ) {
            // 头像 48dp 圆形：有 avatar 则加载网络图，否则昵称首字占位
            val avatarUrl = user.avatar?.takeIf { it.isNotBlank() }
            if (avatarUrl != null) {
                AvatarImage(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    avatarUrl = avatarUrl,
                    contentDescription = user.nickname,
                    defaultResId = Res.drawable.profile_default
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Colors.black_101828),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.nickname.firstOrNull()?.toString() ?: "?",
                        style = FontRegular(fontSize = 20, color = Color.White)
                    )
                }
            }
            // 新动态小红点（Figma 747-85109）
            if (user.hasNewPost) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .align(Alignment.TopEnd)
                        .padding(top = 2.dp, end = 2.dp)
                        .clip(CircleShape)
                        .background(Colors.red_FF383C)
                )
            }
        }
        Text(
            text = user.nickname,
            style = FontRegular(fontSize = 12, color = Colors.black_101828),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * 已关注用户横向滚动区域（Figma 747-85098 Story-uihut）
 * padding: pl-14 pr-18 pt-8 pb-12
 *
 * @param users 已关注用户列表
 * @param onUserClick 用户项点击回调
 */
@Composable
fun FollowedUserHorizontal(
    users: List<FollowedUser>,
    onUserClick: (FollowedUser) -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 14.dp,
            end = 18.dp,
            top = 8.dp,
            bottom = 12.dp
        )
    ) {
        items(users, key = { it.userId }) { user ->
            FollowedUserItem(
                user = user,
                onClick = { onUserClick(user) }
            )
        }
    }
}

@Composable
private fun FollowedUserHorizontalPreview() {
    FollowedUserHorizontal(
        users = listOf(
            FollowedUser(1L, "Allen", null, false),
            FollowedUser(2L, "Capper", null, true)
        )
    )
}
