package com.vortexa.ui.page.imagepreview

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.vortexa.util.resolveApiMediaUrl

/**
 * 图片预览主视图：横向 Pager + 单图缩放 + 底部指示器。
 * 使用 Compose 手势实现双指缩放、双击缩放、拖拽平移。
 *
 * @param imageUrls 图片 URL 列表
 * @param initialIndex 默认展示索引（0-based）
 * @param onBack 返回回调
 */
@Composable
fun ImagePreviewView(
    imageUrls: List<String>,
    initialIndex: Int = 0,
    onBack: () -> Unit
) {
    val pageCount = imageUrls.size
    if (pageCount == 0) return

    val pagerState = rememberPagerState(
        initialPage = initialIndex.coerceIn(0, pageCount - 1),
        pageCount = { pageCount }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = true,
            key = { it }
        ) { page ->
            val model = resolveApiMediaUrl(imageUrls[page])
            ZoomablePreviewImage(
                model = model,
                contentDescription = "预览图 ${page + 1}/${pageCount}",
                modifier = Modifier.fillMaxSize(),
            )
        }

        // 顶部返回区（可点击返回）
        Box(modifier = Modifier.align(Alignment.TopStart)) {
            ImagePreviewTopBar(onBack = onBack)
        }

        // 底部页面指示器
        if (pageCount > 1) {
            ImagePreviewPageIndicator(
                totalCount = pageCount,
                currentIndex = pagerState.currentPage,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp)
            )
        }
    }
}

@Composable
private fun ZoomablePreviewImage(
    model: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    var scale by remember(model) { mutableStateOf(1f) }
    var offset by remember(model) { mutableStateOf(Offset.Zero) }
    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        val nextScale = (scale * zoomChange).coerceIn(1f, 4f)
        scale = nextScale
        offset = if (nextScale == 1f) {
            Offset.Zero
        } else {
            offset + panChange
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = model,
            contentDescription = contentDescription,
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(model) {
                    detectTapGestures(
                        onDoubleTap = {
                            if (scale > 1f) {
                                scale = 1f
                                offset = Offset.Zero
                            } else {
                                scale = 2f
                            }
                        },
                    )
                }
                .transformable(
                    state = transformState,
                    enabled = scale > 1f,
                )
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                },
            contentScale = ContentScale.Fit,
        )
    }
}

/**
 * 顶部返回栏：透明背景，左侧返回按钮区域。
 */
@Composable
private fun ImagePreviewTopBar(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .statusBarsPadding()
            .padding(top = 8.dp, start = 8.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "返回",
                tint = Color.White
            )
        }
    }
}
