package com.vortexa.navigation

import kotlinx.serialization.Serializable

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
        val imageResources: List<String> = emptyList(),
        val videoResources: List<String> = emptyList(),
    ) : AppRoute

    @Serializable
    data class ImagePreview(
        val urls: List<String> = emptyList(),
        val initialIndex: Int = 0,
    ) : AppRoute

    @Serializable
    data class ProfileSubPage(val kind: ProfileSubPageKind) : AppRoute
}

@Serializable
enum class ProfileSubPageKind {
    Collection,
    History,
    Interaction,
}
