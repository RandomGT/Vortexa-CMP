package com.vortexa.ui.page.teach.order.one2one

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.vortexa.config.UserConfig
import com.vortexa.model.ReserveDetail
import com.vortexa.model.ReserveListApiStatus
import com.vortexa.ui.page.teach.ReserveCancelHeroDisplay
import com.vortexa.ui.page.teach.ReserveCancelHeroMessage
import com.vortexa.ui.page.teach.isReserveStatusCancelled
import com.vortexa.ui.page.teach.myclass.one2one.MyClassOneToOnePendingStatusLabels
import com.vortexa.ui.page.teach.myclass.one2one.mapMyClassOneToOneReserveStatusToChinese
import com.vortexa.ui.page.teach.resolveReserveCancelHeroDisplay
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** 课程开始时间常见格式，用于解析 API 返回的 courseStartTime */
private val COURSE_TIME_FORMATTERS = listOf(
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"),
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
    DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
)

internal fun parseCourseStartEpochMilli(courseStartTime: String): Long? {
    if (courseStartTime.isBlank()) return null
    val zone = ZoneId.systemDefault()
    for (fmt in COURSE_TIME_FORMATTERS) {
        try {
            val ldt = LocalDateTime.parse(courseStartTime.trim(), fmt)
            return ldt.atZone(zone).toInstant().toEpochMilli()
        } catch (_: Exception) { /* try next */ }
    }
    return null
}

/** 订单价格/实付：去掉末尾 .00，并加「积分」 */
internal fun formatPointsPrice(raw: String): String {
    val t = raw.trim()
    if (t.isEmpty()) return ""
    val noZeros = if (t.endsWith(".00")) t.dropLast(3) else t
    return "${noZeros}积分"
}

/**
 * 订单详情页：已取消且 [ReserveDetail.cancelOperator] 有值时，不按当前登录身份分支，
 * 统一为「学员已取消该预约」/「导师已拒绝该预约」；其余仍走 [resolveReserveCancelHeroDisplay]。
 */
private fun resolveOrderDetailCancelHeroDisplay(d: ReserveDetail, viewerUserId: Long): ReserveCancelHeroDisplay? {
    if (!isReserveStatusCancelled(d.status)) return null
    val opNorm = d.cancelOperator?.trim()?.lowercase().orEmpty()
    if (opNorm.isNotEmpty()) {
        when (opNorm) {
            "student", "learner" -> return ReserveCancelHeroDisplay.StudentInitiatedCancel
            "teacher", "tutor" -> return ReserveCancelHeroDisplay.TutorSelfCancelled
            else -> { }
        }
    }
    return resolveReserveCancelHeroDisplay(
        d.status,
        viewerUserId,
        d.userId,
        d.teacherId,
        d.cancelUserId,
        d.cancelRole,
        d.cancelOperator,
    )
}

/**
 * 是否应在详情页提供「进入课程」入口：导师已接受、未取消/未结束，且未到下课时间。
 * 兼容接口枚举（TO_START 等）与中文状态文案。
 */
internal fun shouldOfferEnterCourseForReserveStatus(status: String): Boolean {
    val raw = status.trim()
    val u = raw.uppercase()
    if (u == ReserveListApiStatus.TO_ACCEPT ||
        u == ReserveListApiStatus.REJECTED ||
        u == ReserveListApiStatus.COMPLETED
    ) return false
    if (isReserveStatusCancelled(raw)) return false
    if (raw == "已完成" || raw == "已拒绝" || raw == "待接受") return false
    return when (u) {
        ReserveListApiStatus.TO_START -> true
        else -> when (raw) {
            "待完成", "进行中", "即将开始" -> true
            else -> false
        }
    }
}

/**
 * 订单详情页 UI 数据（Figma 336-14162 1V1详情）。
 *
 * @param status 状态中文展示（由接口枚举经 [mapMyClassOneToOneReserveStatusToChinese] 映射），用于明细行与底部主按钮
 */
data class OrderDetailUi(
    val courseTitle: String,
    val teacherName: String,
    val teacherId: Long,
    val teacherAvatarUrl: String? = null,
    val status: String,
    val orderPrice: String,
    val actualPrice: String,
    val orderTime: String,
    val courseTime: String,
    val duration: String,
    /** 课程时长（小时），用于计算结束时间 */
    val courseHours: Int,
    val orderId: String,
    val paymentMethod: String,
    /** 声网频道名，进入课程时与 [teacherId] 一并传给 VideoRtcActivity */
    val channelName: String? = null,
    /** 已取消时 Toolbar 下方归因提示 */
    val cancelHeroDisplay: ReserveCancelHeroDisplay? = null,
    /**
     * 当前登录用户是否为该单导师：[ReserveDetail.teacherId] 与 [UserConfig.getTeacherId] 一致（均 > 0）。
     * 导师端不展示「订单价格」，「实付价格」改为「订单报酬」；且不展示「再次预约」。
     */
    val isViewerTeacher: Boolean = false
) {
    /** 课程开始时间（本地时区毫秒时间戳），供视频课堂顶栏计时使用 */
    fun courseStartEpochMilli(): Long? = parseCourseStartEpochMilli(courseTime)

    /** 课程结束时间（本地时区毫秒时间戳），与 [canEnterCourseRoom] 所依据的下课时间一致 */
    fun courseEndEpochMilli(): Long? {
        val start = parseCourseStartEpochMilli(courseTime) ?: return null
        if (courseHours <= 0) return null
        return start + courseHours * 60L * 60L * 1000L
    }

    /** 待接受 / 进行中时可取消预约，与「我的课堂」一对一筛选项一致 */
    val isPending: Boolean get() = status in MyClassOneToOnePendingStatusLabels

    private fun timeBounds(): Pair<Long, Long>? {
        val start = parseCourseStartEpochMilli(courseTime) ?: return null
        if (courseHours <= 0) return null
        val end = start + courseHours * 60L * 60L * 1000L
        return start to end
    }

    /**
     * 是否展示「进入课程」：状态为导师已接受且未取消时，从此时起至下课前始终展示。
     * 开课前为浅底「进入课程」（点击由外层判断是否可进房）；到开课且未下课为「进入课堂」。
     */
    fun shouldShowEnterCourseButton(): Boolean {
        if (cancelHeroDisplay != null) return false
        if (!shouldOfferEnterCourseForReserveStatus(status)) return false
        val (_, end) = timeBounds() ?: return false
        return System.currentTimeMillis() < end
    }

    /** 已到开课时间且未下课时可跳转声网课堂 */
    fun canEnterCourseRoom(): Boolean {
        val (start, end) = timeBounds() ?: return false
        val now = System.currentTimeMillis()
        return now >= start && now < end
    }

    /** 由接口 [ReserveDetail] 映射为详情页 UI 数据 */
    companion object {
        fun from(d: ReserveDetail, viewerUserId: Long): OrderDetailUi {
            val duration = if (d.hour == 1) "1小时" else "${d.hour}小时"
            val courseTitle = "一对一指导${duration}"
            val cancelHero = resolveOrderDetailCancelHeroDisplay(d, viewerUserId)
            return OrderDetailUi(
                courseTitle = courseTitle,
                teacherName = d.teacherName,
                teacherId = d.teacherId,
                teacherAvatarUrl = d.teacherAvatar?.takeIf { it.isNotBlank() },
                status = mapMyClassOneToOneReserveStatusToChinese(d.status),
                orderPrice = d.orderPrice ?: "",
                actualPrice = d.payAmount ?: "",
                orderTime = d.reserveCreateTime,
                courseTime = d.courseStartTime,
                duration = duration,
                courseHours = d.hour,
                orderId = d.reserveId.toString(),
                paymentMethod = d.payType ?: "",
                channelName = d.channelName,
                cancelHeroDisplay = cancelHero,
                isViewerTeacher = run {
                    val myTeacherId = UserConfig.getTeacherId()
                    d.teacherId > 0L && myTeacherId > 0L && d.teacherId == myTeacherId
                }
            )
        }
    }

    /** 明细行列表，与 Figma 顺序一致；导师视角隐藏「订单价格」，实付行改为「订单报酬」 */
    fun toDetailRows(): List<Pair<String, String>> = buildList {
        add("状态" to status)
        if (!isViewerTeacher) {
            add("订单价格" to formatPointsPrice(orderPrice))
            add("实付价格" to formatPointsPrice(actualPrice))
        } else {
            add("订单报酬" to formatPointsPrice(actualPrice))
        }
        add("下单时间" to orderTime)
        add("课程开始时间" to courseTime)
        add("课程时长" to duration)
        add("订单编号" to orderId)
        add("支付方式" to paymentMethod)
    }
}

/**
 * 一对一订单详情页主视图：Toolbar + 课程卡片 + 记录详情 + 底部操作。
 *
 * @param timeTick 由外层定时递增，用于每分钟刷新「进入课程」显隐
 * @param onTeacherProfileClick 点击导师头像/姓名跳转导师主页
 * @param onResumeRefresh Activity 每次 onResume（不含首次）时回调，用于重新拉取详情
 */
@Composable
fun OrderDetailView(
    ui: OrderDetailUi,
    timeTick: Int = 0,
    onBackClick: () -> Unit = {},
    onTeacherProfileClick: () -> Unit = {},
    onEnterCourseClick: () -> Unit = {},
    onIssueClick: () -> Unit = {},
    onPrimaryClick: () -> Unit = {},
    onResumeRefresh: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    onResumeRefresh?.let { refresh ->
        val latestRefresh by rememberUpdatedState(refresh)
        DisposableEffect(lifecycleOwner) {
            var skipFirstResume = true
            val observer = LifecycleEventObserver { _, event ->
                if (event != Lifecycle.Event.ON_RESUME) return@LifecycleEventObserver
                if (skipFirstResume) {
                    skipFirstResume = false
                } else {
                    latestRefresh()
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }
    }
    val showEnterCourse = remember(ui.status, ui.cancelHeroDisplay, ui.courseTime, ui.courseHours, timeTick) {
        ui.shouldShowEnterCourseButton()
    }
    val canEnterCourseRoom = remember(ui.courseTime, ui.courseHours, timeTick) {
        ui.canEnterCourseRoom()
    }
    // 待处理：学员/导师均可「取消预约」。非待处理：仅学员见「再次预约」；导师永不展示再次预约
    val showPrimaryButton = ui.isPending || !ui.isViewerTeacher
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        OrderDetailToolbar(onBackClick = onBackClick)
        ui.cancelHeroDisplay?.let { display ->
            ReserveCancelHeroMessage(display = display, modifier = Modifier.fillMaxWidth())
        }
        OrderDetailCourseCard(
            courseTitle = ui.courseTitle,
            teacherName = ui.teacherName,
            teacherAvatarUrl = ui.teacherAvatarUrl,
            onTeacherClick = onTeacherProfileClick
        )
        OrderDetailBody(
            detailRows = ui.toDetailRows(),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
        if (showEnterCourse || showPrimaryButton) {
            OrderDetailBottomBar(
                showEnterCourse = showEnterCourse,
                canEnterCourseRoom = canEnterCourseRoom,
                isPending = ui.isPending,
                showPrimaryButton = showPrimaryButton,
                onEnterCourseClick = onEnterCourseClick,
                onPrimaryClick = onPrimaryClick
            )
        }
    }
}

@Composable
@Preview
private fun OrderDetailViewPreview() {
    OrderDetailView(
        ui = OrderDetailUi(
            courseTitle = "一对一指导2小时：从入门到专家精通区块链：从入门到专家",
            teacherName = "刘宇凡",
            teacherId = 1001L,
            status = "待完成",
            orderPrice = "120.00",
            actualPrice = "100.00",
            orderTime = "2025-10-08 16:00",
            courseTime = "2025-10-08 20:00",
            duration = "2小时",
            courseHours = 2,
            orderId = "16456549649612121",
            paymentMethod = "积分",
            channelName = "1001_2002_1709034567890_aB3kM"
        )
    )
}
