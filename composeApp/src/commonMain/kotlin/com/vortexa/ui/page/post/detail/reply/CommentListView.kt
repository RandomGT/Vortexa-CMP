package com.vortexa.ui.page.post.detail.reply

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vortexa.ui.component.ListEndFooter
import com.vortexa.ui.theme.Colors
import com.vortexa.util.formatPostInteractionCount
import com.vortexa.ui.theme.FontMedium
import kotlinx.coroutines.flow.collect

/** 假数据 */
private fun fakeComments() = listOf(
    Comment(
        id = "c1",
        authorName = "张三",
        userId = 1001L,
        isAuthor = true,
        content = "感谢分享！最近也在关注合约，请问你一般用多少倍杠杆？",
        likeCount = 12,
        time = "10 分钟前",
        replies = listOf(
            Reply(
                id = "r1",
                authorName = "铅大家将有几个瞬间",
                userId = 456L,
                replyToName = "张三",
                content = "我一般 3-5 倍，不敢开太高",
                likeCount = 5,
                time = "8 分钟前"
            ),
            Reply(
                id = "r1",
                authorName = "铅大家将有几个瞬间",
                userId = 456L,
                replyToName = "张三",
                content = "我一般 3-5 倍，不敢开太高",
                likeCount = 5,
                time = "8 分钟前"
            ),
            Reply(
                id = "r1",
                authorName = "铅大家将有几个瞬间",
                userId = 456L,
                replyToName = "张三",
                content = "我一般 3-5 倍，不敢开太高",
                likeCount = 5,
                time = "8 分钟前"
            ),
            Reply(
                id = "r1",
                authorName = "铅大家将有几个瞬间",
                userId = 456L,
                replyToName = "张三",
                content = "我一般 3-5 倍，不敢开太高",
                likeCount = 5,
                time = "8 分钟前"
            )

        )
    ),
    Comment(
        id = "c2",
        authorName = "李四",
        isAuthor = false,
        content = "小富即安说得对，合约风险大要控制仓位",
        likeCount = 8,
        time = "30 分钟前",
        replies = emptyList()
    )
)

/**
 * 评论列表
 * 包含标题 + 评论项列表，支持上拉加载更多、空态「暂无评论」、到底提示。
 * @param comments 评论列表
 * @param hasMore 是否还有更多数据
 * @param loadingMore 是否正在加载更多
 * @param onLoadMore 上拉加载更多回调，有评论且 hasMore 时列表接近底部触发
 * @param commentReplyTotal 接口返回的评论+回复总数（帖子详情 replyCount）；null 时标题仅显示「评论」
 */
@Composable
fun CommentListView(
    modifier: Modifier = Modifier,
    comments: List<Comment> = fakeComments(),
    hasMore: Boolean = false,
    loadingMore: Boolean = false,
    onLoadMore: () -> Unit = {},
    commentReplyTotal: Int? = null,
    header: @Composable () -> Unit = {}
) {
    val listState = rememberLazyListState()

    // 接近底部时触发上拉加载
    LaunchedEffect(listState, hasMore, loadingMore) {
        snapshotFlow {
            val info = listState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = info.totalItemsCount
            lastVisible to total
        }.collect { (lastVisible, total) ->
            if (hasMore && !loadingMore && total > 0 && lastVisible >= total - 2) {
                onLoadMore()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                header()
                Text(
                    text = if (commentReplyTotal != null) {
                        "评论（${formatPostInteractionCount(commentReplyTotal)}）"
                    } else {
                        "评论"
                    },
                    style = FontMedium(fontSize = 16, color = Colors.black_101828)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (comments.isEmpty()) {
                item {
                    Text(
                        text = "暂无评论",
                        style = FontMedium(fontSize = 14, color = Colors.gray_667085),
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            } else {
                itemsIndexed(comments, key = { _, c -> c.id }) { index, comment ->
                    if (index > 0) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 16.dp),
                            color = Color(0xFFF3F4F5),
                            thickness = 1.dp
                        )
                    }
                    CommentItemView(comment = comment)
                }

                if (loadingMore) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        }
                    }
                } else if (!hasMore) {
                    item {
                        ListEndFooter(
                            modifier = Modifier
                                .padding(top = 40.dp)
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CommentListViewPreview(){
    CommentListView {

    }
}
