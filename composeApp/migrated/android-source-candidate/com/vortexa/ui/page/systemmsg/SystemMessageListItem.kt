package com.vortexa.ui.page.systemmsg

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.ui.theme.FontRegular
import com.vortexa.ui.theme.FontSemiBold
import com.vortexa.util.extension.click
import com.vortexa.router.AppSchemeRouter

/**
 * 系统通知列表单条（Figma 747-90345）：标题 + 浅灰圆角卡片（正文 + OK 按钮）。
 *
 * @param item 单条通知数据
 * @param onOkClick 点击 OK 按钮回调
 */
@Composable
fun SystemMessageListItem(
    item: SystemMessageItem,
    onOkClick: () -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
        ) {
            Text(
                text = item.time.ifEmpty { "--:--" },
                style = FontRegular(12, color = Colors.black_101828),
                modifier = Modifier.align(Alignment.Center)
            )
        }
        Text(
            text = item.title,
            style = FontSemiBold(fontSize = 15, color = Colors.black_101828)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Colors.gray_F8F9FA)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = item.content,
                style = FontRegular(fontSize = 15, color = Colors.black_101828),
                maxLines = 10,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(Colors.black_101828)
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .click(onClickListener = {
                            item.scheme?.trim()?.takeIf { it.isNotEmpty() }?.let { raw ->
                                AppSchemeRouter.open(context, raw)
                            }
                            onOkClick()
                        }),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.okButtonText,
                        style = FontMedium(
                            fontSize = 12,
                            color = androidx.compose.ui.graphics.Color.White
                        )
                    )
                }
            }
        }
    }
}
