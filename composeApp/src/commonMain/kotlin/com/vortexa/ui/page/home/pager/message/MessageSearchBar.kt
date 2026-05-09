package com.vortexa.ui.page.home.pager.message

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontRegular
import vortexa.composeapp.generated.resources.Res
import vortexa.composeapp.generated.resources.icon_search

/**
 * 消息页搜索栏：圆角浅灰背景，占位「搜索好友」，左侧搜索图标、右侧麦克风图标。
 *
 * @param query 当前输入（受控）
 * @param onQueryChange 输入变化回调
 */
@Composable
fun MessageSearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 8.dp)
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFF2F2F2))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(Res.drawable.icon_search),
            contentDescription = null,
            modifier = Modifier.padding(end = 8.dp),
            colorFilter = ColorFilter.tint(Colors.gray_B2B3BD)
        )
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            textStyle = TextStyle(color = Colors.black_101828, fontSize = 16.sp),
            singleLine = true,
            decorationBox = { inner ->
                if (query.isEmpty()) {
                    Text(
                        text = "搜索好友",
                        style = FontRegular(fontSize = 16, color = Colors.gray_B1B8C6)
                    )
                }
                inner()
            }
        )
    }
}
