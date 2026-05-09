package com.vortexa.ui.page.home.pager.home

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.vortexa.ui.page.search.SearchActivity

class HomeTabViewModel : ViewModel() {
    var currentTab by mutableStateOf(0)
        private set

    fun onTabSelected(index: Int) {
        currentTab = index
    }

    fun jumpToSearch(context: Context) {
        val intent = Intent(context, SearchActivity::class.java)
        context.startActivity(intent)
    }
}
