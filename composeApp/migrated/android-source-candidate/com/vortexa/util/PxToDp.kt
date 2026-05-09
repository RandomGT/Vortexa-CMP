package com.vortexa.util

import android.content.Context
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * px 与 dp 互转工具。
 * 公式：dp = px / density
 *
 * @author LuXin
 */

/**
 * 将 px 转为 dp（浮点）。
 *
 * @param px 像素值
 * @param density 屏幕密度（DisplayMetrics.density）
 * @return 对应的 dp 值
 */
fun pxToDp(px: Float, density: Float): Float =
    if (density > 0f) px / density else 0f

/**
 * 将 px 转为 dp（整型）。
 *
 * @param px 像素值
 * @param density 屏幕密度
 * @return 对应的 dp 值（四舍五入）
 */
fun pxToDp(px: Int, density: Float): Int =
    if (density > 0f) (px / density).toInt() else 0

/**
 * 将 px 转为 Compose [Dp]。
 *
 * @param px 像素值
 * @param density 屏幕密度（如 [LocalDensity.current].density）
 * @return 对应的 Dp
 */
fun pxToDpAsDp(px: Float, density: Float): Dp =
    pxToDp(px, density).dp

/**
 * 使用 [Context] 的 displayMetrics.density 将 px 转为 dp。
 *
 * @param px 像素值
 * @param context 用于获取密度的 Context
 * @return 对应的 dp 值
 */
fun pxToDp(px: Float, context: Context): Float =
    pxToDp(px, context.resources.displayMetrics.density)

/**
 * 使用 Context 将 px 转为 Compose Dp。
 */
fun pxToDpAsDp(px: Float, context: Context): Dp =
    pxToDp(px, context).dp
