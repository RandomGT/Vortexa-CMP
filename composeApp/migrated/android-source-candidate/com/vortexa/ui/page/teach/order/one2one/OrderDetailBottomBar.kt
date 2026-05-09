package com.vortexa.ui.page.teach.order.one2one

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.util.extension.click

/**
 * 底部操作栏：进入课程（导师已接受且未下课期间常显；开课前浅底「进入课程」，到点可进为深色「进入课堂」）+ 主操作按钮。
 */
@Composable
fun OrderDetailBottomBar(
    showEnterCourse: Boolean = false,
    canEnterCourseRoom: Boolean = false,
    isPending: Boolean = false,
    showPrimaryButton: Boolean = true,
    onEnterCourseClick: () -> Unit = {},
    onPrimaryClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val primaryText = if (isPending) "取消预约" else "再次预约"
    val rowShape = RoundedCornerShape(30.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showEnterCourse) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(rowShape)
                    .background(
                        if (canEnterCourseRoom) Colors.black_101828
                        else Colors.gray_EEF0F1
                    )
                    .padding(vertical = 10.dp)
                    .click(onEnterCourseClick),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (canEnterCourseRoom) "进入课堂" else "进入课程",
                    style = FontMedium(
                        fontSize = 16,
                        color = if (canEnterCourseRoom) Color.White else Colors.black_101828
                    )
                )
            }
        }
        if (showPrimaryButton) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(rowShape)
                    .background(Colors.black_101828)
                    .padding(vertical = 10.dp)
                    .click(onPrimaryClick),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = primaryText,
                    style = FontMedium(fontSize = 16, color = Color.White)
                )
            }
        }
    }
}

@Composable
@Preview
private fun OrderDetailBottomBarPreview() {
    OrderDetailBottomBar(showEnterCourse = true, canEnterCourseRoom = true, isPending = false)
}
