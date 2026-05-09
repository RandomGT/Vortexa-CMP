package com.vortexa.ui.page.teach.video

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

/**
 * Token 获取失败时展示的错误内容。
 *
 * @param message 错误提示文案
 * @param modifier 修饰符
 */
@Composable
fun VideoRtcErrorContent(
    message: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = message,
        color = Color.White,
        modifier = modifier
    )
}

@Composable
@Preview(backgroundColor = 0xFF101828)
private fun VideoRtcErrorContentPreview() {
    VideoRtcErrorContent(message = "获取 Token 失败")
}
