package com.vortexa.ui.page.profile.paper.post

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontRegular
import com.vortexa.ui.theme.FontSemiBold

private const val TITLE_MAX_LENGTH = 30

/**
 * 发布帖子页面（Figma 504-56281/56290/56291/56296）：顶部栏、可选提示文案、标题输入、内容输入。
 *
 * @param rejectHint 未过审原因等提示文案，为 null 时不显示该行
 * @param onPublish 点击发帖时回调，参数为 (可见性, 标题, 内容)
 */
@Composable
fun PublishPostView(
    rejectHint: String? = null,
    onPublish: (visibility: PublishVisibility, title: String, content: String) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    var visibility by remember { mutableStateOf(PublishVisibility.Public) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(scrollState)
    ) {
        PublishPostHeader(
            visibility = visibility,
            onVisibilityChange = { visibility = it },
            onPublishClick = { onPublish(visibility, title, content) }
        )

        // 可能被隐藏的提示文案（Figma 504-56290）：红色 14sp
        if (!rejectHint.isNullOrBlank()) {
            Text(
                text = rejectHint,
                style = FontRegular(fontSize = 14, color = Colors.red_FF383C),
                modifier = Modifier.padding(horizontal = 18.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // 标题输入区（Figma 504-56291）：标题 + 字数 0/30，底部分隔线
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, start = 18.dp, end = 18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BasicTextField(
                    value = title,
                    onValueChange = { if (it.length <= TITLE_MAX_LENGTH) title = it },
                    textStyle = FontSemiBold(fontSize = 18, color = Colors.black_101828),
                    singleLine = true,
                    visualTransformation = VisualTransformation.None,
                    cursorBrush = SolidColor(Colors.blue_3266FF),
                    decorationBox = { inner ->
                        Box(modifier = Modifier.weight(1f)) {
                            if (title.isEmpty()) {
                                Text(
                                    text = "请输入标题",
                                    style = FontSemiBold(fontSize = 18, color = Colors.gray_B1B8C6)
                                )
                            }
                            inner()
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${title.length}/$TITLE_MAX_LENGTH",
                    style = FontRegular(fontSize = 14, color = Colors.gray_B1B8C6)
                )
            }
            Spacer(modifier = Modifier.height(7.dp))
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 0.5.dp,
                color = Colors.gray_EBEBEB
            )
        }

        // 内容输入区（Figma 504-56296）：多行 18sp
        BasicTextField(
            value = content,
            onValueChange = { content = it },
            textStyle = FontRegular(fontSize = 18, color = Colors.black_101828),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp, bottom = 10.dp, start = 18.dp, end = 18.dp),
            minLines = 6,
            maxLines = 20,
            cursorBrush = SolidColor(Colors.blue_3266FF),
            decorationBox = { inner ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (content.isEmpty()) {
                        Text(
                            text = "请输入内容",
                            style = FontRegular(fontSize = 18, color = Colors.gray_B1B8C6)
                        )
                    }
                    inner()
                }
            }
        )
    }
}

@Composable
@Preview
fun PublishPostPreview() {
    PublishPostView(
        rejectHint = null,
        onPublish = { _, _, _ -> }
    )
}
