package com.vortexa.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.ui.theme.FontRegular
import com.vortexa.util.extension.click

/**
 * 退出登录二次确认：提示「确认退出登录么？」，底部「取消」「确定」；样式与 [DeletePostConfirmModal]、[UnfollowConfirmModal] 一致。
 *
 * @param onDismiss 关闭弹窗（点击取消或遮罩）
 * @param onConfirm 点击确定，调用方负责清会话并跳转登录等
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogoutConfirmModal(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier.fillMaxWidth(),
        containerColor = Color.White,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "确认退出登录么？",
                style = FontRegular(fontSize = 16, color = Colors.black_101828),
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .background(Colors.gray_EEF0F1)
                        .click(onClickListener = onDismiss),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "取消",
                        style = FontMedium(fontSize = 16, color = Colors.black_101828)
                    )
                }
                LoadingButton(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .background(Colors.black_101828),
                    text = "确定",
                    isLoading = false,
                    onClick = onConfirm
                )
            }
        }
    }
}

@Composable
@Preview
private fun LogoutConfirmModalPreview() {
    LogoutConfirmModal(
        onDismiss = {},
        onConfirm = {}
    )
}
