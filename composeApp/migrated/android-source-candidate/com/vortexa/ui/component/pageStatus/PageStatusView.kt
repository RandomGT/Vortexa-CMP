package com.vortexa.ui.component.pageStatus

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontRegular
import vortexa.composeapp.generated.resources.Res

/**
 * 页面状态视图，用于请求时展示不同状态
 *
 * @param status 当前页面状态
 * @param modifier 修饰符
 * @param failMessage 失败状态下的自定义文案，默认 "加载失败，请稍后重试"
 * @param emptyMessage 空状态下的自定义文案，默认 "暂无数据"
 * @param showFailRefresh 失败状态是否展示刷新按钮
 * @param showEmptyRefresh 空状态是否展示刷新按钮
 * @param onRefresh 刷新按钮点击回调
 * @param failIconRes 失败状态图标资源，默认使用 icon_error
 * @param emptyIconRes 空状态图标资源，默认使用 icon_empty
 *
 * @author LuXin
 * @createTime 2026/2/5
 */
@Composable
fun PageStatusView(
    status: PageStatus,
    modifier: Modifier = Modifier,
    failMessage: String = "加载失败，请稍后重试",
    emptyMessage: String = "暂无数据",
    showFailRefresh: Boolean = true,
    showEmptyRefresh: Boolean = false,
    onRefresh: (() -> Unit)? = null,
    failIconRes: Int = Res.drawable.icon_error,
    emptyIconRes: Int = Res.drawable.icon_empty
) {
    when (status) {
        PageStatus.Success -> {
            // 成功状态隐藏视图，不渲染任何内容
            return
        }
        PageStatus.Loading -> {
            LoadingContent(modifier = modifier)
        }
        PageStatus.Fail -> {
            StatusContent(
                modifier = modifier,
                iconRes = failIconRes,
                message = failMessage,
                showRefreshButton = showFailRefresh,
                onRefresh = onRefresh
            )
        }
        PageStatus.Empty -> {
            StatusContent(
                modifier = modifier,
                iconRes = emptyIconRes,
                message = emptyMessage,
                showRefreshButton = showEmptyRefresh,
                onRefresh = onRefresh
            )
        }
    }
}

/**
 * Loading 状态内容，使用 CircularProgressIndicator 实现动态旋转效果
 * 采用 Compose 内置 indeterminate 模式，自带流畅动画
 */
@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Log.d("PageStatusView", "LoadingContent: 展示加载中状态")
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 3.dp,
            modifier = Modifier.size(48.dp)
        )
    }
}

/**
 * Fail / Empty 状态内容，居中展示图标、文案及可选刷新按钮
 *
 * @param iconRes 图标资源 ID
 * @param message 展示文案
 * @param showRefreshButton 是否展示刷新按钮
 * @param onRefresh 刷新回调
 */
@Composable
private fun StatusContent(
    modifier: Modifier = Modifier,
    iconRes: Int,
    message: String,
    showRefreshButton: Boolean,
    onRefresh: (() -> Unit)?
) {
    Log.d("PageStatusView", "StatusContent: message=$message, showRefresh=$showRefreshButton")
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(150.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = FontRegular(14, Colors.gray_6A7282),
                textAlign = TextAlign.Center
            )
            if (showRefreshButton && onRefresh != null) {
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onRefresh) {
                    Text(
                        text = "点击刷新",
                        style = FontRegular(14, Colors.blue_277DFF)
                    )
                }
            }
        }
    }
}

/**
 * 预览：Loading 状态
 */
@Preview(showBackground = true)
@Composable
private fun PageStatusViewLoadingPreview() {
    PageStatusView(
        status = PageStatus.Loading,
        modifier = Modifier.size(300.dp, 400.dp)
    )
}

/**
 * 预览：Fail 状态（含刷新按钮）
 */
@Preview(showBackground = true)
@Composable
private fun PageStatusViewFailPreview() {
    PageStatusView(
        status = PageStatus.Fail,
        modifier = Modifier.size(300.dp, 400.dp),
        failMessage = "网络异常，请检查网络后重试",
        showFailRefresh = true,
        onRefresh = {}
    )
}

/**
 * 预览：Empty 状态（无刷新按钮）
 */
@Preview(showBackground = true)
@Composable
private fun PageStatusViewEmptyPreview() {
    PageStatusView(
        status = PageStatus.Empty,
        modifier = Modifier.size(300.dp, 400.dp),
        emptyMessage = "暂无相关内容",
        showEmptyRefresh = false
    )
}
