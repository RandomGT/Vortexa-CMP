package com.vortexa.model

/**
 * 涡联学院/有声读物课程卡片数据模型（Figma 337-45169 课程卡片）
 * @param id 唯一标识
 * @param title 课程标题
 * @param teacherName 讲师名称
 * @param purchaseCount 购买人数展示文案，如 "200次"
 * @param tags 标签列表（如：量化交易、短线）
 * @param price 价格
 * @param unit 货币单位
 * @param rating 星级评分
 * @param coverUrl 封面图，相对路径或完整 URL，空则封面区用占位图
 * @param teacherAvatarUrl 讲师头像，空则 UI 使用默认头像占位
 */
data class SchoolCourseCard(
    val id: String,
    val title: String,
    val teacherName: String,
    val purchaseCount: String,
    val tags: List<String>,
    val price: Float,
    val unit: String = "USD",
    val rating: Float = 0f,
    val coverUrl: String? = null,
    val teacherAvatarUrl: String? = null
)
