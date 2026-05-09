package com.vortexa.util

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.util.Log

/**
 * 相册选取图片时的统一限制（发帖、评论/回复共用）。
 *
 * 建议阈值说明（你可按产品再调）：
 * - **长边 4096 px**：全图解码内存约与像素数成正比，再大的图易导致预览/上传解码 OOM 或明显卡顿。
 * - **单文件 10 MB**：控制上传耗时与 ContentResolver 读盘压力；社交平台一般还会在服务端再压图。
 */
object ImagePickValidator {

    const val MAX_LONG_EDGE_PX = 4096
    const val MAX_FILE_SIZE_BYTES = 10L * 1024L * 1024L

    private const val TAG = "ImagePickValidator"

    sealed interface Result {
        data object Ok : Result
        data class FileTooLarge(val sizeBytes: Long) : Result
        data class DimensionsTooLarge(val width: Int, val height: Int) : Result
        data object Unreadable : Result
    }

    fun toastMessage(result: Result): String = when (result) {
        is Result.Ok -> ""
        is Result.FileTooLarge ->
            "单张图片不能超过 ${MAX_FILE_SIZE_BYTES / 1024 / 1024}MB，请选较小的文件"
        is Result.DimensionsTooLarge ->
            "图片尺寸过大（长边不超过 ${MAX_LONG_EDGE_PX}px），请缩小或裁剪后重试"
        is Result.Unreadable -> "无法读取该图片，请换一张试试"
    }

    /** 校验单张图片是否符合像素与文件大小限制。 */
    fun validate(context: Context, uri: Uri): Result {
        val sizeBytes = queryUriFileSizeBytes(context, uri)
        if (sizeBytes != null && sizeBytes > MAX_FILE_SIZE_BYTES) {
            return Result.FileTooLarge(sizeBytes)
        }

        val bounds = decodeImageBoundSize(context, uri)
        if (bounds != null) {
            val (w, h) = bounds
            val longEdge = maxOf(w, h)
            if (longEdge > MAX_LONG_EDGE_PX) {
                return Result.DimensionsTooLarge(w, h)
            }
        } else if (sizeBytes == null) {
            Log.w(TAG, "validate: cannot read size or dimensions, uri=$uri")
            return Result.Unreadable
        }
        return Result.Ok
    }

    /**
     * 过滤 [uris]，只保留通过校验的项；若有剔除则返回首个失败原因便于 Toast。
     * @return first: 合法 Uri 列表，second: 若有任意失败则为首个失败结果，否则 null
     */
    fun filterValidImageUris(context: Context, uris: List<Uri>): Pair<List<Uri>, Result?> {
        if (uris.isEmpty()) return emptyList<Uri>() to null
        val ok = ArrayList<Uri>(uris.size)
        var firstBad: Result? = null
        for (uri in uris) {
            when (val r = validate(context, uri)) {
                is Result.Ok -> ok.add(uri)
                else -> if (firstBad == null) firstBad = r
            }
        }
        return ok to firstBad
    }

    private fun queryUriFileSizeBytes(context: Context, uri: Uri): Long? {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { c ->
            if (c.moveToFirst()) {
                val idx = c.getColumnIndex(OpenableColumns.SIZE)
                if (idx >= 0 && !c.isNull(idx)) return c.getLong(idx)
            }
        }
        return runCatching {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                val len = pfd.statSize
                if (len >= 0) len else null
            }
        }.getOrNull()
    }

    private fun decodeImageBoundSize(context: Context, uri: Uri): Pair<Int, Int>? {
        runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeStream(input, null, opts)
                if (opts.outWidth > 0 && opts.outHeight > 0) {
                    return opts.outWidth to opts.outHeight
                }
            }
        }.onFailure { Log.d(TAG, "decodeImageBoundSize: BitmapFactory failed", it) }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return runCatching {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                val wh = IntArray(2)
                ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                    val size = info.size
                    wh[0] = size.width
                    wh[1] = size.height
                    decoder.setTargetSize(1, 1)
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                }
                if (wh[0] > 0 && wh[1] > 0) wh[0] to wh[1] else null
            }.onFailure { Log.d(TAG, "decodeImageBoundSize: ImageDecoder failed", it) }
                .getOrNull()
        }
        return null
    }
}
