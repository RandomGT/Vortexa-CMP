package com.vortexa.ui.page.teach.myclass.one2one

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vortexa.config.UserConfig
import com.vortexa.model.ReserveListApiStatus
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontMedium
import com.vortexa.ui.theme.FontRegular
import com.vortexa.ui.theme.FontSemiBold
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay

/** 状态为已接受且未结课（「待完成」「进行中」）且距开课 (1h, 24h) 时，标题行右侧倒计时（每秒刷新） */
private const val COUNTDOWN_MIN_REMAINING_MS = 3_600_000L // > 1 小时
private const val COUNTDOWN_MAX_REMAINING_MS = 86_400_000L // < 24 小时

private val MY_CLASS_START_TIME_FORMATTERS = listOf(
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
    DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
    DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
)

private fun parseMyClassItemStartEpochMilli(startTime: String): Long? {
    if (startTime.isBlank()) return null
    val zone = ZoneId.systemDefault()
    val trimmed = startTime.trim()
    for (fmt in MY_CLASS_START_TIME_FORMATTERS) {
        try {
            val ldt = LocalDateTime.parse(trimmed, fmt)
            return ldt.atZone(zone).toInstant().toEpochMilli()
        } catch (_: Exception) {
            /* try next */
        }
    }
    return null
}

/** h:mm:ss，与示例 3:33:24 一致 */
private fun formatCountdownHms(remainingMs: Long): String {
    val totalSec = (remainingMs / 1000L).coerceAtLeast(0L)
    val h = (totalSec / 3600L).toInt()
    val m = ((totalSec % 3600L) / 60L).toInt()
    val s = (totalSec % 60L).toInt()
    return String.format(Locale.getDefault(), "%d:%02d:%02d", h, m, s)
}

/** 可取消预约等与「待结业课节」相关的状态（接口 TO_ACCEPT / TO_START 映射后的中文）。 */
val MyClassOneToOnePendingStatusLabels = setOf("待接受", "待完成", "进行中")

/**
 * 已接受且未取消、未拒绝、未完成：用于距开课倒计时（非「待接受」）。
 * 与 [mapMyClassOneToOneReserveStatusToChinese] 一致：「待完成」= TO_START，「进行中」= 已开课未结课。
 */
private val MyClassOneToOneStartCountdownStatusLabels = setOf("待完成", "进行中")

private fun statusAllowsMyClassStartCountdown(status: String): Boolean =
    status in MyClassOneToOneStartCountdownStatusLabels

/**
 * 将预约列表接口 [status] 枚举串转为列表标题用中文。
 * 接口取值：TO_ACCEPT、TO_START、REJECTED、CANCELED、COMPLETED（大小写不敏感）。
 */
fun mapMyClassOneToOneReserveStatusToChinese(apiStatus: String): String = when (apiStatus.uppercase()) {
    ReserveListApiStatus.TO_ACCEPT -> "待接受"
    ReserveListApiStatus.TO_START -> "待完成"
    ReserveListApiStatus.REJECTED -> "已拒绝"
    ReserveListApiStatus.CANCELED, "CANCELLED" -> "已取消"
    ReserveListApiStatus.COMPLETED -> "已完成"
    else -> apiStatus
}

/**
 * 一对一课程列表项数据（Figma 336-14924 信息区）。
 *
 * @param reserveId 预约订单 ID，点击「详情」时用于跳转订单详情页
 * @param status 预约状态中文（[mapMyClassOneToOneReserveStatusToChinese]，TO_START 为「待完成」）
 * @param startTime 课程开始时间展示文案
 * @param bookTime 预约创建时间展示文案
 * @param studentName 学员展示名（当前用户为该课导师时与「指导对象」一行展示）
 * @param teacherName 导师展示名（否则「导师」一行展示）
 * @param teacherId 导师 ID（与 [com.vortexa.config.UserConfig.getTeacherId] 比对，用于导师端「待接受」跳转课堂小助手）
 * @param duration 时长展示文案
 */
data class MyClassOneToOneItemUi(
    val reserveId: Long,
    val status: String,
    val startTime: String,
    val bookTime: String,
    val studentName: String,
    val teacherName: String,
    val teacherId: Long,
    val duration: String
)

/**
 * 一对一服务列表项卡片（Figma 336-14924）：标题行为服务端 [MyClassOneToOneItemUi.status] 文案 + 4 行信息 + 底部「详情」按钮。
 *
 * @param item 项数据
 * @param onMoreClick 右侧更多点击
 * @param onButtonClick 底部主按钮点击
 */
@Composable
fun MyClassOneToOneListItem(
    item: MyClassOneToOneItemUi,
    onMoreClick: () -> Unit = {},
    onButtonClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val myTeacherId = UserConfig.getTeacherId()
    val iAmThisCourseTeacher =
        item.teacherId > 0L && myTeacherId > 0L && item.teacherId == myTeacherId
    val guideRowLabel: String
    val guideRowValue: String
    if (iAmThisCourseTeacher) {
        guideRowLabel = "指导对象"
        guideRowValue = item.studentName
    } else {
        guideRowLabel = "导师"
        guideRowValue = item.teacherName
    }

    var startCountdownText by remember(item.startTime, item.status) { mutableStateOf<String?>(null) }
    LaunchedEffect(item.startTime, item.status) {
        if (!statusAllowsMyClassStartCountdown(item.status)) {
            startCountdownText = null
            return@LaunchedEffect
        }
        val startMs = parseMyClassItemStartEpochMilli(item.startTime) ?: run {
            startCountdownText = null
            return@LaunchedEffect
        }
        while (true) {
            if (!statusAllowsMyClassStartCountdown(item.status)) {
                startCountdownText = null
                break
            }
            val remaining = startMs - System.currentTimeMillis()
            when {
                remaining <= 0L -> {
                    startCountdownText = null
                    break
                }
                remaining >= COUNTDOWN_MAX_REMAINING_MS -> {
                    startCountdownText = null
                    val over = remaining - COUNTDOWN_MAX_REMAINING_MS
                    delay(minOf(over, 60_000L).coerceAtLeast(1_000L))
                }
                remaining <= COUNTDOWN_MIN_REMAINING_MS -> {
                    startCountdownText = null
                    break
                }
                else -> {
                    startCountdownText = formatCountdownHms(remaining)
                    delay(1_000L)
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onButtonClick)
            .background(Color.White)
    ) {
        // 标题行：左为中文状态（ViewModel 已对枚举做映射）
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, end = 18.dp, top = 20.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.status,
                style = FontSemiBold(fontSize = 18, color = Colors.black_101828)
            )
            startCountdownText?.let { countdown ->
                Text(
                    text = "距离课程开始："+countdown,
                    style = FontSemiBold(fontSize = 15, color = Colors.blue_277DFF)
                )
            }
        }

        listOf(
            "开始时间" to item.startTime,
            "预约时间" to item.bookTime,
            guideRowLabel to guideRowValue,
            "时长" to item.duration
        ).forEach { (label, value) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = FontRegular(fontSize = 15, color = Colors.gray_6A7282)
                )
                Text(
                    text = value,
                    style = FontRegular(fontSize = 15, color = Colors.black_101828)
                )
            }
        }

        // 底部分割线 + 按钮（Figma 336-14947~14950）
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .padding(top = 8.dp, bottom = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .height(1.dp)
                    .background(Color(0xFFF3F4F5))
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(30.dp))
                    .background(Colors.gray_EEF0F1)
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "详情",
                    style = FontMedium(
                        fontSize = 16,
                        color = Colors.black_101828
                    )
                )
            }
        }
    }
}

/**
 * 一对一列表项预览。
 */
@Composable
@Preview
private fun MyClassOneToOneListItemPreview() {
    MyClassOneToOneListItem(
        item = MyClassOneToOneItemUi(
            reserveId = 66666L,
            status = mapMyClassOneToOneReserveStatusToChinese(ReserveListApiStatus.TO_ACCEPT),
            startTime = "2025-12-21 16:00:00",
            bookTime = "2025-12-21 20:00:00",
            studentName = "步惊云",
            teacherName = "无名",
            teacherId = 9001L,
            duration = "2小时"
        ),
    )
}
