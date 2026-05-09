package com.vortexa.ui.page.home.pager.profile

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.DrawableResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.vortexa.ui.component.pageStatus.PageStatusView
import com.vortexa.ui.viewmodel.vortexaViewModel
import com.vortexa.ui.page.profile.paper.management.PaperManagementActivity
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.ui.component.AvatarImage
import com.vortexa.ui.component.LogoutConfirmModal
import com.vortexa.ui.theme.FontRegular
import com.vortexa.util.ToastUtil
import com.vortexa.util.extension.click
import com.vortexa.util.extension.routeToPage
import vortexa.composeapp.generated.resources.Res
import vortexa.composeapp.generated.resources.edit
import vortexa.composeapp.generated.resources.file_chart
import vortexa.composeapp.generated.resources.profile_default

/**
 * 个人资料页，含背景、头像、昵称、统计数据（Figma 746-69757 / 746-69775）
 *
 * @param isSelected 当前是否处于首页「我的」Tab；首次选中时 [loadUserCenterInfo]，离开后再次选中时静默 [refresh]。
 *
 * @author LuXin
 * @createTime 2026/1/19
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ProfileView(isSelected: Boolean = true) {
    var showEditModal by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }
    val viewModel = vortexaViewModel { ProfileViewModel() }
    val context = Context()
    var wasHidden by remember { mutableStateOf(false) }
    var hasLoadedOnce by remember { mutableStateOf(false) }
    LaunchedEffect(isSelected) {
        if (!isSelected) {
            wasHidden = true
            return@LaunchedEffect
        }
        if (!hasLoadedOnce) {
            hasLoadedOnce = true
            viewModel.loadUserCenterInfo()
        } else if (wasHidden) {
            viewModel.refresh(showRefreshing = false)
        }
    }

    val pageStatus by viewModel.pageStatus.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val userCenterInfo by viewModel.userCenterInfo.collectAsState()
    val userInfo = userCenterInfo?.userInfo
    val stats = userCenterInfo?.stats

    PullToRefreshBox(
        modifier = Modifier.fillMaxSize(),
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refresh() }
    ) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
                Modifier
                    .background(Color.White)
                    .fillMaxHeight()
            ) {
                ProfileBG(onLogoutClick = { showLogoutConfirm = true })
                Column(
                    modifier = Modifier.offset(y = -40.dp)
                ) {
                    ProfileHead(
                        modifier = Modifier
                            .zIndex(20f)
                            .align(Alignment.CenterHorizontally),
                        nickname = userInfo?.userName ?: "未登录",
                        avatarUrl = userInfo?.userAvatar,
                        onEditClick = { showEditModal = true }
                    )
                    // 统计数据行：发帖、获赞、关注、粉丝（Figma 746-69775）
                    ProfileStats(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 24.dp),
                        postCount = stats?.postCount ?: 0,
                        likeCount = stats?.likeCount ?: 0,
                        followCount = stats?.followCount ?: 0,
                        fanCount = stats?.fanCount ?: 0
                    )
                // 功能卡片：我的钱包、我的课程（Figma 746-69789）
                ProfileCardsRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    onWalletClick = {
                        ToastUtil.show(context, "即将上线，敬请期待")
                    },
                    onCourseClick = {
                        viewModel.jumpToCourse(context)
                    }
                )
                // 功能入口行：创作中心、互动管理、我的收藏、浏览记录（Figma 746-69802）
                ProfileMenuRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp),
                    onItemClick = { _, index ->
                        when (index) {
                            0 -> viewModel.jumpToCreator(context)
                            1 -> viewModel.jumpToInteraction(context)
                            2 -> viewModel.jumpToCollection(context)
                            3 -> viewModel.jumpToHistory(context)
                        }
                    }
                )
                // 第二行功能：稿件管理（付费推广、客服中心暂不开放）
                ProfileSecondMenuRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 12.dp),
                    onFileChartClick = {
                        context.routeToPage(PaperManagementActivity::class)
                    }
                )
            }
        }
        PageStatusView(
            status = pageStatus,
            modifier = Modifier.fillMaxSize(),
            onRefresh = { viewModel.loadUserCenterInfo() }
        )
    }
    }
    if (showEditModal) {
            val confirmLoading by viewModel.confirmLoading.collectAsState()
            val updateSuccess by viewModel.updateProfileSuccess.collectAsState()
            LaunchedEffect(updateSuccess) {
                if (updateSuccess) {
                    showEditModal = false
                    viewModel.resetUpdateProfileSuccess()
                    viewModel.loadUserCenterInfo()
                }
            }
            EditProfileModal(
                avatarUrl = userInfo?.userAvatar,
                currentUsername = userInfo?.userName ?: "",
                confirmLoading = confirmLoading,
                onDismiss = { showEditModal = false },
                onAvatarSelected = { /* Modal 内部保存，确认时一并提交 */ },
                onConfirm = { newName, avatarUri ->
                    val uid = userInfo?.userId ?: return@EditProfileModal
                    val currentName = userInfo?.userName
                    viewModel.updateUserCenter(uid, newName, avatarUri, currentName, context)
                }
            )
        }

    if (showLogoutConfirm) {
        LogoutConfirmModal(
            onDismiss = { showLogoutConfirm = false },
            onConfirm = {
                showLogoutConfirm = false
                viewModel.logout(context)
            }
        )
    }
}

/**
 * 第二行功能（Figma 746-69827）：稿件管理；
 * 白底圆角卡片，左侧彩色圆图标+右侧文字。
 */
@Composable
fun ProfileSecondMenuRow(
    modifier: Modifier = Modifier,
    onFileChartClick: () -> Unit
) {
    ProfileSecondMenuItem(
        modifier = modifier,
        title = "稿件管理",
        iconRes = Res.drawable.file_chart,
        iconBgColor = Colors.blue_3266FF,
        onClick = onFileChartClick
    )
}

/**
 * 单个第二行功能入口：左侧彩色圆+白色图标，右侧深色文字。
 */
@Composable
private fun ProfileSecondMenuItem(
    modifier: Modifier = Modifier,
    title: String,
    iconRes: DrawableResource,
    iconBgColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Colors.gray_F8F9FA)
            .click(onClick)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconBgColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = title,
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
            }
            Text(
                text = title,
                style = FontRegular(fontSize = 14, Colors.black_242424),
                lineHeight = 20.sp
            )
        }
    }
}


/**
 * 用户统计数据行（Figma 746-69775）：发帖、获赞、关注、粉丝，水平均匀排列。
 *
 * @param postCount 发帖数
 * @param likeCount 获赞数
 * @param followCount 关注数
 * @param fanCount 粉丝数
 */
@Composable
fun ProfileStats(
    modifier: Modifier = Modifier,
    postCount: Int = 0,
    likeCount: Int = 0,
    followCount: Int = 0,
    fanCount: Int = 0
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProfileStatItem(count = postCount, label = "发帖")
        ProfileStatItem(count = likeCount, label = "获赞")
        ProfileStatItem(count = followCount, label = "关注")
        ProfileStatItem(count = fanCount, label = "粉丝")
    }
}

/**
 * 单个统计项：上方数字，下方标签，居中对齐。
 */
@Composable
private fun ProfileStatItem(count: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            style = FontMedium(fontSize = 18, color = Colors.black_242424),
            lineHeight = 24.sp
        )
        Text(
            text = label,
            style = FontRegular(fontSize = 14, color = Colors.gray_6A7282),
            lineHeight = 20.sp
        )
    }
}

@Composable
fun ProfileHead(
    modifier: Modifier = Modifier,
    nickname: String,
    avatarUrl: String?,
    onEditClick: () -> Unit = {}
) {
    Column(modifier,
        horizontalAlignment = Alignment.CenterHorizontally) {
        // 有用户 avatarUrl 时展示该 URL 的网络图像，否则默认图
        AvatarImage(
            modifier = Modifier
                .size(80.dp)
                .border(4.dp, Color.White, shape = CircleShape)
                .clip(CircleShape)
                .click(onEditClick),
            avatarUrl = avatarUrl?.takeIf { it.isNotBlank() },
            contentDescription = "用户头像",
            defaultResId = Res.drawable.profile_default
        )
        Row(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 10.dp)
                .height(28.dp)
                .click(onEditClick),
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = nickname,
                style = FontRegular(20, Colors.black_242424)
            )
            Image(
                painterResource(Res.drawable.edit),
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(20.dp),
                contentDescription = ""
            )
        }
    }
}

@Composable
fun ProfileViewPreview() {
    com.vortexa.ui.theme.BaseTheme { ProfileView() }
}
