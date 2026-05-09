package com.vortexa.ui.page.post.list

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.unit.dp
import com.vortexa.ui.viewmodel.vortexaViewModel
import com.vortexa.ui.page.home.pager.home.HomeCommunicateNavigation
import com.vortexa.ui.page.home.pager.home.recommend.PostItem
import com.vortexa.ui.page.post.detail.PostDetailActivity
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import vortexa.composeapp.generated.resources.Res
import vortexa.composeapp.generated.resources.icon_back

/**
 * 热帖列表页：标题「热帖」+ 返回按钮，底部为每页 10 条的 Post 列表，样式复用 [PostItem]。
 */
@Composable
fun HotPostListView(
    viewModel: HotPostListViewModel = vortexaViewModel { HotPostListViewModel() },
    onBackClick: () -> Unit
) {
    val posts by viewModel.postList.collectAsState()
    val context = Context()
    val dividerColor = Color(0xFFF3F4F5)

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // 标题栏：返回 + 热帖
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(Res.drawable.icon_back),
                contentDescription = "返回",
                modifier = Modifier
                    .size(24.dp)
                    .clickable(onClick = onBackClick),
                tint = Colors.black_101828
            )
            Text(
                text = "热帖",
                style = FontMedium(fontSize = 18, color = Colors.black_101828),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            itemsIndexed(posts, key = { _, post -> post.id }) { index, post ->
                if (index > 0) {
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
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
        }
    }
}

@Composable
fun HotPostListViewPreview() {
    HotPostListView(onBackClick = {})
}
