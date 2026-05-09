package com.vortexa.ui.page.teach.myclass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.util.extension.click
import vortexa.composeapp.generated.resources.Res

/**
 * 我的课程页 TitleBar（Figma 336-14913）：白底，左侧返回箭头，居中标题「我的课程」。
 *
 * @param onBackClick 点击返回按钮回调
 */
@Composable
fun MyClassTitleBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(Color.White)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(Res.drawable.icon_back),
            contentDescription = "返回",
            modifier = Modifier
                .size(24.dp)
                .click(onClickListener = onBackClick),
            tint = Colors.black_101828
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "我的课程",
            style = FontMedium(fontSize = 16, color = Colors.black_101828)
        )
        Spacer(modifier = Modifier.weight(1f))
//        Icon(
//            painterResource(Res.drawable.icon_user_profile_square),
//            contentDescription = "",
//            modifier = Modifier.size(24.dp)
//        )
    }
}

@Composable
@Preview
private fun MyClassTitleBarPreview() {
    MyClassTitleBar(onBackClick = {})
}
