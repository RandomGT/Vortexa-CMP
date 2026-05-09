package com.vortexa.model

/**
 * 推荐卡片数据模型（Figma 节点 747-81595 对应 Item）
 * @param id 唯一标识
 * @param title 标题
 * @param tags 标签列表（如：量化交易、短线）
 * @param price 价格，展示保留一位小数
 * @param unit 报价单位文案（导师卡片多为「积分」，可由接口 priceUnit 下发）
 * @param favorite 星级评分，展示保留一位小数
 * @param imageUrl 封面图 URL，为空时显示占位
 */
data class RecommendCard(
    val id: Long,
    val title: String,
    val tags: List<String> = emptyList(),
    val price: Float = 0f,
    val unit: String = "积分",
    val favorite: Float = 0f,
    val imageUrl: String? = null
)
