package com.vortexa.ui.page.post.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vortexa.ui.component.ClickableLinkText
import com.vortexa.ui.page.home.pager.home.recommend.PostImagesGrid
import com.vortexa.ui.page.imagepreview.ImagePreviewActivity
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontBold
import com.vortexa.ui.theme.FontRegular

/** 帖子详情主体展示所需字段（Figma 747-88771） */
private data class PostDetailBodyData(
    val topicTag: String,
    val title: String,
    val publishTime: String,
    val content: String,
    val images: List<Any>,
    val inlineTags: List<String>,
    val disclaimer: String?,
)

/** Preview 用假数据 */
private fun fakeBodyData() = PostDetailBodyData(
    topicTag = "#杂谈",
    title = "大学生勇闯币圈!!励志做到 100wU 第257天",
    publishTime = "2026.01.01 19:30:23",
    content = "昨天开了一单空单一路补仓补到平均98600成本!终于等到一根大阴线收米,睡醒看见后半夜还有两根大阴线直呼后悔不过合约着东西还是慢慢来!睡觉行情不好把握也没得后悔,小富即安。最近感觉BA手续费好高懒得弄返利小号,准备转个2000U去BG赚点返利收益大家感觉如何",
    images = emptyList(),
    inlineTags = listOf("#比特币", "#我的理财日记", "#web3", "#金融理财", "#区块链", "#投资理财", "#交易员"),
    disclaimer = "(纯分享交易日记非引流!引流号不要 找我)",
)

/**
 * 帖子详情主体（Figma 747-88771）
 * 包含：话题标签、标题、发布时间、正文、内联标签、免责声明
 * @param data 接口数据（PostDetailData），null 时使用假数据（Preview）
 */
@Composable
fun PostDetailBody(
    data: PostDetailData? = null,
    modifier: Modifier = Modifier,
) {
    val displayData = data?.let {
        PostDetailBodyData(
            topicTag = it.topicTag,
            title = it.title,
            publishTime = it.publishTime,
            content = it.content,
            images = it.post.images,
            inlineTags = it.inlineTags,
            disclaimer = it.disclaimer,
        )
    } ?: fakeBodyData()
    val richBlocks = remember(displayData.content) {
        parsePostContentBlocksOrNull(displayData.content)
    }
    val bodyTextStyle = FontRegular(fontSize = 18, color = Colors.black_101828)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 16.dp),
    ) {
        if (displayData.topicTag.isNotEmpty()) {
            Text(
                text = displayData.topicTag,
                style = FontRegular(fontSize = 12, color = Colors.blue_3266FF),
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Text(
            text = displayData.title,
            style = FontBold(fontSize = 26, color = Colors.black_101828).copy(lineHeight = 28.sp),
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = displayData.publishTime,
            style = FontRegular(fontSize = 12, color = Colors.gray_B1B8C6),
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (richBlocks != null) {
            PostDetailRichContent(blocks = richBlocks)
            val tail = buildPostTailAnnotatedString(displayData.inlineTags, displayData.disclaimer)
            if (tail.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                ClickableLinkText(
                    text = tail,
                    style = bodyTextStyle,
                )
            }
        } else {
            val annotatedString = buildPostContentAnnotatedString(
                content = displayData.content,
                inlineTags = displayData.inlineTags,
                disclaimer = displayData.disclaimer,
            )
            ClickableLinkText(
                text = annotatedString,
                style = bodyTextStyle,
            )
            if (displayData.images.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                val ctx = LocalContext.current
                PostImagesGrid(
                    images = displayData.images,
                    onImageClick = { index, urls -> ImagePreviewActivity.start(ctx, urls, index) },
                )
            }
        }
    }
}

@Composable
private fun PostDetailBodyPreview() {
    PostDetailBody()
}
