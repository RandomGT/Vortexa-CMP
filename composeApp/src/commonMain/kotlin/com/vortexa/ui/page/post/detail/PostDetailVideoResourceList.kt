package com.vortexa.ui.page.post.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.ui.theme.FontRegular
import vortexa.composeapp.generated.resources.Res
import vortexa.composeapp.generated.resources.default_pic
import vortexa.composeapp.generated.resources.icon_video

/**
 * 帖子详情视频资源列表，兼容本地 Uri 与远程 URL。
 * @param videoResources 视频资源集合，元素支持 Uri 或 String
 * @param modifier 组件修饰符
 */
@Composable
fun PostDetailVideoResourceList(
    videoResources: List<Any>,
    modifier: Modifier = Modifier
) {
    if (videoResources.isEmpty()) return
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "视频资源",
            style = FontMedium(fontSize = 14, color = Colors.black_101828)
        )
        val coverPlaceholder = painterResource(Res.drawable.default_pic)
        videoResources.forEach { media ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Colors.gray_F3F5F7),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = media,
                    contentDescription = "视频封面",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    placeholder = coverPlaceholder,
                    error = coverPlaceholder,
                    fallback = coverPlaceholder
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .background(Colors.black_101828.copy(alpha = 0.45f))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.icon_video),
                        contentDescription = "视频",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = media.toMediaLabel(),
                        style = FontRegular(fontSize = 12, color = Color.White),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * 将视频资源转换为便于识别的文案。
 * @receiver 视频资源对象
 * @return UI 展示文案
 */
private fun Any.toMediaLabel(): String = when (this) {
    is String -> this
    else -> "video"
}

@Composable
private fun PostDetailVideoResourceListPreview() {
    Column(modifier = Modifier.padding(16.dp)) {
        PostDetailVideoResourceList(
            videoResources = listOf(
                "https://example.com/video-cover.jpg",
                "content://media/external/video/media/1001"
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}
