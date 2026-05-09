package com.vortexa.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 *  desc : 文本样式枚举集合
 *
 *
 *  @author LuXin
 *  @createTime 2026/1/19
 */

fun FontTitle() = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Bold,
    fontSize = 22.sp,
    lineHeight = 28.sp,
    letterSpacing = 0.sp
)

fun FontRegular(fontSize: Int = 14, color: androidx.compose.ui.graphics.Color) = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = fontSize.sp,
    color = color,
    lineHeight = fontSize.sp,
)

fun FontMedium(fontSize: Int = 16, color: androidx.compose.ui.graphics.Color) = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Medium,
    fontSize = fontSize.sp,
    color = color,
    lineHeight = fontSize.sp,
)

fun FontBold(fontSize: Int = 16, color: androidx.compose.ui.graphics.Color) = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Bold,
    fontSize = fontSize.sp,
    color = color,
    lineHeight = fontSize.sp,
)

/** Semibold 字重，用于列表标题等（Figma Text_15_S） */
fun FontSemiBold(fontSize: Int = 15, color: androidx.compose.ui.graphics.Color) = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.SemiBold,
    fontSize = fontSize.sp,
    color = color,
    lineHeight = fontSize.sp,
)


