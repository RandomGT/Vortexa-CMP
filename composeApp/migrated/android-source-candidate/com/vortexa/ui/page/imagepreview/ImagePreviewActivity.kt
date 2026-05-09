package com.vortexa.ui.page.imagepreview

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.core.view.WindowCompat
import com.vortexa.ui.base.BaseActivity
import com.vortexa.ui.theme.BaseTheme

/**
 * 图片预览 Activity。支持传入图片 URL 列表和默认展示索引。
 * 功能：横向滑动切换、双指缩放、双击缩放、拖拽平移。
 */
class ImagePreviewActivity : BaseActivity() {

    @Composable
    override fun ContentPage() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val urls = intent.getStringArrayListExtra(EXTRA_IMAGE_URLS)?.toList().orEmpty()
        val initialIndex = intent.getIntExtra(EXTRA_INITIAL_INDEX, 0)
            .coerceIn(0, (urls.size - 1).coerceAtLeast(0))

        if (urls.isEmpty()) {
            Log.w(TAG, "ContentPage: urls empty, finish")
            finish()
            return
        }

        BaseTheme(
            statusBarTextDark = false,
            navigationBarTextDark = false,
            belowStatusBar = false,
            aboveNavigationBar = false
        ) {
            ImagePreviewView(
                imageUrls = urls,
                initialIndex = initialIndex,
                onBack = { finish() }
            )
        }
    }

    companion object {
        private const val TAG = "ImagePreviewActivity"
        private const val EXTRA_IMAGE_URLS = "extra_image_urls"
        private const val EXTRA_INITIAL_INDEX = "extra_initial_index"

        /**
         * 启动图片预览页。
         * @param context 上下文
         * @param imageUrls 图片 URL 列表，支持 http/https 或本地 content/file 路径
         * @param initialIndex 默认展示第几张（0-based），超出范围时取边界
         */
        fun start(context: Context, imageUrls: List<String>, initialIndex: Int = 0) {
            if (imageUrls.isEmpty()) {
                Log.w(TAG, "start: imageUrls empty, skip")
                return
            }
            context.startActivity(Intent(context, ImagePreviewActivity::class.java).apply {
                putStringArrayListExtra(EXTRA_IMAGE_URLS, ArrayList(imageUrls))
                putExtra(EXTRA_INITIAL_INDEX, initialIndex.coerceIn(0, imageUrls.size - 1))
            })
        }
    }
}
