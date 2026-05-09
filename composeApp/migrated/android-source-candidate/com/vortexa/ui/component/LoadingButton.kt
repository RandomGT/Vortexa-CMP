package com.vortexa.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontRegular

/**
 * 可进入 Loading 态的按钮。Loading 时隐藏文案、居中显示转圈动画并禁用点击。
 *
 * @param modifier 外层修饰符
 * @param text 按钮文案
 * @param isLoading 是否处于加载中（由外部控制，请求结束后置为 false）
 * @param onClick 点击回调，Loading 时不会触发
 * @param textColor 默认文案颜色（仅在不传 [content] 时用于 [text]）
 * @param loadingIndicatorColor Loading 时 [CircularProgressIndicator] 颜色
 * @param content 自定义内部内容；不传则使用默认 [text] 文案。Loading 时由组件统一展示转圈，不渲染 content
 */
@Composable
fun LoadingButton(
    modifier: Modifier,
    text: String,
    isLoading: Boolean,
    onClick: () -> Unit,
    textColor: Color = Color.White,
    loadingIndicatorColor: Color = Color.White,
    content: (@Composable BoxScope.() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .then(if (isLoading) Modifier.semantics { disabled() } else Modifier)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = !isLoading,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = loadingIndicatorColor,
                strokeWidth = 2.dp
            )
        } else {
            if (content != null) {
                content()
            } else {
                androidx.compose.material3.Text(
                    text = text,
                    style = FontRegular(16, textColor)
                )
            }
        }
    }
}

@Composable
@Preview
fun LoadingButtonPreview() {
    Column {
        LoadingButton(
            modifier = Modifier
                .background(Colors.black_101828)
                .size(200.dp, 51.dp),
            text = "预约&支付",
            isLoading = false,
            onClick = {}
        )
        Spacer(modifier = Modifier.size(16.dp))
        LoadingButton(
            modifier = Modifier
                .background(Colors.black_101828)
                .size(200.dp, 51.dp),
            text = "预约&支付",
            isLoading = true,
            onClick = {}
        )
    }
}
