package com.vortexa.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontRegular
import vortexa.composeapp.generated.resources.Res

/**
 * 关注/已关注按钮。未关注时展示「关注」LoadingButton，点击调用关注接口；已关注时展示「已关注」，
 * 点击弹出取消关注确认弹窗，确认后调用取消关注接口。
 *
 * @param userId 目标用户 ID，用于关注/取消关注接口
 * @param isFollowing 是否已关注
 * @param followLoading 关注请求加载态（由 ViewModel 控制）
 * @param unfollowLoading 取消关注请求加载态（由 ViewModel 控制）
 * @param onFollowClick 点击关注时回调，调用方发起关注请求
 * @param onUnfollowConfirm 弹窗中点击确定时回调，调用方发起取消关注请求
 * @param size 尺寸规格
 * @param modifier 修饰符
 */
@Composable
fun FollowButton(
    userId: Long,
    isFollowing: Boolean,
    followLoading: Boolean,
    unfollowLoading: Boolean,
    onFollowClick: () -> Unit,
    onUnfollowConfirm: () -> Unit,
    size: FollowButtonSize = FollowButtonSize.Medium,
    modifier: Modifier = Modifier
) {
    val config = when (size) {
        FollowButtonSize.Small -> FollowButtonConfig(
            iconSize = 12.dp,
            fontSize = 10,
            paddingHorizontal = 8.dp,
            paddingVertical = 4.dp,
            cornerRadius = 16.dp
        )
        FollowButtonSize.Medium -> FollowButtonConfig(
            iconSize = 16.dp,
            fontSize = 14,
            paddingHorizontal = 12.dp,
            paddingVertical = 8.dp,
            cornerRadius = 20.dp
        )
    }

    var showUnfollowModal by remember { mutableStateOf(false) }
    var prevUnfollowLoading by remember { mutableStateOf<Boolean?>(null) }
    LaunchedEffect(unfollowLoading) {
        if (prevUnfollowLoading == true && !unfollowLoading) {
            showUnfollowModal = false
        }
        prevUnfollowLoading = unfollowLoading
    }

    if (isFollowing) {
        // 已关注：展示灰底「已关注」，点击弹出取消关注确认弹窗
        Row(
            modifier = modifier
                .clip(RoundedCornerShape(config.cornerRadius))
                .background(Colors.gray_EEF0F1)
                .clickable { showUnfollowModal = true }
                .padding(horizontal = config.paddingHorizontal, vertical = config.paddingVertical),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "已关注",
                style = FontRegular(fontSize = config.fontSize, Colors.black_101828)
            )
        }
    } else {
        // 未关注：使用 LoadingButton 展示「关注」，请求中显示 loading
        LoadingButton(
            modifier = modifier
                .clip(RoundedCornerShape(config.cornerRadius))
                .background(Colors.black_101828)
                .padding(horizontal = config.paddingHorizontal, vertical = config.paddingVertical),
            text = "关注",
            isLoading = followLoading,
            onClick = onFollowClick,
            content = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_add),
                        contentDescription = null,
                        modifier = Modifier.size(config.iconSize),
                        tint = Color.White
                    )
                    Text(
                        text = "关注",
                        style = FontRegular(fontSize = config.fontSize, Color.White)
                    )
                }
            }
        )
    }

    if (showUnfollowModal) {
        UnfollowConfirmModal(
            onDismiss = { showUnfollowModal = false },
            onConfirm = onUnfollowConfirm,
            unfollowLoading = unfollowLoading
        )
    }
}

/** 关注按钮尺寸规格 */
enum class FollowButtonSize {
    /** 紧凑尺寸，用于评论等场景 */
    Small,
    /** 标准尺寸，用于标题栏 */
    Medium
}

private data class FollowButtonConfig(
    val iconSize: Dp,
    val fontSize: Int,
    val paddingHorizontal: Dp,
    val paddingVertical: Dp,
    val cornerRadius: Dp
)

@Composable
@Preview(showBackground = true)
private fun FollowButtonPreview() {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        FollowButton(
            userId = 1L,
            isFollowing = false,
            followLoading = false,
            unfollowLoading = false,
            onFollowClick = {},
            onUnfollowConfirm = {},
            size = FollowButtonSize.Small
        )
        FollowButton(
            userId = 1L,
            isFollowing = true,
            followLoading = false,
            unfollowLoading = false,
            onFollowClick = {},
            onUnfollowConfirm = {},
            size = FollowButtonSize.Small
        )
    }
}
