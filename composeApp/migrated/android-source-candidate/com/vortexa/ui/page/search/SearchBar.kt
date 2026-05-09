package com.vortexa.ui.page.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontRegular
import vortexa.composeapp.generated.resources.Res

/**
 * 顶部搜索条：返回按钮 + 圆角输入框，占位符「搜索」，回车提交。
 *
 * @param query 当前输入内容（受控）
 * @param onQueryChange 输入内容变化回调
 * @param onSubmit 点击搜索/回车提交时的回调，由调用方根据 keyword 是否非空决定是否展示结果页
 */
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit,
    onSubmit: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Colors.gray_F3F5F7)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painterResource(Res.drawable.icon_search),
                colorFilter = ColorFilter.tint(Colors.gray_B2B3BD),
                contentDescription = "",
                modifier = Modifier.padding(end = 8.dp)
            )
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(
                    color = Colors.black_101828,
                    fontSize = 16.sp
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    focusManager.clearFocus()
                    onSubmit(query)
                }),
                decorationBox = { inner ->
                    if (query.isEmpty()) {
                        Text(
                            text = "搜索",
                            style = FontRegular(fontSize = 16, color = Colors.gray_B1B8C6)
                        )
                    }
                    inner()
                }
            )
        }
    }
}