package com.vortexa.ui.page.home.pager.home

import android.content.Context
import android.content.Intent
import com.vortexa.model.Post
import com.vortexa.router.AppSchemeContract
import com.vortexa.ui.page.home.HomeActivity
import com.vortexa.ui.page.home.pager.home.communicate.moduleLabelToCommunicatePostType

/**
 * 从任意页跳转到首页「Home」主 Tab 下的「交流」子页，并按板块 [postType] 加载列表。
 * 与 [HomeView] / [com.vortexa.ui.page.home.pager.home.communicate.CommunicateView] 配合消费待定状态。
 */
object HomeCommunicateNavigation {

    private var pendingSwitchToCommunicateTab: Boolean = false
    private var pendingPostType: Int? = null

    /**
     * 跳转到首页交流区并选中与帖子板块一致的分区（文案与筛选 Chip 一致：综合、杂谈、交易经验、玩法）。
     */
    fun startFromPost(context: Context, post: Post) {
        val raw = post.module?.trim()?.takeIf { it.isNotEmpty() }
            ?: post.tagName?.trim()?.takeIf { it.isNotEmpty() }
            ?: return
        start(context, raw)
    }

    fun start(context: Context, moduleLabelRaw: String) {
        val postType = moduleLabelToCommunicatePostType(moduleLabelRaw)
        pendingSwitchToCommunicateTab = true
        pendingPostType = postType
        val i = Intent(context, HomeActivity::class).apply {
            putExtra(AppSchemeContract.EXTRA_HOME_TAB, 0)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        context.startActivity(i)
    }

    /**
     * [HomeView] 调用：若本次跳转需要切到「交流」子 Tab，返回 true 并清除标记（[pendingPostType] 留给交流页消费）。
     */
    fun consumePendingSwitchToCommunicateTab(): Boolean {
        if (!pendingSwitchToCommunicateTab) return false
        pendingSwitchToCommunicateTab = false
        return true
    }

    /**
     * [CommunicateView] 在交流 Tab 可见时调用：取走待加载的分区并加载；无则返回 null。
     */
    fun consumePendingCommunicatePostType(): Int? {
        val t = pendingPostType
        pendingPostType = null
        return t
    }
}
