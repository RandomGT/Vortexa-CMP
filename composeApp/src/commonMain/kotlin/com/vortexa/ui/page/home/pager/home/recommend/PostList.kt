package com.vortexa.ui.page.home.pager.home.recommend

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.vortexa.ui.page.home.pager.home.HomeCommunicateNavigation
import com.vortexa.ui.page.post.detail.PostDetailActivity
import androidx.compose.ui.unit.dp
import com.vortexa.ui.viewmodel.vortexaViewModel
import com.vortexa.model.RecommendCard

/**
 *  desc : List of Recommended Posts
 *  Displays a feed of posts with images, tags, and interaction buttons.
 *
 *  @author LuXin
 *  @createTime 2026/2/4
 */
@Composable
fun PostList(
    viewModel: RecommendViewModel = vortexaViewModel { RecommendViewModel() },
    header: @Composable () -> Unit = {},
    footer: @Composable () -> Unit = {},
    onHotPostsExploreMore: (() -> Unit)? = null,
    onNavigateToSchool: (() -> Unit)? = null
) {
    val posts by viewModel.postList.collectAsState()
    val recommendCards by viewModel.recommendCards.collectAsState()
    val context = Context()

    // 分割线颜色：两个 Item 之间 40dp 间隔，中间 1dp 分割线
    val dividerColor = Color(0xFFF3F4F5)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            header()
        }
        itemsIndexed(posts, key = { _, post -> post.id }) { index, post ->
            // 非首项：先画间隔 + 分割线 + 间隔（共 40dp，分割线 1dp）
            if (index > 0) {
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 18.dp),
                    thickness = 1.dp,
                    color = dividerColor
                )
            }
            PostItem(
                post = post,
                onLikeClick = { viewModel.toggleLike(post.id) },
                onBookmarkClick = { viewModel.toggleBookmark(post.id) },
                onCommentClick = {
                    PostDetailActivity.start(context, post, openReplyComposer = true)
                },
                onPostClick = { PostDetailActivity.start(context, post) },
                onModuleClick = { HomeCommunicateNavigation.startFromPost(context, post) }
            )
        }

        item {
            DividerButton(
                "显示更多热帖",
                onClick = onHotPostsExploreMore
            )
        }

        item {
            footer()
        }
        // 推荐卡片网格：每行两个 Item，水平间距 14dp（Figma 747-81595）
        item {
            RecommendGrid(cards = recommendCards, modifier = Modifier.padding(top = 16.dp))
        }

        item {
            DividerButton(
                "显示更多导师",
                modifier = Modifier.padding(top = 20.dp),
                onClick = onNavigateToSchool
            )
        }
    }
}

/** 推荐卡片网格：每行两列，水平间距 14dp。Figma 747-81595 */
@Composable
fun RecommendGrid(
    cards: List<RecommendCard>,
    modifier: Modifier = Modifier
) {
    val horizontalPadding = 16.dp
    val itemSpacing = 14.dp
    val rows = cards.chunked(2)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding),
        verticalArrangement = Arrangement.spacedBy(itemSpacing)
    ) {
        rows.forEach { rowCards ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(itemSpacing)
            ) {
                rowCards.forEach { card ->
                    RecommendCardItem(
                        card = card,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowCards.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun PostListPreview() {
    PostList()
}
