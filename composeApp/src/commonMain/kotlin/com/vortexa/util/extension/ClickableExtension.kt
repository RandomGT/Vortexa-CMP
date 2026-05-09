package com.vortexa.util.extension

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import com.vortexa.util.FastClickUtils
import kotlinx.coroutines.coroutineScope
import kotlin.math.hypot
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 *  desc : 点击效果扩展
 *
 *  @author LuXin
 *  @createTime 2024/5/9
 */

/**
 * 无波纹点击
 */
@Stable
fun Modifier.click(onClickListener: () -> Unit) = this.clickable(
    interactionSource = MutableInteractionSource(),
    indication = null,
    onClick = {
        onClickListener()
    })

@Stable
@OptIn(ExperimentalTime::class)
fun Modifier.throttleClick(
    timeoutMillis: Long = 250, // 默认间隔 250ms
    onClickListener: () -> Unit
) = composed { // 使用 composed 包裹以支持状态记忆
    var lastClickTime by remember { mutableLongStateOf(0L) }
    this.clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = {
            val currentTime = Clock.System.now().toEpochMilliseconds()
            if (currentTime - lastClickTime > timeoutMillis) {
                lastClickTime = currentTime
                onClickListener()
            } else {
            }
        }
    )
}

/**
 * 带有View位置回调的点击事件
 * @param touchSlop 最小滚动距离检测
 */
@Stable
@OptIn(ExperimentalTime::class)
fun Modifier.posClick(touchSlop: Float, onClickListener: (position: Offset?) -> Unit): Modifier {
    var elementPos: Offset? = null
    var handled = false
    var downId = PointerId(0)
    return this
        .onGloballyPositioned {
            elementPos = it.positionInWindow()
        }
        .pointerInput(Unit) {
            coroutineScope {
                awaitEachGesture {
                    // 等待按下事件
                    val down = awaitFirstDown().also { it.consume() }
                    downId = down.id
                    handled = false
                    val downTime = Clock.System.now().toEpochMilliseconds()
                    // 等待所有触摸点抬起（代表点击手势结束）
                    val up = waitForUpOrCancellation()
                    if (up != null && Clock.System.now().toEpochMilliseconds() - downTime <= 300 && !handled) {
                        handled = true
                        up.consume()
                        onClickListener(elementPos)
                    }
                    awaitTouchSlopOrCancellation(downId) { change: PointerInputChange, overSlop: Offset ->
                        if (!handled && hypot(overSlop.x, overSlop.y) >= touchSlop) {
                            handled = true
                            change.consume()
                        }
                    }
                }
            }
        }
}

/**
 * 带防抖的点击事件
 */
@Stable
fun Modifier.safeClickable(
    enabled: Boolean = true,
    interval: Long = 500L,
    onClick: () -> Unit
): Modifier = composed {
    this.click {
        if (!FastClickUtils.isFastClick(interval)) {
            onClick()
        }
    }
}
