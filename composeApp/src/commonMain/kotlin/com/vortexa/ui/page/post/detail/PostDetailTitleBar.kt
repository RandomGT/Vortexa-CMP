package com.vortexa.ui.page.post.detail
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vortexa.model.Post
import com.vortexa.ui.component.AvatarImage
import com.vortexa.ui.component.FollowButton
import com.vortexa.ui.component.FollowButtonSize
import com.vortexa.ui.component.PopupDropdownMenu
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import vortexa.composeapp.generated.resources.Res
import vortexa.composeapp.generated.resources.bookmark_line
import vortexa.composeapp.generated.resources.bookmark_selected
import vortexa.composeapp.generated.resources.ic_more_vert
import vortexa.composeapp.generated.resources.icon_back
import vortexa.composeapp.generated.resources.profile_default

/**
 * 帖子详情页 Title 栏（Figma 747-88745）
 * 包含：返回按钮、头像、姓名、关注按钮（发帖人为自己时隐藏）、收藏按钮、更多按钮
 *
 * @param post 帖子数据，用于展示头像和姓名
 * @param showFollowButton 是否显示关注按钮，发帖人为当前用户时应为 false
 * @param isFollowed 是否已关注
 * @param isCollect 是否已收藏
 * @param followLoading 关注请求加载态
 * @param unfollowLoading 取消关注请求加载态
 * @param onBackClick 返回点击
 * @param onFollowClick 关注点击回调
 * @param onUnfollowConfirm 取消关注确认回调
 * @param onBookmarkClick 收藏点击
 * @param isMyPost 是否为当前用户所发，决定「更多」菜单项（本人：编辑/删除；他人：只看TA）
 * @param onMoreEditClick 更多-编辑
 * @param onMoreDeleteClick 更多-删除
 * @param onlyTaFilterActive 他人帖子时是否已开启「只看 TA」评论筛选（菜单显示「取消只看TA」）
 * @param onMoreOnlyTaClick 更多-只看 TA 或取消只看 TA（由外部根据 [onlyTaFilterActive] 调用筛选/清除）
 * @param onAvatarClick 点击发帖人头像
 */
@Composable
fun PostDetailTitleBar(
    post: Post,
    showFollowButton: Boolean = true,
    isFollowed: Boolean = false,
    isCollect: Boolean = false,
    followLoading: Boolean = false,
    unfollowLoading: Boolean = false,
    isMyPost: Boolean = false,
    onBackClick: () -> Unit,
    onFollowClick: () -> Unit,
    onUnfollowConfirm: () -> Unit,
    onBookmarkClick: () -> Unit,
    onMoreEditClick: () -> Unit = {},
    onMoreDeleteClick: () -> Unit = {},
    onlyTaFilterActive: Boolean = false,
    onMoreOnlyTaClick: () -> Unit = {},
    onAvatarClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var moreMenuExpanded by remember { mutableStateOf(false) }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 返回按钮
        Icon(
            painter = painterResource(Res.drawable.icon_back),
            contentDescription = "返回",
            modifier = Modifier
                .size(24.dp)
                .clickable(onClick = onBackClick),
            tint = Colors.black_101828
        )

        // 头像：网络图或默认 Res.drawable.profile_default，post.avatar 来自 authorAvatar
        AvatarImage(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .clickable(onClick = onAvatarClick),
            avatarUrl = (post.avatar as? String).takeIf { !it.isNullOrBlank() },
            contentDescription = "发帖人头像",
            defaultResId = Res.drawable.profile_default
        )

        // 姓名
        Text(
            text = post.username,
            style = FontMedium(fontSize = 16, color = Colors.black_101828),
            modifier = Modifier.weight(1f),
            maxLines = 1,
            lineHeight = 20.sp
        )

        // 发帖人为自己时不显示关注按钮
        if (showFollowButton) {
            FollowButton(
                userId = post.userId,
                isFollowing = isFollowed,
                followLoading = followLoading,
                unfollowLoading = unfollowLoading,
                onFollowClick = onFollowClick,
                onUnfollowConfirm = onUnfollowConfirm,
                size = FollowButtonSize.Small
            )
        }

        // 收藏按钮
        Icon(
            painter = painterResource(
                if (isCollect) Res.drawable.bookmark_selected else Res.drawable.bookmark_line
            ),
            contentDescription = "收藏",
            modifier = Modifier
                .size(20.dp)
                .clickable(onClick = onBookmarkClick),
            tint = if (isCollect) Colors.gold_F6BD49 else Colors.black_101828
        )

        // 更多：与评论/回复 [PopupDropdownMenu] 同样式（白底圆角阴影浮层）
        Box {
            Icon(
                painter = painterResource(Res.drawable.ic_more_vert),
                contentDescription = "更多",
                modifier = Modifier
                    .size(20.dp)
                    .clickable { moreMenuExpanded = true },
                tint = Colors.black_101828
            )
            val moreOptions = if (isMyPost) {
                listOf("编辑", "删除")
            } else {
                listOf(if (onlyTaFilterActive) "取消只看TA" else "只看TA")
            }
            PopupDropdownMenu(
                modifier = Modifier.width(72.dp),
                expanded = moreMenuExpanded,
                onDismissRequest = { moreMenuExpanded = false },
                options = moreOptions,
                onOptionClick = { index ->
                    moreMenuExpanded = false
                    if (isMyPost) {
                        when (index) {
                            0 -> onMoreEditClick()
                            1 -> onMoreDeleteClick()
                        }
                    } else if (index == 0) {
                        onMoreOnlyTaClick()
                    }
                }
            )
        }
    }
}
