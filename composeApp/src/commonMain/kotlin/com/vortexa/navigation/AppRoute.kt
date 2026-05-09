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
    ) : AppRoute

    @Serializable
    data class PostCreate(
        val editPostId: String? = null,
        val title: String = "",
        val content: String = "",
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
}

@Serializable
enum class ProfileSubPageKind {
    Collection,
    History,
    Interaction,
}

internal fun encodeRouteStringList(value: List<String>): String =
    routeJson.encodeToString(stringListSerializer, value)

internal fun decodeRouteStringList(value: String): List<String> =
    runCatching { routeJson.decodeFromString(stringListSerializer, value) }.getOrDefault(emptyList())
