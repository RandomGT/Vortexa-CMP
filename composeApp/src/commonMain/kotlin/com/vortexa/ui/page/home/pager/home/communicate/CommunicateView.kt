package com.vortexa.ui.page.home.pager.home.communicate

import android.content.Context
import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.unit.dp
import com.vortexa.ui.viewmodel.vortexaViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.vortexa.ui.page.post.create.PostCreateActivity
import com.vortexa.ui.page.home.pager.home.HomeCommunicateNavigation
import com.vortexa.ui.page.home.pager.home.recommend.PostItem
import com.vortexa.ui.theme.Colors
import com.vortexa.util.extension.click
import vortexa.composeapp.generated.resources.Res
import vortexa.composeapp.generated.resources.icon_edit

/**
 * 交流页：上方 Filter 按分区筛选热帖，下方为 Post 列表（分页、每页 4 条），样式与 [PostItem] 一致。
 * 列表接近底部时上拉自动加载下一页；仅首屏布局在首帖与余帖之间穿插导师推荐区，后续页数据直接追加、不再穿插。
 * 支持下拉刷新当前分区列表。右下角发帖入口以 Activity Result 打开 [PostCreateActivity]，发帖成功返回后静默刷新。
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CommunicateView(
    isActiveTab: Boolean = true,
    viewModel: CommunicateViewModel = vortexaViewModel { CommunicateViewModel() }
) {
    val context = Context()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(isActiveTab) {
        if (!isActiveTab) return@LaunchedEffect
        val pending = HomeCommunicateNavigation.consumePendingCommunicatePostType() ?: return@LaunchedEffect
        viewModel.loadPosts(pending)
    }

    DisposableEffect(lifecycleOwner, isActiveTab) {
        if (!isActiveTab) {
            onDispose { }
        } else {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    val pending = HomeCommunicateNavigation.consumePendingCommunicatePostType() ?: return@LifecycleEventObserver
                    viewModel.loadPosts(pending)
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }
    }

    val createPostLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Log.i(TAG, "post create finished with OK, refresh communicate list (silent)")
            viewModel.refresh(showRefreshing = false)
        }
    }
    val selectedPostType by viewModel.selectedPostType.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    Box {
        PullToRefreshBox(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() }
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                CommunicateFilter(
                    selectedPostType = selectedPostType,
                    onSelect = { viewModel.selectPostType(it) }
                )
                CommunicatePost(
                    viewModel = viewModel,
                    scrollToTopWhenActive = isActiveTab
                )
            }
        }

        Box(
            Modifier
                .padding(end = 18.dp, bottom = 26.dp)
                .size(48.dp)
                .background(Colors.black_101828, CircleShape)
                .align(Alignment.BottomEnd)
                .click {
                    createPostLauncher.launch(Intent(context, PostCreateActivity::class))
                }
        ) {
            Image(
                painterResource(Res.drawable.icon_edit), contentDescription = null,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
fun CommunicatePreview() {
    CommunicateView()
}

private const val TAG = "CommunicateView"
