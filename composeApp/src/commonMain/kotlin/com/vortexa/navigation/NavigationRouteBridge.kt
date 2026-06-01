package com.vortexa.navigation

import android.util.Log
import com.vortexa.ui.page.creator.CreatorCenterActivity
import com.vortexa.ui.page.home.HomeActivity
import com.vortexa.ui.page.login.LoginActivity
import com.vortexa.ui.page.login.forget.ForgetActivity
import com.vortexa.ui.page.login.register.RegisterActivity
import com.vortexa.ui.page.post.create.PostCreateActivity
import com.vortexa.ui.page.post.detail.PostDetailActivity
import com.vortexa.ui.page.post.list.HotPostListActivity
import com.vortexa.ui.page.profile.focus.MyFocusActivity
import com.vortexa.ui.page.profile.other.OtherUserProfileActivity
import com.vortexa.ui.page.profile.paper.management.PaperManagementActivity
import com.vortexa.ui.page.profile.collection.CollectionActivity
import com.vortexa.ui.page.profile.history.HistoryActivity
import com.vortexa.ui.page.profile.interaction.InteractionActivity
import com.vortexa.ui.page.systemmsg.SystemMessageActivity
import com.vortexa.ui.page.teach.helper.ClassAssistantActivity
import com.vortexa.ui.page.teach.myclass.MyClassActivity
import com.vortexa.ui.page.teach.order.one2one.OrderDetailActivity
import com.vortexa.ui.page.teach.profile.TeacherProfileActivity
import com.vortexa.ui.page.teach.schedule.ScheduleActivity
import com.vortexa.ui.page.wallet.WalletActivity
import com.vortexa.ui.page.search.SearchActivity
import kotlin.reflect.KClass

object NavigationRouteBridge {
    private const val TAG = "NavigationRouteBridge"

    private var dispatcher: NavigationDispatcher? = null

    fun register(dispatcher: NavigationDispatcher) {
        this.dispatcher = dispatcher
    }

    fun unregister(dispatcher: NavigationDispatcher) {
        if (this.dispatcher === dispatcher) {
            this.dispatcher = null
        }
    }

    fun navigate(route: AppRoute): Boolean {
        val activeDispatcher = dispatcher
        if (activeDispatcher == null) {
            Log.w(TAG, "navigate ignored: dispatcher is not registered, route=$route")
            return false
        }
        activeDispatcher.navigate(route)
        return true
    }

    fun back(): Boolean {
        val activeDispatcher = dispatcher
        if (activeDispatcher == null) {
            Log.w(TAG, "back ignored: dispatcher is not registered")
            return false
        }
        activeDispatcher.back()
        return true
    }

    fun canGoBack(): Boolean = dispatcher?.canGoBack() ?: false

    fun replaceRoot(route: AppRoute): Boolean {
        val activeDispatcher = dispatcher
        if (activeDispatcher == null) {
            Log.w(TAG, "replaceRoot ignored: dispatcher is not registered, route=$route")
            return false
        }
        activeDispatcher.replaceRoot(route)
        return true
    }

    fun routeToPage(target: KClass<*>): Boolean {
        val route = target.toAppRoute()
        if (route == null) {
            Log.w(TAG, "routeToPage ignored: unmapped target=${target.qualifiedName ?: target.simpleName}")
            return false
        }
        return navigate(route)
    }

    fun routeToPage(uriString: String): Boolean {
        val route = uriString.toAppRouteFromScheme()
        if (route == null) {
            Log.w(TAG, "routeToPage ignored: unmapped uri=$uriString")
            return false
        }
        return navigate(route)
    }

    private fun KClass<*>.toAppRoute(): AppRoute? = when (this) {
        LoginActivity::class -> AppRoute.Login
        RegisterActivity::class -> AppRoute.Register
        ForgetActivity::class -> AppRoute.ForgetPassword
        HomeActivity::class -> AppRoute.Home()
        SearchActivity::class -> AppRoute.Search
        PostCreateActivity::class -> AppRoute.PostCreate()
        HotPostListActivity::class -> AppRoute.HotPostList
        CreatorCenterActivity::class -> AppRoute.CreatorCenter
        MyFocusActivity::class -> AppRoute.ProfileSubPage(ProfileSubPageKind.Focus)
        OtherUserProfileActivity::class -> AppRoute.OtherUserProfile(0L)
        PaperManagementActivity::class -> AppRoute.PaperManagement
        SystemMessageActivity::class -> AppRoute.SystemMessage()
        MyClassActivity::class -> AppRoute.MyClass
        TeacherProfileActivity::class -> AppRoute.TeacherProfile(0L)
        ScheduleActivity::class -> AppRoute.Schedule(0L)
        ClassAssistantActivity::class -> AppRoute.ClassAssistant(0)
        OrderDetailActivity::class -> AppRoute.OrderDetail(0)
        WalletActivity::class -> AppRoute.Wallet
        CollectionActivity::class -> AppRoute.ProfileSubPage(ProfileSubPageKind.Collection)
        HistoryActivity::class -> AppRoute.ProfileSubPage(ProfileSubPageKind.History)
        InteractionActivity::class -> AppRoute.ProfileSubPage(ProfileSubPageKind.Interaction)
        PostDetailActivity::class -> {
            Log.w(TAG, "PostDetailActivity requires a postId; use NavigationRouteBridge.navigate(AppRoute.PostDetail(...))")
            null
        }
        else -> null
    }

    private fun String.toAppRouteFromScheme(): AppRoute? {
        val raw = trim()
        if (!raw.startsWith("vortexa://")) return null
        val withoutScheme = raw.removePrefix("vortexa://")
        val routeKey = withoutScheme.substringBefore('?').trim('/')
        val queryPairs = withoutScheme.substringAfter('?', missingDelimiterValue = "").parseQueryPairs()
        val query = queryPairs.associate { it.first to it.second }
        return when (routeKey) {
            "image/preview" -> {
                val repeated = queryPairs
                    .filter { it.first == "url" }
                    .map { it.second.trim() }
                    .filter { it.isNotEmpty() }
                val urls = if (repeated.isNotEmpty()) {
                    repeated
                } else {
                    query["url"]?.trim()?.takeIf { it.isNotEmpty() }?.let(::listOf)
                        ?: query["urls"]
                            ?.split(',')
                            ?.map { it.trim() }
                            ?.filter { it.isNotEmpty() }
                        ?: emptyList()
                }
                if (urls.isEmpty()) return null
                AppRoute.ImagePreview(
                    urlsJson = encodeRouteStringList(urls),
                    initialIndex = query["index"]?.toIntOrNull() ?: 0,
                )
            }
            "teach/rtc" -> {
                val channelName = query["channelName"]?.takeIf { it.isNotBlank() }
                    ?: query["channel"]?.takeIf { it.isNotBlank() }
                    ?: return null
                val teacherId = query["teacherId"]?.toLongOrNull()?.takeIf { it > 0L } ?: return null
                AppRoute.VideoRtc(
                    channelName = channelName,
                    teacherId = teacherId,
                    courseStartMs = query["courseStartMs"]?.toLongOrNull(),
                    courseEndMs = query["courseEndMs"]?.toLongOrNull(),
                )
            }
            else -> null
        }
    }

    private fun String.parseQueryPairs(): List<Pair<String, String>> =
        split('&')
            .filter { it.isNotBlank() }
            .map { part ->
                val key = part.substringBefore('=').decodeUriComponent()
                val value = part.substringAfter('=', missingDelimiterValue = "").decodeUriComponent()
                key to value
            }

    private fun String.decodeUriComponent(): String {
        if ('%' !in this && '+' !in this) return this
        val out = StringBuilder(length)
        var i = 0
        while (i < length) {
            when (val ch = this[i]) {
                '+' -> {
                    out.append(' ')
                    i++
                }
                '%' -> {
                    val hex = substring(i + 1, (i + 3).coerceAtMost(length))
                    val code = hex.toIntOrNull(16)
                    if (hex.length == 2 && code != null) {
                        out.append(code.toChar())
                        i += 3
                    } else {
                        out.append(ch)
                        i++
                    }
                }
                else -> {
                    out.append(ch)
                    i++
                }
            }
        }
        return out.toString()
    }
}

interface NavigationDispatcher {
    fun navigate(route: AppRoute)
    fun back()
    fun canGoBack(): Boolean
    fun replaceRoot(route: AppRoute)
}
