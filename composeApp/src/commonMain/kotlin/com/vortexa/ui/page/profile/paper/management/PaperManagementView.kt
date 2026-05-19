package com.vortexa.ui.page.profile.paper.management

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.vortexa.ui.component.DeletePostConfirmModal
import com.vortexa.ui.component.pageStatus.PageStatus
import com.vortexa.ui.component.pageStatus.PageStatusView
import com.vortexa.ui.theme.belowStatusBar
import com.vortexa.ui.viewmodel.vortexaViewModel
import kotlinx.coroutines.flow.collect

private val PaperManagementButtons = listOf(
    PaperItemButton("数据", isPrimary = false),
    PaperItemButton("删除", isPrimary = false),
    PaperItemButton("编辑", isPrimary = true),
)

/**
 * 稿件管理页面主视图。
 *
 * @param onBackClick 点击返回回调
 * @param onPostClick 点击稿件内容进入详情回调
 * @param onDataClick 点击「数据」回调
 * @param onEditClick 点击「编辑」回调
 */
@Composable
fun PaperManagementView(
    onBackClick: () -> Unit = {},
    onPostClick: (PaperItemData) -> Unit = {},
    onDataClick: (PaperItemData) -> Unit = {},
    onEditClick: (PaperItemData) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: PaperManagementViewModel = vortexaViewModel { PaperManagementViewModel() }
) {
    val paperList by viewModel.paperList.collectAsState()
    val pageStatus by viewModel.pageStatus.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val deletingId by viewModel.deletingPostId.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    var deleteTarget by remember { mutableStateOf<PaperItemData?>(null) }

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

    LaunchedEffect(Unit) {
        viewModel.deletePostFinished.collect {
            deleteTarget = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .belowStatusBar()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            PaperManagementHeader(onBackClick = onBackClick)
            PaperManagementFilter(
                filters = viewModel.paperFilters,
                selectedIndex = selectedFilter,
                onFilterClick = viewModel::onFilterClick
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(paperList, key = { it.postId }) { item ->
                        PaperManagementItem(
                            item = item,
                            buttons = PaperManagementButtons,
                            onPostClick = onPostClick,
                            onDataClick = onDataClick,
                            onDeleteClick = { deleteTarget = it },
                            onEditClick = onEditClick,
                        )
                    }
                }
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

        deleteTarget?.let { target ->
            DeletePostConfirmModal(
                onDismiss = { deleteTarget = null },
                onConfirm = { viewModel.deletePost(target.postId) },
                deleteLoading = deletingId != 0L && deletingId == target.postId
            )
        }
    }
}
