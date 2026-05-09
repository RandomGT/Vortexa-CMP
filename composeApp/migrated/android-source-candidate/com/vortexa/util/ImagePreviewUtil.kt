package com.vortexa.util

import android.content.Context
import android.net.Uri
import android.util.Log
import com.vortexa.lib_net.client.RetrofitClient

/**
 * 将接口返回的图片/头像地址转为 Coil 可用的最终 URL。
 * 绝对地址（http/https）与 content/file Uri 原样返回；相对路径会拼接 [RetrofitClient] 的 baseUrl。
 */
fun resolveApiMediaUrl(raw: String?): String? {
    val trimmed = raw?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) return trimmed
    if (trimmed.startsWith("content://") || trimmed.startsWith("file://")) return trimmed
    val baseUrl = RetrofitClient.getConfig()?.baseUrl?.trimEnd('/').orEmpty()
    if (baseUrl.isBlank()) return trimmed
    return "$baseUrl/${trimmed.trimStart('/')}"
}

/**
 * 将九宫格媒体列表转为图片预览页可用的 URL 列表。
 * 支持：String(网络/相对路径)、Uri(content/file)、Int(资源ID 转为 android.resource://)。
 *
 * @param images 媒体列表，来自 Post/Comment/Reply
 * @param context 用于 android.resource 的 packageName，传 null 时跳过 Int 类型
 * @return 可传入 ImagePreviewActivity 的 URL 列表，无法转换的项会跳过
 */
fun toImagePreviewUrls(images: List<Any>, context: Context? = null): List<String> {
    val result = mutableListOf<String>()
    val baseUrl = RetrofitClient.getConfig()?.baseUrl?.trimEnd('/').orEmpty()
    val packageName = context?.packageName.orEmpty()
    for (item in images) {
        when (item) {
            is String -> {
                val trimmed = item.trim()
                if (trimmed.isBlank()) continue
                val url = when {
                    trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
                    trimmed.startsWith("content://") || trimmed.startsWith("file://") -> trimmed
                    baseUrl.isBlank() -> trimmed
                    else -> "$baseUrl/${trimmed.trimStart('/')}"
                }
                result.add(url)
            }
            is Uri -> result.add(item.toString())
            is Int -> {
                if (packageName.isNotEmpty()) {
                    result.add("android.resource://$packageName/$item")
                } else {
                    Log.w("ImagePreviewUtil", "toImagePreviewUrls: Int resource skipped, need context")
                }
            }
            else -> Log.w("ImagePreviewUtil", "toImagePreviewUrls: unsupported type ${item?.javaClass?.simpleName}")
        }
    }
    return result
}
