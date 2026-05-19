package com.vortexa.ui.page.creator.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.vortexa.ui.component.pageStatus.PageStatus
import com.vortexa.ui.component.pageStatus.PageStatusView
import com.vortexa.ui.viewmodel.vortexaViewModel

@Composable
fun DataCenterView(
    onBackClick: () -> Unit,
    onPostClick: (Long) -> Unit = {},
    viewModel: DataCenterViewModel = vortexaViewModel { DataCenterViewModel() },
    modifier: Modifier = Modifier,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val pageStatus by viewModel.pageStatus.collectAsState()
    val creatorData by viewModel.creatorData.collectAsState()
    val postList by viewModel.postList.collectAsState()
    val selectedDays by viewModel.selectedDays.collectAsState()
    val selectedSortBy by viewModel.selectedSortBy.collectAsState()
    val hasMorePosts by viewModel.hasMorePosts.collectAsState()
    val loadingMorePosts by viewModel.loadingMorePosts.collectAsState()
    var collapsed by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

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

    LaunchedEffect(listState, hasMorePosts, loadingMorePosts, pageStatus) {
        snapshotFlow {
            val info = listState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = info.totalItemsCount
            lastVisible to total
        }.collect { (lastVisible, total) ->
            if (
                pageStatus == PageStatus.Success &&
                hasMorePosts &&
                !loadingMorePosts &&
                total > 0 &&
                lastVisible >= total - 2
            ) {
                viewModel.loadMorePosts()
            }
        }
    }

    Box(
        modifier = modifier
            .background(Color.White)
            .fillMaxSize(),
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 20.dp),
        ) {
            item {
                DataCenterHeader(onBackClick = onBackClick)
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item {
                Column(modifier = Modifier.padding(horizontal = 18.dp)) {
                    DataOverviewCard(
                        data = creatorData,
                        selectedDays = selectedDays,
                        onDaysChange = { viewModel.setSelectedDays(it) },
                        collapsed = collapsed,
                        onCollapsedChange = { collapsed = !collapsed },
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            dataCenterPostListSection(
                selectedSortBy = selectedSortBy,
                list = postList,
                pageStatus = pageStatus,
                hasMorePosts = hasMorePosts,
                loadingMorePosts = loadingMorePosts,
                onSortByChange = { viewModel.setSortBy(it) },
                onItemClick = { onPostClick(it.postId) },
            )
        }

        PageStatusView(
            status = pageStatus,
            modifier = Modifier.fillMaxSize(),
            onRefresh = { viewModel.loadAll() },
        )
    }
}
