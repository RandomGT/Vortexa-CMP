package com.vortexa.navigation

import android.util.Log
import com.vortexa.ui.page.home.HomeActivity
import com.vortexa.ui.page.login.LoginActivity
import com.vortexa.ui.page.login.forget.ForgetActivity
import com.vortexa.ui.page.login.register.RegisterActivity
import com.vortexa.ui.page.post.create.PostCreateActivity
import com.vortexa.ui.page.post.detail.PostDetailActivity
import com.vortexa.ui.page.profile.collection.CollectionActivity
import com.vortexa.ui.page.profile.history.HistoryActivity
import com.vortexa.ui.page.profile.interaction.InteractionActivity
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

    private fun KClass<*>.toAppRoute(): AppRoute? = when (this) {
        LoginActivity::class -> AppRoute.Login
        RegisterActivity::class -> AppRoute.Register
        ForgetActivity::class -> AppRoute.ForgetPassword
        HomeActivity::class -> AppRoute.Home()
        SearchActivity::class -> AppRoute.Search
        PostCreateActivity::class -> AppRoute.PostCreate()
        CollectionActivity::class -> AppRoute.ProfileSubPage(ProfileSubPageKind.Collection)
        HistoryActivity::class -> AppRoute.ProfileSubPage(ProfileSubPageKind.History)
        InteractionActivity::class -> AppRoute.ProfileSubPage(ProfileSubPageKind.Interaction)
        PostDetailActivity::class -> {
            Log.w(TAG, "PostDetailActivity requires a postId; use NavigationRouteBridge.navigate(AppRoute.PostDetail(...))")
            null
        }
        else -> null
    }
}

interface NavigationDispatcher {
    fun navigate(route: AppRoute)
    fun back()
    fun replaceRoot(route: AppRoute)
}
