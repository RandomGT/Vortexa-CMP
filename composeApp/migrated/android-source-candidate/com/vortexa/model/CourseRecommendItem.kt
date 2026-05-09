package com.vortexa.model

/**
 * 课程推荐横向列表单项（Figma 747-82974）
 *
 * @param id 唯一标识
 * @param title 课程标题
 * @param lecturerName 讲师名称
 * @param studentCountText 学员数展示文案，如 "5000+人在学"（数字部分可单独高亮）
 */
data class CourseRecommendItem(
    val id: String,
    val title: String,
    val lecturerName: String,
    val studentCountText: String
)
