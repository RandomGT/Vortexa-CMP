package com.vortexa.ui.page.home.pager.profile

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.vortexa.BuildConfig
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.ui.theme.FontRegular
import com.vortexa.util.extension.click
import java.io.File

/** 底部 Tab 栏高度，与 EditProfileModal 一致 */
private const val TAB_BAR_HEIGHT_DP = 50

private const val PENDING_NONE = 0
private const val PENDING_CAMERA = 1
private const val PENDING_GALLERY = 2

/**
 * 头像来源选择弹窗：拍照 / 从相册选择。
 * 任一选项点击后先校验并申请权限，通过后再执行对应操作。
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
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // 拍照输出用临时文件
    val photoUri = remember {
        val cacheDir = context.cacheDir
        val photoFile = File.createTempFile("avatar_", ".jpg", cacheDir)
        FileProvider.getUriForFile(
            context,
            "${BuildConfig.APPLICATION_ID}.fileprovider",
            photoFile
        )
    }

    var pendingAction by remember { mutableStateOf(PENDING_NONE) }
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            Log.d(TAG, "TakePicture success, uri=$photoUri")
            onAvatarSelected(photoUri)
            onDismiss()
        } else {
            Log.w(TAG, "TakePicture failed or cancelled")
        }
    }

    val pickVisualMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            Log.d(TAG, "PickVisualMedia success, uri=$it")
            onAvatarSelected(it)
            onDismiss()
        } ?: Log.d(TAG, "PickVisualMedia cancelled")
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grantedMap ->
        val allGranted = grantedMap.values.all { it }
        if (allGranted && pendingAction != PENDING_NONE) {
            when (pendingAction) {
                PENDING_CAMERA -> takePictureLauncher.launch(photoUri)
                PENDING_GALLERY -> pickVisualMediaLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
                else -> {}
            }
            pendingAction = PENDING_NONE
        } else if (!allGranted) {
            Log.w(TAG, "Permission denied: $grantedMap")
            Toast.makeText(context, "需要相应权限才能继续", Toast.LENGTH_SHORT).show()
            pendingAction = PENDING_NONE
        }
    }


    /**
     * 拍照：先校验/申请 CAMERA 权限，通过后启动相机
     */
    fun onTakePhotoClick() {
        when {
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                Log.d(TAG, "CAMERA permission granted, launching camera")
                takePictureLauncher.launch(photoUri)
            }
            else -> {
                Log.d(TAG, "Requesting CAMERA permission")
                pendingAction = PENDING_CAMERA
                permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
            }
        }
    }

    /**
     * 从相册选择：先校验/申请相册权限，通过后启动图片选择器。
     * API 33+ 用 READ_MEDIA_IMAGES，以下用 READ_EXTERNAL_STORAGE。
     */
    fun onPickFromGalleryClick() {
        val galleryPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(context, galleryPermission) == PackageManager.PERMISSION_GRANTED -> {
                Log.d(TAG, "Gallery permission granted, launching picker")
                pickVisualMediaLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
            else -> {
                Log.d(TAG, "Requesting gallery permission: $galleryPermission")
                pendingAction = PENDING_GALLERY
                permissionLauncher.launch(arrayOf(galleryPermission))
            }
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
@Preview
private fun AvatarSourceModalPreview() {
    AvatarSourceModal(
        onDismiss = {},
        onAvatarSelected = {}
    )
}

private const val TAG = "AvatarSourceModal"
