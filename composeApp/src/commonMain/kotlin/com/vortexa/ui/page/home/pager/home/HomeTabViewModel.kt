package com.vortexa.ui.page.home.pager.home

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.vortexa.navigation.AppRoute
import com.vortexa.navigation.NavigationRouteBridge

class HomeTabViewModel : ViewModel() {
    var currentTab by mutableStateOf(0)
        private set

    fun onTabSelected(index: Int) {
        currentTab = index
    }

    fun jumpToSearch(context: Context) {
        NavigationRouteBridge.navigate(AppRoute.Search)
    }
}
