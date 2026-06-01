package com.vortexa.ui.page.home.pager.profile

import android.net.Uri
import android.util.Log
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vortexa.platform.MediaPicker
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.ui.theme.FontRegular
import com.vortexa.util.extension.click
import kotlinx.coroutines.launch

/** 底部 Tab 栏高度，与 EditProfileModal 一致 */
private const val TAB_BAR_HEIGHT_DP = 50

/**
 * 头像来源选择弹窗：拍照 / 从相册选择。
 * 选择结果会写入平台临时文件，并以 Uri 形式回传给编辑资料弹窗预览与上传。
 *
 * @param onDismiss 关闭弹窗
 * @param onAvatarSelected 用户选择图片后回调，传入图片 Uri
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarSourceModal(
    onDismiss: () -> Unit,
    onAvatarSelected: (Uri) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    /**
     * 拍照：由平台层负责权限弹窗与相机控制。
     */
    fun onTakePhotoClick() {
        scope.launch {
            val picked = MediaPicker.takePhoto()
            if (picked == null) {
                Log.d(TAG, "Take photo cancelled")
                return@launch
            }
            Log.d(TAG, "Take photo success, uri=${picked.uri}")
            onAvatarSelected(Uri.parse(picked.uri))
            onDismiss()
        }
    }

    /**
     * 从相册选择：由平台层负责权限弹窗与图片选择器。
     */
    fun onPickFromGalleryClick() {
        scope.launch {
            val picked = MediaPicker.pickImages(maxCount = 1).firstOrNull()
            if (picked == null) {
                Log.d(TAG, "Pick image cancelled")
                return@launch
            }
            Log.d(TAG, "Pick image success, uri=${picked.uri}")
            onAvatarSelected(Uri.parse(picked.uri))
            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.fillMaxWidth(),
        containerColor = Colors.gray_F8F9FA,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = TAB_BAR_HEIGHT_DP.dp)
        ) {
            Text(
                text = "选择头像来源",
                style = FontMedium(fontSize = 18, color = Colors.black_242424),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AvatarSourceOption(
                    modifier = Modifier.weight(1f),
                    title = "拍照",
                    onClick = ::onTakePhotoClick
                )
                AvatarSourceOption(
                    modifier = Modifier.weight(1f),
                    title = "从相册选择",
                    onClick = ::onPickFromGalleryClick
                )
            }
        }
    }
}

@Composable
private fun AvatarSourceOption(
    modifier: Modifier = Modifier,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Colors.black_101828)
            .click(onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = FontRegular(fontSize = 16, color = Color.White)
        )
    }
}

@Composable
private fun AvatarSourceModalPreview() {
    AvatarSourceModal(
        onDismiss = {},
        onAvatarSelected = {}
    )
}

private const val TAG = "AvatarSourceModal"
