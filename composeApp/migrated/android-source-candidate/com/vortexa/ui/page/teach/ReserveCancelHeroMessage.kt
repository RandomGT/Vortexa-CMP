package com.vortexa.ui.page.teach

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vortexa.ui.theme.Colors

@Composable
fun ReserveCancelHeroMessage(
    display: ReserveCancelHeroDisplay,
    modifier: Modifier = Modifier
) {
    val black = SpanStyle(color = Colors.black_101828, fontWeight = FontWeight.SemiBold, fontSize = 24.sp)
    val blue = SpanStyle(color = Colors.blue_277DFF, fontWeight = FontWeight.SemiBold, fontSize = 24.sp)
    val text = buildAnnotatedString {
        when (display) {
            ReserveCancelHeroDisplay.StudentInitiatedCancel -> {
                pushStyle(black)
                append("学员")
                pop()
                pushStyle(blue)
                append("已取消")
                pop()
                pushStyle(black)
                append("该预约")
            }
            ReserveCancelHeroDisplay.StudentSelfCancelled -> {
                pushStyle(black)
                append("学员")
                pop()
                pushStyle(blue)
                append("已取消")
                pop()
                pushStyle(black)
                append("该预约")
            }
            ReserveCancelHeroDisplay.TutorInitiatedCancelStudentView -> {
                pushStyle(black)
                append("导师")
                pop()
                pushStyle(blue)
                append("已拒绝")
                pop()
                pushStyle(black)
                append("该预约课程")
            }
            ReserveCancelHeroDisplay.TutorSelfCancelled -> {
                pushStyle(black)
                append("导师")
                pop()
                pushStyle(blue)
                append("已拒绝")
                pop()
                pushStyle(black)
                append("该预约")
            }
            ReserveCancelHeroDisplay.GenericCancelled -> {
                pushStyle(black)
                append("该预约已")
                pop()
                pushStyle(blue)
                append("取消")
            }
        }
    }
    Text(
        text = text,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 24.dp),
        textAlign = TextAlign.Center,
        lineHeight = 32.sp
    )
}

@Preview
@Composable
private fun ReserveCancelHeroMessagePreview() {
    ReserveCancelHeroMessage(display = ReserveCancelHeroDisplay.StudentInitiatedCancel)
}
