package com.vortexa.navigation

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val routeJson = Json { ignoreUnknownKeys = true }
private val stringListSerializer = ListSerializer(String.serializer())

@Serializable
sealed interface AppRoute {
    @Serializable
    data object Splash : AppRoute

    @Serializable
    data class Home(val tab: Int = 0) : AppRoute

    @Serializable
    data object Login : AppRoute

    @Serializable
    data object Register : AppRoute

    @Serializable
    data object ForgetPassword : AppRoute

    @Serializable
    data object Search : AppRoute

    @Serializable
    data class SearchResult(val keyword: String = "") : AppRoute

    @Serializable
    data class PostDetail(
        val postId: String,
        val openReplyComposer: Boolean = false,
        val replyCommentId: Long? = null,
        val replyAuthorName: String = "",
        val replyCommentSnippet: String = "",
        val replyAuthorAvatar: String? = null,
    ) : AppRoute

    @Serializable
    data object HotPostList : AppRoute

    @Serializable
    data class PostCreate(
        val editPostId: String? = null,
        val title: String = "",
        val content: String = "",
        val board: String = "",
        val imageResourcesJson: String = "",
        val videoResourcesJson: String = "",
    ) : AppRoute {
        fun imageResources(): List<String> = decodeRouteStringList(imageResourcesJson)

        fun videoResources(): List<String> = decodeRouteStringList(videoResourcesJson)
    }

    @Serializable
    data class ImagePreview(
        val urlsJson: String = "",
        val initialIndex: Int = 0,
    ) : AppRoute {
        fun urls(): List<String> = decodeRouteStringList(urlsJson)
    }

    @Serializable
    data class ProfileSubPage(val kind: ProfileSubPageKind) : AppRoute

    @Serializable
    data object CreatorCenter : AppRoute

    @Serializable
    data object DataCenter : AppRoute

    @Serializable
    data class SystemMessage(
        val messageType: Int = 0,
        val markReadDialogId: Long? = null,
        val markReadMessageId: Long? = null,
    ) : AppRoute

    @Serializable
    data class OtherUserProfile(val userId: Long) : AppRoute

    @Serializable
    data object PaperManagement : AppRoute

    @Serializable
    data object PublishPostShortcut : AppRoute

    @Serializable
    data object MyClass : AppRoute

    @Serializable
    data class TeacherProfile(val teacherId: Long) : AppRoute

    @Serializable
    data class Schedule(val teacherId: Long) : AppRoute

    @Serializable
    data class ScheduleConfirm(
        val teacherId: Long,
        val reserveDate: String,
        val reserveHour: String,
    ) : AppRoute

    @Serializable
    data class SchedulePayConfirm(
        val teacherId: Long,
        val reserveDate: String,
        val reserveHour: String,
    ) : AppRoute

    @Serializable
    data class ClassAssistant(
        val reserveId: Int,
        val roleQuery: String = "",
    ) : AppRoute

    @Serializable
    data class OrderDetail(val reserveId: Int) : AppRoute

    @Serializable
    data class VideoRtc(
        val channelName: String,
        val teacherId: Long,
        val courseStartMs: Long? = null,
        val courseEndMs: Long? = null,
    ) : AppRoute

    @Serializable
    data object Wallet : AppRoute

    @Serializable
    data object PointRecharge : AppRoute

    @Serializable
    data class WalletDealDetail(
        val dealId: String = "",
        val amount: String = "",
        val action: String = "",
        val date: String = "",
    ) : AppRoute
}

@Serializable
enum class ProfileSubPageKind {
    Collection,
    History,
    Interaction,
    Focus,
}

internal fun encodeRouteStringList(value: List<String>): String =
    routeJson.encodeToString(stringListSerializer, value)

internal fun decodeRouteStringList(value: String): List<String> =
    runCatching { routeJson.decodeFromString(stringListSerializer, value) }.getOrDefault(emptyList())
