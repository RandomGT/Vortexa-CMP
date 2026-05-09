package com.vortexa.model

/**
 * 贴文图片上传响应数据（POST /v/api/home/post/image）。
 *
 * @param url 上传后的图片相对或绝对地址
 */
data class PostImageUploadData(
    val url: String
)
