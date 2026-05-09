package com.vortexa.ui.component

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage
import com.vortexa.util.resolveApiMediaUrl
import vortexa.composeapp.generated.resources.Res

/**
 * 头像展示组件：支持本地 Uri、网络 URL 与默认占位。
 * - [avatarUri] 非空时优先使用（本地未上传头像等），Coil 从 Uri 加载。
 * - 否则 [avatarUrl] 非空时使用 Coil 加载网络图。
 * - 二者均为空时显示 [defaultResId]。加载中/失败时也显示默认图。
 *
 * @param modifier 修饰符，调用方通常需包含 size 与 clip(CircleShape)
 * @param avatarUri 本地头像 Uri（如刚选择未上传），优先于 avatarUrl
 * @param avatarUrl 头像网络 URL，为 null 或空且无 avatarUri 时显示默认图
 * @param contentDescription 无障碍描述
 * @param defaultResId 默认头像资源 ID，用于占位与加载失败
 */
@Composable
fun AvatarImage(
    modifier: Modifier = Modifier,
    avatarUri: Uri? = null,
    avatarUrl: String? = null,
    contentDescription: String? = null,
    defaultResId: Int = Res.drawable.profile_default
) {
    val defaultPainter = painterResource(defaultResId)
    val model = avatarUri ?: resolveApiMediaUrl(avatarUrl)
    if (model != null) {
        AsyncImage(
            model = model,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Crop,
            placeholder = defaultPainter,
            error = defaultPainter,
            fallback = defaultPainter
        )
    } else {
        Image(
            painter = defaultPainter,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    }
}
