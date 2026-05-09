package com.vortexa.ui.page.post.create

import android.content.Context
import android.net.Uri
import android.util.Log
import android.util.Size
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vortexa.ui.theme.Colors
import com.vortexa.util.extension.click
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 发布页媒体预览横向列表，展示用户选中的图片/视频并支持删除。
 * @param selectedMediaList 当前已选媒体列表
 * @param onRemoveMedia 删除某个媒体项时回调
 * @param modifier 外层修饰符
 */
@Composable
fun PostCreateMediaPreviewList(
    selectedMediaList: List<PostCreateSelectedMedia>,
    onRemoveMedia: (PostCreateSelectedMedia) -> Unit,
    modifier: Modifier = Modifier
) {
    if (selectedMediaList.isEmpty()) return
    val context = LocalContext.current
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val horizontalPadding = 18.dp
    val itemSpacing = 10.dp
    val itemSize = (screenWidth - horizontalPadding * 2 - itemSpacing * 2) / 3
    val rowList = selectedMediaList.chunked(3)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(itemSpacing)
    ) {
        rowList.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(itemSpacing)
            ) {
                rowItems.forEach { media ->
                    val previewBitmap = produceState<ImageBitmap?>(initialValue = null, key1 = media.uri) {
                        value = loadMediaPreviewBitmap(context = context, mediaUri = media.uri)
                    }.value
                    Box(modifier = Modifier.size(itemSize)) {
                        if (previewBitmap != null) {
                            Image(
                                bitmap = previewBitmap,
                                contentDescription = "Selected media preview",
                                modifier = Modifier
                                    .size(itemSize)
                                    .clip(RoundedCornerShape(10.dp))
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(itemSize)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Colors.gray_EEF0F1)
                            )
                        }

                        if (media.type == PostCreateMediaType.Video) {
                            Text(
                                text = "视频",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(start = 6.dp, bottom = 6.dp)
                                    .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(4.dp))
                                    .clip(RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 6.dp, end = 6.dp)
                                .size(20.dp)
                                .background(Color.Red, RoundedCornerShape(10.dp))
                                .click { onRemoveMedia(media) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "×",
                                color = Color.White,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }
                    }
                }

                repeat(3 - rowItems.size) {
                    Spacer(modifier = Modifier.size(itemSize))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PostCreateMediaPreviewListPreview() {
    PostCreateMediaPreviewList(
        selectedMediaList = listOf(
            PostCreateSelectedMedia(uri = Uri.EMPTY, type = PostCreateMediaType.Image),
            PostCreateSelectedMedia(uri = Uri.EMPTY, type = PostCreateMediaType.Video)
        ),
        onRemoveMedia = {}
    )
}

/**
 * 发布页选中的媒体数据模型。
 * @param uri 媒体资源 Uri
 * @param type 媒体类型
 */
data class PostCreateSelectedMedia(
    val uri: Uri,
    val type: PostCreateMediaType
)

/** 发布页媒体类型。 */
enum class PostCreateMediaType {
    Image,
    Video
}

/**
 * 异步加载媒体缩略图（图片/视频通用）。
 * @param context 应用上下文，用于访问 ContentResolver
 * @param mediaUri 媒体资源 Uri
 * @return 成功返回 ImageBitmap，失败返回 null
 */
private suspend fun loadMediaPreviewBitmap(
    context: Context,
    mediaUri: Uri
): ImageBitmap? = withContext(Dispatchers.IO) {
    runCatching {
        context.contentResolver
            .loadThumbnail(mediaUri, Size(320, 320), null)
            .asImageBitmap()
    }.onFailure {
        Log.w(TAG, "Load media preview failed, uri=$mediaUri", it)
    }.getOrNull()
}

private const val TAG = "PostCreateMediaList"
