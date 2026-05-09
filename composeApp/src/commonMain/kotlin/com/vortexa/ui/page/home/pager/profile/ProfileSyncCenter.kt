package com.vortexa.ui.page.home.pager.profile

import android.util.Log
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * 个人资料（头像、昵称等）在本机修改成功后，通知首页各 Tab 重新拉取数据，
 * 以便帖子流、关注流等展示当前用户最新信息。
 */
object ProfileSyncCenter {

    private const val TAG = "ProfileSyncCenter"

    private val _events = MutableSharedFlow<Unit>(
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /** 各首页 ViewModel 订阅；收到后应对列表执行静默刷新（不强制展示下拉圈）。 */
    val events: SharedFlow<Unit> = _events.asSharedFlow()

    fun notifyProfileUpdated() {
        val ok = _events.tryEmit(Unit)
        if (ok) {
            Log.d(TAG, "notifyProfileUpdated")
        } else {
            Log.w(TAG, "notifyProfileUpdated dropped")
        }
    }
}
