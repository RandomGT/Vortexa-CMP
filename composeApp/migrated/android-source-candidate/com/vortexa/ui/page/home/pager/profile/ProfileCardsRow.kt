package com.vortexa.ui.page.home.pager.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontBold
import com.vortexa.ui.theme.FontMedium
import com.vortexa.ui.theme.FontRegular
import com.vortexa.util.extension.click
import vortexa.composeapp.generated.resources.Res

/**
 * 功能卡片行（Figma 746-69789）：我的钱包、我的课程，浅蓝背景、圆角、图标。
 * 图标：当前为 drawable 占位，请从 Figma 导出 3D 图标到 mipmap-xxhdpi 后替换：
 * - ic_profile_wallet_card.png（钱包）
 * - ic_profile_course_card.png（课程/笔记本）
 */
@Composable
fun ProfileCardsRow(
    modifier: Modifier = Modifier,
    onWalletClick: () -> Unit,
    onCourseClick: () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ProfileCard(
            modifier = Modifier.weight(1f),
            title = "我的钱包",
            subtitle = "即将上线",
            iconRes = Res.drawable.profile_wallet,
            onClick = onWalletClick
        )
        ProfileCard(
            modifier = Modifier.weight(1f),
            title = "我的课程",
            subtitle = "1v1课程",
            iconRes = Res.drawable.profile_note,
            onClick = onCourseClick
        )
    }
}

/**
 * 单张功能卡片：左侧文案（标题+副标题），右侧图标，圆角浅蓝背景。
 */
@Composable
private fun ProfileCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    iconRes: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Colors.blue_E0F3FF)
            .click(onClick)
            .height(73.dp)
            .padding(start = 16.dp)

    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = FontBold(fontSize = 16, color = Colors.black_242424),
                    lineHeight = 22.sp
                )
                Text(
                    text = subtitle,
                    style = FontRegular(fontSize = 12, color = Colors.gray_6A7282),
                    lineHeight = 18.sp
                )
            }
            Image(
                painter = painterResource(iconRes),
                contentDescription = title,
                modifier = Modifier.size(76.dp)
            )
        }
    }
}

@Composable
@Preview
fun ProfileCardPreview() {
    ProfileCardsRow(onWalletClick = {}, onCourseClick = {})
}