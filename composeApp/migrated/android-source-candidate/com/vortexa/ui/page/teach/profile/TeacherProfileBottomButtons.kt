package com.vortexa.ui.page.teach.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontBold
import com.vortexa.util.extension.click

@Composable
fun TeacherProfileBottomButtons() {
    val viewModel = viewModel(TeacherProfileViewModel::class.java)
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .padding(horizontal = 18.dp)
            .padding(vertical = 10.dp)
            .height(43.dp)
    ) {
        Box(
            modifier = Modifier
                .background(Colors.black_101828, RoundedCornerShape(30.dp))
                .fillMaxHeight()
                .weight(1f)
                .click { viewModel.onScheduleClick(context) }
        ) {
            Text(
                "预约",
                style = FontBold(16, Color.White),
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}


@Composable
@Preview
fun TeacherProfileBottomButtonsPreview() {
    TeacherProfileBottomButtons()
}