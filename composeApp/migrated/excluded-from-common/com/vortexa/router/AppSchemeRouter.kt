package com.vortexa.router

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.vortexa.config.TokenConfig
import com.vortexa.ui.page.creator.CreatorCenterActivity
import com.vortexa.ui.page.creator.statistics.DataCenterActivity
import com.vortexa.ui.page.home.HomeActivity
import com.vortexa.ui.page.home.HomeGuestTabLogin
import com.vortexa.ui.page.home.HomeViewModel
import com.vortexa.ui.page.home.pager.home.communicate.CommunicateActivity
import com.vortexa.ui.page.imagepreview.ImagePreviewActivity
import com.vortexa.ui.page.login.LoginActivity
import com.vortexa.ui.page.post.create.PostCreateActivity
import com.vortexa.ui.page.post.detail.PostDetailActivity
import com.vortexa.ui.page.post.list.HotPostListActivity
import com.vortexa.ui.page.profile.collection.CollectionActivity
import com.vortexa.ui.page.profile.focus.MyFocusActivity
import com.vortexa.ui.page.profile.history.HistoryActivity
import com.vortexa.ui.page.profile.interaction.InteractionActivity
import com.vortexa.ui.page.profile.other.OtherUserProfileActivity
import com.vortexa.ui.page.profile.paper.management.PaperManagementActivity
import com.vortexa.ui.page.profile.paper.post.PublishPostActivity
import com.vortexa.ui.page.search.SearchActivity
import com.vortexa.ui.page.systemmsg.SystemMessageActivity
import com.vortexa.ui.page.systemmsg.SystemMessagePageType
import com.vortexa.ui.page.teach.helper.ClassAssistantActivity
import com.vortexa.ui.page.teach.helper.ClassAssistantRoleScheme
import com.vortexa.ui.page.teach.myclass.MyClassActivity
import com.vortexa.ui.page.teach.order.one2one.OrderDetailActivity
import com.vortexa.ui.page.teach.profile.TeacherProfileActivity
import com.vortexa.ui.page.teach.schedule.ScheduleActivity
import com.vortexa.ui.page.teach.schedule.confirm.ConfirmActivity
import com.vortexa.ui.page.teach.schedule.confirm2.Confirm2Activity
import com.vortexa.ui.page.teach.video.VideoRtcActivity
import com.vortexa.ui.page.wallet.WalletActivity
import com.vortexa.ui.page.wallet.detail.DealDetailActivity

object AppSchemeRouter {

    private const val TAG = "AppSchemeRouter"

    fun isAppScheme(uri: Uri?): Boolean =
        uri != null && uri.scheme.equals(AppSchemeContract.SCHEME, ignoreCase = true)

    fun buildRouteKey(uri: Uri): String? {
        val host = uri.host?.lowercase()?.trim().orEmpty()
        if (host.isEmpty()) return null
        val path = uri.path?.trim().orEmpty().trim('/')
        return if (path.isEmpty()) host else "$host/$path"
    }

    /** 从 push / 浏览器 等 [Intent.data] 解析；无 data 或非本 Scheme 则直接返回。 */
    fun consumeViewIntentIfScheme(context: Context, intent: Intent?) {
        val uri = intent?.data ?: return
        if (!isAppScheme(uri)) return
        var raw = intent.dataString?.trim().orEmpty().ifEmpty { uri.toString().trim() }
        val mergeRole = intent.getStringExtra(AppSchemeContract.EXTRA_MERGE_ROLE_FROM_INTENT)
            ?.trim()
            ?.lowercase()
        if (mergeRole == ClassAssistantRoleScheme.VALUE_TEACHER ||
            mergeRole == ClassAssistantRoleScheme.VALUE_STUDENT
        ) {
            val alreadyHasRole = Regex("(^|[?&])role=", RegexOption.IGNORE_CASE).containsMatchIn(raw)
            if (!alreadyHasRole) {
                raw = if ("?" in raw) "$raw&role=$mergeRole" else "$raw?role=$mergeRole"
            }
        }
        Log.d(TAG, "consumeViewIntent dataString=$raw")
        open(context, raw)
    }

    fun open(context: Context, raw: String): OpenResult {
        val trimmed = raw.trim()
        if (trimmed.isEmpty() || trimmed.length > AppSchemeContract.MAX_URI_LENGTH) {
            Log.w(TAG, "open: empty or too long")
            return OpenResult.Malformed
        }
        val uri = Uri.parse(trimmed)
        if (!isAppScheme(uri)) {
            Log.w(TAG, "open: not app scheme")
            return OpenResult.Malformed
        }
        val routeKey = buildRouteKey(uri) ?: run {
            Log.w(TAG, "open: no route key")
            return OpenResult.Malformed
        }
        Log.d(TAG, "open: routeKey=$routeKey")

        if (routeRequiresLogin(routeKey) && TokenConfig.getToken().isEmpty()) {
            PendingRouteStore.save(trimmed)
            val loginIntent = Intent(context, LoginActivity::class.java)
            startActivitySafe(context, loginIntent)
            return OpenResult.Unauthorized
        }

        return dispatch(context, uri, routeKey)
    }

    private fun routeRequiresLogin(routeKey: String): Boolean = when (routeKey) {
        "home", "search", "post/hot" -> false
        else -> true
    }

    private fun dispatch(context: Context, uri: Uri, routeKey: String): OpenResult {
        return when (routeKey) {
            "home" -> openHome(context, uri)
            "post/detail" -> {
                val postId = uri.getQueryParameter("postId")
                    ?: uri.getQueryParameter("id")
                    ?: run {
                        Log.w(TAG, "post/detail missing postId")
                        return OpenResult.Malformed
                    }
                if (postId.isBlank()) return OpenResult.Malformed
                PostDetailActivity.start(context, postId)
                OpenResult.Success
            }
            "user/profile" -> {
                val userIdStr = uri.getQueryParameter("userId")
                    ?: run {
                        Log.w(TAG, "user/profile missing userId")
                        return OpenResult.Malformed
                    }
                val userId = userIdStr.toLongOrNull() ?: run {
                    Log.w(TAG, "user/profile invalid userId")
                    return OpenResult.Malformed
                }
                OtherUserProfileActivity.start(context, userId)
                OpenResult.Success
            }
            "search" -> {
                startActivitySafe(context, Intent(context, SearchActivity::class.java))
                OpenResult.Success
            }
            "post/hot" -> {
                startActivitySafe(context, Intent(context, HotPostListActivity::class.java))
                OpenResult.Success
            }
            "post/create" -> openPostCreate(context, uri)
            "paper/publish" -> {
                val postId = uri.getQueryParameter("postId")?.toLongOrNull() ?: 0L
                PublishPostActivity.start(context, postId)
                OpenResult.Success
            }
            "message/system" -> {
                val typeParam = uri.getQueryParameter("type")?.toIntOrNull()
                val messageType = when (typeParam) {
                    SystemMessagePageType.CLASSROOM_ASSISTANT -> SystemMessagePageType.CLASSROOM_ASSISTANT
                    else -> SystemMessagePageType.SYSTEM
                }
                val intent = Intent(context, SystemMessageActivity::class.java).apply {
                    putExtra(SystemMessageActivity.EXTRA_MESSAGE_TYPE, messageType)
                }
                startActivitySafe(context, intent)
                OpenResult.Success
            }
            "teacher/profile" -> {
                val idStr = uri.getQueryParameter("teacherId")
                    ?: uri.getQueryParameter("userId")
                    ?: run {
                        Log.w(TAG, "teacher/profile missing teacherId/userId")
                        return OpenResult.Malformed
                    }
                val teacherId = idStr.toLongOrNull() ?: run {
                    Log.w(TAG, "teacher/profile invalid id")
                    return OpenResult.Malformed
                }
                TeacherProfileActivity.start(context, teacherId)
                OpenResult.Success
            }
            "teach/schedule" -> {
                val teacherIdStr = uri.getQueryParameter("teacherId")
                    ?: run {
                        Log.w(TAG, "teach/schedule missing teacherId")
                        return OpenResult.Malformed
                    }
                val teacherId = teacherIdStr.toLongOrNull() ?: run {
                    Log.w(TAG, "teach/schedule invalid teacherId")
                    return OpenResult.Malformed
                }
                ScheduleActivity.start(context, teacherId)
                OpenResult.Success
            }
            "teach/schedule/confirm" -> {
                val teacherId = uri.getQueryParameter("teacherId")?.toLongOrNull() ?: run {
                    Log.w(TAG, "teach/schedule/confirm missing teacherId")
                    return OpenResult.Malformed
                }
                val reserveDate = uri.getQueryParameter("reserveDate")?.trim().orEmpty()
                    .takeIf { it.isNotEmpty() }
                    ?: run {
                        Log.w(TAG, "teach/schedule/confirm missing reserveDate")
                        return OpenResult.Malformed
                    }
                val reserveHour = uri.getQueryParameter("reserveHour")?.trim().orEmpty()
                    .takeIf { it.isNotEmpty() }
                    ?: run {
                        Log.w(TAG, "teach/schedule/confirm missing reserveHour")
                        return OpenResult.Malformed
                    }
                val courseTitle = uri.getQueryParameter("courseTitle")
                val teacherName = uri.getQueryParameter("teacherName")
                ConfirmActivity.start(
                    context,
                    teacherId,
                    reserveDate,
                    reserveHour,
                    courseTitle,
                    teacherName
                )
                OpenResult.Success
            }
            "teach/schedule/confirm2" -> {
                val teacherId = uri.getQueryParameter("teacherId")?.toLongOrNull() ?: run {
                    Log.w(TAG, "teach/schedule/confirm2 missing teacherId")
                    return OpenResult.Malformed
                }
                val reserveDate = uri.getQueryParameter("reserveDate")?.trim().orEmpty()
                    .takeIf { it.isNotEmpty() }
                    ?: run {
                        Log.w(TAG, "teach/schedule/confirm2 missing reserveDate")
                        return OpenResult.Malformed
                    }
                val reserveHour = uri.getQueryParameter("reserveHour")?.trim().orEmpty()
                    .takeIf { it.isNotEmpty() }
                    ?: run {
                        Log.w(TAG, "teach/schedule/confirm2 missing reserveHour")
                        return OpenResult.Malformed
                    }
                val courseTitle = uri.getQueryParameter("courseTitle")
                val teacherName = uri.getQueryParameter("teacherName")
                Confirm2Activity.start(
                    context,
                    teacherId,
                    reserveDate,
                    reserveHour,
                    courseTitle,
                    teacherName
                )
                OpenResult.Success
            }
            "teach/myclass" -> {
                startActivitySafe(context, Intent(context, MyClassActivity::class.java))
                OpenResult.Success
            }
            "order/detail" -> {
                val idStr = uri.getQueryParameter("orderId")
                    ?: uri.getQueryParameter("reserveId")
                    ?: run {
                        Log.w(TAG, "order/detail missing orderId")
                        return OpenResult.Malformed
                    }
                val reserveId = idStr.toIntOrNull() ?: run {
                    Log.w(TAG, "order/detail invalid orderId")
                    return OpenResult.Malformed
                }
                OrderDetailActivity.start(context, reserveId)
                OpenResult.Success
            }
            "teach/class-assistant" -> {
                val idStr = uri.getQueryParameter("reserveId")
                    ?: uri.getQueryParameter("orderId")
                    ?: run {
                        Log.w(TAG, "teach/class-assistant missing reserveId")
                        return OpenResult.Malformed
                    }
                val reserveId = idStr.toIntOrNull() ?: run {
                    Log.w(TAG, "teach/class-assistant invalid reserveId")
                    return OpenResult.Malformed
                }
                val roleRaw = uri.getQueryParameter("role")
                if (!ClassAssistantRoleScheme.isAllowedLiteral(roleRaw)) {
                    Log.w(TAG, "teach/class-assistant invalid role=$roleRaw")
                    return OpenResult.Malformed
                }
                val roleQuery = roleRaw?.trim()?.lowercase()?.takeIf { it.isNotEmpty() }
                Log.d(TAG, "teach/class-assistant: reserveId=$reserveId role=$roleQuery")
                ClassAssistantActivity.start(context, reserveId, roleQuery)
                OpenResult.Success
            }
            "teach/rtc" -> {
                val channelName = uri.getQueryParameter("channelName")
                    ?: uri.getQueryParameter("channel")
                    ?: run {
                        Log.w(TAG, "teach/rtc missing channelName")
                        return OpenResult.Malformed
                    }
                if (channelName.isBlank()) return OpenResult.Malformed
                val teacherId = uri.getQueryParameter("teacherId")?.toLongOrNull() ?: run {
                    Log.w(TAG, "teach/rtc missing teacherId")
                    return OpenResult.Malformed
                }
                if (teacherId <= 0L) {
                    Log.w(TAG, "teach/rtc invalid teacherId=$teacherId")
                    return OpenResult.Malformed
                }
                val courseStartMs = uri.getQueryParameter("courseStartMs")?.toLongOrNull()
                val courseEndMs = uri.getQueryParameter("courseEndMs")?.toLongOrNull()
                VideoRtcActivity.start(context, channelName, teacherId, courseStartMs, courseEndMs)
                OpenResult.Success
            }
            "creator/center" -> {
                startActivitySafe(context, Intent(context, CreatorCenterActivity::class.java))
                OpenResult.Success
            }
            "creator/statistics", "creator/data" -> {
                startActivitySafe(context, Intent(context, DataCenterActivity::class.java))
                OpenResult.Success
            }
            "wallet" -> {
                startActivitySafe(context, Intent(context, WalletActivity::class.java))
                OpenResult.Success
            }
            "wallet/deal" -> {
                val dealId = uri.getQueryParameter("dealId")?.trim()?.takeIf { it.isNotEmpty() }
                DealDetailActivity.start(context, dealId)
                OpenResult.Success
            }
            "profile/interaction" -> {
                startActivitySafe(context, Intent(context, InteractionActivity::class.java))
                OpenResult.Success
            }
            "profile/collection" -> {
                startActivitySafe(context, Intent(context, CollectionActivity::class.java))
                OpenResult.Success
            }
            "profile/history" -> {
                startActivitySafe(context, Intent(context, HistoryActivity::class.java))
                OpenResult.Success
            }
            "profile/focus" -> {
                startActivitySafe(context, Intent(context, MyFocusActivity::class.java))
                OpenResult.Success
            }
            "paper/management" -> {
                startActivitySafe(context, Intent(context, PaperManagementActivity::class.java))
                OpenResult.Success
            }
            "image/preview" -> openImagePreview(context, uri)
            "communicate" -> {
                startActivitySafe(context, Intent(context, CommunicateActivity::class.java))
                OpenResult.Success
            }
            else -> {
                Log.w(TAG, "unknown route: $routeKey")
                OpenResult.UnknownRoute
            }
        }
    }

    private fun openPostCreate(context: Context, uri: Uri): OpenResult {
        val postId = uri.getQueryParameter("postId")?.trim().orEmpty()
        if (postId.isEmpty()) {
            PostCreateActivity.start(context)
            return OpenResult.Success
        }
        val title = uri.getQueryParameter("title").orEmpty()
        val content = uri.getQueryParameter("content").orEmpty()
        val board = uri.getQueryParameter("board")?.takeIf { it.isNotBlank() }
        val images = commaSeparatedQuery(uri, "images")
        val videos = commaSeparatedQuery(uri, "videos")
        PostCreateActivity.startForEdit(context, postId, title, content, board, images, videos)
        return OpenResult.Success
    }

    private fun openImagePreview(context: Context, uri: Uri): OpenResult {
        val repeated = uri.getQueryParameters("url").map { it.trim() }.filter { it.isNotEmpty() }
        val urls = if (repeated.isNotEmpty()) {
            repeated
        } else {
            val single = uri.getQueryParameter("url")?.trim()?.takeIf { it.isNotEmpty() }
            if (single != null) listOf(single) else commaSeparatedQuery(uri, "urls")
        }
        if (urls.isEmpty()) {
            Log.w(TAG, "image/preview missing urls")
            return OpenResult.Malformed
        }
        val index = uri.getQueryParameter("index")?.toIntOrNull() ?: 0
        ImagePreviewActivity.start(context, urls, index)
        return OpenResult.Success
    }

    private fun commaSeparatedQuery(uri: Uri, name: String): List<String> =
        uri.getQueryParameter(name)
            ?.split(',')
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            .orEmpty()

    private fun openHome(context: Context, uri: Uri): OpenResult {
        val tab = uri.getQueryParameter("tab")?.toIntOrNull() ?: 0
        if (tab !in 0..4) {
            Log.w(TAG, "home invalid tab=$tab")
            return OpenResult.Malformed
        }
        when (context) {
            is HomeActivity -> {
                if (HomeGuestTabLogin.openGuestLoginInsteadOfTab(context, tab)) {
                    return OpenResult.Success
                }
                val vm = ViewModelProvider(context)[HomeViewModel::class.java]
                vm.onTabClick(tab)
            }
            else -> {
                val i = Intent(context, HomeActivity::class.java).apply {
                    putExtra(AppSchemeContract.EXTRA_HOME_TAB, tab)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                startActivitySafe(context, i)
            }
        }
        return OpenResult.Success
    }

    fun startActivitySafe(context: Context, intent: Intent) {
        if (context !is Activity) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}

/**
 * 登录成功后：优先消费 [PendingRouteStore]，否则进入首页。
 */
object PostAuthNavigator {

    fun navigateAfterLogin(context: Context, inlineAuth: Boolean = false) {
        navigateAfterAuth(context, useFinishAffinity = false, inlineAuth = inlineAuth)
    }

    fun navigateAfterRegister(context: Context) {
        navigateAfterAuth(context, useFinishAffinity = true, inlineAuth = false)
    }

    private fun navigateAfterAuth(
        context: Context,
        useFinishAffinity: Boolean,
        inlineAuth: Boolean = false,
    ) {
        val pending = PendingRouteStore.consume()
        val act = context as? Activity
        val opener = act ?: context.applicationContext
        if (!pending.isNullOrBlank()) {
            when (AppSchemeRouter.open(opener, pending)) {
                OpenResult.Success -> {
                    if (useFinishAffinity) act?.finishAffinity() else act?.finish()
                    return
                }
                OpenResult.Unauthorized -> {
                    PendingRouteStore.save(pending)
                }
                OpenResult.Malformed, OpenResult.UnknownRoute -> { }
            }
        }
        if (inlineAuth) {
            if (useFinishAffinity) act?.finishAffinity() else act?.finish()
            return
        }
        val app = context.applicationContext
        AppSchemeRouter.startActivitySafe(
            app,
            Intent(app, HomeActivity::class.java)
        )
        if (useFinishAffinity) act?.finishAffinity() else act?.finish()
    }
}
