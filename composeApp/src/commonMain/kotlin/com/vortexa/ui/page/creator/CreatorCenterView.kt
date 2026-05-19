package com.vortexa.ui.page.creator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.vortexa.config.UserConfig
import com.vortexa.model.CreatorCertification
import com.vortexa.ui.component.pageStatus.PageStatusView
import com.vortexa.ui.viewmodel.vortexaViewModel
import vortexa.composeapp.generated.resources.Res
import vortexa.composeapp.generated.resources.icon_data
import vortexa.composeapp.generated.resources.icon_msg
import vortexa.composeapp.generated.resources.icon_page

private val certificationTagDefaultColors = listOf(
    Color(0xFF8DD3FF),
    Color(0xFFFFCFA3),
    Color(0xFFAEECC2),
    Color(0xFFFFC6D9),
    Color(0xFFD6C4FF),
)

private fun buildCertificationTags(
    certifications: List<CreatorCertification>?,
): List<CreatorCenterHeaderTagItem> =
    certifications
        .orEmpty()
        .mapNotNull { it.name.takeIf(String::isNotBlank) }
        .mapIndexed { index, name ->
            CreatorCenterHeaderTagItem(
                text = name,
                backgroundColor = certificationTagDefaultColors[index % certificationTagDefaultColors.size],
            )
        }

/**
 * 创作者中心主页面：标题栏 + 近 x 日数据 + 快捷入口 + 有奖活动横幅 + 任务激励。
 */
@Composable
fun CreatorCenterView(
    onBackClick: () -> Unit,
    onDataCenterClick: () -> Unit = {},
    onInteractionClick: () -> Unit = {},
    onPaperManagementClick: () -> Unit = {},
    viewModel: CreatorViewModel = vortexaViewModel { CreatorViewModel() },
    modifier: Modifier = Modifier,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val pageStatus by viewModel.pageStatus.collectAsState()
    val creatorData by viewModel.creatorData.collectAsState()
    val creatorUserInfo by viewModel.creatorUserInfo.collectAsState()
    val activities by viewModel.activities.collectAsState()
    val tasks by viewModel.tasks.collectAsState()

    val headerNickname = creatorUserInfo?.userName?.takeIf { it.isNotBlank() }
        ?: UserConfig.getNickname()?.takeIf { it.isNotBlank() }
        ?: "Capper"
    val headerAvatarUrl = creatorUserInfo?.userAvatar?.takeIf { it.isNotBlank() }

    DisposableEffect(lifecycleOwner) {
        var firstResume = true
        val observer = LifecycleEventObserver { _, event ->
            if (event != Lifecycle.Event.ON_RESUME) return@LifecycleEventObserver
            if (firstResume) {
                firstResume = false
            } else {
                viewModel.loadAll(silent = true)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(modifier = modifier.fillMaxSize().background(Color.White)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(Color.White),
        ) {
            CreatorCenterHeader(
                onBackClick = onBackClick,
                nickname = headerNickname,
                tags = buildCertificationTags(creatorUserInfo?.certifications),
                avatarUrl = headerAvatarUrl,
            )
            Column(
                modifier = Modifier
                    .padding(horizontal = 18.dp)
                    .padding(top = 16.dp, bottom = 20.dp),
            ) {
                CreatorCenterDataCard(
                    days = creatorData?.days ?: 7,
                    postCount = creatorData?.postCount ?: 0,
                    viewCount = creatorData?.viewCount ?: 0,
                    likeCount = creatorData?.likeCount ?: 0,
                    commentCount = creatorData?.commentCount ?: 0,
                )
                Spacer(modifier = Modifier.height(16.dp))
                CreatorCenterQuickEntry(
                    entries = listOf(
                        CreatorQuickEntryItem("稿件管理", Res.drawable.icon_page, onPaperManagementClick),
                        CreatorQuickEntryItem("数据中心", Res.drawable.icon_data, onDataCenterClick),
                        CreatorQuickEntryItem("互动管理", Res.drawable.icon_msg, onInteractionClick),
                    ),
                )
                Spacer(modifier = Modifier.height(16.dp))
                CreatorCenterBannerSection(activities = activities)
                Spacer(modifier = Modifier.height(16.dp))
                CreatorCenterTaskSection(tasks = tasks)
            }
        }
        PageStatusView(
            status = pageStatus,
            modifier = Modifier.fillMaxSize(),
            onRefresh = { viewModel.loadAll() },
        )
    }
}
