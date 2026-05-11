package com.vortexa.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vortexa.config.TokenConfig
import com.vortexa.ui.page.home.HomePostCreateSyncCenter
import com.vortexa.ui.page.home.HomePage
import com.vortexa.ui.page.login.LoginScreen
import com.vortexa.ui.page.login.forget.ForgetView
import com.vortexa.ui.page.login.register.RegisterPage
import com.vortexa.ui.page.post.create.PostCreateEditArgs
import com.vortexa.ui.page.post.create.PostCreateView
import com.vortexa.ui.page.post.detail.PostDetailReplyComposerHint
import com.vortexa.ui.page.post.detail.PostDetailView
import com.vortexa.ui.page.post.list.HotPostListView
import com.vortexa.ui.page.profile.collection.CollectionView
import com.vortexa.ui.page.profile.history.HistoryView
import com.vortexa.ui.page.profile.interaction.InteractionView
import com.vortexa.ui.page.search.SearchView
import com.vortexa.ui.page.search.result.SearchResultView
import com.vortexa.ui.page.splash.SplashPage
import com.vortexa.ui.shell.ImagePreviewShell

@Composable
fun VortexaRoot() {
    val navController = rememberNavController()
    val currentBackStackEntry = navController.currentBackStackEntryAsState()

    val dispatcher = remember(navController) {
        object : NavigationDispatcher {
            override fun navigate(route: AppRoute) {
                navController.navigate(route.toNavRoute())
            }

            override fun back() {
                navController.popBackStack()
            }

            override fun canGoBack(): Boolean = navController.previousBackStackEntry != null

            override fun replaceRoot(route: AppRoute) {
                navController.replaceRoot(route)
            }
        }
    }

    DisposableEffect(dispatcher) {
        NavigationRouteBridge.register(dispatcher)
        onDispose { NavigationRouteBridge.unregister(dispatcher) }
    }

    WideBackGestureLayer(
        enabled = currentBackStackEntry.value != null && navController.previousBackStackEntry != null,
    ) {
        NavHost(
            navController = navController,
            startDestination = NavRoutes.Splash,
        ) {
            composable(NavRoutes.Splash) {
                SplashPage(onSplashFinish = {
                    val route = if (TokenConfig.getToken().isNotEmpty()) {
                        AppRoute.Home()
                    } else {
                        AppRoute.Login
                    }
                    dispatcher.replaceRoot(route)
                })
            }
            composable(NavRoutes.Home) {
                val tab = NavigationPayloadStore.homeTab
                HomePage(initialTab = tab)
            }
            composable(NavRoutes.Login) {
                LoginScreen(
                    onRegisterClick = { dispatcher.navigate(AppRoute.Register) },
                    onForgetClick = { dispatcher.navigate(AppRoute.ForgetPassword) },
                    onLoginSuccess = { dispatcher.replaceRoot(AppRoute.Home()) },
                )
            }
            composable(NavRoutes.Register) {
                RegisterPage(onRegisterSuccess = { dispatcher.replaceRoot(AppRoute.Home()) })
            }
            composable(NavRoutes.ForgetPassword) {
                ForgetView(
                    onResetSuccess = { dispatcher.replaceRoot(AppRoute.Login) },
                    onLoginClick = { dispatcher.replaceRoot(AppRoute.Login) },
                )
            }
            composable(NavRoutes.Search) { SearchView(onBack = dispatcher::back) }
            composable(NavRoutes.SearchResult) {
                SearchResultView(keyword = NavigationPayloadStore.searchKeyword)
            }
            composable(NavRoutes.PostDetail) {
                PostDetailView(
                    postId = NavigationPayloadStore.postId,
                    replyComposerHint = NavigationPayloadStore.replyComposerHint,
                    openReplyComposerOnLoad =
                        NavigationPayloadStore.openReplyComposer &&
                            NavigationPayloadStore.replyComposerHint == null,
                    onBack = dispatcher::back,
                )
            }
            composable(NavRoutes.HotPostList) {
                HotPostListView(onBackClick = dispatcher::back)
            }
            composable(NavRoutes.PostCreate) {
                PostCreateView(
                    editArgs = NavigationPayloadStore.postCreateEditArgs,
                    onPublishSuccess = {
                        HomePostCreateSyncCenter.notifyPostCreated()
                        dispatcher.back()
                    }
                )
            }
            composable(NavRoutes.ImagePreview) {
                ImagePreviewShell(
                    urls = NavigationPayloadStore.imagePreviewUrls,
                    initialIndex = NavigationPayloadStore.imagePreviewInitialIndex,
                    onBack = dispatcher::back,
                )
            }
            composable(NavRoutes.ProfileSubPage) {
                val kind = NavigationPayloadStore.profileSubPageKind
                when (kind) {
                    ProfileSubPageKind.Collection -> CollectionView(onBackClick = dispatcher::back)
                    ProfileSubPageKind.History -> HistoryView(onBackClick = dispatcher::back)
                    ProfileSubPageKind.Interaction -> InteractionView(onBackClick = dispatcher::back)
                }
            }
        }
    }
}

private fun NavController.replaceRoot(route: AppRoute) {
    navigate(route.toNavRoute()) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}

private object NavRoutes {
    const val Splash = "splash"
    const val Home = "home"
    const val Login = "login"
    const val Register = "register"
    const val ForgetPassword = "forgetPassword"
    const val Search = "search"
    const val SearchResult = "searchResult"
    const val PostDetail = "postDetail"
    const val HotPostList = "hotPostList"
    const val PostCreate = "postCreate"
    const val ImagePreview = "imagePreview"
    const val ProfileSubPage = "profileSubPage"
}

private object NavigationPayloadStore {
    var homeTab: Int = 0
    var searchKeyword: String = ""
    var postId: String = ""
    var openReplyComposer: Boolean = false
    var replyComposerHint: PostDetailReplyComposerHint? = null
    var imagePreviewUrls: List<String> = emptyList()
    var imagePreviewInitialIndex: Int = 0
    var profileSubPageKind: ProfileSubPageKind = ProfileSubPageKind.Collection
    var postCreateEditArgs: PostCreateEditArgs? = null
}

private fun AppRoute.toNavRoute(): String = when (this) {
    AppRoute.Splash -> NavRoutes.Splash
    is AppRoute.Home -> {
        NavigationPayloadStore.homeTab = tab
        NavRoutes.Home
    }
    AppRoute.Login -> NavRoutes.Login
    AppRoute.Register -> NavRoutes.Register
    AppRoute.ForgetPassword -> NavRoutes.ForgetPassword
    AppRoute.Search -> NavRoutes.Search
    is AppRoute.SearchResult -> {
        NavigationPayloadStore.searchKeyword = keyword
        NavRoutes.SearchResult
    }
    is AppRoute.PostDetail -> {
        NavigationPayloadStore.postId = postId
        NavigationPayloadStore.openReplyComposer = openReplyComposer
        NavigationPayloadStore.replyComposerHint = replyCommentId?.let {
            PostDetailReplyComposerHint(
                commentId = it,
                authorName = replyAuthorName,
                commentSnippet = replyCommentSnippet,
                authorAvatar = replyAuthorAvatar,
            )
        }
        NavRoutes.PostDetail
    }
    AppRoute.HotPostList -> NavRoutes.HotPostList
    is AppRoute.PostCreate -> {
        NavigationPayloadStore.postCreateEditArgs = editPostId?.takeIf { it.isNotBlank() }?.let {
            PostCreateEditArgs(
                postId = it,
                title = title,
                content = content,
                board = board.takeIf { value -> value.isNotBlank() },
                imageResources = imageResources(),
                videoResources = videoResources()
            )
        }
        NavRoutes.PostCreate
    }
    is AppRoute.ImagePreview -> {
        NavigationPayloadStore.imagePreviewUrls = urls()
        NavigationPayloadStore.imagePreviewInitialIndex = initialIndex
        NavRoutes.ImagePreview
    }
    is AppRoute.ProfileSubPage -> {
        NavigationPayloadStore.profileSubPageKind = kind
        NavRoutes.ProfileSubPage
    }
}
