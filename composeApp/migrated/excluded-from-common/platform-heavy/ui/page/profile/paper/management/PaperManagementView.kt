package com.vortexa.ui.page.profile.paper.management

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vortexa.ui.component.DeletePostConfirmModal
import com.vortexa.ui.component.pageStatus.PageStatus
import com.vortexa.ui.component.pageStatus.PageStatusView

/**
 * 稿件管理页面主视图。
 * 包含头部、筛选栏和稿件列表，对接 GET /v/api/user/posts 接口。
 */
@Composable
fun PaperManagementView(
    viewModel: PaperManagementViewModel = viewModel()
) {
    val paperList by viewModel.paperList.collectAsState()
    val pageStatus by viewModel.pageStatus.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        var firstResume = true
        val observer = LifecycleEventObserver { _, event ->
            if (event != Lifecycle.Event.ON_RESUME) return@LifecycleEventObserver
            if (firstResume) {
                firstResume = false
            } else {
                viewModel.loadPosts(silent = true)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = { PaperManagementHeader() }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            PaperManagementFilter()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                PaperManagementList(viewModel = viewModel)
                PageStatusView(
                    status = when {
                        pageStatus == PageStatus.Success && paperList.isEmpty() -> PageStatus.Empty
                        else -> pageStatus
                    },
                    modifier = Modifier.fillMaxSize(),
                    emptyMessage = "暂无稿件",
                    showEmptyRefresh = true,
                    onRefresh = { viewModel.loadPosts() }
                )
            }
        }
    }
}

/**
 * 稿件列表组件。
 * 展示稿件列表，每项使用 [PaperManagementItem]，底部按钮均分宽度。
 */
@Composable
fun PaperManagementList(
    viewModel: PaperManagementViewModel = viewModel()
) {
    val paperList by viewModel.paperList.collectAsState()
    val deletingId by viewModel.deletingPostId.collectAsState()
    var deleteTarget by remember { mutableStateOf<PaperItemData?>(null) }
    LaunchedEffect(Unit) {
        viewModel.deletePostFinished.collect {
            deleteTarget = null
        }
    }
    val sampleButtons = listOf(
        PaperItemButton("数据", isPrimary = false),
        PaperItemButton("删除", isPrimary = false),
        PaperItemButton("编辑", isPrimary = true),
    )
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(paperList, key = { it.postId }) { item ->
                PaperManagementItem(
                    item = item,
                    buttons = sampleButtons,
                    onDeleteClick = { deleteTarget = item },
                )
            }
        }
        deleteTarget?.let { target ->
            DeletePostConfirmModal(
                onDismiss = { deleteTarget = null },
                onConfirm = { viewModel.deletePost(target.postId) },
                deleteLoading = deletingId != 0L && deletingId == target.postId
            )
        }
    }
}

/**
 * 预览组件
 */
@Composable
fun PaperManagementPreview() {
    PaperManagementView()
}
