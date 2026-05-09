package com.vortexa.ui.page.home.pager.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vortexa.ui.component.AvatarImage
import com.vortexa.ui.component.LoadingButton
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.ui.theme.FontRegular
import com.vortexa.util.extension.click

/** 底部 Tab 栏高度，与 HomeTabContent 一致，弹窗内容需留出该空间 */
private const val TAB_BAR_HEIGHT_DP = 50

/**
 * 编辑资料弹窗（Figma 504-55348）
 * 从底部滑出，全宽，定位在 NavigationBar（Tab 栏）上方。
 * 点击「修改头像」会弹出 AvatarSourceModal，供用户选择拍照或从相册选择，会先校验并申请权限。
 * 确认按钮为 LoadingButton，提交时显示 loading。
 *
 * @param avatarUrl 当前头像，null 则用默认占位
 * @param currentUsername 当前用户名
 * @param confirmLoading 确认按钮是否处于加载态（须由 ViewModel 在请求成功或失败后置 false，以结束 LoadingButton）
 * @param onDismiss 关闭弹窗
 * @param onAvatarSelected 用户选择头像图片后，Modal 内部保存 Uri，确认时一并传入 onConfirm
 * @param onConfirm 确认提交，传入新用户名、新头像 Uri（未选则 null）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileModal(
    avatarUrl: String? = null,
    currentUsername: String = "",
    confirmLoading: Boolean = false,
    onDismiss: () -> Unit = {},
    onAvatarSelected: (Uri) -> Unit = {},
    onConfirm: (userName: String, avatarUri: Uri?) -> Unit = { _, _ -> }
) {
    var showAvatarSourceModal by remember { mutableStateOf(false) }
    var selectedAvatarUri by remember { mutableStateOf<Uri?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.fillMaxWidth(),
        containerColor = Color.White,
        dragHandle = null
    ) {
        DialogInfo(
            avatarUrl = avatarUrl,
            selectedAvatarUri = selectedAvatarUri,
            currentUsername = currentUsername,
            confirmLoading = confirmLoading,
            onChangeAvatar = { showAvatarSourceModal = true },
            onConfirm = { userName -> onConfirm(userName, selectedAvatarUri) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = TAB_BAR_HEIGHT_DP.dp)
        )
    }

    if (showAvatarSourceModal) {
        AvatarSourceModal(
            onDismiss = { showAvatarSourceModal = false },
            onAvatarSelected = { uri ->
                selectedAvatarUri = uri
                onAvatarSelected(uri)
                showAvatarSourceModal = false
            }
        )
    }
}

@Composable
fun DialogInfo(
    modifier: Modifier = Modifier,
    avatarUrl: String? = null,
    selectedAvatarUri: Uri? = null,
    currentUsername: String = "",
    confirmLoading: Boolean = false,
    onChangeAvatar: () -> Unit = {},
    onConfirm: (String) -> Unit = {}
) {
    var username by remember(currentUsername) { mutableStateOf(currentUsername) }
    Column(
        modifier = modifier
            .background(Color.White)
            .padding(24.dp)
    ) {
        // 标题
        Text(
            text = "编辑资料",
            style = FontMedium(fontSize = 18, color = Colors.black_242424),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 头像
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 有用户 avatarUrl 时展示该 URL 的网络图像；本次选了新图则优先显示未上传图；否则默认图
            AvatarImage(
                avatarUri = selectedAvatarUri,
                avatarUrl = avatarUrl?.takeIf { it.isNotBlank() },
                contentDescription = "头像",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "修改头像",
                style = FontRegular(fontSize = 14, Colors.blue_3266FF),
                modifier = Modifier.click(onChangeAvatar)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 用户名
        Text(
            text = "用户名",
            style = FontRegular(fontSize = 14, Colors.black_242424),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        BasicTextField(
            value = username,
            onValueChange = { username = it },
            textStyle = FontRegular(fontSize = 16, Colors.black_242424),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            visualTransformation = VisualTransformation.None,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Colors.gray_F3F5F7)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    innerTextField()
                }
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 确认按钮（Loading 时隐藏文案、显示转圈并禁用点击）
        LoadingButton(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Colors.black_101828),
            text = "确认",
            isLoading = confirmLoading,
            onClick = { onConfirm(username) }
        )
    }
}

@Composable
@Preview
fun DialogInfoPreview() {
    DialogInfo(
        modifier = Modifier.fillMaxWidth(),
        currentUsername = "2123"
    )
}