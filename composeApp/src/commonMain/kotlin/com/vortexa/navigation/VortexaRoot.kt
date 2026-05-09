package com.vortexa.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.vortexa.ui.page.home.HomePage
import com.vortexa.ui.page.login.LoginScreen
import com.vortexa.ui.page.login.forget.ForgetView
import com.vortexa.ui.page.login.register.RegisterPage
import com.vortexa.ui.page.post.create.PostCreateView
import com.vortexa.ui.page.post.detail.PostDetailView
import com.vortexa.ui.page.profile.collection.CollectionView
import com.vortexa.ui.page.profile.history.HistoryView
import com.vortexa.ui.page.profile.interaction.InteractionView
import com.vortexa.ui.page.search.SearchView
import com.vortexa.ui.page.search.result.SearchResultView
import com.vortexa.ui.page.splash.SplashPage
import com.vortexa.ui.shell.ImagePreviewShell

@Composable
fun VortexaRoot() {
    val backStack = remember { mutableStateListOf<AppRoute>(AppRoute.Splash) }

    val dispatcher = remember {
        object : NavigationDispatcher {
            override fun navigate(route: AppRoute) {
                backStack.add(route)
            }

            override fun back() {
                if (backStack.size > 1) {
                    backStack.removeAt(backStack.lastIndex)
                }
            }

            override fun replaceRoot(route: AppRoute) {
                backStack.clear()
                backStack.add(route)
            }
        }
    }

    DisposableEffect(dispatcher) {
        NavigationRouteBridge.register(dispatcher)
        onDispose { NavigationRouteBridge.unregister(dispatcher) }
    }

    NavDisplay(
        backStack = backStack,
        onBack = dispatcher::back,
        entryProvider = entryProvider {
            entry<AppRoute.Splash> {
                Log.d("VortexaRoot", "enter Splash")
                SplashPage(onSplashFinish = {
                    Log.d("VortexaRoot", "Splash finished -> Login")
                    dispatcher.replaceRoot(AppRoute.Login)
                })
            }
            entry<AppRoute.Home> { route ->
                Log.d("VortexaRoot", "enter Home route=$route")
                HomePage()
            }
            entry<AppRoute.Login> {
                LoginScreen(
                    onRegisterClick = { dispatcher.navigate(AppRoute.Register) },
                    onForgetClick = { dispatcher.navigate(AppRoute.ForgetPassword) },
                    onLoginSuccess = { dispatcher.replaceRoot(AppRoute.Home()) },
                )
            }
            entry<AppRoute.Register> {
                RegisterPage(onRegisterSuccess = { dispatcher.replaceRoot(AppRoute.Home()) })
            }
            entry<AppRoute.ForgetPassword> {
                ForgetView(
                    onResetSuccess = { dispatcher.replaceRoot(AppRoute.Login) },
                    onLoginClick = { dispatcher.replaceRoot(AppRoute.Login) },
                )
            }
            entry<AppRoute.Search> { SearchView(onBack = dispatcher::back) }
            entry<AppRoute.SearchResult> { route ->
                SearchResultView(keyword = route.keyword)
            }
            entry<AppRoute.PostDetail> { route ->
                PostDetailView(
                    postId = route.postId,
                    openReplyComposerOnLoad = route.openReplyComposer,
                )
            }
            entry<AppRoute.PostCreate> { route ->
                PostCreateView()
            }
            entry<AppRoute.ImagePreview> { route ->
                ImagePreviewShell(
                    urls = route.urls,
                    initialIndex = route.initialIndex,
                    onBack = dispatcher::back,
                )
            }
            entry<AppRoute.ProfileSubPage> { route ->
                when (route.kind) {
                    ProfileSubPageKind.Collection -> CollectionView(onBackClick = dispatcher::back)
                    ProfileSubPageKind.History -> HistoryView(onBackClick = dispatcher::back)
                    ProfileSubPageKind.Interaction -> InteractionView(onBackClick = dispatcher::back)
                }
            }
        },
    )
}
