package com.vortexa.util

import java.util.Locale
import kotlin.math.abs

/**
 * 帖子互动数字展示（列表/详情等处收藏、点赞、评论数等）。
 *
 * - 小于等于 1000：原样整数字符串（如 234、1000）。
 * - 大于 1000 且小于 10 万：以 k 为单位（如 2000 → 2k，1500 → 1.5k）。
 * - 达到 10 万及以上：以 W 为单位，每 1W 表示一万（如 100000 → 10W，120000 → 12W）。
 */
fun formatPostInteractionCount(count: Int): String {
    if (count < 0) return "0"
    val n = count.toLong()
    return when {
        n >= 100_000L -> formatWithSuffix(n, divisor = 10_000L, suffix = "W")
        n > 1_000L -> formatWithSuffix(n, divisor = 1_000L, suffix = "k")
        else -> count.toString()
    }
}

private fun formatWithSuffix(n: Long, divisor: Long, suffix: String): String {
    val v = n.toDouble() / divisor.toDouble()
    val body = if (abs(v - v.toLong().toDouble()) < 1e-9) {
        v.toLong().toString()
    } else {
        String.format(Locale.US, "%.1f", v).trimEnd('0').trimEnd('.')
    }
    return body + suffix
}
